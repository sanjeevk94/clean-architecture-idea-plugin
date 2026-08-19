package com.korina.cleanarch.idea.generator

import com.korina.cleanarch.idea.schema.*
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import java.io.File

/**
 * Generates all Clean Architecture boilerplate files directly from an [IdeFeatureSchema],
 * writing them to the project's file system using plain Java IO — **no Gradle required**.
 *
 * All content is built in memory on the background thread, then written to disk.
 * IntelliJ's VFS is refreshed asynchronously afterwards so files appear immediately
 * in the Project view without requiring a manual File > Synchronize.
 *
 * Generated files:
 *  Domain module  → Model, Repository interface, UseCase(s)
 *  Data module    → Entity (Room), DAO (Room), DTO (Retrofit), ApiService (Retrofit),
 *                   Mapper, RepositoryImpl
 *  App module     → DI module (Hilt or Koin)
 */
class DirectFileGenerator(
    private val project: Project,
    private val onOutput: (String) -> Unit,
    private val onComplete: (success: Boolean, generatedFiles: List<String>) -> Unit
) {

    // ── Public entry point ────────────────────────────────────────────────────

    fun generate(schema: IdeFeatureSchema) {
        ProgressManager.getInstance().run(object : Task.Backgroundable(
            project,
            "Generating ${schema.featureName} feature…",
            /* canBeCancelled = */ false
        ) {
            override fun run(indicator: ProgressIndicator) {
                indicator.isIndeterminate = true
                indicator.text = "Building file templates for '${schema.featureName}'…"

                try {
                    val projectDir = File(
                        project.basePath
                            ?: throw IllegalStateException("Project has no base directory.")
                    )

                    // Build all (absolutePath → content) pairs in memory on the background thread
                    val fileMap = buildFileMap(schema, projectDir)

                    indicator.text = "Writing ${fileMap.size} file(s)…"

                    val generatedFiles = mutableListOf<String>()

                    // Write via Java IO — no IntelliJ WriteAction required for plain file I/O
                    for ((absolutePath, content) in fileMap) {
                        val file = File(absolutePath)
                        file.parentFile.mkdirs()
                        file.writeText(content, Charsets.UTF_8)
                        generatedFiles.add(absolutePath)
                        emit("  ✅  ${absolutePath.removePrefix(projectDir.absolutePath)}\n")
                    }

                    // Refresh VFS on the EDT so the IDE recognises new files immediately
                    ApplicationManager.getApplication().invokeLater {
                        LocalFileSystem.getInstance().refreshAndFindFileByIoFile(projectDir)
                    }

                    emit("\n✅  Done!  ${generatedFiles.size} file(s) generated.\n")
                    complete(success = true, files = generatedFiles)

                } catch (ex: Exception) {
                    emit("\n❌  Error: ${ex.message}\n")
                    complete(success = false, files = emptyList())
                }
            }
        })
    }

    // ── File map ──────────────────────────────────────────────────────────────

    private fun buildFileMap(schema: IdeFeatureSchema, projectDir: File): Map<String, String> {
        val map  = LinkedHashMap<String, String>()
        val name = schema.featureName   // e.g. "Station"
        val pkg  = schema.packageBase   // e.g. "com.korina.myapp"

        val domainSrc = resolveSourceDir(projectDir, schema.moduleRouting.domain)
        val dataSrc   = resolveSourceDir(projectDir, schema.moduleRouting.data)
        val appSrc    = resolveSourceDir(projectDir, schema.moduleRouting.app)

        // ── Domain ────────────────────────────────────────────────────────────
        map[path(domainSrc, pkg, "domain/model",      "${name}Model")]       = genModel(schema)
        map[path(domainSrc, pkg, "domain/repository", "${name}Repository")]  = genRepository(schema)

        if (schema.options.generateUseCasePerEndpoint && schema.api.endpoints.isNotEmpty()) {
            schema.api.endpoints.forEach { ep ->
                val ucName = ep.name.capitalized() + "UseCase"
                map[path(domainSrc, pkg, "domain/usecase", ucName)] = genUseCasePerEndpoint(schema, ep)
            }
        } else {
            val firstEp = schema.api.endpoints.firstOrNull()
            val ucName  = if (firstEp != null) firstEp.name.capitalized() + "UseCase"
                          else "Get${name}UseCase"
            map[path(domainSrc, pkg, "domain/usecase", ucName)] = genDefaultUseCase(schema)
        }

        // ── Data ──────────────────────────────────────────────────────────────
        if (schema.storage.engine == IdeStorageEngine.ROOM) {
            map[path(dataSrc, pkg, "data/local/entity", "${name}Entity")] = genEntity(schema)
            map[path(dataSrc, pkg, "data/local/dao",    "${name}Dao")]    = genDao(schema)
        }
        if (schema.api.client == IdeApiClient.RETROFIT) {
            map[path(dataSrc, pkg, "data/remote/dto",     "${name}Dto")]        = genDto(schema)
            map[path(dataSrc, pkg, "data/remote/service", "${name}ApiService")] = genApiService(schema)
        }
        map[path(dataSrc, pkg, "data/mapper",     "${name}Mapper")]         = genMapper(schema)
        map[path(dataSrc, pkg, "data/repository", "${name}RepositoryImpl")] = genRepositoryImpl(schema)

        // ── DI ────────────────────────────────────────────────────────────────
        map[path(appSrc, pkg, "di", "${name}Module")] = genDiModule(schema)

        return map
    }

    // ── Path helpers ──────────────────────────────────────────────────────────

    /** Resolves the `src/main/kotlin` (or `src/main/java`) source root for a Gradle module. */
    private fun resolveSourceDir(projectDir: File, module: String): File {
        val moduleDir = File(projectDir, module.removePrefix(":").replace(":", "/"))
        return listOf("src/main/kotlin", "src/main/java")
            .map { File(moduleDir, it) }
            .firstOrNull { it.exists() }
            ?: File(moduleDir, "src/main/kotlin")   // will be created on first write
    }

    /** Builds an absolute file path: `<sourceDir>/<pkg.as.path>/<sub>/<className>.kt` */
    private fun path(sourceDir: File, pkg: String, sub: String, className: String): String =
        File(sourceDir, "${pkg.replace('.', '/')}/$sub/$className.kt").absolutePath

    // ── String utilities ──────────────────────────────────────────────────────

    private fun String.capitalized()   = replaceFirstChar(Char::uppercaseChar)
    private fun String.decapitalized() = replaceFirstChar(Char::lowercaseChar)

    /** Converts `camelCase` → `camel_case` for @ColumnInfo / @SerialName. */
    private fun String.toSnakeCase() =
        replace(Regex("([a-z])([A-Z])"), "$1_$2").lowercase()

    private fun IdeFieldDefinition.kotlinType() = if (nullable) "$type?" else type

    /** Extracts `{paramName}` placeholders from a URL path like `/orders/{id}`. */
    private fun pathParams(path: String): List<String> =
        Regex("\\{(\\w+)\\}").findAll(path).map { it.groupValues[1] }.toList()

    // ── Template: Domain Model ────────────────────────────────────────────────

    private fun genModel(schema: IdeFeatureSchema): String {
        val name   = schema.featureName
        val pkg    = schema.packageBase
        val fields = schema.fields.joinToString("\n") { f ->
            "    val ${f.name}: ${f.kotlinType()},"
        }
        return """
package $pkg.domain.model

data class ${name}Model(
$fields
)
""".trimStart()
    }

    // ── Template: Domain Repository interface ─────────────────────────────────

    private fun genRepository(schema: IdeFeatureSchema): String {
        val name = schema.featureName
        val pkg  = schema.packageBase

        val methods = if (schema.api.endpoints.isEmpty()) {
            "    suspend fun get${name}s(): List<${name}Model>"
        } else {
            schema.api.endpoints.joinToString("\n") { ep ->
                val params    = pathParams(ep.path).joinToString(", ") { "$it: Long" }
                val returnType = if (ep.returnsList) "List<${name}Model>" else "${name}Model?"
                "    suspend fun ${ep.name}($params): $returnType"
            }
        }

        return """
package $pkg.domain.repository

import $pkg.domain.model.${name}Model

interface ${name}Repository {
$methods
}
""".trimStart()
    }

    // ── Template: Default UseCase (one per feature) ───────────────────────────

    private fun genDefaultUseCase(schema: IdeFeatureSchema): String {
        val name    = schema.featureName
        val pkg     = schema.packageBase
        val firstEp = schema.api.endpoints.firstOrNull()

        val ucName: String
        val paramStr: String
        val returnType: String
        val callExpr: String

        if (firstEp != null) {
            val pp    = pathParams(firstEp.path)
            ucName    = firstEp.name.capitalized() + "UseCase"
            paramStr  = pp.joinToString(", ") { "$it: Long" }
            returnType = if (firstEp.returnsList) "List<${name}Model>" else "${name}Model?"
            callExpr  = "repository.${firstEp.name}(${pp.joinToString(", ")})"
        } else {
            ucName    = "Get${name}UseCase"
            paramStr  = ""
            returnType = "List<${name}Model>"
            callExpr  = "repository.get${name}s()"
        }

        return """
package $pkg.domain.usecase

import $pkg.domain.model.${name}Model
import $pkg.domain.repository.${name}Repository

class $ucName(
    private val repository: ${name}Repository,
) {
    suspend operator fun invoke($paramStr): $returnType = $callExpr
}
""".trimStart()
    }

    // ── Template: UseCase per endpoint ────────────────────────────────────────

    private fun genUseCasePerEndpoint(schema: IdeFeatureSchema, ep: IdeEndpointDefinition): String {
        val name   = schema.featureName
        val pkg    = schema.packageBase
        val ucName = ep.name.capitalized() + "UseCase"
        val pp     = pathParams(ep.path)
        val paramStr   = pp.joinToString(", ") { "$it: Long" }
        val returnType = if (ep.returnsList) "List<${name}Model>" else "${name}Model?"
        val callExpr   = "repository.${ep.name}(${pp.joinToString(", ")})"

        return """
package $pkg.domain.usecase

import $pkg.domain.model.${name}Model
import $pkg.domain.repository.${name}Repository

class $ucName(
    private val repository: ${name}Repository,
) {
    suspend operator fun invoke($paramStr): $returnType = $callExpr
}
""".trimStart()
    }

    // ── Template: Room Entity ─────────────────────────────────────────────────

    private fun genEntity(schema: IdeFeatureSchema): String {
        val name  = schema.featureName
        val pkg   = schema.packageBase
        val table = schema.storage.tableName.ifBlank { "${name.lowercase()}s" }

        val fields = schema.fields.mapIndexed { idx, f ->
            val snake   = f.name.toSnakeCase()
            val primary = if (idx == 0) "    @PrimaryKey\n" else ""
            "$primary    @ColumnInfo(name = \"$snake\")\n    val ${f.name}: ${f.kotlinType()},"
        }.joinToString("\n")

        return """
package $pkg.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "$table")
data class ${name}Entity(
$fields
)
""".trimStart()
    }

    // ── Template: Room DAO ────────────────────────────────────────────────────

    private fun genDao(schema: IdeFeatureSchema): String {
        val name   = schema.featureName
        val pkg    = schema.packageBase
        val table  = schema.storage.tableName.ifBlank { "${name.lowercase()}s" }
        val first  = schema.fields.firstOrNull()
        val idName = first?.name ?: "id"
        val idType = first?.type ?: "Long"

        return """
package $pkg.data.local.dao

import androidx.room.*
import $pkg.data.local.entity.${name}Entity

@Dao
interface ${name}Dao {

    @Query("SELECT * FROM $table")
    suspend fun getAll(): List<${name}Entity>

    @Query("SELECT * FROM $table WHERE $idName = :$idName LIMIT 1")
    suspend fun getById($idName: $idType): ${name}Entity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<${name}Entity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: ${name}Entity)

    @Delete
    suspend fun delete(entity: ${name}Entity)
}
""".trimStart()
    }

    // ── Template: Retrofit DTO ────────────────────────────────────────────────

    private fun genDto(schema: IdeFeatureSchema): String {
        val name = schema.featureName
        val pkg  = schema.packageBase
        val addSerial = schema.options.addSerializable

        val serialImports = if (addSerial)
            "import kotlinx.serialization.SerialName\nimport kotlinx.serialization.Serializable\n\n" else ""
        val serialAnnotation = if (addSerial) "@Serializable\n" else ""

        val fields = schema.fields.joinToString("\n") { f ->
            val snake = f.name.toSnakeCase()
            val serialName = if (addSerial && snake != f.name)
                "    @SerialName(\"$snake\")\n" else ""
            "$serialName    val ${f.name}: ${f.kotlinType()},"
        }

        return """
package $pkg.data.remote.dto

${serialImports}${serialAnnotation}data class ${name}Dto(
$fields
)
""".trimStart()
    }

    // ── Template: Retrofit ApiService ─────────────────────────────────────────

    private fun genApiService(schema: IdeFeatureSchema): String {
        val name = schema.featureName
        val pkg  = schema.packageBase

        // Collect unique HTTP methods for imports
        val httpMethods = if (schema.api.endpoints.isEmpty()) setOf("GET")
                          else schema.api.endpoints.map { it.httpMethod.uppercase() }.toSet()
        val httpImports = httpMethods.sorted().joinToString("\n") { "import retrofit2.http.$it" }

        val hasPathParams = schema.api.endpoints.any { pathParams(it.path).isNotEmpty() }
        val pathImport = if (hasPathParams) "\nimport retrofit2.http.Path" else ""

        val methods = if (schema.api.endpoints.isEmpty()) {
            "    @GET(\"/${name.lowercase()}s\")\n    suspend fun get${name}s(): List<${name}Dto>"
        } else {
            schema.api.endpoints.joinToString("\n\n") { ep ->
                val pp     = pathParams(ep.path)
                val params = pp.joinToString(", ") { "@Path(\"$it\") $it: Long" }
                val ret    = if (ep.returnsList) "List<${name}Dto>" else "${name}Dto?"
                "    @${ep.httpMethod.uppercase()}(\"${ep.path}\")\n    suspend fun ${ep.name}($params): $ret"
            }
        }

        return """
package $pkg.data.remote.service

import $pkg.data.remote.dto.${name}Dto
$httpImports$pathImport

interface ${name}ApiService {
$methods
}
""".trimStart()
    }

    // ── Template: Mapper ──────────────────────────────────────────────────────

    private fun genMapper(schema: IdeFeatureSchema): String {
        val name        = schema.featureName
        val pkg         = schema.packageBase
        val hasRoom     = schema.storage.engine == IdeStorageEngine.ROOM
        val hasRetrofit = schema.api.client    == IdeApiClient.RETROFIT

        val imports = buildString {
            if (hasRoom)     appendLine("import $pkg.data.local.entity.${name}Entity")
            if (hasRetrofit) appendLine("import $pkg.data.remote.dto.${name}Dto")
            append("import $pkg.domain.model.${name}Model")
        }

        val fieldMap = schema.fields.joinToString(",\n") { f ->
            "    ${f.name} = ${f.name}"
        }

        val dtoMapper = if (hasRetrofit) """

fun ${name}Dto.toDomain(): ${name}Model = ${name}Model(
$fieldMap
)""" else ""

        val entityMapper = if (hasRoom) """

fun ${name}Entity.toDomain(): ${name}Model = ${name}Model(
$fieldMap
)

fun ${name}Model.toEntity(): ${name}Entity = ${name}Entity(
$fieldMap
)""" else ""

        return """
package $pkg.data.mapper

$imports
$dtoMapper$entityMapper
""".trimStart()
    }

    // ── Template: RepositoryImpl ──────────────────────────────────────────────

    private fun genRepositoryImpl(schema: IdeFeatureSchema): String {
        val name        = schema.featureName
        val pkg         = schema.packageBase
        val hasRoom     = schema.storage.engine == IdeStorageEngine.ROOM
        val hasRetrofit = schema.api.client    == IdeApiClient.RETROFIT

        val imports = buildString {
            if (hasRoom) {
                appendLine("import $pkg.data.local.dao.${name}Dao")
                appendLine("import $pkg.data.mapper.toDomain")
                appendLine("import $pkg.data.mapper.toEntity")
            }
            if (hasRetrofit) {
                if (!hasRoom) appendLine("import $pkg.data.mapper.toDomain")
                appendLine("import $pkg.data.remote.service.${name}ApiService")
            }
            appendLine("import $pkg.domain.model.${name}Model")
            append("import $pkg.domain.repository.${name}Repository")
        }

        val ctorParams = buildList {
            if (hasRoom)     add("    private val dao: ${name}Dao,")
            if (hasRetrofit) add("    private val apiService: ${name}ApiService,")
        }.joinToString("\n")

        val overrides = if (schema.api.endpoints.isEmpty()) {
            val body = when {
                hasRetrofit -> "apiService.get${name}s().map { it.toDomain() }"
                hasRoom     -> "dao.getAll().map { it.toDomain() }"
                else        -> "emptyList()"
            }
            "    override suspend fun get${name}s(): List<${name}Model> = $body"
        } else {
            schema.api.endpoints.joinToString("\n\n") { ep ->
                val pp         = pathParams(ep.path)
                val paramStr   = pp.joinToString(", ") { "$it: Long" }
                val returnType = if (ep.returnsList) "List<${name}Model>" else "${name}Model?"
                val callArgs   = pp.joinToString(", ")
                val body = when {
                    hasRetrofit && ep.returnsList  -> "apiService.${ep.name}($callArgs).map { it.toDomain() }"
                    hasRetrofit && !ep.returnsList -> "apiService.${ep.name}($callArgs)?.toDomain()"
                    hasRoom     && ep.returnsList  -> "dao.getAll().map { it.toDomain() }"
                    hasRoom     && !ep.returnsList -> "dao.getById(${pp.firstOrNull() ?: ""})?.toDomain()"
                    else                           -> if (ep.returnsList) "emptyList()" else "null"
                }
                "    override suspend fun ${ep.name}($paramStr): $returnType = $body"
            }
        }

        return """
package $pkg.data.repository

$imports

class ${name}RepositoryImpl(
$ctorParams
) : ${name}Repository {
$overrides
}
""".trimStart()
    }

    // ── Template: DI Module ───────────────────────────────────────────────────

    private fun genDiModule(schema: IdeFeatureSchema): String =
        when (schema.di.framework) {
            IdeDiFramework.HILT -> genHiltModule(schema)
            IdeDiFramework.KOIN -> genKoinModule(schema)
        }

    private fun genHiltModule(schema: IdeFeatureSchema): String {
        val name = schema.featureName
        val pkg  = schema.packageBase
        return """
package $pkg.di

import $pkg.data.repository.${name}RepositoryImpl
import $pkg.domain.repository.${name}Repository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ${name}Module {

    @Binds
    @Singleton
    abstract fun bind${name}Repository(impl: ${name}RepositoryImpl): ${name}Repository
}
""".trimStart()
    }

    private fun genKoinModule(schema: IdeFeatureSchema): String {
        val name        = schema.featureName
        val pkg         = schema.packageBase
        val hasRoom     = schema.storage.engine == IdeStorageEngine.ROOM
        val hasRetrofit = schema.api.client    == IdeApiClient.RETROFIT
        val implArgs    = buildList {
            if (hasRoom)     add("get()")
            if (hasRetrofit) add("get()")
        }.joinToString(", ")

        return """
package $pkg.di

import $pkg.data.repository.${name}RepositoryImpl
import $pkg.domain.repository.${name}Repository
import org.koin.dsl.module

val ${name.decapitalized()}Module = module {
    single<${name}Repository> { ${name}RepositoryImpl($implArgs) }
}
""".trimStart()
    }

    // ── EDT dispatch helpers ──────────────────────────────────────────────────

    private fun emit(text: String) =
        ApplicationManager.getApplication().invokeLater { onOutput(text) }

    private fun complete(success: Boolean, files: List<String>) =
        ApplicationManager.getApplication().invokeLater { onComplete(success, files) }
}


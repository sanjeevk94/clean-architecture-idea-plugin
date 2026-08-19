package com.korina.cleanarch.idea.schema

import kotlinx.serialization.Serializable

// ---------------------------------------------------------------------------
// Schema data classes for the IntelliJ plugin.
//
// These define the full feature schema collected by FeatureSchemaDialog and
// consumed by DirectFileGenerator to write all Clean Architecture files directly.
// ---------------------------------------------------------------------------

@Serializable
data class IdeFeatureSchema(
    val featureName: String,
    val packageBase: String,
    val moduleRouting: IdeModuleRouting = IdeModuleRouting(),
    val fields: List<IdeFieldDefinition>,
    val storage: IdeStorageConfig = IdeStorageConfig(),
    val api: IdeApiConfig = IdeApiConfig(),
    val di: IdeDiConfig = IdeDiConfig(),
    val options: IdeGenerationOptions = IdeGenerationOptions()
)

@Serializable
data class IdeModuleRouting(
    val domain: String = ":domain",
    val data:   String = ":data",
    val app:    String = ":app"
)

@Serializable
data class IdeFieldDefinition(
    val name:     String,
    val type:     String,
    val nullable: Boolean = false
)

// ── Storage ─────────────────────────────────────────────────────────────────

@Serializable
data class IdeStorageConfig(
    val engine:    IdeStorageEngine = IdeStorageEngine.NONE,
    val tableName: String           = ""
)

@Serializable
enum class IdeStorageEngine { ROOM, DATASTORE, NONE }

// ── API ─────────────────────────────────────────────────────────────────────

@Serializable
data class IdeApiConfig(
    val client:    IdeApiClient             = IdeApiClient.NONE,
    val endpoints: List<IdeEndpointDefinition> = emptyList()
)

@Serializable
enum class IdeApiClient { RETROFIT, KTOR, NONE }

@Serializable
data class IdeEndpointDefinition(
    val name:        String,
    val httpMethod:  String,
    val path:        String,
    val returnsList: Boolean = false
)

// ── DI ──────────────────────────────────────────────────────────────────────

@Serializable
data class IdeDiConfig(
    val framework: IdeDiFramework = IdeDiFramework.HILT
)

@Serializable
enum class IdeDiFramework { HILT, KOIN }

// ── Options ─────────────────────────────────────────────────────────────────

@Serializable
data class IdeGenerationOptions(
    val generateUseCasePerEndpoint: Boolean = false,
    val coroutinesFlow:             Boolean = true,
    val addSerializable:            Boolean = true
)

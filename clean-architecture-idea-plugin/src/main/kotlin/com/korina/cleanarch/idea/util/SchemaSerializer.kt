package com.korina.cleanarch.idea.util

import com.korina.cleanarch.idea.schema.*
import com.korina.cleanarch.idea.ui.model.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Converts a [UiFeatureSchema] (dialog state) into an [IdeFeatureSchema] or
 * a pretty-printed JSON string.
 *
 * Primary use: [toIdeSchema] — converts the dialog state directly to an
 * [IdeFeatureSchema] which is handed to [com.korina.cleanarch.idea.generator.DirectFileGenerator].
 * [toJson] is retained for debugging / export purposes.
 */
object SchemaSerializer {

    private val json = Json {
        prettyPrint    = true
        encodeDefaults = true
    }

    fun toJson(ui: UiFeatureSchema): String = json.encodeToString(toSchema(ui))

    /** Converts a [UiFeatureSchema] directly to [IdeFeatureSchema] (no JSON round-trip needed). */
    fun toIdeSchema(ui: UiFeatureSchema): IdeFeatureSchema = toSchema(ui)

    // ── Conversion ────────────────────────────────────────────────────────────

    private fun toSchema(ui: UiFeatureSchema): IdeFeatureSchema = IdeFeatureSchema(
        featureName   = ui.featureName,
        packageBase   = ui.packageBase,
        moduleRouting = IdeModuleRouting(
            domain = ui.domainModule,
            data   = ui.dataModule,
            app    = ui.appModule
        ),
        fields        = ui.fields.map { f ->
            IdeFieldDefinition(
                name     = f.name,
                type     = f.type,
                nullable = f.nullable
            )
        },
        storage       = IdeStorageConfig(
            engine    = ui.storageEngine.toIde(),
            tableName = ui.tableName.ifBlank { "${ui.featureName.lowercase()}s" }
        ),
        api           = IdeApiConfig(
            client    = ui.apiClient.toIde(),
            endpoints = ui.endpoints.map { e ->
                IdeEndpointDefinition(
                    name        = e.name,
                    httpMethod  = e.httpMethod,
                    path        = e.path,
                    returnsList = e.returnsList
                )
            }
        ),
        di            = IdeDiConfig(framework = ui.diFramework.toIde()),
        options       = IdeGenerationOptions(
            generateUseCasePerEndpoint = ui.generateUseCasePerEndpoint,
            coroutinesFlow             = ui.coroutinesFlow,
            addSerializable            = ui.addSerializable
        )
    )

    // ── Enum mapping helpers ──────────────────────────────────────────────────

    private fun StorageEngineUi.toIde(): IdeStorageEngine = when (this) {
        StorageEngineUi.ROOM      -> IdeStorageEngine.ROOM
        StorageEngineUi.DATASTORE -> IdeStorageEngine.DATASTORE
        StorageEngineUi.NONE      -> IdeStorageEngine.NONE
    }

    private fun ApiClientUi.toIde(): IdeApiClient = when (this) {
        ApiClientUi.RETROFIT -> IdeApiClient.RETROFIT
        ApiClientUi.KTOR     -> IdeApiClient.KTOR
        ApiClientUi.NONE     -> IdeApiClient.NONE
    }

    private fun DiFrameworkUi.toIde(): IdeDiFramework = when (this) {
        DiFrameworkUi.HILT -> IdeDiFramework.HILT
        DiFrameworkUi.KOIN -> IdeDiFramework.KOIN
    }
}

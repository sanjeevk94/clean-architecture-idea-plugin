package com.korina.cleanarch.idea.ui.model

// ---------------------------------------------------------------------------
// Lightweight UI-state data classes used by FeatureSchemaDialog.
// SchemaSerializer converts these into IdeFeatureSchema → JSON.
// ---------------------------------------------------------------------------

/** Mutable snapshot of everything the user entered in the dialog. */
data class UiFeatureSchema(
    var featureName:               String                      = "",
    var packageBase:               String                      = "com.korina.myapp",
    var domainModule:              String                      = ":domain",
    var dataModule:                String                      = ":data",
    var appModule:                 String                      = ":app",
    val fields:                    MutableList<UiFieldDef>     = mutableListOf(),
    var storageEngine:             StorageEngineUi             = StorageEngineUi.ROOM,
    var tableName:                 String                      = "",
    var apiClient:                 ApiClientUi                 = ApiClientUi.RETROFIT,
    val endpoints:                 MutableList<UiEndpointDef>  = mutableListOf(),
    var diFramework:               DiFrameworkUi               = DiFrameworkUi.HILT,
    var generateUseCasePerEndpoint: Boolean                    = false,
    var coroutinesFlow:            Boolean                     = true,
    var addSerializable:           Boolean                     = true
)

data class UiFieldDef(
    var name:     String  = "",
    var type:     String  = "String",
    var nullable: Boolean = false
)

data class UiEndpointDef(
    var name:        String  = "",
    var httpMethod:  String  = "GET",
    var path:        String  = "/",
    var returnsList: Boolean = false
)

// ── Enums used in combo boxes ────────────────────────────────────────────────

enum class StorageEngineUi { ROOM, DATASTORE, NONE }
enum class ApiClientUi     { RETROFIT, KTOR, NONE }
enum class DiFrameworkUi   { HILT, KOIN }

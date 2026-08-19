# Clean Architecture Boilerplate Generator — Technical Plan & Specification

> **Version:** 1.1 — Updated for Phase 2  
> **Stack:** Gradle 9.4.1 · AGP 9.2.1 · Kotlin 2.2.21 · KotlinPoet 2.3.0

---

## 1 · Architecture Overview & Component Diagram

```
┌─────────────────────────────────────────────────────────────────────┐
│                     Developer's Android Studio                       │
│                                                                      │
│  ┌──────────────────────────────────────────────────────────────┐   │
│  │         clean-architecture-idea-plugin (IntelliJ SDK)        │   │
│  │                                                              │   │
│  │  GenerateFeatureAction (AnAction)                            │   │
│  │       │                                                      │   │
│  │       ▼                                                      │   │
│  │  FeatureSchemaDialog (DialogWrapper)                         │   │
│  │    [Feature name, fields, DI, storage, endpoints]            │   │
│  │       │  serialises to  FeatureSchema (JSON)                 │   │
│  │       ▼                                                      │   │
│  │  GradleTaskInvoker                                           │   │
│  │    • GradleConnector (Tooling API)                           │   │
│  │    • writes schema file → build/cleanarch/schema.json        │   │
│  │    • runs :generateCleanArchFeature                          │   │
│  └──────────────────────────────────────────────────────────────┘   │
│           │  Tooling API (in-process or daemon)                      │
└───────────┼─────────────────────────────────────────────────────────┘
            │
┌───────────▼──────────────────────────────────────────────────────────┐
│             Target Multi-Module Android Project (this repo)          │
│                                                                      │
│  ┌──────────────────────────────────────────────────────────────┐   │
│  │      clean-architecture-gradle-plugin (Gradle Plugin API)    │   │
│  │                                                              │   │
│  │  CleanArchPlugin (Plugin<Project>)                           │   │
│  │    • registers CleanArchExtension                            │   │
│  │    • registers GenerateCleanArchFeatureTask                  │   │
│  │         │                                                    │   │
│  │         ▼                                                    │   │
│  │  SchemaParser (kotlinx.serialization JSON)                   │   │
│  │         │                                                    │   │
│  │         ▼                                                    │   │
│  │  CodeGeneratorOrchestrator                                   │   │
│  │    ├── DomainGenerator  (KotlinPoet)                         │   │
│  │    ├── DataGenerator    (KotlinPoet)                         │   │
│  │    └── DiGenerator      (KotlinPoet)                         │   │
│  │         │  writes FileSpec objects → returns List<File>      │   │
│  │         ▼                                                    │   │
│  │  FileWriterService  →  :domain/src, :data/src, :app/src      │   │
│  │  GenerationManifest →  build/cleanarch/generation-manifest   │   │
│  └──────────────────────────────────────────────────────────────┘   │
│                                                                      │
│   :domain          :data           :app (DI)                        │
│   ─────────        ─────────       ─────────────                    │
│   Model            Entity          HiltModule / KoinModule           │
│   Repository IF    DTO             (binds Impl → Interface)          │
│   UseCase          Mapper                                            │
│                    DAO / Service                                     │
│                    RepositoryImpl                                    │
└──────────────────────────────────────────────────────────────────────┘
```

**Key contract:** The IDE plugin writes `build/cleanarch/schema.json`, passes its absolute path
as the Gradle task input property `--schemaFile`, then streams task output back to the IDE's
`ToolWindowManager` via a `BuildLauncher` `ResultHandler`.

---

## 2 · Schema Specification (JSON)

**File:** `build/cleanarch/schema.json`

```json
{
  "featureName": "Product",
  "packageBase": "com.korina.myapp",
  "moduleRouting": {
    "domain": ":domain",
    "data": ":data",
    "app": ":app"
  },
  "fields": [
    { "name": "id",          "type": "Long",    "nullable": false },
    { "name": "title",       "type": "String",  "nullable": false },
    { "name": "description", "type": "String",  "nullable": true  },
    { "name": "price",       "type": "Double",  "nullable": false }
  ],
  "storage": { "engine": "ROOM", "tableName": "products" },
  "api": {
    "client": "RETROFIT",
    "endpoints": [
      { "name": "getProducts", "httpMethod": "GET", "path": "/products",      "returnsList": true  },
      { "name": "getProduct",  "httpMethod": "GET", "path": "/products/{id}", "returnsList": false }
    ]
  },
  "di": { "framework": "HILT" },
  "options": {
    "generateUseCasePerEndpoint": false,
    "coroutinesFlow": true,
    "addSerializable": true
  }
}
```

### Enum Reference

| Field | Allowed Values |
|---|---|
| `storage.engine` | `ROOM`, `DATASTORE`, `NONE` |
| `api.client` | `RETROFIT`, `KTOR`, `NONE` |
| `api.endpoints[].httpMethod` | `GET`, `POST`, `PUT`, `DELETE`, `PATCH` |
| `di.framework` | `HILT`, `KOIN` |

---

## 3 · Module Breakdown & Design

### Module A — `clean-architecture-gradle-plugin`

**Coordinates:** `com.korina.cleanarch:gradle-plugin:1.0.0`  
**Plugin ID:** `com.korina.cleanarchitecture`

#### Source Files

| File | Responsibility |
|---|---|
| `CleanArchPlugin.kt` | `Plugin<Project>`. Registers extension + task, wires defaults. |
| `CleanArchExtension.kt` | DSL: `schemaFile`, `manifestFile`, `outputDir`. |
| `GenerateCleanArchFeatureTask.kt` | `@InputFile schemaFile`, `@OutputFile manifestFile`, `@Option schemaFileOverride`, `@Option dryRun`. |
| `schema/FeatureSchema.kt` | `@Serializable` data class tree. |
| `schema/SchemaParser.kt` | `Json.decodeFromString<FeatureSchema>()`. |
| `schema/SchemaValidator.kt` | Validation + `SchemaValidationException`. |
| `generator/CodeGeneratorOrchestrator.kt` | Orchestrates generators, returns `List<File>`. |
| `generator/GeneratorContext.kt` | Pre-computed names and resolved module source roots. |
| `generator/domain/DomainGenerator.kt` | Model, Repository interface, UseCase(s). |
| `generator/data/DataGenerator.kt` | Entity, DTO, Mapper, DAO, ApiService, RepositoryImpl. |
| `generator/di/DiGenerator.kt` | Hilt `@Module` or Koin `module { }`. |
| `writer/FileWriterService.kt` | Writes `FileSpec` to source root, returns `File`. |
| `writer/GenerationManifest.kt` | Read/write manifest; stale-file cleanup. |
| `util/NameUtils.kt` | `toPascalCase()`, `toCamelCase()`, `toSnakeCase()`. |
| `util/KotlinPoetExtensions.kt` | `buildDataClass`, `parameterizedBy`, `toTypeName`. |
| `util/ParameterizedTypeHelper.java` | Java bridge for Kotlin 2.2.x KotlinPoet overload bug. |

#### Incremental Build Design

```
Inputs:   @InputFile  schemaFile         (content-fingerprinted via SHA-256)
Outputs:  @OutputFile manifestFile       (build/cleanarch/generation-manifest.txt)

UP-TO-DATE:  schemaFile unchanged → manifest unchanged → skip
Re-execute:  schemaFile changed → regenerate → rewrite manifest
Stale files: newManifest - oldManifest → delete orphaned .kt files
```

#### CLI Surface

```bash
./gradlew generateCleanArchFeature
./gradlew generateCleanArchFeature --dryRun=true
./gradlew generateCleanArchFeature --schemaFile=/path/to/other-schema.json
```

---

### Module B — `clean-architecture-idea-plugin` *(Phase 3–4)*

**Plugin ID:** `com.korina.cleanarchitecture.idea`

#### Source Files

| File | Responsibility |
|---|---|
| `actions/GenerateFeatureAction.kt` | `AnAction` — opens dialog from Project View / Generate menu. |
| `ui/FeatureSchemaDialog.kt` | `DialogWrapper` with Schema tab (name + fields table) and Options tab. |
| `ui/FieldTableModel.kt` | `AbstractTableModel` backing the `JBTable`. |
| `gradle/GradleTaskInvoker.kt` | Writes JSON, connects via `GradleConnector`, runs task. |
| `gradle/GradleOutputProcessor.kt` | Parses task output, posts `GenerationResult`. |
| `toolwindow/CleanArchToolWindowFactory.kt` | `ToolWindowFactory` with `ConsoleView`. |
| `notifications/CleanArchNotifier.kt` | Balloon notifications on success/failure. |
| `util/SchemaSerializer.kt` | `FeatureSchema` → JSON → `VirtualFileSystem`. |

---

## 4 · Implementation Roadmap

### Phase 1 ✅ DONE — Gradle Plugin Core & KotlinPoet Templates

All three generators, FileWriterService, SchemaParser/Validator, composite build, 20 unit tests.

### Phase 2 ✅ DONE — Incremental Build & Integration Tests

- `GenerationManifest` for UP-TO-DATE tracking and stale-file cleanup
- `@OutputFile manifestFile` on the task (removes `@UntrackedTask`)
- `--schemaFile` CLI override option
- 14 GradleRunner integration tests in `GenerateFeatureTaskTest.kt` covering:
  - All 10 generated files present
  - Manifest written with ≥ 9 entries
  - UP-TO-DATE on second run (no schema change)
  - Re-execution after schema edit
  - Dry-run writes no files
  - Stale file deletion on feature rename

### Phase 3  NEXT — IntelliJ Plugin Scaffold & Dialog UI

1. Add `clean-architecture-idea-plugin/` module with `org.jetbrains.intellij.platform` plugin.
2. `plugin.xml` — action, tool window, notification group.
3. `FeatureSchemaDialog` — name field, `JBTable` fields editor, DI/storage/API combos.
4. `SchemaSerializer` — dialog state → `FeatureSchema` JSON.
5. `GenerateFeatureAction` — opens dialog, serialises schema, hands off to invoker.

### Phase 4  — Tooling API Integration & Live Console

1. `GradleTaskInvoker` — `GradleConnector` + `BuildLauncher`.
2. Live output streaming → `CleanArchToolWindowFactory` ConsoleView.
3. Generated-files tree on completion.
4. `CleanArchNotifier` balloons.
5. `.zip` build via `buildPlugin` task.

---

## 5 · File & Package Tree

```
CleanArchitecturePlugin/
├── settings.gradle.kts
├── build.gradle.kts
├── gradle/libs.versions.toml
├── TECHNICAL_SPEC.md
├── README.md
├── build/cleanarch/
│   ├── schema.json
│   └── generation-manifest.txt          ← written at runtime
│
├── domain/                              ← :domain (pure Kotlin)
├── data/                                ← :data (Android library)
├── app/                                 ← :app (Hilt consumer)
│
├── clean-architecture-gradle-plugin/
│   ├── settings.gradle.kts
│   ├── build.gradle.kts
│   └── src/
│       ├── main/
│       │   ├── kotlin/com/example/cleanarch/gradle/
│       │   │   ├── CleanArchPlugin.kt
│       │   │   ├── CleanArchExtension.kt
│       │   │   ├── GenerateCleanArchFeatureTask.kt
│       │   │   ├── schema/         FeatureSchema, SchemaParser, SchemaValidator
│       │   │   ├── generator/      Orchestrator, Context, domain/, data/, di/
│       │   │   ├── writer/         FileWriterService, GenerationManifest
│       │   │   └── util/           NameUtils, KotlinPoetExtensions
│       │   ├── java/com/example/cleanarch/gradle/util/
│       │   │   └── ParameterizedTypeHelper.java
│       │   └── resources/META-INF/gradle-plugins/
│       │       └── com.korina.cleanarchitecture.properties
│       └── test/
│           ├── kotlin/com/example/cleanarch/gradle/
│           │   ├── SchemaParserTest.kt
│           │   ├── DomainGeneratorTest.kt
│           │   ├── DataGeneratorTest.kt
│           │   └── integration/
│           │       └── GenerateFeatureTaskTest.kt
│           └── resources/
│               ├── sample-schema.json
│               └── integration-schema.json
│
└── clean-architecture-idea-plugin/      ← Phase 3–4 (not yet implemented)
    ├── build.gradle.kts
    └── src/main/
        ├── kotlin/com/example/cleanarch/idea/
        │   ├── actions/GenerateFeatureAction.kt
        │   ├── ui/FeatureSchemaDialog.kt, FieldTableModel.kt
        │   ├── gradle/GradleTaskInvoker.kt, GradleOutputProcessor.kt
        │   ├── toolwindow/CleanArchToolWindowFactory.kt
        │   ├── notifications/CleanArchNotifier.kt
        │   └── util/SchemaSerializer.kt
        └── resources/META-INF/plugin.xml
```

---

## 6 · Further Considerations

- **Schema format:** JSON used; YAML can be added via `com.charleskorn.kaml` + file-extension detect in `SchemaParser`.
- **Plugin distribution:** Gradle Plugin Portal for the Gradle plugin; JetBrains Marketplace for the IDE plugin.
- **Kotlin 2.2.x + KotlinPoet:** `ParameterizedTypeHelper.java` bridge sidesteps the overload resolution bug.
- **Android module:** `:data` is `com.android.library`; KSP/Room compiler wiring is consumer responsibility.
- **KTOR support:** Accepted by schema validator but data-layer generation is stubbed; planned for a future sprint.
- **DataStore:** Accepted in schema; no generator wired yet.
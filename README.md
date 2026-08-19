# Clean Architecture Generator

> **One-click Clean Architecture boilerplate for Android multi-module projects.**  
> Install the IDE plugin → open your project → fill in a dialog → 10 files generated instantly.

[![JetBrains Marketplace](https://img.shields.io/badge/JetBrains%20Marketplace-Clean%20Architecture%20Generator-blue)](https://plugins.jetbrains.com)
[![Version](https://img.shields.io/badge/version-1.0.0-green)]()
[![IDE Support](https://img.shields.io/badge/IDE-IntelliJ%202023.2%2B%20%7C%20Android%20Studio-orange)]()

---

## Install

**From JetBrains Marketplace** (recommended):
```
Settings → Plugins → Marketplace → search "Clean Architecture Generator" → Install
```

**From zip** (for testing):
```
Settings → Plugins → ⚙ → Install Plugin from Disk → select .zip
```

No Gradle plugin. No `build.gradle.kts` changes. No setup.

---

## How to Use

### 1 · Open the dialog

Any of these three ways:

| Where | How |
|-------|-----|
| Top menu | **Tools → Generate Clean Arch Feature…** |
| Project view | Right-click any package → **Generate Clean Arch Feature…** |
| Editor | **Alt+Insert** → **Generate Clean Arch Feature…** |

---

### 2 · Fill in the Schema tab

| Field | Example | Description |
|-------|---------|-------------|
| Feature name | `Order` | PascalCase — drives all generated class names |
| Package base | `com.mycompany.myapp` | Root package of your app |
| Domain module | `:domain` | Gradle module path for domain layer |
| Data module | `:data` | Gradle module path for data layer |
| App module | `:app` | Gradle module path for DI module |
| Fields | `orderId: String` `totalAmount: Double` | Add rows with **+**, pick type from dropdown |

---

### 3 · Fill in the Options tab

| Field | Options | Description |
|-------|---------|-------------|
| DI framework | `HILT` / `KOIN` | Generates the appropriate DI module |
| Storage engine | `ROOM` / `NONE` | Generates `Entity` + `DAO` when ROOM |
| Table name | `orders` | Room `@Entity(tableName = …)` |
| API client | `RETROFIT` / `NONE` | Generates `DTO` + `ApiService` when RETROFIT |
| Endpoints | `getOrders GET /orders` `getOrder GET /orders/{id}` | Add rows, tick **returnsList** for list endpoints |
| Generate UseCase per endpoint | ✅ / ☐ | One UseCase per endpoint vs one shared UseCase |
| Wrap lists in Flow<> | ✅ / ☐ | Repository returns `Flow<List<T>>` |
| Add @Serializable to DTO | ✅ / ☐ | Adds `@Serializable` + `@SerialName` |

---

### 4 · Click Generate

All files appear instantly in the Project view. The output panel at the bottom shows each file written.

---

## What Gets Generated

For a feature named **`Order`** with Room + Retrofit + Hilt:

```
domain/src/main/kotlin/com/mycompany/myapp/
  domain/model/            OrderModel.kt           ← data class with all fields
  domain/repository/       OrderRepository.kt      ← interface (getOrders / getOrder)
  domain/usecase/          GetOrdersUseCase.kt     ← one UseCase per endpoint
                           GetOrderUseCase.kt

data/src/main/kotlin/com/mycompany/myapp/
  data/local/entity/       OrderEntity.kt          ← @Entity(tableName="orders")
  data/local/dao/          OrderDao.kt             ← @Dao with getAll/getById/insert/delete
  data/remote/dto/         OrderDto.kt             ← @Serializable with @SerialName
  data/remote/service/     OrderApiService.kt      ← Retrofit @GET interface
  data/mapper/             OrderMapper.kt          ← toDomain() / toEntity() ext funs
  data/repository/         OrderRepositoryImpl.kt  ← wires DAO + ApiService

app/src/main/java/com/mycompany/myapp/
  di/                      OrderModule.kt          ← @Module @InstallIn(SingletonComponent)
```

**10 files. Zero boilerplate written by hand.**

---

## Supported Configurations

| Layer | Options |
|-------|---------|
| Domain | Model · Repository interface · UseCase (single or per-endpoint) |
| Local storage | Room (`@Entity`, `@Dao`) or None |
| Remote API | Retrofit (`@Serializable` DTO, `@GET`/`@POST` service) or None |
| DI | Hilt (`@Module @InstallIn`) or Koin (`module { single<> }`) |
| IDE compatibility | IntelliJ IDEA 2023.2+ · Android Studio Iguana+ |

---

## Project Structure

```
CleanArchitecturePlugin/
├── clean-architecture-idea-plugin/     ← Self-contained IDE plugin
│   └── src/main/kotlin/
│       ├── actions/    GenerateFeatureAction.kt
│       ├── generator/  DirectFileGenerator.kt   ← writes files via Java IO
│       ├── schema/     IdeFeatureSchema.kt
│       ├── ui/         FeatureSchemaDialog.kt, FieldTableModel, EndpointTableModel
│       ├── util/       SchemaSerializer.kt
│       ├── toolwindow/ CleanArchToolWindowFactory.kt
│       └── notifications/ CleanArchNotifier.kt
│
├── domain/                             ← Sample :domain module (reference output)
├── data/                               ← Sample :data module (reference output)
└── app/                                ← Sample :app module (reference output)
```

---

## Publishing

See **[PUBLISHING.md](PUBLISHING.md)** for the full step-by-step guide to publish
to JetBrains Marketplace and the Gradle Plugin Portal.

---

## Schema Field Reference

| Field | Type | Required | Default | Description |
|-------|------|----------|---------|-------------|
| `featureName` | String (PascalCase) | ✅ | — | e.g. `"Order"` |
| `packageBase` | String | ✅ | — | e.g. `"com.mycompany.myapp"` |
| `moduleRouting.domain` | String | — | `":domain"` | Gradle module for domain layer |
| `moduleRouting.data` | String | — | `":data"` | Gradle module for data layer |
| `moduleRouting.app` | String | — | `":app"` | Gradle module for DI |
| `fields[].name` | String (camelCase) | ✅ | — | Field name |
| `fields[].type` | String | ✅ | — | `String`, `Long`, `Int`, `Double`, `Boolean` |
| `fields[].nullable` | Boolean | — | `false` | Appends `?` to type |
| `storage.engine` | `ROOM` \| `NONE` | — | `NONE` | Enables Entity + DAO generation |
| `storage.tableName` | String | if ROOM | `"{name}s"` | Room table name |
| `api.client` | `RETROFIT` \| `NONE` | — | `NONE` | Enables DTO + ApiService generation |
| `api.endpoints[].name` | String (camelCase) | ✅ | — | Function name e.g. `getOrders` |
| `api.endpoints[].httpMethod` | `GET` \| `POST` \| `PUT` \| `DELETE` | ✅ | — | HTTP verb |
| `api.endpoints[].path` | String | ✅ | — | e.g. `"/orders/{id}"` |
| `api.endpoints[].returnsList` | Boolean | — | `false` | Returns `List<T>` vs `T?` |
| `di.framework` | `HILT` \| `KOIN` | — | `HILT` | DI module style |
| `options.generateUseCasePerEndpoint` | Boolean | — | `false` | One UseCase per endpoint |
| `options.coroutinesFlow` | Boolean | — | `true` | Wrap list returns in `Flow<>` |
| `options.addSerializable` | Boolean | — | `true` | Add `@Serializable` to DTO |

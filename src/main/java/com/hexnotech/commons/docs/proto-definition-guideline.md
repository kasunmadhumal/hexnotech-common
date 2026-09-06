# Proto Definition Guideline

Standards for authoring `.proto` files in the aeroOPS platform.

---

## Guidelines

1. **Use kebab-case plural nouns for URL paths** — no verbs in the path.
2. **Map HTTP methods to operations strictly** — `POST` create, `PUT` update, `DELETE` delete, `GET` read, `POST /search` search with body.
3. **`DELETE` must not include `body: "*"`** — use path parameter `{id}` only.
4. **Use `google.protobuf.Empty` as request for no-input RPCs** — e.g. get-all.
5. **Use one `<Entity>Request` message for both create and update** — `id <= 0` means create, `id > 0` means update.
6. **Reuse `IdRequest` from `Common.proto` for delete** — do not define a custom single-id request.
7. **Use one `<Entity>` response message for single-entity responses** — same message returned by create, update, and get-by-id.
8. **Wrap list responses in `<Entity>ListResponse`** — never return a bare `repeated` at RPC level.
9. **Reuse the same entity message inside the list wrapper** — no separate summary/lite DTO.
10. **Use `UpdateResponse` from `Common.proto` for delete and state-change mutations** — do not define a custom status response.
11. **Use `POST /resource/search` with `<Entity>SearchRequest` when search has a body** — include filter fields and pagination (`pageNumber`, `pageSize`) in the message.
12. **Declare enums at file scope with `UPPER_SNAKE_CASE` values** — first value (`0`) must be the default/unknown.
13. **Reserve audit fields at numbers 9–12** — `createdBy`, `createdDate`, `lastUpdated`, `lastUpdatedBy`.
14. **Add `reserved` for removed field numbers and names** — prevents accidental reuse.
15. **Prefer types from `Common.proto`** — `IdRequest`, `UpdateResponse`, `DateTimeRangeFilter`, `ChangeLogRequest/Response`.
16. **Every file must declare** `syntax = "proto3"`, `package com.accelaero.ops.manager`, and `option java_multiple_files = true`.

---

## Samples

### URL Naming

```
✓  /crew-types
✗  /crewTypes  |  /getCrewTypes  |  /crew-type
```

### HTTP Method Mapping

| Operation | Method | URL |
|---|---|---|
| Create | `POST` | `/resource` |
| Update | `PUT` | `/resource` |
| Delete | `DELETE` | `/resource/{id}` |
| Get all | `GET` | `/resource` |
| Get by ID | `GET` | `/resource/{id}` |
| Search | `POST` | `/resource/search` |

### Request — single model for create and update (Guideline 5)

```proto
message ExampleRequest {
  int64  id   = 1;  // <= 0: create, > 0: update
  string name = 2;
}
```

### Response — entity and list wrapper (Guidelines 7, 8, 9)

```proto
message Example {
  int64  id            = 1;
  string name          = 2;
  string createdBy     = 9;
  string createdDate   = 10;
  string lastUpdated   = 11;
  string lastUpdatedBy = 12;
}

message ExampleListResponse {
  repeated Example examples = 1;
}
```

### Search request (Guideline 11)

```proto
message ExampleSearchRequest {
  string name       = 1;
  int32  pageNumber = 10;
  int32  pageSize   = 11;
}
```

### Service — full RPC mapping (Guidelines 2, 3, 4, 6, 10)

```proto
service ExampleService {

  rpc createExample(ExampleRequest) returns (Example) {
    option (google.api.http) = { post: "/examples" body: "*" };
  }

  rpc updateExample(ExampleRequest) returns (Example) {
    option (google.api.http) = { put: "/examples" body: "*" };
  }

  rpc deleteExample(IdRequest) returns (UpdateResponse) {
    option (google.api.http) = { delete: "/examples/{id}" };
  }

  rpc getAllExamples(google.protobuf.Empty) returns (ExampleListResponse) {
    option (google.api.http) = { get: "/examples" };
  }

  rpc searchExamples(ExampleSearchRequest) returns (ExampleListResponse) {
    option (google.api.http) = { post: "/examples/search" body: "*" };
  }
}
```


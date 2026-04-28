# OpenAPI Sync Rule

When any file under `src/main/java/com/postech/workshop_service/api/controllers/` is modified (created, edited, or deleted), you MUST update the OpenAPI spec file at `src/main/java/com/postech/workshop_service/api/controllers/openapi.yaml` to keep it in sync.

## What to sync

- **New endpoint**: Add the corresponding path + operation to `openapi.yaml`
- **Removed endpoint**: Remove the corresponding path + operation from `openapi.yaml`
- **Changed parameter** (path, query, request body): Update the parameters/requestBody in `openapi.yaml`
- **Changed response** (status code, schema): Update the responses in `openapi.yaml`
- **New/changed DTO** referenced by a controller: Update the corresponding schema in `components/schemas`

## Scope

- Applies to all `*Controller.java` files in the controllers package
- Does NOT apply to `GlobalExceptionHandler.java` (error responses are already documented under `ErrorResponse` schema)
- The `openapi.yaml` must always reflect the **actual runtime behavior** of the controllers, not aspirational or planned behavior

## Verification

After updating `openapi.yaml`, verify consistency by comparing:
1. Every `@RequestMapping` / `@GetMapping` / `@PostMapping` / `@PutMapping` / `@DeleteMapping` in the controller has a matching entry in `openapi.yaml`
2. Every path in `openapi.yaml` has a matching controller method
3. Parameter names, types, and required flags match

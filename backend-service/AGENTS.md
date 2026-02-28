# SpeisList — Backend Agent Instructions

Java 25, Spring Boot 3.5.6, Spring Data JPA, Spring Security (OAuth2 JWT).  
Spring AI 1.1.0-M4 MCP server exposed on `/mcp` (Streamable HTTP).  
Database: H2 (dev/test), PostgreSQL (prod).

---

## Commands

```bash
# Run all tests (excludes BackendApplicationTests which needs a live Keycloak)
./gradlew test

# Run a single test class
./gradlew test --tests "com.speislist.backend.shoppinglist.service.ShoppingListServiceTest"

# Run a single test method (use nested class dot syntax)
./gradlew test --tests "com.speislist.backend.shoppinglist.service.ShoppingListServiceTest.CreateShoppingListTests.shouldCreateShoppingListForUser"

# Run tests for a whole package
./gradlew test --tests "com.speislist.backend.inventory.*"

# Build (without tests)
./gradlew build -x test

# Apply Spotless formatting
./gradlew spotlessApply

# Check formatting without applying
./gradlew spotlessCheck

# Run the application
./gradlew bootRun
```

---

## Code Style

### General
- Java 25; use modern language features (records, pattern matching, `var`, text blocks) where they improve clarity.
- All source files live under `src/main/java/com/speislist/backend/`.
- Format with Palantir Java Format via `./gradlew spotlessApply` before committing.

### Package / Module Structure
Each domain (e.g. `shoppinglist`, `inventory`, `user`) has sub-packages:
`controller/`, `service/`, `repository/`, `entity/`, `dto/request/`, `dto/response/`, `exception/`, `util/`, `mcp/`

### Lombok
- Use `@Getter @Setter` on entities instead of writing accessors.
- Use `@RequiredArgsConstructor` on services and controllers (constructor injection).
- Use `@Builder @Data` on DTO classes.
- Use `@NoArgsConstructor @AllArgsConstructor @EqualsAndHashCode` on embeddable composite-key classes.

### Entities
- IDs use `@GeneratedValue(strategy = GenerationType.IDENTITY)` — the DB assigns them on INSERT.  
  **Never** read `entity.getId()` before calling `repository.save(entity)`; the ID is `null` until then.
- Timestamps use `@CreationTimestamp`.
- Use `CascadeType.ALL` + `orphanRemoval = true` on `@OneToMany` collections.
- Composite keys implement `Serializable`, carry `@Embeddable`, and use `@EmbeddedId` / `@MapsId` on the owning entity.

### Services
- Annotate transactional mutating methods with `@Transactional`; read-only queries with `@Transactional(readOnly = true)`.
- Access control is enforced **in the service layer**, not the controller. Non-members receive a "not found" exception (404 semantics, not 403) to avoid leaking existence.
- Helper methods package-private (no modifier) when they need to be called from sibling services in the same package.

### DTOs and Mappers
- Request DTOs live in `dto/request/`, response DTOs in `dto/response/`.
- Mappers are utility classes with a `private` constructor (`@NoArgsConstructor(access = AccessLevel.PRIVATE)`) and only `public static` methods.
- When a collection field may be `null`, mappers must return `List.of()` as the fallback — **never `null`**.

### Exceptions
- Domain exceptions extend `RuntimeException` with a descriptive message (`"Entity not found with id: " + id`).
- HTTP status mapping is done in `GlobalExceptionHandler` (`@RestControllerAdvice`) using `ProblemDetail`.

### Controllers
- Annotate every endpoint with `@SecuredOperation(summary = "...")` (combines `@PreAuthorize("isAuthenticated()")` + OpenAPI `bearerAuth`).
- Inject the authenticated user via `@AuthenticationPrincipal Jwt jwt` and pass `jwt.getSubject()` to the service.
- Return `ResponseEntity<T>` with explicit status codes (`CREATED`, `NO_CONTENT`, etc.).

### MCP Tools
- Annotate tool classes with `@Service` and methods with `@McpTool`.
- Resolve the current user via `JwtUserService.getCurrentUser()`, not from `SecurityContextHolder` directly (exception: `getShoppingLists` uses `authentication.getName()` — prefer `jwtUserService`).

### Naming
- Classes: `PascalCase`. Methods/variables: `camelCase`. Constants: `UPPER_SNAKE_CASE`.
- Repository query methods: `findBy…`, `deleteBy…`, `existsBy…`.
- Boolean entity fields: `isCompleted` (not `completed`) — Lombok generates `getIsCompleted()`.

---

## Testing

- Unit tests use Mockito (`@ExtendWith(MockitoExtension.class)`) — no Spring context loaded.
- Group related tests inside `@Nested` inner classes named after the method under test.
- Use `@DisplayName` on both nested classes and test methods.
- Assert with AssertJ (`assertThat`, `assertThatThrownBy`).
- Mock repositories and sibling services; never mock the class under test.
- `BackendApplicationTests.contextLoads()` requires a running Keycloak — it is expected to fail in offline environments.

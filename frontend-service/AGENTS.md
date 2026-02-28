# SpeisList — Frontend Agent Instructions

TypeScript 5.7, React 19, TanStack Start/Router/Query, Tailwind CSS v4.  
Auth via Keycloak. Build tool: Vite 7. Package manager: pnpm.

---

## Commands

```bash
# Install dependencies
pnpm install

# Start dev server (port 3000)
pnpm dev

# Run all tests (Vitest, non-watch)
pnpm test

# Run a single test file
pnpm exec vitest run src/path/to/file.test.tsx

# Lint
pnpm lint

# Format
pnpm format

# Lint + format check (CI-style)
pnpm check

# Production build
pnpm build
```

---

## Code Style

### General
- TypeScript strict mode; avoid `any`. Use `unknown` when the type is truly unknown.
- `import type` for type-only imports (`import type React from "react"`).
- Prefer named exports; use default exports only for route components.
- All shared types live in `src/data/types.ts`.

### Formatting (Biome 2.2.4)
- **Indentation:** tabs.
- **Quotes:** double quotes for JS/TS strings.
- Import organisation is automatic (`organizeImports: on`).
- Run `pnpm check` before committing; CI enforces it.

### React / Components
- Prop types as inline `Readonly<{ ... }>` objects on function parameters — no separate `Props` interface needed for simple cases.
- Small, single-purpose sub-components in the same file are fine (e.g., `LoadingView`, `EmptyState`).
- Use TanStack Query for all server state; do not use `useEffect` + `useState` for data fetching.
- Auth state comes from `useKeycloakAuth()` in `src/integrations/keycloak/root-provider.tsx`.
- Install shadcn/ui components via `pnpm dlx shadcn@latest add <component>` — do not copy components manually.

### Path Aliases
- `@/` maps to `src/` (configured in `vite.config.ts` via `vite-tsconfig-paths`).

### Routing
- File-based routing via TanStack Router. Add new routes as files under `src/routes/`.
- `src/routeTree.gen.ts` is auto-generated — **do not edit it manually**.

### Styling
- Tailwind CSS v4. Use utility classes directly; avoid custom CSS unless unavoidable.
- Do not edit `src/styles.css` (excluded from Biome).

---

## Testing

- Tests live alongside source files as `*.test.tsx` / `*.test.ts`.
- Use Testing Library (`@testing-library/react`) for component tests.
- Run with `pnpm test` (Vitest, no watch) or `pnpm exec vitest run <file>` for a single file.

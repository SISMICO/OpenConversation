# Project Rules

This is a JavaScript/TypeScript, Documentation / Markdown, Kotlin, Docker / Containers, SQL / Database project.

## Project Structure

<!-- TODO: Describe your project structure here -->
<!-- Example:
- `src/` - Application source code
- `tests/` - Test files
- `docs/` - Documentation
-->

## Code Standards

### JavaScript/TypeScript

- Use TypeScript with strict mode where possible
- Prefer ESM imports over CommonJS require
- Use async/await over raw Promises

### Documentation / Markdown

- Use consistent heading levels
- Keep line length under 120 characters where practical
- Include code examples for technical documentation

### Kotlin

- Use data classes for DTOs
- Prefer val over var
- Use coroutines for async operations

### Docker / Containers

- Use multi-stage builds for smaller images
- Pin base image versions
- Do not run as root in containers

### SQL / Database

- Use migrations for all schema changes
- Add indexes for frequently queried columns
- Use parameterized queries to prevent SQL injection

## Commands

### JavaScript/TypeScript

- **Build:** `npm run build`
- **Test:** `npm test`
- **Lint:** `npm run lint`

### Documentation / Markdown

- **Lint:** `markdownlint "**/*.md"`

### Kotlin

- **Build:** `gradle build`
- **Test:** `gradle test`
- **Lint:** `gradle ktlintCheck`

### Docker / Containers

- **Build:** `docker build .`
- **Lint:** `hadolint Dockerfile`

### SQL / Database

- **Lint:** `sqlfluff lint`

## Custom Agents

The following custom subagents are available (invoke with `@agent-name`):

- **@backend-developer**: Server-side logic, APIs, and data processing
- **@frontend-developer**: UI implementation, components, and browser APIs
- **@fullstack-developer**: End-to-end feature development across the stack
- **@typescript-pro**: TypeScript type system, generics, and advanced patterns
- **@java-architect**: Java architecture, JVM tuning, and enterprise patterns
- **@kotlin-specialist**: Kotlin idioms, coroutines, and multiplatform development
- **@react-specialist**: React hooks, state management, and component design
- **@spring-boot-engineer**: Spring Boot auto-config, DI, and reactive stack
- **@database-administrator**: Database provisioning, replication, and backup strategy
- **@docker-expert**: Docker optimization, multi-stage builds, and security
- **@platform-engineer**: Internal developer platforms and self-service tooling
- **@sre-engineer**: Site reliability, monitoring, and incident response
- **@code-reviewer**: Code review with security and performance focus
- **@data-analyst**: Data exploration, visualization, and statistical analysis
- **@database-optimizer**: Query optimization, indexing, and schema design
- **@postgres-pro**: PostgreSQL tuning, extensions, and advanced SQL
- **@sql-pro**: Advanced SQL queries, window functions, and optimization
- **@build-engineer**: Build system configuration, caching, and optimization
- **@dependency-manager**: Dependency updates, audit, and compatibility checks
- **@docs-writer**: Technical documentation and API reference writing
- **@git-workflow-manager**: Git workflow, branching strategy, and commit hygiene
- **@test-writer**: Test generation following project patterns
- **@technical-writer**: User guides, tutorials, and knowledge base articles
- **@api-designer**: REST/GraphQL API design and contract definition

## Available Skills

The following skills are installed and will be loaded on demand:

- **git-release**: Release notes and version bumps
- **test-patterns**: Test generation following project conventions
- **dependency-audit**: Audit dependencies for vulnerabilities and license issues
- **changelog-generate**: Changelog generation from commit history
- **ci-pipeline**: CI pipeline configuration and optimization

## Conventions

- Use conventional commits: `feat:`, `fix:`, `chore:`, `docs:`, `refactor:`, `test:`
- Write meaningful commit messages that explain the "why"
- Keep PRs focused on a single concern

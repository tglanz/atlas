# Single entry point for the common tasks. Each target only calls the project's own tools.
# Run `make help` to list them.

JAR := build/libs/atlas_backend.jar

.DEFAULT_GOAL := help
.PHONY: help run build test format

help: ## List the available commands
	@grep -E '^[a-z-]+:.*## ' $(MAKEFILE_LIST) | awk -F':.*## ' '{printf "  make %-10s %s\n", $$1, $$2}'

run: build ## Build and run the app: one process, the backend serving the UI
	cd atlas_backend && java -jar $(JAR)

build: ## Build the single runnable jar, with the UI inside
	cd atlas_backend && ./gradlew bootJar

test: ## Run the backend and UI tests
	cd atlas_backend && ./gradlew test
	cd atlas_ui && pnpm install --frozen-lockfile && pnpm test

format: ## Format the backend and UI code
	cd atlas_backend && ./gradlew spotlessApply
	cd atlas_ui && pnpm install --frozen-lockfile && pnpm format

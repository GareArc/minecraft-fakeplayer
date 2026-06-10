# fakeplayer – developer Makefile
# Run `make help` to see all targets.

GRADLEW   := ./gradlew
JAR_DIR   := build/libs
VERSION   := $(shell grep '^revision' gradle.properties | cut -d= -f2)
DIST_JAR  := $(JAR_DIR)/fakeplayer-$(VERSION).jar

# Optional: set SERVER_DIR to auto-deploy after build
# e.g.  make deploy SERVER_DIR=~/servers/test/plugins
SERVER_DIR ?=

.DEFAULT_GOAL := help

# ── Build ──────────────────────────────────────────────────────────────────────

.PHONY: build
build:  ## Build the distribution fat JAR (output: build/libs/fakeplayer-<version>.jar)
	$(GRADLEW) :fakeplayer-dist:shadowJar

.PHONY: build-fast
build-fast:  ## Build skipping tests (same as build – no tests exist yet)
	$(GRADLEW) :fakeplayer-dist:shadowJar --parallel

.PHONY: clean
clean:  ## Delete all build outputs
	$(GRADLEW) clean

.PHONY: rebuild
rebuild: clean build  ## Clean then build

# ── Deployment ────────────────────────────────────────────────────────────────

.PHONY: deploy
deploy: build  ## Copy the JAR to $$SERVER_DIR (requires SERVER_DIR to be set)
	@if [ -z "$(SERVER_DIR)" ]; then \
	  echo "ERROR: set SERVER_DIR to your server's plugins directory."; \
	  echo "       e.g.  make deploy SERVER_DIR=~/servers/test/plugins"; \
	  exit 1; \
	fi
	@mkdir -p "$(SERVER_DIR)"
	cp "$(DIST_JAR)" "$(SERVER_DIR)/"
	@echo "Deployed $(DIST_JAR) → $(SERVER_DIR)/"

# ── Versioning ────────────────────────────────────────────────────────────────

.PHONY: version
version:  ## Print the current plugin version
	@echo $(VERSION)

.PHONY: bump-patch
bump-patch:  ## Bump the patch version (0.3.20 → 0.3.21)
	@scripts/bump-version.sh patch

.PHONY: bump-minor
bump-minor:  ## Bump the minor version (0.3.20 → 0.4.0)
	@scripts/bump-version.sh minor

.PHONY: bump-major
bump-major:  ## Bump the major version (0.3.20 → 1.0.0)
	@scripts/bump-version.sh major

# ── Module inspection ─────────────────────────────────────────────────────────

.PHONY: modules
modules:  ## List all Gradle sub-projects
	$(GRADLEW) projects --quiet

.PHONY: deps
deps:  ## Print dependency tree for the dist module
	$(GRADLEW) :fakeplayer-dist:dependencies --configuration runtimeClasspath

# ── Checks ────────────────────────────────────────────────────────────────────

.PHONY: check
check:  ## Run all verification tasks
	$(GRADLEW) check

.PHONY: verify-env
verify-env:  ## Verify the dev environment (Java version, lib/ JARs, etc.)
	@scripts/setup.sh --check

# ── Setup ─────────────────────────────────────────────────────────────────────

.PHONY: setup
setup:  ## First-time dev environment setup (installs git hooks, checks prerequisites)
	@scripts/setup.sh

# ── Help ──────────────────────────────────────────────────────────────────────

.PHONY: help
help:  ## Show this help message
	@echo "fakeplayer $(VERSION) – available targets:"
	@echo ""
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) \
	  | awk 'BEGIN {FS = ":.*?## "}; {printf "  \033[36m%-18s\033[0m %s\n", $$1, $$2}'
	@echo ""
	@echo "  Variables: SERVER_DIR (deploy target, e.g. ~/servers/test/plugins)"

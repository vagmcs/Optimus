SHELL:=/usr/bin/env bash -euo pipefail -c
.DEFAULT_GOAL := help

CURRENT_DIR:=$(shell dirname $(realpath $(lastword $(MAKEFILE_LIST))))
PROJECT_VERSION=$(shell grep "^ThisBuild / version[ ]*:=[ ]*" version.sbt | sed -e 's/ThisBuild \/ version[ ]*:\=[ ]*\"\(.*\)\"/\1/g')

###  help                 : Prints usage information
.PHONY: help
help:
	@echo "Makefile usage:"
	@echo ""
	@sed -n 's/^###//p' < $(CURRENT_DIR)/Makefile | sort

###  format               : Format code
.PHONY: format
format:
	@sbt scalafmt

###  test                 : Test project
.PHONY: compile
compile:
	@sbt clean +compile

###  test                 : Test project
.PHONY: test
test:
	@sbt +core/test +solver-oj/test

###  build                : Clean, build and test project
.PHONY: build
build: compile test

###  release              : Creates a release
.PHONY: release
release: build
	@echo "Releasing version '${PROJECT_VERSION}'"
	@git tag -a v"${PROJECT_VERSION}" -m "version ${PROJECT_VERSION}"
	@sbt +package
	@sbt +publishSigned
	@sbt sonatypeReleaseAll
	@cz changelog --file-name "docs/release_notes/${PROJECT_VERSION}.md" v${PROJECT_VERSION}
	@cat "docs/release_notes/${PROJECT_VERSION}.md" | tail -n +3 > "docs/release_notes/${PROJECT_VERSION}.md"
	@gh release create v"${PROJECT_VERSION}" -F "docs/release_notes/${PROJECT_VERSION}.md" \
		./core/target/scala-2.12/optimus_2.12-${PROJECT_VERSION}.jar \
		./core/target/scala-2.13/optimus_2.13-${PROJECT_VERSION}.jar \
		./core/target/scala-3.1.3/optimus_3-${PROJECT_VERSION}.jar \
		./solver-oj/target/scala-2.12/optimus-solver-oj_2.12-${PROJECT_VERSION}.jar \
		./solver-oj/target/scala-2.13/optimus-solver-oj_2.13-${PROJECT_VERSION}.jar \
		./solver-oj/target/scala-3.1.3/optimus-solver-oj_3-${PROJECT_VERSION}.jar \
		./solver-lp/target/scala-2.12/optimus-solver-lp_2.12-${PROJECT_VERSION}.jar \
		./solver-lp/target/scala-2.13/optimus-solver-lp_2.13-${PROJECT_VERSION}.jar \
		./solver-lp/target/scala-3.1.3/optimus-solver-lp_3-${PROJECT_VERSION}.jar \
		./solver-gurobi/target/scala-2.12/optimus-solver-gurobi_2.12-${PROJECT_VERSION}.jar \
		./solver-gurobi/target/scala-2.13/optimus-solver-gurobi_2.13-${PROJECT_VERSION}.jar \
		./solver-gurobi/target/scala-3.1.3/optimus-solver-gurobi_3-${PROJECT_VERSION}.jar \
		./solver-mosek/target/scala-2.12/optimus-solver-mosek_2.12-${PROJECT_VERSION}.jar \
		./solver-mosek/target/scala-2.13/optimus-solver-mosek_2.13-${PROJECT_VERSION}.jar \
		./solver-mosek/target/scala-3.1.3/optimus-solver-mosek_3-${PROJECT_VERSION}.jar
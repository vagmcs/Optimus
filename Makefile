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
	@sbt +publishSigned
	@sbt sonatypeReleaseAll
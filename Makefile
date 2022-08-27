SHELL:=/usr/bin/env bash -euo pipefail -c
.DEFAULT_GOAL := help

CURRENT_DIR:=$(shell dirname $(realpath $(lastword $(MAKEFILE_LIST))))

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
.PHONY: test
test:
	@sbt test

###  build                : Clean, build and test project
.PHONY: build
build:
	@sbt rebuild

###  release              : Creates a release
.PHONY: release
deploy: build
	@sbt +publishSigned
	@sbt sonatypeReleaseAll
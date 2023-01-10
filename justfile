#!/usr/bin/env just --justfile

watch-test:
    npx shadow-cljs watch test

bundle:
    npx shadow-cljs release app
    ./export.sh
    ./publish.sh

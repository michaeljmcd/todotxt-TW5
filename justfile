#!/usr/bin/env just --justfile

# Launches a Node-backed REPL in the project directory.
repl:
    clj -M --main cljs.main --repl -re node

# Runs and re-runs tests continually.
watch-test:
    npx shadow-cljs watch test

# Does a release build on ClojureScript code.
compile-clj:
    npx shadow-cljs release app

# Builds a standalone wiki with the todotxt plugin installed.
bundle: compile-clj export edition

# Collects code and tiddlers and copies them to a shared location.
export:
  #!/usr/bin/env sh

  TARGETDIR="target/todotxt"

  if [ ! -e ${TARGETDIR} ]; then
      mkdir -p ${TARGETDIR}
  fi

  rm -rf ${TARGETDIR}/*

  cp tiddlers/*.tid tiddlers/*.info tiddlers/*.js ${TARGETDIR}
  mkdir ${TARGETDIR}/files
  cp -r public/js/*.js ${TARGETDIR}/files
  cp tiddlers/public.files ${TARGETDIR}/files/tiddlywiki.files

# Builds todo.txt edition after all files have been copied to the correct
# location.
edition:
  #!/bin/sh

  EDITION="todotxt"

  pushd ../../..
  node ./tiddlywiki.js editions/${EDITION} --build index
  popd

# Deletes generated files and directories.
clean:
  #!/bin/sh

  rm -rf out target public

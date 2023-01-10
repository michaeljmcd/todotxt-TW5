#!/usr/bin/env just --justfile

repl:
    clj -M --main cljs.main --repl -re node

watch-test:
    npx shadow-cljs watch test

compile-clj:
    npx shadow-cljs release app

bundle: compile-clj export edition

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

edition:
  #!/bin/sh

  EDITION="todotxt"

  pushd ../../..
  node ./tiddlywiki.js editions/${EDITION} --build index
  popd

clean:
  #!/bin/sh

  rm -rf out target public

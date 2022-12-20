#!/bin/sh

#EDITION="empty"
EDITION="todotxt"

pushd ../../..
node ./tiddlywiki.js editions/${EDITION} --build index
popd

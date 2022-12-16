#!/bin/sh
#
pushd ../../..
node ./tiddlywiki.js editions/empty --build index
popd

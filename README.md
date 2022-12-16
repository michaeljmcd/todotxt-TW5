# todotxt-TW5
A todo.txt plugin for TiddlyWiki 5

## Development

The plugin itself is written in [ClojureScript](https://clojurescript.org).

To build:

    # In plugins/michaeljmcd/todotxt-dev
    yarn install
    # clj -M -m cljs.main -co build.edn -O advanced --compile
    npx shadow-cljs release app
    ./export.sh

    # At TW5 top level:
    node ./tiddlywiki.js editions/empty --build index

The shell script at the end picks apart the directory and makes a fair copy
before running.

To run tests, first compile the tests down to node and run them.

    npx shadow-cljs compile test && node out/node-tests.js 

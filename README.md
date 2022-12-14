# todotxt-TW5
A todo.txt plugin for TiddlyWiki 5

## Development

The plugin itself is written in [ClojureScript](https://clojurescript.org).

To build:

    yarn install
    clj -M -m cljs.main -co build.edn -O advanced --compile

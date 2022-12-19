# todotxt-TW5

A todo.txt plugin for TiddlyWiki 5. This adds a new `text/x-todo` file type to
the wiki that follows the [TODO.txt](https://github.com/todotxt/todo.txt)
format.

## Development

The plugin itself is written in [ClojureScript](https://clojurescript.org).

There are two pieces, the plugin itself and an edition with the plugin defined
for demo and install purposes. The easiest way is probably to build the plugin
(instructions below) and link the plugin and edition to a TW5 build.

Assume for the sake of argument that you have something like this:

    $ ls src
     TiddlyWiki5 todotxt-TW5

Where the first is [TiddlyWiki5](https://github.com/Jermolene/TiddlyWiki5) and
the second is a clone of this repository.

To build:

    # In todotxt-TW5
    yarn install
    npx shadow-cljs release app && ./export.sh

    # At TW5 top level:
    node ./tiddlywiki.js editions/empty --build index
    # or ./publish.sh

The shell script at the end picks apart the directory and makes a fair copy
before running.

From there, soft link:

    $ cd TiddlyWiki5/editions
    $ ln -s ~/src/todotxt-TW5/edition todotxt
    $ cd ..
    $ mkdir plugins/michaeljmcd
    $ cd plugins/michaeljmcd
    $ ln -s ~/src/todotxt-TW5/target todotxt

And build the wiki edition

    $ cd ../..
    $ node ./tiddlywiki.js editions/todotxt --build index

To run tests, first compile the tests down to node and run them.

    npx shadow-cljs compile test && node out/node-tests.js 

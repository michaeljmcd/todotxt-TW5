/*\
title: $:/plugins/michaeljmcd/todotxt/todoparser.js
type: application/javascript
module-type: parser

Registers todo.core as a parser for tiddlywiki.

\*/

(function() {
  "use strict";
  $tw.modules.execute('$:/plugins/michaeljmcd/todotxt/app.js');
  var opts = JSON.parse($tw.wiki.getTiddlerText("$:/plugins/michaeljmcd/todotxt/config"));

  var TodoParser = function(type, text, options) {
    this.tree = todo.core.todo_to_wiki(text, opts);
  };

  exports["text/x-todo"] = TodoParser;
})();


/*\
title: $:/plugins/michaeljmcd/todotxt/todoparser.js
type: application/javascript
module-type: parser

Load the library

\*/

(function() {
  var TodoParser = function(type, text, options) {
    this.tree = todo.core.todo_to_wiki(text);
  };

  exports["text/x-todo"] = TodoParser;
})();


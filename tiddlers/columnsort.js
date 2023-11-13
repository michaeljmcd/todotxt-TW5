/*\
title: $:/plugins/michaeljmcd/todotxt/columnsort.js
type: application/javascript
module-type: widget

Table header sort widget

\*/
(function(){

/*jslint node: true, browser: true */
/*global $tw: false */
"use strict";

var Widget = require("$:/core/modules/widgets/widget.js").widget;

var ColumnSortWidget = function(parseTreeNode,options) {
	this.initialise(parseTreeNode,options);
};


ColumnSortWidget.prototype = new Widget();

ColumnSortWidget.prototype.render = function(parent,nextSibling) {
	this.parentDomNode = parent;
	// Compute our attributes
	this.computeAttributes();
	// Execute our logic
	this.execute();

    var state = JSON.parse($tw.wiki.getTiddlerText("$:/plugins/michaeljmcd/todotxt/state"))[this.todoTiddler];

    if (state === null || state === undefined) {
        state = {'sort': {}};
    }

	this.inputNode = this.document.createElement("a");
	this.inputNode.setAttribute("class", "todo-columnsort");

    var linkText = this.columnDescription;
    if (state['sort'][this.columnName] === "asc") {
        linkText += "\u25B2";
    }
    if (state['sort'][this.columnName] === "desc") {
        linkText += "\u25BC";
    }

    this.textNode = this.document.createTextNode(linkText);
    this.inputNode.appendChild(this.textNode);

	$tw.utils.addEventListeners(this.inputNode,[
		{name: "click", handlerObject: this, handlerMethod: "handleChangeEvent"}
	]);

	parent.insertBefore(this.inputNode,nextSibling);
};

ColumnSortWidget.prototype.execute = function() {
    this.columnName = this.getAttribute("column-name");
    this.columnDescription = this.getAttribute("column-description");
	this.todoTiddler = this.getVariable("currentTiddler");

    var draftTitle = $tw.wiki.getTiddler(this.todoTiddler).fields["draft.of"];

    if (draftTitle !== null && draftTitle !== undefined) {
        this.todoTiddler = draftTitle;
    }

	// Make the child widgets
	this.makeChildWidgets();
};

ColumnSortWidget.prototype.handleChangeEvent = function(e) {
    var state = JSON.parse($tw.wiki.getTiddlerText("$:/plugins/michaeljmcd/todotxt/state"));

    if (state[this.todoTiddler] === null || state[this.todoTiddler] === undefined) {
        state[this.todoTiddler] = {};
    }

    var newSort = {};
    newSort[this.columnName] = "asc";

    if (state[this.todoTiddler] && state[this.todoTiddler]['sort']) {
        if (state[this.todoTiddler]['sort'][this.columnName] === "asc") {
            newSort[this.columnName] = "desc";
        } else if (state[this.todoTiddler]['sort'][this.columnName] === "desc") {
            newSort[this.columnName] = "asc";
        } 

        state[this.todoTiddler]['sort'] = newSort;
    } else {
        state[this.todoTiddler] = {};
        state[this.todoTiddler]['sort'] = newSort;
    }

	var tiddler = this.wiki.getTiddler(this.todoTiddler);

    if (tiddler) {
		var parseResult = todo.core.parse_todos(tiddler.fields.text);
		var todos = parseResult.result; 

        todos = todo.core.sort_todos(todos, state[this.todoTiddler]['sort'])

        $tw.wiki.setText("$:/plugins/michaeljmcd/todotxt/state", "text", null, JSON.stringify(state));
        $tw.wiki.setText(this.todoTiddler, "text", null, todo.core.todo_to_text(todos));
        $tw.rootWidget.dispatchEvent({type: "tm-auto-save-wiki"});
    }
};

exports['todo-columnsort'] = ColumnSortWidget;

})();

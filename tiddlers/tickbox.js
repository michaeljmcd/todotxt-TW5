/*\
title: $:/plugins/michaeljmcd/todotxt/tickbox.js
type: application/javascript
module-type: widget

Checkbox widget

\*/
(function(){

/*jslint node: true, browser: true */
/*global $tw: false */
"use strict";

var Widget = require("$:/core/modules/widgets/widget.js").widget;

var TickboxWidget = function(parseTreeNode,options) {
	this.initialise(parseTreeNode,options);
};


TickboxWidget.prototype = new Widget();

TickboxWidget.prototype.render = function(parent,nextSibling) {
	this.parentDomNode = parent;
	// Compute our attributes
	this.computeAttributes();
	// Execute our logic
	this.execute();

	this.inputNode = this.document.createElement("input");
	this.inputNode.setAttribute("type", "checkbox");
	this.inputNode.setAttribute("class", "todo-checkbox");

	if (this.isChecked === "true") {
		this.inputNode.setAttribute("checked", "");
	}

	$tw.utils.addEventListeners(this.inputNode,[
		{name: "change", handlerObject: this, handlerMethod: "handleChangeEvent"}
	]);

	parent.insertBefore(this.inputNode,nextSibling);
};

TickboxWidget.prototype.handleChangeEvent = function(e) {
	//alert("Status change requested for " + this.todoTiddler + ":" + this.lineNumber);
	var tiddler = this.wiki.getTiddler(this.todoTiddler);

	if (tiddler) { 
		var parseResult = todo.core.parse_todos(tiddler.fields.text);
		//TODO: validate
		var todos = parseResult.result; 
		var index = parseInt(this.lineNumber);

		if (todos[index].complete === true) {
			todos[index].complete = false;
			delete todos[index]["completion-date"];
		} else {
			todos[index].complete = true;

			if ('creation-date' in todos[index]) {
				todos[index]["completion-date"] = todo.core.current_date();
			}
		}

		$tw.wiki.setText(this.todoTiddler, "text", null, todo.core.todo_to_text(todos));
	}
};

TickboxWidget.prototype.execute = function() {
	this.isChecked = this.getAttribute("checked");
	this.todoTiddler = this.getAttribute("todo-tiddler", this.getVariable("currentTiddler"));
	this.lineNumber = this.getAttribute("line-number");

	// Make the child widgets
	this.makeChildWidgets();
};


exports['todo-tickbox'] = TickboxWidget;

})();

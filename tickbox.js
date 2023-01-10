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

	if (this.isChecked === "true") {
		this.inputNode.setAttribute("checked", "");
	}

	$tw.utils.addEventListeners(this.inputNode,[
		{name: "change", handlerObject: this, handlerMethod: "handleChangeEvent"}
	]);

	parent.insertBefore(this.inputNode,nextSibling);
};

TickboxWidget.prototype.handleChangeEvent = function(e) {
	alert("Status change requested for " + this.todoTiddler + ":" + this.lineNumber);
};

TickboxWidget.prototype.execute = function() {
	this.isChecked = this.getAttribute("checked");
	this.todoTiddler = this.getAttribute("todo-tiddler", this.getVariable("currentTiddler"));
	this.lineNumber = this.getAttribute("line-number");

	// Make the child widgets
	this.makeChildWidgets();
};


exports.tickbox = TickboxWidget;

})();

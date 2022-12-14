/*\
title: $:/plugins/michaeljmcd/todotxt/plugin.js
type: application/javascript
module-type: startup

Load the library

\*/

exports.startup = function() {
  $tw.modules.execute('$:/plugins/michaeljmcd/todotxt/app.js');
}

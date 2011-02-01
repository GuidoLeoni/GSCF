<%
/**
 * Setup / Migrate assistant
 *
 * @author Jeroen Wesbeek
 * @since 20101111
 * @package setup
 *
 * Revision information:
 * $Rev$
 * $Author$
 * $Date$
 */
%>
<html>
<head>
	<meta name="layout" content="main"/>
	<link rel="stylesheet" href="${resource(dir: 'css', file: 'wizard.css')}"/>
	<script type="text/javascript" src="${resource(dir: 'js', file: 'jquery.qtip-1.0.0-rc3.js', plugin: 'gdt')}"></script>
	<script type="text/javascript" src="${resource(dir: 'js', file: 'jquery.ui.autocomplete.html.js', plugin: 'gdt')}"></script>
	<script type="text/javascript" src="${resource(dir: 'js', file: 'table-editor.js', plugin: 'gdt')}"></script>
	<script type="text/javascript" src="${resource(dir: 'js', file: 'ontology-chooser.js', plugin: 'gdt')}"></script>
	<script type="text/javascript" src="${resource(dir: 'js', file: 'SelectAddMore.js', plugin: 'gdt')}"></script>
	<script type="text/javascript" src="${resource(dir: 'js', file: 'wizard.js')}"></script>
</head>
<body>
<g:render template="common/wizard"/>
</body>
</html>
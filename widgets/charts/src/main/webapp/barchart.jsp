<!DOCTYPE html>
<html>
<head>
    <%
        String neonServerUrl = getServletContext().getInitParameter("neon.url");
        String owfServerUrl = getServletContext().getInitParameter("owf.url");
    %>

    <title>Timeline widget</title>

    <link rel="stylesheet" type="text/css" href="css/bootstrap/bootstrap.min.css">
    <link rel="stylesheet" type="text/css" href="css/widgetbase.css">
    <link rel="stylesheet" type="text/css" href="css/barchart.css">
    <link rel="stylesheet" type="text/css" href="<%=neonServerUrl%>/css/neon.css">
    <script src="<%=owfServerUrl%>/js/owf-widget.js"></script>
    <script src="<%=neonServerUrl%>/js/neon.js"></script>

    <!-- build:js js/charts.js -->
    <script src="js-lib/d3/d3.v3.min.js"></script>
    <script src="js-lib/jquery/jquery-1.10.1.min.js"></script>
    <script src="js-lib/jqueryui/jquery-ui-1.10.3.custom.min.js"></script>
    <script src="js-lib/lodash/1.3.1/lodash.min.js"></script>
    <script src="js/toggle.js"></script>
    <script src="javascript/namespaces.js"></script>
    <script src="javascript/barchart.js"></script>
    <!-- endbuild -->

    <!-- build:js js/chartwidget.js -->
    <script src="javascript/chartwidget.js"></script>
    <!-- endbuild -->

    <!-- build:js js/barchartwidget.js -->
    <script src="javascript/barchartwidget.js"></script>
    <!-- endbuild -->


    <script>
        OWF.relayFile = 'js/eventing/rpc_relay.uncompressed.html';
        neon.query.SERVER_URL = '<%=neonServerUrl%>';
        neon.util.AjaxUtils.useDefaultStartStopCallbacks();
    </script>

</head>
<body>

<div class="options-bar">
    <div class="toggle">
        <img class="toggle-image"/>
        <label class=options-label>Options</label>
    </div>
    <div class="options">
        <div class="controls-row">
            <div class="control-group">
                <label class="control-label" for="x">x-axis</label>

                <div class="controls">
                    <select id="x" class="dropdown"></select>
                </div>
            </div>

            <div class="control-group">
                <label class="control-label" for="y">y-axis</label>

                <div class="controls">
                    <select id="y" class="dropdown"></select>
                </div>
            </div>
        </div>
    </div>
</div>

<div id="chart" class="chart-div"></div>

</body>
</html>

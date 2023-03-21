<%@ page import="com.sun.org.apache.xpath.internal.operations.Bool" %>
<%@ page contentType="text/html;charset=UTF-8" %>
<html>


<head>
    <title>Root Home</title>
    <link href="${pageContext.request.contextPath}/css/style.css" rel="stylesheet" type="text/css">
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Raleway:wght@100;200;300;400;500;600;700&display=swap" rel="stylesheet">
</head>
<body>

    <h1 class="header">Welcome to the Summer 2022 Project 3 Enterprise Database System</h1>
    <h2 class="sub-header">A Servlet/JSP-based Multi-tiered Enterprise Application Using a Tomcat Container</h2>
    <hr class="line-break center-text"/>

    <p id="user-type-p">You are connected to the Project 3 Enterprise Systems database as a <mark class="user-type">root-level</mark> user.</p>
    <p>Please enter any valid SQL query or update command in the box below</p>

    <form method="post" id="SQLForm" action="RootUserApp">
        <div id="command-div">
            <label for="command-text-area"></label>
            <textarea id="command-text-area" name="command" placeholder="Enter Command..."></textarea>
        </div>

        <div class="buttons">
            <button type="submit" id="submitBtn">Execute Command</button>
            <button type="reset" onclick="eraseText();">Reset Form</button>
            <button type="button" onclick="eraseData();">Clear Results</button>
        </div>
    </form>

    <p>All execution results will appear below this line.</p>
    <hr class="line-break center-text"/>
    <h3 class="center-text">Database Results:</h3>

    <div id="databaseResults-div">
        <%
            String resultString = (String) request.getAttribute("result");
        %>

        <%=resultString%>
    </div>

    <br><br>

    <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.6.0/jquery.min.js"></script>
    <script type="text/javascript">
        function eraseText() {
            $("#command-text-area").html("");
        }
    </script>

    <script type="text/javascript">
        function eraseData() {
            $("#databaseResults").remove();
        }
    </script>

    <script>
        $("#SQLForm").submit(function(e) {

            e.preventDefault();

            let form = $(this);
            let actionUrl = form.attr('action');
            let dataString = form.serialize();

            $.ajax({
                type: "POST",
                url: actionUrl,
                data: dataString,
                success: function() {
                    $('#databaseResults-div').load(location.href + ' #databaseResults-div')
                    console.log(dataString)
                }
            });
        });
    </script>

</body>
</html>

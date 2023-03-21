<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Data Entry Home</title>
    <link href="${pageContext.request.contextPath}/css/dataEntry.css" rel="stylesheet" type="text/css">
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Raleway:wght@100;200;300;400;500;600;700&display=swap" rel="stylesheet">
</head>
<body>
    <h1 class="header">Welcome to the Summer 2022 Project 3 Enterprise Database System</h1>
    <h2 class="sub-header">A Servlet/JSP-based Multi-tiered Enterprise Application Using a Tomcat Container</h2>
    <hr class="line-break center-text"/>

    <p id="user-type-p">You are connected to the Project 3 Enterprise Systems database as a <mark class="user-type">data-entry-level</mark> user.</p>
    <p>Please enter the data values in the form below to add a new record to the shipments table.</p>

    <form method="post" id="dataEntryForm" action="DataEntryApp">
        <br>
        <table style="color: red" id="dataEntry-table">
            <tr id="dataEntry-names">
                <th>snum</th>
                <th>pnum</th>
                <th>jnum</th>
                <th>quantity</th>
            </tr>
            <tr id="dataEntry-inputs">
                <td><label for="snum">
                    <input type="text" placeholder="snum" id="snum" name="snum">
                </label></td>
                <td><label for="pnum">
                    <input type="text" placeholder="pnum" id="pnum" name="pnum">
                </label></td>
                <td><label for="jnum">
                    <input type="text" placeholder="jnum" id="jnum" name="jnum">
                </label></td>
                <td><label for="quantity">
                    <input type="text" placeholder="quantity" id="quantity" name="quantity">
                </label></td>
            </tr>
        </table>
        <br>
        <div class="buttons">
            <button type="submit" id="enterRecord">Enter Record Into Database</button>
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

<%-- Scripts --%>
    <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.6.0/jquery.min.js"></script>
    <script type="text/javascript">
        function eraseData() {
            $("#databaseResults").remove();
        }
    </script>

    <script>
        $("#dataEntryForm").submit(function(e) {

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

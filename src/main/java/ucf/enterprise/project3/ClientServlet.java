package ucf.enterprise.project3;

import ucf.enterprise.msql.SQLHandler;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.sql.DataSource;
import java.io.IOException;
import java.sql.SQLException;

public class ClientServlet extends HttpServlet {
    String commandString = "";
    String resultString = "";
    SQLHandler sqlHandler = new SQLHandler();


    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        System.out.println("\nIn doGet() for Client");
        DataSource dataSource = sqlHandler.getUserDataSource("client");

        try {
            sqlHandler.setConnection(dataSource.getConnection());
            sqlHandler.resultString(commandString);
            request.setAttribute("errorString", sqlHandler.getErrorString());

            if(!sqlHandler.getValidQuery()) {
                resultString = sqlHandler.getErrorString();
            } else {
                resultString = sqlHandler.buildTable();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        request.setAttribute("result", resultString);
        request.getRequestDispatcher("/clientHome.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        commandString = request.getParameter("command");

        request.setAttribute("command", commandString);
        request.getRequestDispatcher("/clientHome.jsp").forward(request, response);
    }
}

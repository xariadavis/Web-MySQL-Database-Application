/* Name: Xaria Davis
Course: CNT 4714 – Summer 2022 – Project Three
Assignment title: A Three-Tier Distributed Web-Based Application
Date: August 4, 2022
*/

package ucf.enterprise.project3;

import ucf.enterprise.msql.SQLHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.sql.SQLException;

public class RootServlet extends HttpServlet {
    String commandString = "";
    String resultString = "";
    SQLHandler sqlHandler = new SQLHandler();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        System.out.println("\nIn doGet() for root.");
        DataSource dataSource = sqlHandler.getUserDataSource("root");

        try {
            sqlHandler.setConnection(dataSource.getConnection());
            sqlHandler.resultString(commandString);
            request.setAttribute("errorString", sqlHandler.getErrorString());

            if(!sqlHandler.getValidQuery()) {
                resultString = sqlHandler.getErrorString();
            } else if(sqlHandler.getBusinessLogicTriggered()) {
                resultString = sqlHandler.getBusinessLogic();
            } else {
                resultString = sqlHandler.buildTable();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        request.setAttribute("result", resultString);
        request.getRequestDispatcher("/rootHome.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        commandString = request.getParameter("command");

        request.setAttribute("command", commandString);
        request.getRequestDispatcher("/rootHome.jsp").forward(request, response);
    }
}

/* Name: Xaria Davis
Course: CNT 4714 – Summer 2022 – Project Three
Assignment title: A Three-Tier Distributed Web-Based Application
Date: August 4, 2022
*/

package ucf.enterprise.project3;

import ucf.enterprise.msql.SQLHandler;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.sql.DataSource;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

public class DataEntryServlet extends HttpServlet {
    String resultString = "";
    SQLHandler sqlHandler = new SQLHandler();
    String snum, pnum, jnum, quantity;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        System.out.println("In doGet() for data entry.");
        DataSource dataSource = sqlHandler.getUserDataSource("root");
        String query = " insert into shipments (snum, pnum, jnum, quantity)"
                + " values (?, ?, ?, ?)";

        try {

            sqlHandler.setConnection(dataSource.getConnection());
            PreparedStatement preparedStmt = sqlHandler.getConnection().prepareStatement(query);
            preparedStmt.setString (1, snum);
            preparedStmt.setString (2, pnum);
            preparedStmt.setString (3, jnum);
            preparedStmt.setString(4, quantity);

            System.out.println("snum is: " + snum);
            System.out.println("Pnum is: " + pnum);
            System.out.println("Jnum is: " + jnum);
            System.out.println("QUANTITY is: " + quantity);
            preparedStmt.execute();

            if(Integer.parseInt(quantity) >= 100) {
                resultString = "<p style=\\\"outline: #0B5351 dashed 2px; width: 50%; padding: 15px; margin-right: auto; margin-left: auto;\n\">New shipment record successfully entered in the database. Business logic triggered.</p>";
                // Execute part 1 of bonus business logic
                Statement statement = sqlHandler.getConnection().createStatement();
                statement.addBatch("drop table if exists beforeShipments;");
                statement.addBatch("create table beforeShipments like shipments;");
                statement.addBatch("insert into beforeShipments select * from shipments;");
                statement.executeBatch();

                // Execute the query
                statement.executeUpdate(query);

                // Execute part 2 of bonus business logic
                statement.addBatch("update suppliers\n" +
                        "set status = status + 5\n" +
                        "where suppliers.snum in\n" +
                        "   (select distinct snum\n" +
                        "       from shipments\n" +
                        "       where shipments.quantity >= 100\n" +
                        "       and\n" +
                        "       not exists (select *\n" +
                        "           from beforeShipments\n" +
                        "           where shipments.snum = beforeShipments.snum\n" +
                        "           and shipments.pnum = beforeShipments.pnum\n" +
                        "           and shipments.jnum = beforeShipments.jnum\n" +
                        "           and beforeShipments.quantity >= 100\n" +
                        "       )\n" +
                        "     );");
                statement.executeBatch();

                // Drop the database
                statement.addBatch("drop table beforeShipments;");
                statement.executeBatch();
            } else {
                resultString = "<p style=\"outline: #0B5351 dashed 2px; width: 50%; padding: 15px; margin-right: auto; margin-left: auto;\n\">New shipment record successfully entered in the database. Business logic not triggered.</p>";
            }

        } catch (SQLException e) {
            System.out.println("Error string: " + e.getMessage());
            resultString = "<p style=\"outline: #0B5351 dashed 2px; width: 50%; padding: 15px; margin-right: auto; margin-left: auto;\n\"><strong>ERROR EXECUTING THE MYSQL STATEMENT:</strong><br>" + e.getMessage() + "</p>";
        }

        request.setAttribute("result", resultString);
        request.getRequestDispatcher("/dataEntryHome.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        snum = request.getParameter("snum");
        pnum = request.getParameter("pnum");
        jnum = request.getParameter("jnum");
        quantity = request.getParameter("quantity");
        request.getRequestDispatcher("/dataEntryHome.jsp").forward(request, response);
    }
}

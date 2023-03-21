/* Name: Xaria Davis
Course: CNT 4714 – Summer 2022 – Project Three
Assignment title: A Three-Tier Distributed Web-Based Application
Date: August 4, 2022
*/

package ucf.enterprise.msql;

import com.google.common.collect.Lists;
import com.mysql.cj.jdbc.MysqlDataSource;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

public class SQLHandler {
    private String url;
    private String username;
    private String password;
    private MysqlDataSource dataSource = null;
    private Connection connection = null;
    private String errorString;
    private int numColumns;
    private int numRows;
    private List<String> columnNames;
    private List<String> cellData;
    private List<List<String>> rowData;
    private int rowsAffectedUpdate;
    private Boolean validQuery;
    private Boolean businessLogicTriggered;
    private String businessLogic;

    private int numRowsQuery;
    private int numRowsBusiness;

    // Return DataSource
    public DataSource getUserDataSource(String userType) {
        InputStream inputStream;
        Properties properties = new Properties();

        // Load the properties file
        try {
            inputStream = getClass().getClassLoader().getResourceAsStream(userType + ".properties");

            properties.load(inputStream);
            setDataSource(new MysqlDataSource());
            getDataSource().setURL(properties.getProperty("MYSQL_DB_URL"));
            getDataSource().setUser(properties.getProperty("MYSQL_DB_USERNAME"));
            getDataSource().setPassword(properties.getProperty("MYSQL_DB_PASSWORD"));
            System.out.println("Connected to Database!");
        } catch (IOException e) {
            e.printStackTrace();
        }

        setUrl(properties.getProperty("MYSQL_DB_URL"));
        setUsername(properties.getProperty("MYSQL_DB_USERNAME"));
        setPassword(properties.getProperty("MYSQL_DB_PASSWORD"));

        return getDataSource();
    }

    public void resultString(String query) {
        ResultSet resultSet = null;
        try {
            Statement statement = getConnection().createStatement();
            if(!query.toLowerCase(Locale.ROOT).contains("select")) {
                setBusinessLogicTriggered(true);

                if(query.toLowerCase(Locale.ROOT).contains("shipment")) {
                    // Execute part 1 of bonus business logic
                    statement.addBatch("drop table if exists beforeShipments;");
                    statement.addBatch("create table beforeShipments like shipments;");
                    statement.addBatch("insert into beforeShipments select * from shipments;");
                    statement.executeBatch();

                    // Execute the query
                    statement.executeUpdate(query);

                    // Get the number of rows affected by the query
                    setNumRowsQuery(getRowsAffected(statement));

                    // Execute part 2 of bonus business logic
                    statement.addBatch("""
                            update suppliers
                            set status = status + 5
                            where suppliers.snum in
                               (select distinct snum
                                   from shipments
                                   where shipments.quantity >= 100
                                   and
                                   not exists (select *
                                       from beforeShipments
                                       where shipments.snum = beforeShipments.snum
                                       and shipments.pnum = beforeShipments.pnum
                                       and shipments.jnum = beforeShipments.jnum
                                       and beforeShipments.quantity >= 100
                                   )
                                 );""");
                    statement.executeBatch();

                    // Get the number of rows affected by that
                    setNumRowsBusiness(getRowsAffected(statement));

                    // Drop the database
                    statement.addBatch("drop table beforeShipments;");
                    statement.executeBatch();

                    setBusinessLogic("<p style=\"outline: #0B5351 dashed 2px; width: 50%; padding: 15px; margin-right: auto; margin-left: auto;\n\">The statement executed successfully. <strong>" + getNumRowsQuery() + "</strong> row(s) affected.<br><br>Business Logic detected! - Updating Supplier Status<br>Business Logic updated <strong>" + getNumRowsBusiness() + "</strong> supplier status marks.</p>");
                } else { // Business logic was not triggered
                    statement.executeUpdate(query);
                    // Get the number of rows affected by the update
                    setNumRowsQuery(getRowsAffected(statement));
                    setBusinessLogic("<p style=\"outline: #0B5351 dashed 2px; width: 50%; padding: 15px; margin-right: auto; margin-left: auto;\n\">The statement executed successfully. A total of <strong>" + getNumRowsQuery() + "</strong> row(s) were updated.<br><br>" + "Business Logic Not Triggered!</p>");
                }

            } else {
                setBusinessLogicTriggered(false);
                resultSet = statement.executeQuery(query);
            }

            ResultSetMetaData metaData;
            if (resultSet != null) {
                metaData = resultSet.getMetaData();
                setNumColumns(metaData.getColumnCount());

                int columnCount = metaData.getColumnCount();
                setNumColumns(columnCount);

                setColumnNames(new ArrayList<>());
                for(int i = 1; i <= columnCount; i++) {
                    getColumnNames().add(metaData.getColumnName(i));
                }

                setCellData(new ArrayList<>());
                while (resultSet.next()) {
                    for(int i = 1; i <= columnCount; i++) {
                        getCellData().add(resultSet.getString(i));
                    }
                }
            }

            setNumRows(getCellData().size() / getNumColumns());
            setRowData(Lists.partition(getCellData(), getNumColumns()));
            buildTable();
            setErrorString(null);
            setValidQuery(true);
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Issue in executeCommand");
            setValidQuery(false);
            setErrorString(e.getMessage());
        }
    }

    private int getRowsAffected(Statement statement) {
        ResultSet rowsAffected;
        int numRows = 0;
        // Get the number of rows affected by the update
        try {
            rowsAffected = statement.executeQuery("SELECT ROW_COUNT();");
            rowsAffected.next();
            numRows =  Integer.parseInt(rowsAffected.getString("ROW_COUNT()"));

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return numRows;
    }

    public String buildTable() {
        StringBuilder tableHeader = new StringBuilder();
        StringBuilder tableRows = new StringBuilder();
        StringBuilder table = new StringBuilder();
        String temp;

        table.append("<table class=\"databaseResults\" id=\"databaseResults\">\n");

        // Get table headers
        tableHeader.append("<tr>\n");
        for(int i = 0; i < getNumColumns(); i++) {
            temp = "<th>" + getColumnNames().get(i) + "</th>\n";
            tableHeader.append(temp);
        }
        tableHeader.append("</tr>\n");
        table.append(tableHeader);

        for(int i = 0; i < getNumRows(); i++) {
            tableRows.append("<tr>\n");
            for(int j = 0; j < getNumColumns(); j++) {
                temp = "<td>" + getRowData().get(i).get(j) + "</td>\n";
                tableRows.append(temp);
            }
            tableRows.append("</tr\n");
        }

        table.append(tableRows);
        table.append("</table>\n");
        return String.valueOf(table);
    }

    // ======= Getters/Setters ======= //

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public MysqlDataSource getDataSource() {
        return dataSource;
    }

    public void setDataSource(MysqlDataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Connection getConnection() {
        return connection;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    public String getErrorString() {
        return "<p style=\"outline: #0B5351 dashed 2px; width: 50%; padding: 15px; margin-right: auto; margin-left: auto;\n\"><strong>ERROR EXECUTING THE MYSQL STATEMENT:</strong><br>" + errorString + "</p>";
    }

    public void setErrorString(String errorString) {
        this.errorString = errorString;
    }

    public int getNumColumns() {
        return numColumns;
    }

    public void setNumColumns(int numColumns) {
        this.numColumns = numColumns;
    }

    public int getNumRows() {
        return numRows;
    }

    public void setNumRows(int numRows) {
        this.numRows = numRows;
    }

    public List<String> getColumnNames() {
        return columnNames;
    }

    public void setColumnNames(List<String> columnNames) {
        this.columnNames = columnNames;
    }

    public List<String> getCellData() {
        return cellData;
    }

    public void setCellData(List<String> cellData) {
        this.cellData = cellData;
    }

    public List<List<String>> getRowData() {
        return rowData;
    }

    public void setRowData(List<List<String>> rowData) {
        this.rowData = rowData;
    }

    public int getRowsAffectedUpdate() {
        return rowsAffectedUpdate;
    }

    public void setRowsAffectedUpdate(int rowsAffectedUpdate) {
        this.rowsAffectedUpdate = rowsAffectedUpdate;
    }

    public Boolean getValidQuery() {
        return validQuery;
    }

    public void setValidQuery(Boolean validQuery) {
        this.validQuery = validQuery;
    }

    public String getBusinessLogic() {
        return businessLogic;
    }

    public void setBusinessLogic(String businessLogic) {
        this.businessLogic = businessLogic;
    }

    public int getNumRowsQuery() {
        return numRowsQuery;
    }

    public void setNumRowsQuery(int numRowsQuery) {
        this.numRowsQuery = numRowsQuery;
    }

    public int getNumRowsBusiness() {
        return numRowsBusiness;
    }

    public void setNumRowsBusiness(int numRowsBusiness) {
        this.numRowsBusiness = numRowsBusiness;
    }

    public Boolean getBusinessLogicTriggered() {
        return businessLogicTriggered;
    }

    public void setBusinessLogicTriggered(Boolean businessLogicTriggered) {
        this.businessLogicTriggered = businessLogicTriggered;
    }
}

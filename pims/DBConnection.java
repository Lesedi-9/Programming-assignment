package pims;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/pims?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
    //private static final String URL = "jdbc:mysql://localhost:3306/pims?zeroDateTimeBehavior=CONVERT_TO_NULL [root on Default schema]";
    private static final String USER = "root";
    private static final String PASSWORD = ""; 

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    //static Connection getConnection() {
    //    throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    //}
}
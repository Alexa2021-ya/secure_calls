package com.example.project;
import android.content.Context;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DBConnector {
    private static final String URL = "jdbc:mysql://" + MainActivity.URL_SERVER + ":3306/opensips";
    private static final String USERNAME = "opensips";
    private static final String PASSWORD = "opensipsrw";
    private static Connection conn;

    public Connection getConnectionDB() {
        try {
            return DriverManager.getConnection(URL, USERNAME, PASSWORD);
        }
        catch (Exception e) {
            return null;
        }
    }

    public void addUser(String username, String password, String domain) throws SQLException {
        conn = getConnectionDB();

        if (conn != null) {
            Statement statement = conn.createStatement();
            String sql = "INSERT INTO subscriber (username, password, domain) VALUES ('" + username + "', '" + password + "', '" + domain + "')";
            System.out.println(sql);
            statement.executeUpdate(sql);
        }
    }

    public void updateUser(String id, String username, String password, String domain) throws SQLException {
        conn = getConnectionDB();

        if (conn != null) {
            Statement statement = conn.createStatement();
            String sql = "UPDATE subscriber SET username = '" + username + "', password = '" + password + "', domain = '" + domain + "' WHERE username = '" + id + "'";
            statement.executeUpdate(sql);
        }
    }

    public void deleteUser(String username) throws SQLException {
        conn = getConnectionDB();

        if (conn != null) {
            Statement statement = conn.createStatement();
            String sql = "DELETE FROM subscriber WHERE username = '" + username + "'";
            statement.executeUpdate(sql);
        }
    }

    public void showUsers(Context context) throws SQLException {
        conn = getConnectionDB();

        if (conn != null) {
            for (int i = MainActivity.contacts.size() - 1; i >= 0; i--) {
                String user = MainActivity.contacts.get(i).getNumber();
                int startIndex = Math.max(user.length() - 10, 0); // Начальный индекс для получения последних 10 символов
                String lastTenChars = user.substring(startIndex);
                String sql = "SELECT * FROM subscriber WHERE username LIKE '%" + lastTenChars + "'";
                Statement statement = conn.prepareStatement(sql);

                ResultSet resultSet = statement.executeQuery(sql);

                if (!resultSet.next()) {
                    MainActivity.contacts.remove(i);
                }
            }

            DatabaseHelper databaseHelper = new DatabaseHelper(context);
            databaseHelper.addContacts(MainActivity.contacts);
        }
    }

    public boolean isUser(String username) throws SQLException {
        conn = getConnectionDB();

        if (conn != null) {
            String sql = "SELECT COUNT(*) FROM subscriber WHERE username = '" + username + "'";
            Statement statement = conn.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);

            int count = 0;
            if (resultSet.next()) {
                count = resultSet.getInt(1);
            }

            if (count > 0) {
                return true;
            }
            return false;
        }
        return false;
    }
}

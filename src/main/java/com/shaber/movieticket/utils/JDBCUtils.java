package com.shaber.movieticket.utils;

import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.Properties;

public class JDBCUtils {
    private static String url = null;
    private static String user = null;
    private static String password = null;
    static {
        InputStream fin = JDBCUtils.class.getClassLoader().getResourceAsStream("db.properties");
        Properties properties = new Properties();
        try {
            properties.load(fin);
            String driver = properties.getProperty("driver");
            url = properties.getProperty("url");
            user = properties.getProperty("username");
            password = properties.getProperty("password");
            Class.forName(driver);
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static Connection getConnection(){
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(url, user, password);
        } catch (SQLException e) {
            System.out.println("数据库连接参数异常，请检查连接地址、用户名、密码");
            throw new RuntimeException(e);
        }
        return conn;
    }

    public static PreparedStatement getPreparedStatement(Connection conn, String sql, Object... params){
        PreparedStatement ps = null;
        try {
            ps = conn.prepareStatement(sql);
            for(int i = 0; i < params.length; i++){
                ps.setObject(i+1,params[i]);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return ps;
    }


    public static void close(Connection conn, Statement stmt, ResultSet rs){
        try {
            if(rs != null){rs.close();}
            if(conn != null){conn.close();}
            if (stmt != null){stmt.close();}
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}

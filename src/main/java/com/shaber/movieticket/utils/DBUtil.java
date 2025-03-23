package com.shaber.movieticket.utils;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Properties;

public class DBUtil {
    // 静态不继承获取db.properties配置文件
    private static final String PROPERTIES_FILE = "db.properties";
    private static Properties properties = new Properties();

    static {
        try (InputStream input = DBUtil.class.getClassLoader().getResourceAsStream(PROPERTIES_FILE)) {
            if (input == null) {
                throw new RuntimeException("Sorry, unable to find " + PROPERTIES_FILE);
            }
            // 将文件名导入properties对象
            properties.load(input);
            // 可选：加载数据库驱动
            String driver = properties.getProperty("driver");
            if (driver != null) {
                Class.forName(driver);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException("Failed to initialize database utilities", ex);
        }
    }

    private DBUtil() {
        // 私有构造方法，防止实例化
    }

    public static Connection getConnection() throws SQLException {
        // 连接数据库
        String url = properties.getProperty("url");
        String user = properties.getProperty("username");
        String password = properties.getProperty("password");
        return DriverManager.getConnection(url, user, password);
    }

    // 可选：提供一个关闭连接的方法（尽管通常建议使用try-with-resources）
    public static void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
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
}


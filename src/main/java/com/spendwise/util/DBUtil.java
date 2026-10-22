package com.spendwise.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * The only class that knows how to open a database connection.
 * Settings come from db.properties, never from source code. Lookup order:
 *   1. file path in JVM option  -Dspendwise.config=C:/path/db.properties
 *   2. file path in environment variable SPENDWISE_CONFIG
 *   3. db.properties on the classpath (src/main/resources/db.properties)
 *
 * MVP note: one connection per call, no pool. DAOs close it with try-with-resources.
 */
public final class DBUtil {

    private static Properties config; // cached after the first successful load

    private DBUtil() { }

    public static Connection getConnection() throws SQLException {
        Properties p = loadConfig();
        try {
            Class.forName("oracle.jdbc.OracleDriver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("Oracle JDBC driver (ojdbc11) is not deployed. "
                    + "Run Maven > Update Project and re-publish the server.", e);
        }
        DriverManager.setLoginTimeout(10);
        return DriverManager.getConnection(buildUrl(p), required(p, "db.username"), required(p, "db.password"));
    }

    private static String buildUrl(Properties p) {
        String url = p.getProperty("db.url", "").trim();
        if (!url.isEmpty()) {
            return url;
        }
        String host = required(p, "db.host");
        String port = required(p, "db.port");
        String service = required(p, "db.service");
        if (!port.matches("\\d{2,5}")) {
            throw new IllegalStateException("db.port must be a number, e.g. 1521");
        }
        return "jdbc:oracle:thin:@//" + host + ":" + port + "/" + service; // SERVICE NAME syntax
    }

    private static String required(Properties p, String key) {
        String v = p.getProperty(key);
        if (v == null || v.trim().isEmpty() || v.trim().startsWith("YOUR_")) {
            throw new IllegalStateException("db.properties: please set a real value for '" + key + "'.");
        }
        return v.trim();
    }

    private static synchronized Properties loadConfig() {
        if (config != null) {
            return config;
        }
        Properties p = new Properties();
        String path = System.getProperty("spendwise.config");
        if (path == null || path.trim().isEmpty()) {
            path = System.getenv("SPENDWISE_CONFIG");
        }
        try {
            if (path != null && !path.trim().isEmpty()) {
                try (InputStream in = Files.newInputStream(Paths.get(path.trim()))) {
                    p.load(in);
                }
            } else {
                try (InputStream in = DBUtil.class.getClassLoader().getResourceAsStream("db.properties")) {
                    if (in == null) {
                        throw new IllegalStateException("db.properties not found. Copy "
                                + "src/main/resources/db.properties.template to "
                                + "src/main/resources/db.properties and fill it in.");
                    }
                    p.load(in);
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("Cannot read the database configuration file: " + e.getMessage());
        }
        config = p;
        return config;
    }
}

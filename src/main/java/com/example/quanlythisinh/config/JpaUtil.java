package com.example.quanlythisinh.config;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

/**
 * Build {@link EntityManagerFactory} once.
 * Read {@code application.properties}.
 * For MySQL, auto-migrate some optional columns to NULL if old schema is strict.
 */
public final class JpaUtil {
    private static final EntityManagerFactory EMF = buildEntityManagerFactory();

    private JpaUtil() {
    }

    /** Read config, run {@link #ensureMysqlOptionalNullColumns(Properties)}, then create PU {@code qlts-pu}. */
    private static EntityManagerFactory buildEntityManagerFactory() {
        Properties properties = new Properties();
        try (InputStream input = JpaUtil.class.getClassLoader().getResourceAsStream("application.properties")) {
            if (input != null) {
                properties.load(input);
            }
        } catch (IOException ex) {
            throw new RuntimeException("Cannot read application.properties", ex);
        }

        ensureMysqlOptionalNullColumns(properties);

        try {
            Properties jpaProps = new Properties();
            jpaProps.put("jakarta.persistence.jdbc.url", properties.getProperty("db.url"));
            jpaProps.put("jakarta.persistence.jdbc.user", properties.getProperty("db.username"));
            jpaProps.put("jakarta.persistence.jdbc.password", properties.getProperty("db.password"));
            jpaProps.put("jakarta.persistence.jdbc.driver", "com.mysql.cj.jdbc.Driver");
            jpaProps.put("hibernate.dialect",
                    properties.getProperty("hibernate.dialect", "org.hibernate.dialect.MySQLDialect"));
            jpaProps.put("hibernate.hbm2ddl.auto", properties.getProperty("hibernate.hbm2ddl.auto", "none"));
            jpaProps.put("hibernate.show_sql", properties.getProperty("hibernate.show_sql", "false"));
            jpaProps.put("hibernate.format_sql", properties.getProperty("hibernate.format_sql", "true"));
            jpaProps.put("hibernate.jdbc.time_zone", properties.getProperty("hibernate.jdbc.time_zone", "UTC"));
            jpaProps.put("hibernate.archive.autodetection", "class");
            return Persistence.createEntityManagerFactory("qlts-pu", jpaProps);
        } catch (Exception ex) {
            throw new RuntimeException("Cannot init JPA", ex);
        }
    }

    /**
     * MySQL: if some columns are still NOT NULL, run ALTER to allow NULL.
     * This keeps the app compatible with older schemas.
     */
    private static void ensureMysqlOptionalNullColumns(Properties properties) {
        String url = properties.getProperty("db.url", "");
        if (!url.contains("jdbc:mysql")) {
            return;
        }
        String user = properties.getProperty("db.username");
        String pass = properties.getProperty("db.password");

        try (Connection c = DriverManager.getConnection(url, user, pass)) {
            if (columnExistsAndNotNull(c, "DiemThi", "diemMon1")) {
                try (Statement st = c.createStatement()) {
                    st.executeUpdate(
                            "ALTER TABLE DiemThi "
                                    + "MODIFY diemMon1 DECIMAL(4,2) NULL, "
                                    + "MODIFY diemMon2 DECIMAL(4,2) NULL, "
                                    + "MODIFY diemMon3 DECIMAL(4,2) NULL");
                }
                System.err.println("[JpaUtil] DiemThi scores -> NULL OK.");
            }
            if (columnExistsAndNotNull(c, "DangKyNguyenVong", "nganhDangKy")) {
                try (Statement st = c.createStatement()) {
                    st.executeUpdate("ALTER TABLE DangKyNguyenVong MODIFY nganhDangKy VARCHAR(120) NULL");
                }
                System.err.println("[JpaUtil] DangKyNguyenVong.nganhDangKy -> NULL OK.");
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Cannot ALTER NULL columns (check DB permission).", ex);
        }
    }

    /** Return true if the column exists and IS_NULLABLE = 'NO'. */
    private static boolean columnExistsAndNotNull(Connection c, String tableLogical, String columnName)
            throws SQLException {
        String sql =
                "SELECT c.IS_NULLABLE FROM information_schema.COLUMNS c "
                        + "WHERE c.TABLE_SCHEMA = DATABASE() AND c.TABLE_NAME = ? AND c.COLUMN_NAME = ?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, resolveTableName(c, tableLogical));
            ps.setString(2, columnName);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return false;
                }
                return "NO".equalsIgnoreCase(rs.getString(1));
            }
        }
    }

    /** Resolve real table name from information_schema (case-insensitive on Windows). */
    private static String resolveTableName(Connection c, String tableLogical) throws SQLException {
        try (PreparedStatement ps =
                c.prepareStatement(
                        "SELECT TABLE_NAME FROM information_schema.TABLES "
                                + "WHERE TABLE_SCHEMA = DATABASE() AND LOWER(TABLE_NAME) = LOWER(?)")) {
            ps.setString(1, tableLogical);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString(1);
                }
            }
        }
        return tableLogical;
    }

    /** Create {@link EntityManager} for a unit of work (remember to close). */
    public static EntityManager createEntityManager() {
        return EMF.createEntityManager();
    }

    /** Close EMF on app shutdown (called from {@code MainApp}). */
    public static void shutdown() {
        if (EMF.isOpen()) {
            EMF.close();
        }
    }
}


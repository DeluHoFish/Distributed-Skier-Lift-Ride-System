package DAO.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class DBConnectionManager {
  private static HikariDataSource dataSource;
  private static final Object LOCK = new Object();

  public static void initializePool() {
    if (dataSource == null) {
      synchronized (LOCK) {
        if (dataSource == null) {
          try{
            Class.forName("com.mysql.cj.jdbc.Driver");
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl("unknown");
            config.setUsername("unknown");
            config.setPassword("unknown");

            config.setMaximumPoolSize(180);
            config.setMinimumIdle(80);
            config.setIdleTimeout(300000);
            config.setMaxLifetime(900000);
            config.setConnectionTimeout(30000);

            dataSource = new HikariDataSource(config);
          } catch (ClassNotFoundException e) {
            throw new RuntimeException("MySQL JDBC Driver not found", e);
          }

        }
      }
    }

  }

  public static Connection getConnection() throws SQLException {
    if (dataSource == null) {
      initializePool();
    }
      return dataSource.getConnection();
  }


  public static void shutdown() {
    if (dataSource != null) {
      dataSource.close();
    }
  }
}

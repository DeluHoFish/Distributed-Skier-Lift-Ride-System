package database.dao;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import database.entity.LiftRide;
import database.entity.SkierLiftRideEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Class EventDAOImpl implements the database methods in interface EventDAO
 */
public class EventDAOImpl implements EventDAO{

  private HikariDataSource dataSource;

  public EventDAOImpl() {this.initializePool();}


  private void initializePool() {
    HikariConfig config = new HikariConfig();
    config.setJdbcUrl("jdbc:mysql://cs6650db1.csuk2fno42gc.us-west-2.rds.amazonaws.com:3306/cs6650_database?"
        + "rewriteBatchedStatements=true&useServerPrepStmts=true&cachePrepStmts=true&useLocalSessionState=true");
    config.setUsername("honzie");
    config.setPassword("CS6650dbhonzie");

    config.setMaximumPoolSize(80);
    config.setMinimumIdle(10);
    config.setIdleTimeout(300000);
    config.setConnectionTimeout(20000);

    this.dataSource = new HikariDataSource(config);
  }

  private Connection getConnection() throws SQLException {
    return this.dataSource.getConnection();
  }

  @Override
  public void shutdown() {
    if (dataSource != null) {
      dataSource.close();
    }
  }

  /**
   * Method that Inserts a batch of new data into the database.
   */
  @Override
  public void batchInsertEvents(Set<Entry<SkierLiftRideEvent, LiftRide>> batch) {
    String sql = "INSERT INTO events (skier_id, resort_id, season_id, day_id, lift_id, time) "
        + "VALUES (?, ?, ?, ?, ?, ?)";

    try(Connection connection = dataSource.getConnection()) {
      connection.setAutoCommit(false);
      try(PreparedStatement stmt = connection.prepareStatement(sql)) {
        for(Entry<SkierLiftRideEvent, LiftRide> entry: batch) {
          SkierLiftRideEvent event = entry.getKey();
          LiftRide liftRide = entry.getValue();
          stmt.setInt(1, event.getSkierId());
          stmt.setInt(2, event.getResortId());
          stmt.setString(3, event.getSeasonId());
          stmt.setString(4, event.getDayId());
          stmt.setInt(5, liftRide.getLiftId());
          stmt.setInt(6, liftRide.getTime());
          stmt.addBatch();
        }

        stmt.executeBatch();
        connection.commit();
      }

    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  /**
   * Method that finds and returns the existing data in the database.
   */
  @Override
  public Set<Entry<SkierLiftRideEvent, LiftRide>> findExistingSkierEvents() {
    String sql = "SELECT * FROM events";
    Set<Entry<SkierLiftRideEvent, LiftRide>> result = new HashSet<>();
    try(Connection connection = dataSource.getConnection()) {
      try(PreparedStatement stmt = connection.prepareStatement(sql)) {
        ResultSet rs = stmt.executeQuery();
        while(rs.next()) {
          result.add(Map.entry(new SkierLiftRideEvent(rs.getInt("skier_id"),
              rs.getInt("resort_id"),
              rs.getString("season_id"),
              rs.getString("day_id")),
              new LiftRide(rs.getInt("lift_id"),
                  rs.getInt("time"))));
        }
      }

    } catch (SQLException e) {
      e.printStackTrace();
    }
    return result;
  }
}

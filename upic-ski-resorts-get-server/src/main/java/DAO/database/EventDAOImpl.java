package DAO.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class EventDAOImpl implements EventDAO {

  public EventDAOImpl() {
  }

  @Override
  public Integer getNumOfUniqueSkiersAtTime(Integer resortId, String seasonId, String dayId) {

    String sql = "SELECT COUNT(DISTINCT skier_id) AS skier_count "
        + "FROM events WHERE resort_id = ? AND season_id = ? AND day_id = ?";

    try(Connection connection = DBConnectionManager.getConnection()) {

      try(PreparedStatement stmt = connection.prepareStatement(sql)) {
        stmt.setInt(1, resortId);
        stmt.setString(2, seasonId);
        stmt.setString(3, dayId);

        ResultSet rs = stmt.executeQuery();
        if(rs.next()){
          int result = rs.getInt("skier_count");
          return result;
        }
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }

    return 0;
  }

  @Override
  public Integer getSkierResortVerticalTotal(Integer skierId, List<String> resorts,
      List<String> seasons) {
    if (skierId != null && resorts != null) {
//      String sql = "SELECT skier_id, lift_id FROM events WHERE skier_id = ?";
      StringBuilder queryBuilder = new StringBuilder();
      if(seasons != null) {
        queryBuilder.append("SELECT * FROM events WHERE skier_id = ? ");
        queryBuilder.append("AND resort_id IN (");
        queryBuilder.append("?, ".repeat(resorts.size() - 1)).append("?) ");
        queryBuilder.append("AND season_id IN (");
        queryBuilder.append("?, ".repeat(seasons.size()- 1)).append("?)");
      } else {
        queryBuilder.append("SELECT * FROM events WHERE skier_id = ? ");
        queryBuilder.append("AND resort_id IN (");
        queryBuilder.append("?, ".repeat(resorts.size() - 1)).append("?)");
      }

      try(Connection connection = DBConnectionManager.getConnection()) {
        try(PreparedStatement stmt = connection.prepareStatement(queryBuilder.toString())){
          int paramIdx = 1;

          stmt.setInt(paramIdx++, skierId);

          for(String resort: resorts) {
            stmt.setInt(paramIdx++, Integer.parseInt(resort));
          }

          if(seasons != null) {
            for(String season: seasons) {
              stmt.setString(paramIdx++, season);
            }
          }
//          stmt.setInt(1, skierId);

          ResultSet rs = stmt.executeQuery();
          Integer verticalTotal = 0;
          while(rs.next()) {
            int liftId = rs.getInt("lift_id");
            verticalTotal += liftId * 10;
          }


          return verticalTotal;
        }

      } catch (SQLException e) {
        e.printStackTrace();
      }
    }

    return null;
  }


  @Override
  public Integer getSkiDayVerticalForSkier(Integer resortId, String seasonId, String dayId,
      Integer skierId) {

    String sql = "SELECT * FROM events WHERE skier_id = ? AND resort_id = ? AND season_id = ? AND day_id = ?";

    try(Connection connection = DBConnectionManager.getConnection() ) {
      try(PreparedStatement stmt = connection.prepareStatement(sql)) {
        stmt.setInt(1, skierId);
        stmt.setInt(2, resortId);
        stmt.setString(3, seasonId);
        stmt.setString(4, dayId);

        int verticalTotal = 0;

        ResultSet rs = stmt.executeQuery();
        while(rs.next()) {
          verticalTotal += rs.getInt("lift_id") * 10;
        }

        return verticalTotal;
      }

    } catch (SQLException e) {
      e.printStackTrace();
    }

    return 0;
  }

}

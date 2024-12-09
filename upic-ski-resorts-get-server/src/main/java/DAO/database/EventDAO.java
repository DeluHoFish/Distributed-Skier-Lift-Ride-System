package DAO.database;

import java.util.List;


public interface EventDAO {

  Integer getNumOfUniqueSkiersAtTime(Integer resortId, String seasonId, String dayId);

  Integer getSkierResortVerticalTotal(Integer skierId, List<String> resorts, List<String> seasons);

  Integer getSkiDayVerticalForSkier(Integer resortId, String seasonId, String dayId, Integer skierId);

}

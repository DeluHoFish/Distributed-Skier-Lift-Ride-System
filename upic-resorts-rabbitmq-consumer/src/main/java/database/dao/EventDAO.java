package database.dao;

import database.entity.LiftRide;
import database.entity.SkierLiftRideEvent;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public interface EventDAO {
  void shutdown();

  void batchInsertEvents(Set<Entry<SkierLiftRideEvent, LiftRide>> batch);

  Set<Entry<SkierLiftRideEvent, LiftRide>> findExistingSkierEvents();
}

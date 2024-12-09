package database.entity;

import java.util.Objects;

public class LiftRide {

  private int id;

  private int liftId;

  private int time;

  private int skierEventId;

  public LiftRide() {
  }

  public LiftRide(int liftId, int time) {
    this.liftId = liftId;
    this.time = time;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public int getLiftId() {
    return liftId;
  }

  public void setLiftId(int liftId) {
    this.liftId = liftId;
  }

  public int getTime() {
    return time;
  }

  public void setTime(int time) {
    this.time = time;
  }

  public int getSkierEventId() {
    return skierEventId;
  }

  public void setSkierEventId(int skierEventId) {
    this.skierEventId = skierEventId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    LiftRide liftRide = (LiftRide) o;
    return liftId == liftRide.liftId && time == liftRide.time;
  }

  @Override
  public int hashCode() {
    return Objects.hash(liftId, time);
  }

  @Override
  public String toString() {
    return "LiftRide{" +
        "id=" + id +
        ", liftId=" + liftId +
        ", time=" + time +
        ", skierEventId=" + skierEventId +
        '}';
  }
}

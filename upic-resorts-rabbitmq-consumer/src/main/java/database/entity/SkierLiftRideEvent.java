package database.entity;

import java.util.Objects;

public class SkierLiftRideEvent {
  private int id;
  private int skierId;
  private int resortId;
  private String seasonId;
  private String dayId;

  public SkierLiftRideEvent() {
  }

  public SkierLiftRideEvent(int id, int skierId, int resortId, String seasonId, String dayId) {
    this.id = id;
    this.skierId = skierId;
    this.resortId = resortId;
    this.seasonId = seasonId;
    this.dayId = dayId;
  }

  public SkierLiftRideEvent(int skierId, int resortId, String seasonId, String dayId) {
    this.skierId = skierId;
    this.resortId = resortId;
    this.seasonId = seasonId;
    this.dayId = dayId;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public int getSkierId() {
    return skierId;
  }

  public void setSkierId(int skierId) {
    this.skierId = skierId;
  }

  public int getResortId() {
    return resortId;
  }

  public void setResortId(int resortId) {
    this.resortId = resortId;
  }

  public String getSeasonId() {
    return seasonId;
  }

  public void setSeasonId(String seasonId) {
    this.seasonId = seasonId;
  }

  public String getDayId() {
    return dayId;
  }

  public void setDayId(String dayId) {
    this.dayId = dayId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SkierLiftRideEvent event = (SkierLiftRideEvent) o;
    return skierId == event.skierId && resortId == event.resortId && Objects.equals(
        seasonId, event.seasonId) && Objects.equals(dayId, event.dayId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(skierId, resortId, seasonId, dayId);
  }

  @Override
  public String toString() {
    return "SkierLiftRideEvent{" +
        "id=" + id +
        ", skierId=" + skierId +
        ", resortId=" + resortId +
        ", seasonId='" + seasonId + '\'' +
        ", dayId='" + dayId + '\'' +
        '}';
  }
}

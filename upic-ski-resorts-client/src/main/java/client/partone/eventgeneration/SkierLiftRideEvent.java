package client.partone.eventgeneration;

import io.swagger.client.model.LiftRide;
import java.util.Random;

public class SkierLiftRideEvent {
  private int skierID;
  private int resortID = 5;
  private final String seasonID = "2024";
  private final String dayID = "3";
  private LiftRide liftRide;
  private Random rand = new Random();

  public SkierLiftRideEvent(){
    this.skierID=this.rand.nextInt(1, 100001);
    //this.resortID=this.rand.nextInt(1, 11);
    this.liftRide = new LiftRide();
    this.liftRide.setLiftID(this.rand.nextInt(1, 41));
    this.liftRide.setTime(this.rand.nextInt(1, 361));
  }

  public int getSkierID() {
    return skierID;
  }

  public int getResortID() {
    return resortID;
  }

  public String getSeasonID() {
    return seasonID;
  }
  public String getDayID() {
    return dayID;
  }
  public LiftRide getLiftRide() {
    return liftRide;
  }

  @Override
  public String toString() {
    return "SkierLiftRideEvent{" +
        "skierID=" + skierID +
        ", resortID=" + resortID +
        ", seasonID='" + seasonID + '\'' +
        ", dayID='" + dayID + '\'' +
        ", liftRide=" + liftRide +
        '}';
  }
}

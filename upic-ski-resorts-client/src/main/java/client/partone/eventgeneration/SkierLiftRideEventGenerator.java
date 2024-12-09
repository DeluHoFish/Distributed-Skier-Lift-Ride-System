package client.partone.eventgeneration;

import java.util.ArrayList;
import java.util.List;

public class SkierLiftRideEventGenerator {
  private int eventNum;
  private List<SkierLiftRideEvent> eventList;

  public SkierLiftRideEventGenerator(int eventNum){
    this.eventNum=eventNum;
    this.eventList = new ArrayList<>();
    for(int i = 0; i < eventNum; i++){
      this.eventList.add(new SkierLiftRideEvent());
    }
  }

  public int getEventNum() {
    return eventNum;
  }

  public List<SkierLiftRideEvent> getEventList() {
    return eventList;
  }
}

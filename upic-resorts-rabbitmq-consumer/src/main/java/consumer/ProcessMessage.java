package consumer;

import database.entity.LiftRide;
import database.entity.SkierLiftRideEvent;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Class ProcessMessage is for converting the messages to a set of entries for data insertion.
 */
public class ProcessMessage {

  public ProcessMessage() {
  }


  /**
   * Method that takes a list of messages and converts them to a set of entries
   * with key SkierLiftRideEvent and value LiftRide.
   */
  public Set<Entry<SkierLiftRideEvent, LiftRide>> convertToObject(List<String> messages) {
    Set<Entry<SkierLiftRideEvent, LiftRide>> result = new HashSet<>();
    for(String message: messages) {
      Map<String, String> info = parseMessage(message);
      SkierLiftRideEvent event = new SkierLiftRideEvent();
      LiftRide liftRide = new LiftRide();
      for(String item: info.keySet()) {
        String content = info.get(item);
        setValue(event, liftRide, item, content);
      }
      result.add(Map.entry(event, liftRide));
    }
    return result;
  }

  /**
   * Method that parses messages into corresponding message pieces.
   */
  private Map<String,String> parseMessage(String message) {
    Map<String, String> info = new HashMap<>();
    String[] pieces = message.split(",");
    for(String piece: pieces) {
      String[] item = piece.split(":");
      info.put(item[0].trim(), item[1].trim());
    }
    return info;
  }

  /**
   * Set the values for SkierLiftRideEvent and LiftRide objects
   */
  private void setValue(SkierLiftRideEvent event, LiftRide liftRide, String item, String content) {
    try {
      if(item.equals("liftId") || item.equals("time")) {
        Field liftField = liftRide.getClass().getDeclaredField(item);
        liftField.setAccessible(true);
        int value = Integer.parseInt(content);
        liftField.set(liftRide, value);
      } else {
        Field eventField = event.getClass().getDeclaredField(item);
        eventField.setAccessible(true);
        Object value = findType(eventField.getType(), content);
        eventField.set(event, value);
      }

    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private Object findType(Class<?> fieldType, String content) {
    if(fieldType == String.class) return content;
    else return Integer.parseInt(content);
  }
}

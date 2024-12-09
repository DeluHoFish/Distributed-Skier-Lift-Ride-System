import consumer.MultiThreadedConsumeAndWrite;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * Main class
 */
public class ConsumeAndWrite {

  public static void main(String[] args) throws IOException, TimeoutException {
    MultiThreadedConsumeAndWrite consumer = new MultiThreadedConsumeAndWrite("unknown",
        "unkown", "unknown",60, 70);

    Runtime.getRuntime().addShutdownHook(new Thread(consumer::shutdown));

    consumer.consumeStart();
  }

}

import com.rabbitmq.client.CancelCallback;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class MultiThreadedConsumer {

  private final String QUEUE_NAME = "test";
  private Map<String, List<String>> skierMap = Collections.synchronizedMap(new HashMap<>());
  private ConnectionFactory factory;
  private int numThreads;
  private ExecutorService executorService;


  public MultiThreadedConsumer(String host, int port, String username, String password, int numThreads){
    this.numThreads = numThreads;
    this.executorService = Executors.newFixedThreadPool(numThreads);

    this.factory = new ConnectionFactory();
    this.factory.setHost(host);
    this.factory.setPort(port);
    this.factory.setUsername(username);
    this.factory.setPassword(password);
    this.factory.setVirtualHost("/");
    this.factory.setRequestedHeartbeat(30);      // 30 seconds heartbeat
    this.factory.setConnectionTimeout(15000);     // 15 seconds connection timeout
    this.factory.setHandshakeTimeout(15000);      // 15 seconds handshake timeout
    this.factory.setAutomaticRecoveryEnabled(true);
  }

  public void consumeStart(){
    System.out.println("Starting " + numThreads + " consumer threads...");

    for(int i = 0; i < numThreads; i++){
      final int threadNum = i;
      executorService.submit(()->consumeMessage(threadNum));
    }
  }

  private void consumeMessage(int threadNum){
    try{
      final Connection connection = factory.newConnection();
      final Channel channel = connection.createChannel();

      System.out.println("Thread " + threadNum + " connected to RabbitMQ");
      channel.queueDeclare(QUEUE_NAME, true, false, false, null);
      channel.basicQos(1);

      DeliverCallback deliverCallback = (consumerTag, delivery) -> {
        String message = new String(delivery.getBody(), "UTF-8");
        System.out.println(" Received '" + message +"'" );
        try{
          addToMap(message);
          System.out.println("Added to map.");
          channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
        } catch (Exception e){
          System.err.println("Thread " + threadNum + " - Error adding message to map.");
          channel.basicNack(delivery.getEnvelope().getDeliveryTag(), false, true);
        }
      };

      CancelCallback cancelCallback = consumerTag -> {
        System.out.println("Thread " + threadNum + " - Consumer cancelled");
      };

      channel.basicConsume(QUEUE_NAME, false, deliverCallback, cancelCallback);

    }catch (Exception e){
      System.err.println("Thread " + threadNum + " - Error: " + e.getMessage());
    }
  }

  public void shutdown() {
    System.out.println("Shutting down consumer threads...");
    executorService.shutdown();
    try {
      if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
        executorService.shutdownNow();
      }
    } catch (InterruptedException e) {
      executorService.shutdownNow();
      Thread.currentThread().interrupt();
    }
  }


  private void addToMap(String message){
    String[] messageParts = message.split(";");
    if(skierMap.containsKey(messageParts[0])) skierMap.get(messageParts[0]).add(messageParts[1]);
    else{
      skierMap.put(messageParts[0], new ArrayList<>());
      skierMap.get(messageParts[0]).add(messageParts[1]);
    }
  }

  public static void main(String[] args) throws IOException, TimeoutException {
    MultiThreadedConsumer consumer = new MultiThreadedConsumer("ec2-52-24-246-126.us-west-2.compute.amazonaws.com",
        5672, "userH", "userh123456", 50);

    Runtime.getRuntime().addShutdownHook(new Thread(consumer::shutdown));

    consumer.consumeStart();
  }

}

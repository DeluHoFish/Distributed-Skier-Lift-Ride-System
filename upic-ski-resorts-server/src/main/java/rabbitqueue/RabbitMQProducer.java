package rabbitqueue;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import java.nio.charset.StandardCharsets;

public class RabbitMQProducer {
  private ConnectionFactory factory;

  private int messageSent;


  private synchronized void counter(){
    this.messageSent++;
  }

  public RabbitMQProducer(String host, String userName, String password){
    this.factory = new ConnectionFactory();
    this.factory.setHost(host);
    this.factory.setUsername(userName);
    this.factory.setPassword(password);
    this.factory.setPort(5672);
    this.factory.setVirtualHost("/");
    this.factory.setRequestedHeartbeat(60);      // 30 seconds heartbeat
    this.factory.setConnectionTimeout(60000);     // 15 seconds connection timeout
    this.factory.setShutdownTimeout(10000);
    this.factory.setNetworkRecoveryInterval(5000);
    this.factory.setAutomaticRecoveryEnabled(true);
    this.messageSent = 0;
  }

  public boolean sendMessage(String message) throws Exception {
    try(Connection connection = this.factory.newConnection();
        Channel channel = connection.createChannel()){

      channel.confirmSelect();
      channel.basicQos(1);
      channel.queueDeclare("test", true, false, false, null);
      channel.basicPublish("", "test", null, message.getBytes(StandardCharsets.UTF_8));

      this.counter();

      System.out.println(messageSent + " Sent '" + message + "'");

      return true;
    } catch (Exception e){
      System.err.println("Error sending message: " + e.getMessage());
      return false;
    }
  }

  private void sleep(long millis) {
    try {
      Thread.sleep(millis);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }

}

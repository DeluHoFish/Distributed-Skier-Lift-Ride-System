package rabbitqueue;

import com.rabbitmq.client.Channel;
import java.nio.charset.StandardCharsets;

public class RabbitMQProducer {
  private ServerChannelPool pool;

  public RabbitMQProducer(String host, String userName, String password, int maxChannels) {
    this.pool = new ServerChannelPool(host, userName, password, maxChannels);
  }

  public ServerChannelPool getPool() {
    return pool;
  }

  public boolean sendMessage(String message) throws Exception {
    try{
      Channel channel= pool.borrowChannel();

      channel.confirmSelect();
      channel.queueDeclare("test", true, false, false, null);
      channel.basicPublish("", "test", null, message.getBytes(StandardCharsets.UTF_8));


      System.out.println(" Sent '" + message + "'");

      pool.returnChannel(channel);
      return true;
    } catch (Exception e){
      System.err.println("Error sending message: " + e.getMessage());
      return false;
    }
  }

}

package rabbitqueue;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Class ServerChannelPool creates a pool of channels for server to send the messages.
 */
public class ServerChannelPool implements AutoCloseable{

  private Connection connection;
  private final BlockingQueue<Channel> channelPool;
  private final int maxChannels;
  private final AtomicInteger channelCounter;
  private volatile boolean shutdown;

  public ServerChannelPool(String host, String username, String password, int maxPoolSize) {
    this.channelPool = new LinkedBlockingQueue<>();
    this.channelCounter = new AtomicInteger(0);
    this.maxChannels = maxPoolSize;
    this.shutdown = false;

    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost(host);
    factory.setUsername(username);
    factory.setPassword(password);
    factory.setPort(5672);
    factory.setVirtualHost("/");
    try{
      this.connection = factory.newConnection();
    } catch (Exception e) {
      e.printStackTrace();
    }

    for(int i = 0; i < maxPoolSize; i++) {
      createChannel();
    }
  }

  private void createChannel() {
    try{
      Channel channel = connection.createChannel();
      channel.basicQos(1);
      channelPool.offer(channel);
      channelCounter.incrementAndGet();
    } catch (Exception e) {
      e.printStackTrace();
    }

  }

  public Channel borrowChannel() throws IOException, InterruptedException {
    if(shutdown) {
      throw new IllegalStateException("Channel pool is shut down");
    }

    Channel channel = channelPool.poll();
    if(channel == null && channelCounter.get() < maxChannels) {
      createChannel();
      channel = channelPool.take();
    }

    if(channel != null && !channel.isOpen()) {
      channelCounter.decrementAndGet();
      return borrowChannel();
    }

    return channel;
  }

  public void returnChannel(Channel channel) {
    if(channel != null && channel.isOpen()) {
      channelPool.offer(channel);
    } else {
      channelCounter.decrementAndGet();
    }
  }

  @Override
  public void close() throws Exception {
    shutdown = true;

    Channel channel;
    while((channel = channelPool.poll()) != null) {
      try{
        if(channel.isOpen()) {
          channel.close();
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }

    if(connection != null && connection.isOpen()) {
      connection.close();
    }
  }
}


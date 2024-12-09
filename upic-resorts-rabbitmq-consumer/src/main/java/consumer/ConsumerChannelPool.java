package consumer;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Class ConsumerChannelPool creates a pool of channels for threads to consume messages.
 */
public class ConsumerChannelPool {
  private final BlockingQueue<Channel> pool;
  private final int maxSize;
  private final Connection connection;
  private final AtomicInteger currentSize;
  private volatile boolean shutdown;

  public ConsumerChannelPool(String host, String username, String password, int maxPoolSize)
      throws IOException, TimeoutException {
    this.maxSize = maxPoolSize;
    this.currentSize = new AtomicInteger(0);
    this.pool = new LinkedBlockingQueue<>(maxPoolSize);
    this.shutdown = false;

    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost(host);
    factory.setPort(5672);
    factory.setUsername(username);
    factory.setPassword(password);
    factory.setVirtualHost("/");
    this.connection = factory.newConnection();

    for(int i = 0; i < maxSize; i++) {
      createChannel();
    }

  }

  private void createChannel() throws IOException {
    Channel channel = connection.createChannel();;
    channel.basicQos(1);
    pool.offer(channel);
    currentSize.incrementAndGet();
  }

  public Channel borrowChannel() throws IOException, InterruptedException {
    if(shutdown) {
      throw new IllegalStateException("Channel pool is shut down.");
    }

    Channel channel = pool.poll();
    if (channel == null && currentSize.get() < maxSize) {
      createChannel();
      channel = pool.take();
    }

    if (channel != null && !channel.isOpen()) {
      currentSize.decrementAndGet();
      return borrowChannel();
    }

    return channel;
  }

  public void returnChannel(Channel channel) {
    if(channel != null && channel.isOpen() && !shutdown) {
      pool.offer(channel);
    } else {
      currentSize.decrementAndGet();
    }
  }

  public void shutdown() throws IOException {
    shutdown = true;

    Channel channel;
    while((channel = pool.poll()) != null) {
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


  public int getActiveConnectionNum() {
    return currentSize.get();
  }

  public int getAvailableConnectionNum() {
    return pool.size();
  }
}

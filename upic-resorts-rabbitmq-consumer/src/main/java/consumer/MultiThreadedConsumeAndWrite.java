package consumer;

import com.rabbitmq.client.CancelCallback;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DeliverCallback;
import database.dao.EventDAO;
import database.dao.EventDAOImpl;
import database.entity.LiftRide;
import database.entity.SkierLiftRideEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Class MultiThreadedConsumeAndWrite initializes the multiple threads for consuming
 * messages and write the data to the database.
 */
public class MultiThreadedConsumeAndWrite {

  private final String QUEUE_NAME = "test";
  private final int QUEUE_SIZE = 20000;
  private final int BATCH_SIZE = 1000;
  private final int BATCH_TIMEOUT_MS = 5000;
  private ConsumerChannelPool channelPool;
  private int numThreads;
  private ExecutorService executorService;
  private EventDAO eventDAO = new EventDAOImpl();
  private ProcessMessage pm = new ProcessMessage();

  /* Blocking Queue for temporarily storing the messages coming in.*/
  private BlockingQueue<String> messageBuffer = new LinkedBlockingQueue<>(QUEUE_SIZE);

  /* Set cache for storing the existing events in database. */
  private Set<Entry<SkierLiftRideEvent, LiftRide>>
      skierEventsCache = Collections.synchronizedSet(new HashSet<>());
  private AtomicBoolean isRunning = new AtomicBoolean(true);

  public MultiThreadedConsumeAndWrite(String host, String username, String password, int numThreads, int maxPoolSize)
      throws IOException, TimeoutException {
    this.numThreads = numThreads;
    this.executorService = Executors.newFixedThreadPool(numThreads);
    this.channelPool = new ConsumerChannelPool(host, username, password, maxPoolSize);
    this.initializeCache();
  }

  /**
   * Inner Class BatchProcessor for processing the messages in a batch unit.
   */
  private class BatchProcessor implements Runnable {

    private final List<String> batch = Collections.synchronizedList(new ArrayList<>());
    private long lastProcessTime = System.currentTimeMillis();

    @Override
    public void run() {
      while (isRunning.get()) {
        try {
          // extract messages from the buffer and add to the batch list.
          String message = messageBuffer.poll(100, TimeUnit.MILLISECONDS);
          if (message != null) {
            batch.add(message);
          }

          if (shouldProcessBatch()) {
            processBatch();
          }
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
          break;
        }
      }
    }

    /**
     * Function that decides whether the batch should be processed.
     */
    private boolean shouldProcessBatch() {
      return !batch.isEmpty() && (
          batch.size() >= BATCH_SIZE ||
              System.currentTimeMillis() - lastProcessTime >= BATCH_TIMEOUT_MS
      );
    }

    private synchronized void processBatch() {
      Set<Entry<SkierLiftRideEvent, LiftRide>> batchSet = pm.convertToObject(batch);
      /* remove the existing data from the batch set
        generated from the new messages in the batch.*/
      for(Entry<SkierLiftRideEvent, LiftRide> entry: skierEventsCache) {
        if(batchSet.contains(entry)) {
          batchSet.remove(entry);
        }
      }
      eventDAO.batchInsertEvents(batchSet);

      /* Add the batch to the cache after batch is add to the database.*/
      skierEventsCache.addAll(batchSet);

      /* Clean the batch for new messages*/
      batch.clear();
      lastProcessTime = System.currentTimeMillis();
    }
  }


  public void consumeStart(){
    System.out.println("Starting " + numThreads + " consumer threads...");

    for(int i = 0; i < numThreads; i++){
      final int threadNum = i;
      BatchProcessor batchProcessor = new BatchProcessor();
      executorService.submit(()-> {
        consumeMessage(threadNum);
        batchProcessor.run();
      });
    }
  }

  /** Function that queries the database and store the existing data
   * into the in-memory set before the program starts to consume new messages
   */
  private void initializeCache() {
    skierEventsCache.addAll(eventDAO.findExistingSkierEvents());
  }



  private void consumeMessage(int threadNum){
    try{
      Channel channel = channelPool.borrowChannel();

      System.out.println("Thread " + threadNum + " connected to RabbitMQ");
      channel.queueDeclare(QUEUE_NAME, true, false, false, null);

      DeliverCallback deliverCallback = (consumerTag, delivery) -> {
        String message = new String(delivery.getBody(), "UTF-8");
        System.out.println(" Received '" + message +"'" );
        try{
          boolean added = messageBuffer.offer(message, 5, TimeUnit.SECONDS);
          if(added) {
            channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
          } else {
            channel.basicReject(delivery.getEnvelope().getDeliveryTag(), true);
          }
        } catch (Exception e){
          System.err.println("Error processing message: " + e.getMessage());
          channel.basicReject(delivery.getEnvelope().getDeliveryTag(), false);
        }
      };

      CancelCallback cancelCallback = consumerTag -> {
        System.out.println("Thread " + threadNum + " - Consumer cancelled");
      };

      channel.basicConsume(QUEUE_NAME, false, deliverCallback, cancelCallback);

      channelPool.returnChannel(channel);
    }catch (Exception e){
      System.err.println("Thread " + threadNum + " - Error: " + e.getMessage());
    }
  }

  public void shutdown() {
    System.out.println("Shutting down consumer threads...");
    isRunning.set(false);
    eventDAO.shutdown();
    try{
      channelPool.shutdown();
    } catch (Exception e) {
      e.printStackTrace();
    }

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
}

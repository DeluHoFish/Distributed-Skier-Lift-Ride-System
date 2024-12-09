package client;

import client.partone.SendRequest;
import client.partone.eventgeneration.SkierLiftRideEventGenerator;
import java.util.concurrent.CountDownLatch;
import client.parttwo.CalculateData;
import client.parttwo.CreateCSVRecord;

public class MultithreadedClient {
  private final int STARTUP_THREADS_NUM = 32;
  private final int TOTAL_REQUEST = 200000;
  private int successfulCount = 0;
  private int unsuccessfulCount = 0;

  public synchronized void count(int successful, int unsuccessful) {
    this.successfulCount += successful;
    this.unsuccessfulCount += unsuccessful;
  }

  public int getSuccessfulCount() {
    return successfulCount;
  }

  public int getUnsuccessfulCount() {
    return unsuccessfulCount;
  }

  public void start(){
    MultithreadedClient client = new MultithreadedClient();
    SkierLiftRideEventGenerator generator = new SkierLiftRideEventGenerator(client.TOTAL_REQUEST);
    CountDownLatch latchOne = new CountDownLatch(client.STARTUP_THREADS_NUM / 4);
    CountDownLatch latchTwo = new CountDownLatch((200 - client.STARTUP_THREADS_NUM) / 3);
    CreateCSVRecord createCSVRecord = new CreateCSVRecord();

    long start = System.currentTimeMillis();
    for(int i = 0, j = 0; i < client.STARTUP_THREADS_NUM / 4; i++, j += 4000){
      SendRequest sendRequest = new SendRequest(generator.getEventList().subList(j, j+4000));
      new Thread(() ->{
        sendRequest.run();
        client.count(sendRequest.getSuccessfulReq(), sendRequest.getUnsuccessfulReq());
        createCSVRecord.writeFile(sendRequest.getLatencyRecord());
        latchOne.countDown();
      }).start();
    }

    for(int i = 0, j = 0; i < (200 - client.STARTUP_THREADS_NUM) / 3 ; i++, j += 3000){
      SendRequest sendRequest = new SendRequest(generator.getEventList().subList(j, j+3000));
      new Thread(()->{
        sendRequest.run();
        client.count(sendRequest.getSuccessfulReq(), sendRequest.getUnsuccessfulReq());
        createCSVRecord.writeFile(sendRequest.getLatencyRecord());
        latchTwo.countDown();
      }).start();
    }

    try {
      latchOne.await();
      latchTwo.await();
    } catch (InterruptedException e){
      e.printStackTrace();
    }

    long end1 = System.currentTimeMillis();
    long totalTime1 = end1 - start;
    System.out.println("Client(Part 1)");
    System.out.println("Runtime: " + totalTime1 + " millisecs");
    int threads = client.STARTUP_THREADS_NUM / 4 + (200 - client.STARTUP_THREADS_NUM) / 3;
    System.out.println("Threads used: " + threads);
    System.out.println("Successful requests: " + client.getSuccessfulCount());
    System.out.println("Unsuccessful request: " + client.getUnsuccessfulCount());
    double totalThroughput1 = (client.getSuccessfulCount() + client.getUnsuccessfulCount()) * 1000 / totalTime1;
    System.out.println("Total throughput: " + totalThroughput1 + " requests per second");

    CalculateData cd = new CalculateData("data/request_record.csv");

    long end2 = System.currentTimeMillis();
    long totalTime2 = end2 - start;
    double totalThroughput2 = (client.getSuccessfulCount() + client.getUnsuccessfulCount()) * 1000 / totalTime2;
    System.out.println("\nClient(part 2)");
    System.out.println("Mean response time: " + cd.getMean() + " millisecs");
    System.out.println("Median response time: " + cd.getMedian() + " millisecs");
    System.out.println("Throughput: " + totalThroughput2 + " requests per second");
    System.out.println("p99 response time: " + cd.getP99() +" millisecs");
    System.out.println("Min response time: " + cd.getMin() +" millisecs");
    System.out.println("Max response time: " + cd.getMax() +" millisecs");
    double within = totalThroughput2 / totalThroughput1;
    System.out.println("Throughput client part2 divided by part 1: " + String.format("%.3f", within * 100) + "%");
  }


  public static void main(String[] args) throws InterruptedException {
    new MultithreadedClient().start();
  }

}

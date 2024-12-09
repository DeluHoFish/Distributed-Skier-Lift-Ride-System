package client.partone;

import client.partone.eventgeneration.SkierLiftRideEvent;
import io.swagger.client.ApiClient;
import io.swagger.client.ApiException;
import io.swagger.client.ApiResponse;
import io.swagger.client.api.SkiersApi;
import java.util.ArrayList;
import java.util.List;

/**
 * Class SendRequest contains the function that takes a list of events and send the requests.
 */
public class SendRequest implements Runnable{

  private final int RETRY_TIMES = 5;
  private List<SkierLiftRideEvent> events;
  private List<String[]> latencyRecord = new ArrayList<>();
  private int successfulReq;
  private int unsuccessfulReq;
  public SendRequest(List<SkierLiftRideEvent> events){
    this.events = events;
    this.successfulReq = 0;
    this.unsuccessfulReq = 0;
  }

  /**
   * The method that sends the requests.
   */
  @Override
  public void run() {
    ApiClient apiClient = new ApiClient();
    apiClient.setBasePath("http://unknownIP:8080/");
    SkiersApi apiInstance = new SkiersApi(apiClient);
    for(SkierLiftRideEvent event: events){
      try {
        String[] data = new String[4];
        long start = System.currentTimeMillis();
        data[0] = Long.toString(start);
        data[1] = "POST";
        for (int attempt = 0; attempt < RETRY_TIMES; attempt++) {
          try {
            ApiResponse<Void> response = apiInstance.writeNewLiftRideWithHttpInfo(
                event.getLiftRide(), event.getResortID(), event.getSeasonID(),
                event.getDayID(), event.getSkierID());
            if (response.getStatusCode() == 201) {
              long end = System.currentTimeMillis();
              successfulReq++;
              data[2] = Long.toString(end - start);
              data[3] = Integer.toString(response.getStatusCode());
              this.latencyRecord.add(data);
              break;
            }
          } catch (ApiException e) {
            System.err.println("Exception when calling SkierApi#writeNewLiftRide");
            e.printStackTrace();
            if (e.getCode() >= 400 && e.getCode() < 600) {
              if (attempt == RETRY_TIMES - 1) {
                unsuccessfulReq++;
                long end = System.currentTimeMillis();
                data[2] = Long.toString(end - start);
                data[3] = Integer.toString(e.getCode());
                this.latencyRecord.add(data);
                System.out.println("5 attempts used.");
              } else {
                System.out.println("Failed to send the request in attempt " + (attempt + 1)
                    + ". 5 attempts in total.");
              }
            }
          }
        }
      } catch (Exception e) {
        System.err.println(e.getMessage());
        e.printStackTrace();
      }
    }
  }

  public List<SkierLiftRideEvent> getEvents() {
    return events;
  }

  public int getSuccessfulReq() {
    return successfulReq;
  }

  public int getUnsuccessfulReq() {
    return unsuccessfulReq;
  }

  public List<String[]> getLatencyRecord() {
    return latencyRecord;
  }
}

package server;

import com.google.gson.Gson;
import io.swagger.client.model.LiftRide;
import java.io.BufferedReader;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import rabbitqueue.RabbitMQProducer;



@WebServlet("/skiers/*")
public class SkiersServlet extends HttpServlet {

  private Gson gson = new Gson();

  private RabbitMQProducer producer;


  @Override
  public void init() {
     producer = new RabbitMQProducer(
         "unknown",
        "unknown",
         "unknown",
         60);
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {

    try{

      String urlPath = req.getPathInfo();

      if (urlPath == null || urlPath.isEmpty()) {
        res.setStatus(HttpServletResponse.SC_NOT_FOUND);
        res.getWriter().write("missing parameters");
        return;
      }

      String[] pathParts = urlPath.split("/");

      if(pathParts.length != 8){
        res.setStatus(HttpServletResponse.SC_NOT_FOUND);
        res.getWriter().write("incorrect parameters");
        return;
      }

      int resortID = Integer.parseInt(pathParts[1]);
      String seasonID = pathParts[3];
      String dayID = pathParts[5];
      int skierID = Integer.parseInt(pathParts[7]);

      BufferedReader reader = req.getReader();
      LiftRide liftRide = gson.fromJson(reader, LiftRide.class);

      boolean valid = processLiftRide(liftRide, resortID, seasonID, dayID, skierID);
      boolean sent = false;

      if(valid){
        String message = "skierId: " + skierID + ", resortId: " + resortID + ", seasonId: " + seasonID + ", dayId: " +
        dayID + ", liftId: " + liftRide.getLiftID() + ", time: " + liftRide.getTime();
        sent = producer.sendMessage(message);
      }

      if (sent) {
        res.setStatus(HttpServletResponse.SC_CREATED);
        res.getWriter().write("Lift ride recorded successfully");
      } else {
        res.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to record lift ride");
      }
    } catch (NumberFormatException e) {
      res.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid ID format");
    } catch (Exception e) {
      res.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "An error occurred");
    }
  }

  private boolean processLiftRide(LiftRide liftRide, int resortID, String seasonID, String dayID, int skierID) {
    if(skierID < 1 || skierID > 100000) return false;
    if(resortID != 5) return false;
    if(!seasonID.equals("2024")) return  false;
    if(!dayID.equals("1") && !dayID.equals("2") && !dayID.equals("3")) return false;
    if(liftRide.getLiftID() < 1 || liftRide.getLiftID() > 40) return false;
    if(liftRide.getTime() < 1 || liftRide.getTime() > 360) return false;
    return true;
  }

  @Override
  public void destroy() {
    try{
      producer.getPool().close();
    } catch (Exception e){
      e.printStackTrace();
    }
    super.destroy();
  }
}
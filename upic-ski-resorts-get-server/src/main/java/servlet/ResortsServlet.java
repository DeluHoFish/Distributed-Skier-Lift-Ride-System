package servlet;

import DAO.cache.RedisCacheManager;
import DAO.database.EventDAO;
import DAO.database.EventDAOImpl;
import com.google.gson.Gson;
import io.swagger.client.model.ResortSkiers;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/resorts/*")
public class ResortsServlet extends HttpServlet {

  private EventDAO eventDAO;

  private Gson gson = new Gson();


  @Override
  public void init() throws ServletException {
    super.init();
    this.eventDAO = new EventDAOImpl();
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse res)
      throws ServletException, IOException {

    try {
      String urlPath = req.getPathInfo();

      if (urlPath == null || urlPath.isEmpty()) {
        res.setStatus(HttpServletResponse.SC_NOT_FOUND);
        res.getWriter().write("missing parameters");
        return;
      }

      String[] pathParts = urlPath.split("/");

      if (pathParts.length != 7) {
        res.setStatus(HttpServletResponse.SC_NOT_FOUND);
        res.getWriter().write("incorrect parameters");
        return;
      }

      int resortId = Integer.parseInt(pathParts[1]);
      String seasonId = pathParts[3];
      String dayId = pathParts[5];

      boolean valid = processLiftRide(resortId, seasonId, dayId);
      String queryInfo = "resortId: " + resortId + ", seasonId: " + seasonId + ", dayId: " + dayId;
      if (valid) {
        Integer numSkiers = RedisCacheManager.get(queryInfo, Integer.class);
        if(numSkiers == null) {
          numSkiers = eventDAO.getNumOfUniqueSkiersAtTime(resortId, seasonId, dayId);
          RedisCacheManager.set(queryInfo, numSkiers);
        }


        res.setContentType("application/json");
        res.setCharacterEncoding("UTF-8");

        ResortSkiers resortSkiers = new ResortSkiers();
        resortSkiers.setTime(queryInfo);
        resortSkiers.setNumSkiers(numSkiers);
        String jsonResortSkiers = gson.toJson(resortSkiers);
        PrintWriter out = res.getWriter();
        out.print(jsonResortSkiers);
        out.flush();
        res.setStatus(HttpServletResponse.SC_OK);
      } else {
        res.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Invalid data");
      }

    } catch (NumberFormatException e) {
      res.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid ID format");
    } catch (Exception e) {
      res.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "An error occurred");
    }
  }

  private boolean processLiftRide(int resortID, String seasonID, String dayID) {
    if(resortID < 1 || resortID > 10) return false;
    if(!seasonID.equals("2024")) return  false;
    if(!(dayID.equals("1") || dayID.equals("2") || dayID.equals("3"))) return false;
    return true;
  }

}

package servlet;

import DAO.cache.RedisCacheManager;
import DAO.database.EventDAO;
import DAO.database.EventDAOImpl;
import com.google.gson.Gson;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;



@WebServlet("/skiers/*")
public class SkiersServlet extends HttpServlet {

  private Gson gson = new Gson();
  private EventDAO eventDAO;
  //private Map<String, Integer> skierVerticalCache = Collections.synchronizedMap(new HashMap<>());
  //private Map<String, Integer> skiDayVericalCache = Collections.synchronizedMap(new HashMap<>());

  @Override
  public void init() throws ServletException {
    super.init();
    eventDAO = new EventDAOImpl();
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

      if(pathParts.length == 3) {
        handleResortsTotal(pathParts, req, res);
      } else if (pathParts.length == 8) {
        handleSkierDayVertical(pathParts, req, res);
      } else {
        res.setStatus(HttpServletResponse.SC_NOT_FOUND);
        res.getWriter().write("incorrect parameters");
      }

    } catch (Exception e) {
      res.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "An error occurred");
    }
  }


  public void handleResortsTotal(String[] pathParts, HttpServletRequest req, HttpServletResponse res) {
    try {
      Integer skierId = Integer.parseInt(pathParts[1]);

      if(validateSkierId(skierId)) {
        String[] resortArray = req.getParameterValues("resort");
        String[] seasonArray = req.getParameterValues("season");


        List<String> resorts = (resortArray != null) ? Arrays.asList(resortArray) : null;
        List<String> seasons = (seasonArray != null) ? Arrays.asList(seasonArray) : null;

        String queryInfo = "skierId: " + skierId + ", resorts: " + resorts + ", seasons: " + seasons;
        Integer skierVertical = RedisCacheManager.get(queryInfo, Integer.class);
        if(skierVertical == null) {
          skierVertical = eventDAO.getSkierResortVerticalTotal(skierId, resorts, seasons);
          RedisCacheManager.set(queryInfo, skierVertical);
        }
//        if(skierVerticalCache.containsKey(queryInfo)) {
//          skierVertical = skierVerticalCache.get(queryInfo);
//        } else {
//          skierVertical = eventDAO.getSkierResortVerticalTotal(skierId, resorts, seasons);
//          skierVerticalCache.put(queryInfo, skierVertical);
//        }



        res.setContentType("application/json");
        res.setCharacterEncoding("UTF-8");

        String result = queryInfo + "; skierVertical: " + skierVertical;

        PrintWriter out = res.getWriter();
        out.print(gson.toJson(result));
        out.flush();
        res.setStatus(HttpServletResponse.SC_OK);
      } else {
        res.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Invalid data");
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

  }

  private boolean validateSkierId(Integer skierId) {
    return skierId >= 1 && skierId <= 100000;
  }

  public void handleSkierDayVertical(String[] pathParts, HttpServletRequest req, HttpServletResponse res) {
    try{
      Integer resortId = Integer.parseInt(pathParts[1]);
      String seasonId = pathParts[3];
      String dayId = pathParts[5];
      Integer skierId = Integer.parseInt(pathParts[7]);

      if(validateId(resortId, seasonId, dayId, skierId)) {
        String queryInfo = "skierId: " + skierId + ", resortId: " + resortId + ", seasonId: " + seasonId +", dayId: " + dayId;
        Integer result = RedisCacheManager.get(queryInfo, Integer.class);
        if(result == null) {
          result = eventDAO.getSkiDayVerticalForSkier(resortId, seasonId, dayId, skierId);
          RedisCacheManager.set(queryInfo, result);
        }
//        if(skiDayVericalCache.containsKey(queryInfo)) {
//          result = skiDayVericalCache.get(queryInfo);
//        } else {
//          result = eventDAO.getSkiDayVerticalForSkier(resortId, seasonId, dayId, skierId);
//          skiDayVericalCache.put(queryInfo, result);
//        }

        res.setContentType("application/json");
        res.setCharacterEncoding("UTF-8");
        String output = queryInfo + "; skierDayVertical: " + result;
        PrintWriter out = res.getWriter();
        out.print(gson.toJson(output));
        out.flush();
        res.setStatus(HttpServletResponse.SC_OK);
      } else {
        res.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Invalid data");
      }


    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private boolean validateId(Integer resortId, String seasonId, String dayId, Integer skierId) {
    if(resortId < 1 || resortId > 10) return false;
    if(!seasonId.equals("2024")) return false;
    if(!dayId.equals("1") && !dayId.equals("2") && !dayId.equals("3")) return false;
    if(skierId < 1 || skierId > 100000) return false;
    return true;
  }

}
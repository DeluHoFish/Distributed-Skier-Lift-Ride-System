package servlet;

import DAO.cache.RedisCacheManager;
import DAO.database.DBConnectionManager;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

@WebListener
public class DatabaseContextListener implements ServletContextListener {

  @Override
  public void contextInitialized(ServletContextEvent sce) {
    DBConnectionManager.initializePool();
    RedisCacheManager.initializePool();
  }

  @Override
  public void contextDestroyed(ServletContextEvent sce) {
    DBConnectionManager.shutdown();
    RedisCacheManager.shutdown();
  }
}

package com.rsshandler;

import com.rsshandler.servlets.FavoriteServlet;
import com.rsshandler.servlets.PlaylistServlet;
import com.rsshandler.servlets.UserServlet;
import com.rsshandler.servlets.VideoServlet;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

public class PodcastServerImpl implements PodcastServer {
  private Server server;
  public static int PORT = 8083;

  public PodcastServerImpl() {
    server = new Server(PORT);
    ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
    context.setContextPath("/");
    server.setHandler(context);
    context.addServlet(new ServletHolder(new UserServlet()),"/user.rss");
    context.addServlet(new ServletHolder(new PlaylistServlet()),"/playlist");
    context.addServlet(new ServletHolder(new FavoriteServlet()),"/favorite");
    context.addServlet(new ServletHolder(new VideoServlet()),"/video.mp4");
  }

  public boolean stop() {
    try {
      server.stop();
      return true;
    } catch (Exception e) {
      e.printStackTrace();
      return false;
    }
  }

  public boolean start() {
    try {
      server.start();
      server.join();
      return true;
    } catch (Exception e) {
      e.printStackTrace();
      return false;
    }
  }
  
  public static void main(String args[]) {
	  PodcastServerImpl server = new PodcastServerImpl();
	  server.start();
  }
}

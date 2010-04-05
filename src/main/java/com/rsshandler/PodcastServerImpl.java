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
  private int port;
  private boolean proxyMode = false;

  public PodcastServerImpl(int port, boolean proxyMode) {
  	this.proxyMode = proxyMode;
		this.setPort(port);
  }

  public boolean stop() {
    try {
      server.stop();
      server.destroy();
      return true;
    } catch (Exception e) {
      e.printStackTrace();
      return false;
    }
  }

  public boolean start() {
    try {
      server = new Server(getPort());
      ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
      context.setContextPath("/");
      server.setHandler(context);
      context.addServlet(new ServletHolder(new UserServlet()),"/user.rss");
      context.addServlet(new ServletHolder(new PlaylistServlet()),"/playlist");
      context.addServlet(new ServletHolder(new FavoriteServlet()),"/favorite");
      context.addServlet(new ServletHolder(new VideoServlet(proxyMode)),"/video.mp4");
      server.start();
      server.join();
      return true;
    } catch (Exception e) {
      e.printStackTrace();
      return false;
    }
  }
  
	public void setPort(int port) {
	  this.port = port;
  }

	public int getPort() {
	  return port;
  }

	public static void main(String args[]) {
  	int port = 8083;
  	boolean proxyMode = false;
  	if (args.length > 1) {
  		port = Integer.parseInt(args[1]);
  	}
  	if (args.length > 2) {
  		proxyMode = "true".equals(args[2]);
  	}
	  PodcastServerImpl server = new PodcastServerImpl(port, proxyMode);
	  server.start();
  }

	public void setProxyMode(boolean proxyMode) {
	  this.proxyMode = proxyMode;
  }

	public boolean isProxyMode() {
	  return proxyMode;
  }
}

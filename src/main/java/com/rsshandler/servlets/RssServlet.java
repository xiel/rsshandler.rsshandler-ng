package com.rsshandler.servlets;

import com.rsshandler.Gui;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//TODO add easy error report, configurable from GUI
public abstract class RssServlet extends HttpServlet {
	Logger logger = Logger.getLogger(this.getClass().getName());
	
  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
  	CookieManager cookieManager = new CookieManager();
  	CookieHandler.setDefault(cookieManager);
  	cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_NONE);
  	List<String> headers = Collections.list(request.getHeaderNames());
  	for (String name : headers) {
  		logger.info(String.format("Header: %s, %s", name, request.getHeader(name)));
  	}
  	String host = request.getParameter("host");
    String port = request.getParameter("port");
    int format = Integer.parseInt(request.getParameter("format"));
    String parameters = String.format("?alt=rss&v=2&max-results=%s", request.getParameter("size"));
    URL url = new URL(getRssUrl(request)+parameters);
    logger.info(String.format("RSS URL: %s", url));
    URLConnection connection = url.openConnection();
    logger.info(String.format("Request properties: %s", connection.getRequestProperties()));
    String str = Utils.readString(connection.getInputStream());
    logger.info(String.format("Headers: %s", connection.getHeaderFields()));
    str = replaceEnclosures(str, format, host, port);
    logger.info(str);
    response.setContentType("application/rss+xml");
    response.setStatus(HttpServletResponse.SC_OK);
    response.getWriter().write(str);
  }

  protected abstract String getRssUrl(HttpServletRequest request);

//TODO try to implement normal XML parsing and element insertion, this way it should be more stable
  String replaceEnclosures(String str, int format, String host, String port) {
    Pattern pattern = Pattern.compile("<link>http\\://www.youtube.com/watch\\?v=(.+?)</link><author>");
    Matcher matcher = pattern.matcher(str);
    String formattype = "video/mp4";
    if (format == Gui.FLV) {
      formattype = "video/x-flv";
    }
    while (matcher.find()) {
      String link = matcher.group(1);
      logger.info(String.format("Video id: %s", link));
//      int end = -1;
//      String videoid = link;
//      if ((end = videoid.indexOf('&')) > -1) {
//    	  videoid = videoid.substring(0, end);
//      }
//      logger.info(link);
      String oldenclosure = String.format("%s</link><author>", link);
      String newenclosure = String.format("%s</link><enclosure url=\"http://"+host+":"+port+"/video.mp4?format=%s&amp;id=%s\" length=\"35\" type=\"%s\"/><author>", link, format, link, formattype);
      str = str.replace(oldenclosure, newenclosure);
    }
//    Pattern pattern2 = Pattern.compile("<title>(.*?)</title>");
//    Matcher matcher2 = pattern2.matcher(str);
//    while (matcher2.find()) {
//      String link = matcher2.group(1);
//      System.out.println(link);
//    }    
    
//    str = str.replaceAll("<title>(.*?)</title>", "<title>zzzz</title>");
//    Pattern desc = Pattern.compile("<description>.*?</description>", Pattern.DOTALL);
//    Matcher descmatcher = desc.matcher(str);
//    str = descmatcher.replaceAll("<description></description>");
    return str;
  }

}
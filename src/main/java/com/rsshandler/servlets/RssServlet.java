package com.rsshandler.servlets;

import com.rsshandler.Gui;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//TODO add logging and easy error report, configurable from GUI
public abstract class RssServlet extends HttpServlet {

  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    String host = request.getParameter("host");
    String port = request.getParameter("port");
    int format = Integer.parseInt(request.getParameter("format"));
    URL url = new URL(getRssUrl(request));
    URLConnection connection = url.openConnection();
    String str = Utils.readString(connection.getInputStream());
    str = replaceEnclosures(str, format, host, port);
    response.setContentType("application/rss+xml");
    response.setStatus(HttpServletResponse.SC_OK);
    response.getWriter().write(str);
  }

  protected abstract String getRssUrl(HttpServletRequest request);

  String replaceEnclosures(String str, int format, String host, String port) {
    Pattern pattern = Pattern.compile("<link>http\\://www.youtube.com/watch\\?v=(.+?)</link><author>");
    Matcher matcher = pattern.matcher(str);
    String formattype = "video/mp4";
    if (format == Gui.FLV) {
      formattype = "video/x-flv";
    }
    while (matcher.find()) {
      String link = matcher.group(1);
//      int end = -1;
//      String videoid = link;
//      if ((end = videoid.indexOf('&')) > -1) {
//    	  videoid = videoid.substring(0, end);
//      }
//      System.out.println(link);
      String oldenclosure = String.format("%s</link><author>", link);
      String newenclosure = String.format("%s</link><enclosure url=\"http://"+host+":"+port+"/video.mp4?format=%s&amp;id=%s\" duration=\"35\" type=\"%s\"/><author>", link, format, link, formattype);
      str = str.replace(oldenclosure, newenclosure);
    }
//    Pattern desc = Pattern.compile("<description>.*?</description>", Pattern.DOTALL);
//    Matcher descmatcher = desc.matcher(str);
//    str = descmatcher.replaceAll("<description></description>");
    return str;
  }

}
package com.rsshandler.servlets;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VideoServlet extends HttpServlet {

  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	String id = request.getParameter("id");
	int format = Integer.parseInt(request.getParameter("format"));
	System.out.println(String.format("Video: %s, %s", id, format));
	URL url = new URL(String.format("http://www.youtube.com/watch?v=%s&fmt=%s", id, format));
    URLConnection connection = url.openConnection();
    String str = Utils.readString(connection.getInputStream());
    String redirect = getVideoLink(str);
    response.sendRedirect(redirect);
  }

  private String getVideoLink(String content) {
	  Pattern pattern = Pattern.compile("img.src = 'http:([^']+)'");
	  Matcher matcher = pattern.matcher(content);
	  if (matcher.find()) {
        String url = matcher.group(1);
	    url = url.replace("\\/", "/");
	    url = url.replace("preload", "videoplayback");
	    return "http:"+url;
	  }
	  throw new IllegalStateException("Cann't find video file address: "+ content);
  }

}
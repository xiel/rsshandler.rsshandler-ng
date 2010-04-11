package com.rsshandler.servlets;

import javax.servlet.http.HttpServletRequest;

public class StandardServlet extends RssServlet {

	@Override
  protected String getRssUrl(HttpServletRequest request) {
	  String type = request.getParameter("id");
	  if (type.equals("recently_featured")) {
	    return "http://gdata.youtube.com/feeds/api/standardfeeds/recently_featured";
	  }
    if (type.equals("most_viewed")) {
      return "http://gdata.youtube.com/feeds/api/standardfeeds/most_viewed";
    }
    if (type.equals("most_popular")) {
      return "http://gdata.youtube.com/feeds/api/standardfeeds/most_popular";
    }
    if (type.equals("most_recent")) {
      return "http://gdata.youtube.com/feeds/api/standardfeeds/most_recent";
    }
    if (type.equals("most_discussed")) {
      return "http://gdata.youtube.com/feeds/api/standardfeeds/most_discussed";
    }
    if (type.equals("most_linked")) {
      return "http://gdata.youtube.com/feeds/api/standardfeeds/most_linked";
    }
    if (type.equals("most_responded")) {
      return "http://gdata.youtube.com/feeds/api/standardfeeds/most_responded";
    }
    if (type.equals("top_rated")) {
      return "http://gdata.youtube.com/feeds/api/standardfeeds/top_rated";
    }
    if (type.equals("top_favorites")) {
      return "http://gdata.youtube.com/feeds/api/standardfeeds/top_favorites";
    }
    if (type.equals("recently_featured")) {
      return "http://gdata.youtube.com/feeds/api/standardfeeds/recently_featured";
    }
    throw new IllegalArgumentException("Type is not supported: " + type);
	}
}
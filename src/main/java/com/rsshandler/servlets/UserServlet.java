package com.rsshandler.servlets;

import javax.servlet.http.HttpServletRequest;

public class UserServlet extends RssServlet {

	@Override
  protected String getRssUrl(HttpServletRequest request) {
	  return String.format("http://www.youtube.com/rss/user/%s/videos.rss", request.getParameter("id"));
  }

}
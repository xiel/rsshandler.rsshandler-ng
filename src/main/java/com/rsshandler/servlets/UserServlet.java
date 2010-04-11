package com.rsshandler.servlets;

import javax.servlet.http.HttpServletRequest;

public class UserServlet extends RssServlet {

	@Override
  protected String getRssUrl(HttpServletRequest request) {
	  return String.format("http://gdata.youtube.com/feeds/base/users/%s/uploads", request.getParameter("id"));
  }

}
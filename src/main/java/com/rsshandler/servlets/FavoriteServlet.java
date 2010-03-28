package com.rsshandler.servlets;

import javax.servlet.http.HttpServletRequest;

public class FavoriteServlet extends RssServlet {

	@Override
  protected String getRssUrl(HttpServletRequest request) {
		return String.format("http://gdata.youtube.com/feeds/api/users/%s/favorites?v=2&alt=rss", request.getParameter("id"));
  }
}
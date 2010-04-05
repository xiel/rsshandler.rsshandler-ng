package com.rsshandler.servlets;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VideoServlet extends HttpServlet {
	Logger logger = Logger.getLogger(this.getClass().getName());
	private boolean proxy;
	
	public VideoServlet(boolean proxy) {
		this.proxy = proxy;
	}

  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
  	CookieManager cookieManager = new CookieManager();
  	CookieHandler.setDefault(cookieManager);
  	cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_NONE);
  	List<String> headers = Collections.list(request.getHeaderNames());
  	for (String name : headers) {
  		logger.info(String.format("Header: %s, %s", name, request.getHeader(name)));
  	}
  	String id = request.getParameter("id");
		int format = Integer.parseInt(request.getParameter("format"));
		logger.info(String.format("Video: %s, %s", id, format));
		URL url = new URL(String.format("http://www.youtube.com/watch?v=%s&fmt=%s", id, format));
    URLConnection connection = url.openConnection();
    logger.info(String.format("Request properties: %s", connection.getRequestProperties()));
    String str = Utils.readString(connection.getInputStream());
    logger.info(String.format("Headers: %s", connection.getHeaderFields()));
    String redirect = getVideoLink2(str);
    if (proxy) {
      logger.info(String.format("Proxy: %s", redirect));
    	proxyVideo(redirect, response);
    } else {
      logger.info(String.format("Redirect: %s", redirect));
    	response.sendRedirect(redirect);
    }
  }
  
  private void proxyVideo(String redirect, HttpServletResponse response) throws IOException {
		URL url = new URL(redirect);
		URLConnection connection = url.openConnection();
		InputStream is = connection.getInputStream();
		OutputStream os = response.getOutputStream();
    byte arr[] = new byte[4096];
    int len = -1;
    int total = 0;
    while ((len = is.read(arr)) != -1) {
      os.write(arr, 0, len);
      total += len;
    }
    logger.info(String.format("Sent: %s bytes", total));
  }

  private String getVideoLink2(String content) throws UnsupportedEncodingException {
	  Pattern pattern = Pattern.compile("%7C(http.+?videoplayback.+?)%2C");
	  Matcher matcher = pattern.matcher(content);
	  if (matcher.find()) {
      String url = matcher.group(1);
      return URLDecoder.decode(url, "UTF-8");
	  }
	  throw new IllegalStateException("Cann't find video file address: "+ content);
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
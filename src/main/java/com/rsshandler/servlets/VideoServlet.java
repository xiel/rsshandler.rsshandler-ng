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
import java.util.TreeMap;


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
	URL url = new URL(String.format("http://www.youtube.com/watch?v=%s", id));
    URLConnection connection = url.openConnection();
    logger.info(String.format("Request properties: %s", connection.getRequestProperties()));
    String str = Utils.readString(connection.getInputStream());
    logger.info(String.format("Headers: %s", connection.getHeaderFields()));
	
    int fallback = Integer.parseInt(request.getParameter("fb"));
    logger.info(String.format("Fallback behavior: %s", fallback));
    String redirect = getVideoLink2(str,format,fallback);
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
  
  private String getVideoLink2(String content, int fmt, int fb) throws UnsupportedEncodingException {
	  Pattern pattern = Pattern.compile("%7C(http.+?videoplayback.+?)%2C");
	  Matcher matcher = pattern.matcher(content);
	  String format = "%26itag%3D" + fmt + "%26";
	  String url = null;
	  
	  /*
	  * https://secure.wikimedia.org/wikipedia/en/wiki/YouTube#Quality_and_codecs
 	  * 5 = 400x240 @ FLV; 34 = 640x360 @ FLV; 35 = 854x480 @FLV;
	  * 18 = 480x270 @ MP4; 22 = 1280x720 @ MP4; 37 = 1920x1080 @MP4
	  */
	  url = findLink(matcher, format);

	  if (url == null && fb == 1) {
		TreeMap fmts = new TreeMap();
		fmts.put(5, new VideoFormat(5,1,0));
		fmts.put(34, new VideoFormat(34,1,5));
		fmts.put(35, new VideoFormat(35,1,34));
		fmts.put(18, new VideoFormat(18,2,0));
		fmts.put(22, new VideoFormat(22,2,18));
		fmts.put(37, new VideoFormat(37,2,22));
		 
	  	logger.info("getVideoLink2: Did not find preferred format.  checking for fallback");
		Object vf = fmts.get(fmt);
		
		while (vf != null) {
			VideoFormat v = (VideoFormat) vf;
			
			// need to reset positional state to ensure a valid search for fallback check.
			matcher.reset();

			int srchformat = v.getFallback();
			
			logger.info("Preferred format " + fmt + 
				" not found for video.  Searching for format " + srchformat);
			format = "%26itag%3D" + srchformat + "%26";
			
			logger.info("getVideoLink2:" + format);
			url = findLink(matcher,format);
			if (url != null) {
				break;
			}
			
			vf = fmts.get(srchformat);
		}
	  }
	  
	  if (url != null) {
		return URLDecoder.decode(url, "UTF-8");
	  }
	  
	  throw new IllegalStateException("Can't find video file address");
  }
 
  private String findLink(Matcher matcher, String format) {
	  String url = null;

	  while (matcher.find()) {
		String u = matcher.group(1);
		logger.info("Found test url: " + u);
		
		if (u.indexOf(format) > -1) {
			logger.info("findLink: match (" + u + ") for format " + format);
			url = u;
			break;
		}
	  }

	  return url;
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
  
  
  public class VideoFormat {
	  private int resid    = 0;  // resolution id
	  private int type     = 0;  // We only support FLV & MP4 currently  
	  private int fallback = 0;  // the id for the fallback resolution
	
	  public VideoFormat(int r, int t, int f) {
		  resid = r;
		  type = t;
		  fallback = f;
	  }
	
	  public int getFallback() {
		  return fallback;
	  }
	
	  public int getResId() {
		  return resid;
	  }
	
	  public int getType() {
		  return type;
	  }
  }
}
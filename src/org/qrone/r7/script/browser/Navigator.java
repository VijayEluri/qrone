package org.qrone.r7.script.browser;

import javax.servlet.http.HttpServletRequest;

public class Navigator{
	
	public Navigator(HttpServletRequest request){
		userAgent = request.getHeader("UserAgent");
	}
	
	public String userAgent;
}

package org.qrone.r7.handler;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.qrone.r7.resolver.URIResolver;

public class CascadeHandler implements URIHandler{
	private List<URIHandler> list = new ArrayList<URIHandler>();

	public void add(URIHandler r){
		list.add(r);
	}
	
	public void add(int index, URIHandler r){
		list.add(index, r);
	}

	@Override
	public boolean handle(HttpServletRequest request, HttpServletResponse response) {
		for (Iterator<URIHandler> i = list.iterator(); i.hasNext();) {
			URIHandler r = i.next();
			if(r.handle(request,response)){
				return true;
			}
		}
		return false;
	}
}
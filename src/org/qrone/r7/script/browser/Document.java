package org.qrone.r7.script.browser;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.arnx.jsonic.JSON;

import org.qrone.r7.parser.HTML5Deck;
import org.qrone.r7.parser.HTML5Template;
import org.qrone.r7.script.ServletScope;

public class Document extends HTML5Template{
	private HttpServletRequest request;
	private PrintWriter writer;
	
	public Document(HttpServletRequest request, HttpServletResponse response, HTML5Deck deck, String uri) throws IOException{
		super(deck, uri);
		this.request = request;
		this.writer = response.getWriter();
	}
	
	public String getCookie(){
		return request.getHeader("Cookie");
	}

	public void out() {
		super.out();
		writer.append(toString());
	}
	
	public void write(Object out) throws IOException{
		if(out instanceof String)
			writer.append((String)out);
		else
			writer.append(JSON.encode(out));
	}
	
	public void flush() throws IOException{
		writer.flush();
	}
	
	public void close() throws IOException{
		writer.close();
	}
}

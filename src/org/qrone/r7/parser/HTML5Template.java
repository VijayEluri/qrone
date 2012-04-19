package org.qrone.r7.parser;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.arnx.jsonic.JSON;

import org.qrone.util.QrONEUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import se.fishtank.css.selectors.NodeSelectorException;
import se.fishtank.css.selectors.dom.DOMNodeSelector;

public class HTML5Template{
	
	//private List<Object> list = new ArrayList<Object>();
	//private StringBuilder b = new StringBuilder(10240);
	
	protected HTML5OM om;
	protected Set<HTML5OM> xomlist;
	protected URI uri;
	protected boolean loaded = false;
	protected String id;
	protected String ticket;
	
	protected HTML5Template(HTML5OM om, Set<HTML5OM> xomlist, URI uri, String ticket){
		this.uri = uri;
		this.om = om;
		this.xomlist = xomlist;
		this.ticket = ticket;
		loaded = true;
	}

	public HTML5Template(HTML5OM om, URI uri, String ticket){
		this(om, new HashSet<HTML5OM>(), uri, ticket);
		id = QrONEUtils.uniqueid();
	}
	
	public HTML5Template(HTML5OM om){
		this(om, new HashSet<HTML5OM>(), null, null);
	}
	
	public HTML5Template(HTML5Deck deck, String path, String ticket) throws IOException{
		this(null, new HashSet<HTML5OM>(), null, ticket);
		if(deck.getResolver().exist(path)){
			try {
				uri = new URI(path);
				om = deck.compile(uri);
				loaded = true;
			} catch (URISyntaxException e) {
			}
		}
	}
	
	public void load(String path) throws IOException, URISyntaxException{
		URI u = uri.resolve(path);
		if(om.getDeck().getResolver().exist(u.toString())){
			om = om.getDeck().compile(u);
		}
	}
	
	public boolean isLoaded(){
		return loaded;
	}

	/*
	public HTML5Template append(){
		HTML5Template t = new HTML5Template(om, xomlist, uri, ticket);
		list.add(t);
		b = new StringBuilder();
		list.add(b);
		return t;
	}

	public void append(String key, String value){
		if(value != null)
			b.append(value);
	}
	
	public void append(HTML5Template t){
		list.add(t);
		b = new StringBuilder();
		list.add(b);
	}
	
	public void append(char str){
		b.append(String.valueOf(str));
	}

	public void append(CharSequence str){
		b.append(str);
	}
	
	public void append(String str){
		b.append(str);
	}
	*/
	/*s
	public String serialize() {
		StringBuffer b = new StringBuffer();
		for (Iterator<Object> i = list.iterator(); i
				.hasNext();) {
			b.append(i.next().toString());
		}
		return b.toString();
	}
	*/
	public Object $(String selector){
		return select(selector);
	}

	public Object $(String selector, HTML5Node node){
		return select(selector, node);
	}
	
	public HTML5NodeSet select(String selector){
		return new HTML5NodeSet(this, om.select(selector));
	}

	public HTML5NodeSet select(String selector, HTML5Node node){
		if( node.getDocument() != this ){
			return node.getDocument().select(selector, node);
		}
		return new HTML5NodeSet(this, om.select(selector, node.get(false)));
	}

	private Map<Element, HTML5Element> node5map = new Hashtable<Element, HTML5Element>();
	public HTML5Element override(Element node){
		HTML5Element e = node5map.get(node);
		if(e == null){
			e = new HTML5Element(om, this, node);
			node5map.put(node, e);
		}
		return e;
	}
	
	public HTML5Element get(Element node) {
		return node5map.get(node);
	}

	public HTML5Node getElementsByTagName(String tagName){
		return select(tagName);
	}
	
	public HTML5Node getElementsByClassName(String clsName){
		return select("." + clsName);
	}
	
	public HTML5Node getElementById(String id){
		return select("#" + id);
	}
	
	/*
	private void set(Element node, String html){
		HTML5Element e = node5map.get(node);
		if(e == null){
			e = new HTML5Element(om, node);
			node5map.put(node, e);
		}
		e.html(html);
	}
	
	private void set(Element node, NodeLister lister){
		HTML5Element e = node5map.get(node);
		if(e == null){
			e = new HTML5Element(om, node);
			node5map.put(node, e);
		}
		e.list(lister);
	}
	*/
	
/*
	public void set(Object o){
		if(o instanceof List){
			for(Object i : (List)o){
				set(i);
			}
		}else if(o instanceof Map){
			for (Object i : ((Map)o).entrySet()) {
				set(i);
			}
		}else if(o instanceof Entry){
			Entry e = (Entry)o;
			set("#" + e.getKey(), e.getValue());
		}
	}

	public void set(final String selector, final List o){
		set(select(selector),o,null);
	}

	public void set(final HTML5Node node, final List o){
		set(node, o, null);
	}
	
	public void set(final HTML5Node node, final List o, final Function f){
		if(o instanceof Map){
			node.exec(new HTML5NodeSet.Delegate() {
				public void call(final HTML5Element e) {
					e.html(new Function() {
						@Override
						public Object call(Object... args) {
							HTML5Template t = new HTML5Template(om, xomlist, uri, ticket);
							Set<Entry> entryset = ((Map)o).entrySet();
							for (Entry el : entryset) {
								HTML5Template tt = new HTML5Template(om, xomlist, uri, ticket);
								if(f != null){
									((Function)o).call(tt);
								}else{
									tt.set(node.select(".key"), el.getKey());
									tt.set(node.select(".value"), el.getValue());
								}
								tt.visit(e);
								t.append(tt);
							}
							return t;
						}
					});
				}
			});
		}else if(o instanceof List){
			node.exec(new HTML5NodeSet.Delegate() {
				public void call(final HTML5Element e) {
					e.html(new Function() {

						@Override
						public Object call(Object... args) {
							HTML5Template t = new HTML5Template(om, xomlist, uri, ticket);
							for (Iterator iterator = ((List)o).iterator(); iterator
							.hasNext();) {
								HTML5Template tt = new HTML5Template(om, xomlist, uri, ticket);
								if(f != null){
									((Function)o).call(tt);
								}else{
									tt.set(iterator.next());
								}
								tt.visit(e);
								t.append(tt);
							}
							return t;
						}
					});
				}
			});
		}else{
			String str = o.toString();
			if(o instanceof Number && ((Number)o).doubleValue() == ((Number)o).intValue()){
				str = String.valueOf(((Number)o).intValue());
			}
			
			node.html(str);
		}
	}
	*/
	
/*
	public void set(String selector, NodeLister lister){
		select(selector).listup(lister);
	}
	*/
	/*
	private boolean initialized = false;
	private Map<String, NodeLister> selectmap = new Hashtable<String, NodeLister>();
	private Map<Element, NodeLister> nodemap;
	
	public void set(String selector, final String value){
		set(selector, new NodeLister() {
			@Override
			public void accept(HTML5Template t, HTML5Element e) {
				t.append(value);
			}
		});
	}
	
	*/

	/*
	public void visit(HTML5Element e){
		e.getOM().process(this, this, e.get(), null, xomlist, ticket);
	}
	
	private Map<String, Iterator<Node>> selecting
		= new Hashtable<String, Iterator<Node>>();
	public void out(String selector){
		if(selecting.containsKey(selector)){
			Iterator<Node> iter = selecting.get(selector);
			if(iter != null){
				if(!iter.hasNext())
					iter = om.select(selector).iterator();
				om.process(this, iter.next(), null, xomlist, ticket);
			}
		}else{
			Set<Node> nodes = om.select(selector);
			if(nodes != null && !nodes.isEmpty()){
				Iterator<Node> iter = nodes.iterator();
				selecting.put(selector, iter);
				om.process(this, iter.next(), null, xomlist, ticket);
			}else{
				selecting.put(selector, null);
			}
		}
	}
	*/
	/*
	public void out(HTML5NodeSet e) {
		e.exec(new HTML5NodeSet.Delegate() {
			@Override
			public void call(HTML5Element e) {
				visit(e);
			}
		});
	}
	
	public void out(HTML5Element e) {
		visit(e);
	}
	*/

	public void out(HTML5Writer w, HTML5NodeSet set) {
		//final String uniqueid = QrONEUtils.uniqueid();
		for (Iterator<Node> iter = set.get().iterator(); iter.hasNext();) {
			om.process(w, this, iter.next(), null, xomlist, ticket);
			
		}
		
	}
	
	public void out(HTML5Writer w, HTML5Element e) {
		//final String uniqueid = QrONEUtils.uniqueid();
		om.process(w, this, e.get(), null, xomlist, ticket);
	}
	
	public void out(HTML5Writer w, Document e) {
		//final String uniqueid = QrONEUtils.uniqueid();
		om.process(w, this, e, null, xomlist, ticket);
	}

	/*
	public void out(HTML5OM om, NodeProcessor p) {
		final String uniqueid = QrONEUtils.uniqueid();
		om.process(this, p, om.getBody(), uniqueid, xomlist);
	}
	
	public void out(HTML5OM om) {
		out(om, this);
	}
	
	public void out() {
		if(om != null)
			om.process(this, om.getDocument(), null, xomlist, ticket);
	}
	*/

	public HTML5Element getBody() {
		return new HTML5Element(om, this, om.getBody());
	}

	public URI getURI() {
		return uri;
	}

	/*
	public void write(Object out) throws IOException{
		if(out instanceof String)
			append((String)out);
		else
			append(JSON.encode(out));
	}

	public void writeln(Object out) throws IOException{
		write(out);
		write("\n");
	}
	*/
	
	public HTML5Template newTemplate() {
		HTML5Template t =  new HTML5Template(om, xomlist, uri, ticket);
		//t.id = id;
		//t.node5map = node5map;
		return t;
	}
	
}

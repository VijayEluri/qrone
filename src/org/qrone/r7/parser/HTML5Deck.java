package org.qrone.r7.parser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.qrone.img.ImageBufferService;
import org.qrone.parser.XDeck;
import org.qrone.r7.QrONEUtils;
import org.qrone.r7.resolver.FileResolver;
import org.qrone.r7.resolver.URIResolver;
import org.qrone.r7.tag.HTML5TagHandler;
import org.w3c.dom.Element;
import org.w3c.dom.css.CSSRuleList;
import org.xml.sax.SAXException;

public class HTML5Deck extends XDeck<HTML5OM> {
	private CSS3Deck cssdeck;
	private ImageSpriter spriter;
	private List<HTML5TagHandler> handlers = new ArrayList<HTML5TagHandler>();
	
	public HTML5Deck(File file, ImageBufferService service){
		this(new FileResolver(file), service);
	}
    
    public HTML5Deck(URIResolver resolver, ImageBufferService service){
    	super(resolver);
    	spriter = new ImageSpriter(resolver, service);
    	cssdeck = new CSS3Deck(resolver);
    
    }

    protected boolean updated(HTML5OM t, URI uri){
    	for (Iterator<CSS3OM> i = t.getStyleSheets().iterator(); i
				.hasNext();) {
			CSS3OM cssom = i.next();
			if(resolver.updated(cssom.getURI()))
				return true;
		}
    	return super.updated(t, uri);
    }
    
    public void update(URI uri){
    	try {
			spriter.update(uri);
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    
    public ImageSpriter getSpriter(){
    	return spriter;
    }
    
	public void addTagHandler(HTML5TagHandler h){
		handlers.add(h);
	}
	
	public List<HTML5TagHandler> getTagHandlers() {
		return handlers;
	}


	@Override
	public HTML5OM compile(URI uri, InputStream in, String encoding)
			throws Exception {
		HTML5OM xom = new HTML5OM(this, cssdeck, uri);
		xom.parse(resolver);
		return xom;
	}
	
	private Map<String, String> inlineJSMap = new Hashtable<String, String>();

	public void outputScripts(HTML5Writer b, HTML5Set set, URI file){

		//---------------
		// script src
		//---------------
		Set<String> hash = new HashSet<String>();
		for (Iterator<Element> i = set.jslibs.iterator(); i.hasNext();) {
			Element el = i.next();
			if(!hash.contains(el.getAttribute("src"))){
				hash.add(el.getAttribute("src"));
				if(!el.hasAttribute("inline")){
					b.append("<script src=\"");
					b.append(QrONEUtils.escape(el.getAttribute("src")));
					b.append("\"></script>");
				}
			}
		}

		//---------------
		// script inline
		//---------------
		StringBuffer js = new StringBuffer();
		hash = new HashSet<String>();
		for (Iterator<Element> i = set.jslibs.iterator(); i.hasNext();) {
			Element el = i.next();
			if(!hash.contains(el.getAttribute("src"))){
				hash.add(el.getAttribute("src"));
				if(el.hasAttribute("inline")){
					String key = el.getAttribute("src");
					String ijsc = inlineJSMap.get(key);
					if(ijsc == null){
						try {
							String ijs = JSParser.compress(
									QrONEUtils.convertStreamToString(
											resolver.getInputStream(
													file.resolve(key))));
							inlineJSMap.put(key, ijs);
							js.append(ijs);
						} catch (IOException e1) {
							e1.printStackTrace();
						}
					}else{
						js.append(ijsc);
					}
				}
			}
		}
		
		js.append(set.js.toString());
		if(js.length() > 0){
			b.append("<script>");
			b.append(js.toString());
			b.append("</script>");
		}
	}
	public void outputStyles(HTML5Writer b, HTML5Set set, URI file){

		//---------------
		// css href
		//---------------
		
		for (Iterator<Element> i = set.csslibs.iterator(); i.hasNext();) {
			Element el = i.next();
			b.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"");
			b.append(QrONEUtils.escape(el.getAttribute("href")));
			b.append("\" />");
		}

		//---------------
		// css inline
		//---------------
		StringBuffer css = new StringBuffer();
		for (Iterator<CSS3OM> j = set.css.iterator(); j.hasNext();) {
			CSSRuleList l = j.next().getStyleSheet().getCssRules();
			for (int i = 0; i < l.getLength(); i++) {
				css.append(l.item(i).getCssText());
			}
		}
		
		String css2 = CSS3Parser.compress(css.toString());
		if(css2.length() > 0){
			b.append("<style>");
			b.append(css2);
			b.append("</style>");
		}
	}
	
	public static class HTML5Set{
		public StringBuffer js = new StringBuffer();
		public List<CSS3OM> css = new ArrayList<CSS3OM>();
		public List<Element> jslibs = new ArrayList<Element>();
		public List<Element> csslibs = new ArrayList<Element>();
	}
}

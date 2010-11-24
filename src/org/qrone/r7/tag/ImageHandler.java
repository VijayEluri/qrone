package org.qrone.r7.tag;

import java.io.IOException;
import java.net.URI;

import org.qrone.r7.Extension;
import org.qrone.r7.parser.HTML5Deck;
import org.qrone.r7.parser.HTML5Element;
import org.qrone.util.QrONEUtils;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;

@Extension
public class ImageHandler implements HTML5TagHandler {
	private HTML5Deck deck;
	public ImageHandler(HTML5Deck deck) {
		this.deck = deck;
	}
	
	@Override
	public HTML5TagResult process(HTML5Element e) {
		spriteTag(e.getOM().getURI(), e.get());
		return null;
	}

	public void spriteTag(URI file, Element e){
		try {
			String src = e.getAttribute("src");
			URI uri = file.resolve(src);
			if(e.getNodeName().equals("img") 
					&& src != null
					&& deck.getResolver().exist(uri.toString())){
				
				String style = e.getAttribute("style");
				e.setAttribute("style", deck.getSpriter().addISprite(uri) + style);
				
				e.setAttribute("src", QrONEUtils.relativize(file,deck.getSpriter().addTransparentDot()).toString());
			}
		} catch (DOMException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
}
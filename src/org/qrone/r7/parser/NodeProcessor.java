package org.qrone.r7.parser;

import org.w3c.dom.Element;

public interface NodeProcessor {
	public boolean isTarget(Element node);
	public void processTarget(HTML5OM om, Element node);
}
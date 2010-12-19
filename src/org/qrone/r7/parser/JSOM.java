package org.qrone.r7.parser;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;

public class JSOM implements Comparable<JSOM>{
	private URI uri;
	private JSDeck deck;
	private Script script;

	public JSOM(JSDeck deck) {
		this.deck = deck;
	}
	
	public void parser(URI uri) throws IOException{
		InputStream in = deck.getResolver().getInputStream(uri);
		try{
			this.uri = uri;
			script = JSDeck.getContext().compileReader(new InputStreamReader(
					in, "utf8"), 
					uri.toString(), 0, null);
		}finally{
			in.close();
		}
	}
	
	public Object run(Scriptable scope){
		scope.put("window", scope, scope);
		return script.exec(JSDeck.getContext(), scope);
	}

	public Object run(Scriptable scope, Object... prototypes){
		Scriptable parent = scope;
		for (int i = 0; i < prototypes.length; i++) {
			Scriptable window = (Scriptable)Context.javaToJS(prototypes[i], scope);
			parent.setPrototype(window);
			parent = window;
		}
		return run(scope);
	}

	@Override
	public int compareTo(JSOM o) {
		return uri.compareTo(o.uri);
	}

}

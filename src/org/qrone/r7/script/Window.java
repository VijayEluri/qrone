package org.qrone.r7.script;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.EncoderException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.net.URLCodec;
import org.ho.yaml.Yaml;
import org.mozilla.javascript.Scriptable;
import org.qrone.r7.ObjectConverter;
import org.qrone.r7.QrONEUtils;
import org.qrone.r7.parser.JSOM;


public class Window extends JSObject{
	public PrintStream in = System.out;
	public PrintStream out = System.out;
	public Document document;
	public Location location;
	public Object query;
	public JSON JSON;
	
	public Window(ServletScope ss) throws IOException{
		super(ss);
		document = new Document(ss);
		
		location = new Location(ss);
		document.location = location;
		
		query = getQuery();
		document.query = query;
		document.location.query = query;
		JSON = new JSON(ss);
	}

	public String stringify(Object out){
		return ObjectConverter.stringify(out);
	}
	
	public Object getQuery(){
		Scriptable o = newScriptable();
		Map<String, String[]> map = ss.request.getParameterMap();
		for (Iterator<Entry<String, String[]>> i = map.entrySet().iterator(); i
				.hasNext();) {
			Entry<String, String[]> e = i.next();
			if(e.getValue().length == 1){
				o.put(e.getKey(), o, e.getValue()[0]);
			}else if(e.getValue().length > 1){
				Scriptable l = newScriptable();
				for (int j = 0; j < e.getValue().length; j++) {
					l.put(j, l, e.getValue()[j]);
				}
				o.put(e.getKey(), o, l);
			}
		}
		return o;
	}
	
	public void require_once(String path) throws IOException, URISyntaxException{
		JSOM om = ss.vm.compile(new URI(path));
		if(!ss.required.contains(om)){
			ss.required.add(om);
			om.run(ss.scope);
		}
	}
	
	public String load_file(String path) throws IOException, URISyntaxException{
		if(ss.resolver.exist(path)){
			return QrONEUtils.convertStreamToString(ss.resolver.getInputStream(new URI(path)));
		}
		return null;
	}

	public Object load_properties(String path) throws IOException, URISyntaxException{
		if(ss.resolver.exist(path)){
			InputStream in = ss.resolver.getInputStream(new URI(path));
			Properties p = new Properties();
			p.load(in);
			in.close();
			Map<Object, Object> map = new Hashtable<Object, Object>();
			for (Iterator<Entry<Object, Object>> i = p.entrySet().iterator(); i
					.hasNext();) {
				Entry<Object, Object> t = i.next();
				map.put(t.getKey(), t.getValue());
			}
			return map;
		}
		return null;
	}
	
	public Object load_yaml(String path) throws IOException, URISyntaxException{
		if(ss.resolver.exist(path)){
			InputStream in = ss.resolver.getInputStream(new URI(path));
			Object o = Yaml.load(in);
			in.close();
			return o;
		}
		return null;
	}
	
	public void require(String path) throws IOException, URISyntaxException{
		JSOM om = ss.vm.compile(new URI(path));
		if(!ss.required.contains(om)){
			ss.required.add(om);
		}
		om.run(ss.scope);
	}
	
	public byte[] base64_decode(String base64String){
		return Base64.decodeBase64(base64String);
	}
	
	public String base64_encode(byte[] binaryData){
		return Base64.encodeBase64String(binaryData);
	}

	public String unescape(String str) throws DecoderException{
		URLCodec c = new URLCodec();
		return c.decode(str);
	}
	
	public String escape(String str) throws EncoderException{
		URLCodec c = new URLCodec();
		return c.encode(str);
	}

	public String md2(String data){
		return digest_safe("MD2", data);
	}
	
	public String md5(String data){
		return digest_safe("MD5", data);
	}
	
	public String sha1(String data){
		return digest_safe("SHA-1", data);
	}

	public String sha256(String data){
		return digest_safe("SHA-256", data);
	}

	public String sha384(String data){
		return digest_safe("SHA-384", data);
	}
	
	public String sha512(String data){
		return digest_safe("SHA-512", data);
	}
	
	private String digest_safe(String type, String data){
		try {
			return digest(type, data);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return null;
	}

	public String digest(String type, String data) throws NoSuchAlgorithmException{
		MessageDigest md = MessageDigest.getInstance(type);
		byte[] digest = data.getBytes();
		md.update(digest);

		StringBuffer b = new StringBuffer();
		for (int i = 0; i < digest.length; i++) {
			int d = digest[i];
			if (d < 0) {
				d += 256;
			}
			if (d < 16) {
				b.append('0');
			}
			b.append(Integer.toString(d, 16));
		}
		return b.toString();
	}

}

package org.qrone.r7.handler;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mozilla.javascript.Scriptable;
import org.openid4java.OpenIDException;
import org.openid4java.consumer.ConsumerManager;
import org.openid4java.consumer.VerificationResult;
import org.openid4java.discovery.DiscoveryInformation;
import org.openid4java.discovery.Identifier;
import org.openid4java.message.AuthRequest;
import org.openid4java.message.AuthSuccess;
import org.openid4java.message.ParameterList;
import org.openid4java.message.ax.AxMessage;
import org.openid4java.message.ax.FetchRequest;
import org.openid4java.message.ax.FetchResponse;
import org.qrone.r7.QrONEUtils;
import org.qrone.r7.resolver.URIResolver;
import org.qrone.r7.script.browser.User;

public class OpenIDHandler implements URIHandler{
	private static ConsumerManager manager;
	//private URIResolver resolver;
	private User user;
	
	public OpenIDHandler(URIResolver resolver) {
		//this.resolver = resolver;
	}
	
	@Override
	public boolean handle(HttpServletRequest request, HttpServletResponse response) {
		String path = request.getPathInfo();
		if(path.equals("/openid/login")){
			LoginPack pack = (LoginPack)QrONEUtils.unpackQ64(request.getParameter("pack"));
			handleLogin(request, response, pack.url, pack.attributes);
		}else if(path.equals("/openid/verify")){
			handleVerify(request, response);
		}else if(path.equals("/openid/logout")){
			handleLogout(request, response);
		}
		
		Cookie cookie = QrONEUtils.getCookie(request.getCookies(), "Q");
		if(cookie != null){
			user = User.createUser(cookie.getValue());
		}
		return false;
	}

	public User getUser() {
		return user;
	}

	public String loginURL(HttpServletRequest req, HttpServletResponse res,
			String url, Scriptable attributes, String doneURL){
		LoginPack pack = new LoginPack();
		pack.url = url;
		if(attributes != null){
			pack.attributes = new HashMap<String, String>();
			Object[] ids = attributes.getIds();
			for (int i = 0; i < ids.length; i++) {
				if(ids[i] instanceof String){
					Object v = attributes.get((String)ids[i], attributes);
					if(v instanceof String){
						pack.attributes.put((String)ids[i], (String)v);
					}
				}
			}
		}
		return getBaseURL(req) + "/login?pack=" 
			+ QrONEUtils.packQ64(pack) + "&.done=" + QrONEUtils.escape(doneURL);
	}

	public String logoutURL(HttpServletRequest req, HttpServletResponse res,
			String doneURL){
		return getBaseURL(req) + "/logout?.done=" + QrONEUtils.escape(doneURL);
	}
	
	private String getBaseURL(HttpServletRequest req){
		return "http://" + req.getLocalName() + "/openid";
	}
	
	public boolean handleLogin(HttpServletRequest req, HttpServletResponse res,
			String url, Map<String, String> attributes){
		try
		{
			if(manager == null){
				manager = new ConsumerManager();
			}
			
			List discoveries = manager.discover(url);
			DiscoveryInformation discovered = manager.associate(discoveries);
			AuthRequest authReq = manager.authenticate(discovered, 
					getBaseURL(req) + "/verify?d=" + QrONEUtils.packQ64(discovered) 
					+ "&.done=" + QrONEUtils.escape(req.getParameter(".done")));
			FetchRequest fetch = FetchRequest.createFetchRequest();
			
			for (Iterator<Entry<String, String>> i = attributes.entrySet().iterator(); i
					.hasNext();) {
				Entry<String, String> e = i.next();
				fetch.addAttribute(e.getKey(), e.getValue(), true);
			}
			
			authReq.addExtension(fetch);
			res.sendRedirect(authReq.getDestinationUrl(true));
			return true;
		}catch (OpenIDException e){
		} catch (IOException e) {
		}
		return false;
	}

	public boolean handleLogout(HttpServletRequest req, HttpServletResponse res){
		Cookie c = new Cookie("Q", "");
		c.setMaxAge(0);
		res.addCookie(c);
		try {
			res.sendRedirect(req.getParameter(".done"));
			return true;
		} catch (IOException e) {}
		return false;
	}
	
    // --- processing the authentication response ---
    public boolean handleVerify(HttpServletRequest req, HttpServletResponse res)
    {
        try{
            ParameterList response =
                    new ParameterList(req.getParameterMap());
            DiscoveryInformation discovered = 
            	(DiscoveryInformation)QrONEUtils.unpackQ64(req.getParameter("d"));

            StringBuffer receivingURL = req.getRequestURL();
            String queryString = req.getQueryString();
            if (queryString != null && queryString.length() > 0)
                receivingURL.append("?").append(req.getQueryString());
            VerificationResult verification = manager.verify(
                    receivingURL.toString(),
                    response, discovered);
            Identifier verified = verification.getVerifiedId();
            if (verified != null)
            {
                AuthSuccess authSuccess =
                        (AuthSuccess) verification.getAuthResponse();
                
                String name = null;
                if (authSuccess.hasExtension(AxMessage.OPENID_NS_AX))
                {
                    FetchResponse fetchResp = (FetchResponse) authSuccess
                            .getExtension(AxMessage.OPENID_NS_AX);
                    
                    if(name == null)
                    	name = fetchResp.getAttributeValue("login");
                }
                
                User user = new User(name, verified.getIdentifier());
                Cookie qcookie = new Cookie("Q", user.getQCookie());
                res.addCookie(qcookie);
                res.sendRedirect(req.getParameter(".done"));
                return true;
            }
        }catch (OpenIDException e){
        } catch (IOException e) {
		}
        return false;
    }
    
    public static class LoginPack implements Serializable{
		private static final long serialVersionUID = 7001446077656573040L;
		public String url;
    	public String done;
    	public Map<String, String> attributes;
    }

}
package org.qrone.r7.app;

import java.io.File;
import java.net.UnknownHostException;

import javax.servlet.ServletContext;

import org.qrone.database.DatabaseService;
import org.qrone.img.ImageSpriteService;
import org.qrone.kvs.KeyValueStoreService;
import org.qrone.login.CookieHandler;
import org.qrone.mongo.MongoResolver;
import org.qrone.png.PNGMemoryImageService;
import org.qrone.r7.PortingService;
import org.qrone.r7.fetcher.HTTPFetcher;
import org.qrone.r7.github.GitHubRepositoryService;
import org.qrone.r7.github.GitHubResolver;
import org.qrone.r7.handler.DefaultHandler;
import org.qrone.r7.handler.ExtendableURIHandler;
import org.qrone.r7.resolver.FileResolver;
import org.qrone.r7.resolver.FilteredResolver;
import org.qrone.r7.resolver.InternalResourceResolver;
import org.qrone.r7.resolver.URIResolver;

import com.mongodb.Mongo;
import com.mongodb.MongoException;

public class QrONEURIHandler extends ExtendableURIHandler {
	private GitHubResolver github;
	private GitHubRepositoryService repository;
	private MongoResolver cache;
	
	public QrONEURIHandler( ServletContext cx, PortingService service ){
		try {
			KeyValueStoreService kvs = service.getKeyValueStoreService();
			HTTPFetcher fetcher = service.getURLFetcher();
			cache = new MongoResolver(new Mongo().getDB("qrone"), "qrone.cache");
			DatabaseService db = service.getDatabaseService();
			service.setURIResolver(resolver);
	
			// Local Files overrides at anytime.
			resolver.add(new FileResolver(new File("./htdocs/"), true));
			
			// Login/Crumb Service
			CookieHandler cookie = new CookieHandler(kvs);
			
			service.setSecurityService(new CookieHandler(service.getKeyValueStoreService()));
			handler.add(cookie);
			
			// Scale9 Service
			PNGMemoryImageService imagebufferservice = new PNGMemoryImageService();
			ImageSpriteService imagespriteservice = new ImageSpriteService(resolver, cache, imagebufferservice);
			
			service.setImageBufferService(imagebufferservice);
			service.setImageSpriteService(imagespriteservice);
			resolver.add(imagespriteservice);
			
			// Github Admintool
			github = new GitHubResolver(fetcher, cache, 
					"qronon","qrone-admintool","master");
			repository = new GitHubRepositoryService(fetcher, cache, db);
			service.setRepositoryService(repository);
			
			resolver.add(github);
			resolver.add(repository.getResolver());
			resolver.add(new FilteredResolver("/system/resource/", new InternalResourceResolver(cx)));
			resolver.add(service.getFileSystemService());
			
			
			handler.add(github);
			handler.add(repository);
			
			// DefaultHandler
			DefaultHandler defaulthandler = new DefaultHandler(service);
			rawextend(defaulthandler);
			rawextend(this);
			handler.add(defaulthandler);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MongoException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void clean(){
		cache.drop();
	}
}
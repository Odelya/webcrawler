package com.inteview.lightricks;

import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;


/**
 * Create a simple web crawler java.
 * Requirements:
 * 1. Support resume
 * 2. Support no-redownload in case of no changes
 * 3. Support depth 
 * 4. Save to TLS
 * 5. NOTE: doesn't support multi-threading
 * @author Odelya Holiday
 *
 * 
 */
@SpringBootApplication
public class App {
	
	public static void main(String[] args) {
		if (args == null || args.length < 2)
			throw new RuntimeException("Must insert root url and depth");
		String pageRoot = args[0];
		String depth = args[1];
		if (!StringUtils.isNumeric(depth)) {
			throw new RuntimeException("Depth should be numeric");
		}
		ApplicationContext ctx = SpringApplication.run(App.class, args);
		CrawlerManager cralwerManager = ctx.getBean(CrawlerManager.class);
		cralwerManager.start(pageRoot, Integer.parseInt(depth));

	}

}

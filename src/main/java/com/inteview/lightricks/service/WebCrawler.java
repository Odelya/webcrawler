/**
 * 
 */
package com.inteview.lightricks.service;

import com.inteview.lightricks.model.WebURL;

/**
 * @author Odelya Holiday
 *
 *        
 */
public interface WebCrawler {

	public WebURL preProcessPage(WebURL url);

	public WebURL processPage(WebURL url);

	public boolean isValidPage(String url);

	public WebURL postProcessPage(WebURL url);
	 
}

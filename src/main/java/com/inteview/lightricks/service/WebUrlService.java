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
public interface WebUrlService {
	
	Iterable<WebURL> findPagesInProcess();
	
	Iterable<WebURL> findAllPages();
	
}

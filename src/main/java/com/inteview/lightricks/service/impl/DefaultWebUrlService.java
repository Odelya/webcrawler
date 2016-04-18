/**
 * 
 */
package com.inteview.lightricks.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.inteview.lightricks.model.WebURL;
import com.inteview.lightricks.repository.WebUrlRepository;
import com.inteview.lightricks.service.WebUrlService;

/**
 * @author Odelya Holiday
 *
 */
@Service
public class DefaultWebUrlService implements WebUrlService {
	
	@Autowired
	WebUrlRepository repository;

	public Iterable<WebURL> findPagesInProcess() {
		return repository.findPagesInProgress();
	}

	public Iterable<WebURL> findAllPages() {
		return repository.findAll();
	}

}

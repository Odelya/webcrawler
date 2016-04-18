/**
 * 
 */
package com.inteview.lightricks.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.inteview.lightricks.model.WebURL;

/**
 * @author Odelya Holiday
 *
 *         
 */
public interface WebUrlRepository extends JpaRepository<WebURL, String> {

	@Query("select w from WebURL w where w.process = true")
	List<WebURL> findPagesInProgress();
	
}

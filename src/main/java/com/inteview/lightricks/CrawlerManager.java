/**
 * 
 */
package com.inteview.lightricks;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.inteview.lightricks.model.WebURL;
import com.inteview.lightricks.service.WebCrawler;
import com.inteview.lightricks.service.WebUrlService;
import com.univocity.parsers.tsv.TsvWriter;
import com.univocity.parsers.tsv.TsvWriterSettings;

/**
 * @author Odelya Holiday
 *  This handles the cralwer. 
 *  
 */
@Component
public class CrawlerManager {

	private static final String RATIO_HEADER_NAME = "ratio";

	private static final String DEPTH_HEADER_NAME = "depth";

	private static final String URL_HEADER_NAME = "url";

	private static final String RESULT_TSV_NAME = "/result.tsv";

	@Value("${mainpath}")
	private String userFilePath;

	Queue<WebURL> pagesToCrawl = new LinkedList<WebURL>();

	@Autowired
	WebUrlService webUrlService;

	@Autowired
	WebCrawler webCrawler;

	public void start(String pageRoot, int deep) {

		Collection<WebURL> pagesInProcess = (Collection<WebURL>) webUrlService.findPagesInProcess();
		if (CollectionUtils.isEmpty(pagesInProcess))
			if (webCrawler.isValidPage(pageRoot)) {
				pagesToCrawl.add(new WebURL(pageRoot, 0));
			} else {
				System.out.println("invalid root page. No pages to crawl");
				return;
			}
		else {
			pagesToCrawl.addAll(pagesInProcess);
		}
		
		while (!pagesToCrawl.isEmpty()) {
			
			WebURL currentUrl = pagesToCrawl.remove();

			currentUrl = webCrawler.processPage(currentUrl);
			
			addCurrentUrlLinksToCrawl(deep, currentUrl);

			currentUrl = webCrawler.postProcessPage(currentUrl);

		}

		writeTSVFile();

	}

	private void addCurrentUrlLinksToCrawl(int deep, WebURL currentUrl) {
		if (currentUrl.getDepth() < deep && !CollectionUtils.isEmpty(currentUrl.getLinks())) {
			for (String url : currentUrl.getLinks()) {
				if (webCrawler.isValidPage(url)){
					WebURL newPageToCrawl = new WebURL(url, currentUrl.getDepth() + 1);
					
					newPageToCrawl = webCrawler.preProcessPage(newPageToCrawl);
					
					pagesToCrawl.add(newPageToCrawl);
				}
					
			}
		}
	}

	/**
	 * once finished, save to main directory the TLS file
	 * // assuming that all the pages are required to be as TLS. otherwise
	 *	// needs to save the pages only from current run
	 *	// TODO not clear from task
	 */
	private void writeTSVFile() {
		List<WebURL> webUrlPages = (List<WebURL>) webUrlService.findAllPages();

		FileOutputStream outputWriter;
		try {
			outputWriter = new FileOutputStream(userFilePath + RESULT_TSV_NAME);

			TsvWriter writer = new TsvWriter(outputWriter, new TsvWriterSettings());

			writer.writeHeaders(URL_HEADER_NAME, DEPTH_HEADER_NAME, RATIO_HEADER_NAME);

			for (WebURL webURL : webUrlPages) {
				writer.writeRow(webURL.toArrayOuput());
			}
			writer.close();
		} catch (FileNotFoundException e) {
			// TODO handle exception
			e.printStackTrace();
		}
	}

}

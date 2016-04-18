/**
 * 
 */
package com.inteview.lightricks.service.impl;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.inteview.lightricks.model.WebURL;
import com.inteview.lightricks.repository.WebUrlRepository;
import com.inteview.lightricks.service.WebCrawler;

/**
 * @author Odelya Holiday
 *
 * Implementation of HTML web page crawler        
 */
@Component
public class HtmlWebCrawler implements WebCrawler {

	private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.1 (KHTML, like Gecko) Chrome/13.0.782.112 Safari/535.1";

	private static final int MAX_REDIRECT = 3;

	private static final SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz");

	@Autowired
	WebUrlRepository repository;

	@Value("${mainpath}")
	private String userFilePath;

	/**
	 * Saves the page to db. if it exists,it updates the existing one with last saved date to prevent download if there were no changes
	 */
	public WebURL preProcessPage(WebURL webUrl) {
		WebURL cachedWebUrl = repository.findOne(webUrl.getUrl());
		if (cachedWebUrl != null) {
			webUrl.setUpdated(cachedWebUrl.getUpdated());
		}
		webUrl.setProcess(true);
		repository.save(webUrl);
		return webUrl;
	}

	/**
	 * If there are changes from last time that the page was downloaded, it will download to disc and calculate ratio between same domain and others in page
	 */
	public WebURL processPage(WebURL webUrl) {
		String url = webUrl.getUrl();

		try {
			Connection.Response connection = Jsoup.connect(url).followRedirects(true).userAgent(USER_AGENT).execute();
			Date lastModifiedDate = format.parse(connection.header("Last-Modified"));
			Calendar c = Calendar.getInstance();
			c.setTime(lastModifiedDate);
			Long lastModifiedMilis = c.getTimeInMillis();

			if (shouldReDownloadContent(lastModifiedMilis, webUrl)) {

				webUrl.setUpdated(lastModifiedMilis);

				Document htmlDocument = connection.parse();

				downloadToDisc(url, htmlDocument);

				webUrl = extractLinksCalculateRatio(webUrl, htmlDocument);
			}

		} catch (Exception e) {
			System.out.println("error while trying to reach page"); // TODO
																	// handle
																	// exceptions
		}

		return webUrl;
	}

	private boolean shouldReDownloadContent(Long currentUrlLastModified, WebURL cachedWebUrl) throws ParseException {

		Long cachedWebUrlLastModified = cachedWebUrl.getUpdated();
		return cachedWebUrlLastModified == null
				|| cachedWebUrlLastModified != null && cachedWebUrlLastModified.longValue() != currentUrlLastModified;
	}

	private void downloadToDisc(String url, Document htmlDocument) throws IOException {
		String filePath = getFilePath(url);

		Path path = Paths.get(filePath);

		if (!Files.exists(path)) {
			Files.createDirectories(path);
		}
		filePath += url.replaceAll("[^a-zA-Z0-9.-]", "_");
		filePath += ".html";
		path = Paths.get(filePath);
		Files.write(path, htmlDocument.html().getBytes());
	}

	/**
	 * Same domain means www.test.com = www.test.com/hello.html, but sub.test.com != www.test.com
	 * @param url
	 * @param htmlDocument
	 * @return
	 * @throws MalformedURLException
	 */
	private WebURL extractLinksCalculateRatio(WebURL url, Document htmlDocument) throws MalformedURLException {
		String processedPageHost = new URL(url.getUrl()).getHost();
		Elements linksOnPage = htmlDocument.select("a[href]");
		float sameDomain = 0f;
		for (Element link : linksOnPage) {
			String linkUrl = link.absUrl("href");
			if (new URL(linkUrl).getHost().equals(processedPageHost)) {
				sameDomain++;
			}
			url.getLinks().add(linkUrl);
		}
		url.setRatio((float) sameDomain / linksOnPage.size());
		return url;
	}

	/**
	 * Crawl only HTTP site
	 */
	public boolean isValidPage(String url) {
		return getContentType(url, 0).contains("text/html");
	}

	public WebURL postProcessPage(WebURL webUrl) {
		webUrl.setProcess(false);
		repository.save(webUrl);
		return webUrl;
	}

	/**
	 * we will crawl only text/html pages
	 * @param urlString
	 * @param maxLoop
	 * @return
	 */
	private String getContentType(String urlString, int maxLoop) {
		if (maxLoop == MAX_REDIRECT)
			return "";
		URL url = null;
		HttpURLConnection connection = null;
		try {
			url = new URL(urlString);
			connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("HEAD");
			if (isRedirect(connection.getResponseCode())) {
				String newUrl = connection.getHeaderField("Location");
				return getContentType(newUrl, ++maxLoop);
			}
		} catch (IOException e) {
			System.out.println("Error"); // TODO handle exceptions
		}
		String contentType = connection.getContentType();
		return contentType;
	}

	/**
	 * Check status code for redirects
	 * 
	 * @param statusCode
	 * @return true if matched redirect group
	 */
	private boolean isRedirect(int statusCode) {
		if (statusCode == HttpURLConnection.HTTP_MOVED_TEMP || statusCode == HttpURLConnection.HTTP_MOVED_PERM
				|| statusCode == HttpURLConnection.HTTP_SEE_OTHER) {
			return true;
		}
		return false;
	}

	/**
	 * directory path of the following example; www.wikipedia.com
	 * com/wikipedia/www
	 * @param url
	 * @return
	 * @throws MalformedURLException
	 */
	private String getFilePath(String url) throws MalformedURLException {
		String[] folders = new URL(url).getHost().split("\\.");
		String folderPath = new String(userFilePath);
		for (int i = folders.length - 1; i >= 0; i--) {
			folderPath += folders[i];

			folderPath += "/";
		}
		return folderPath;
	}
}

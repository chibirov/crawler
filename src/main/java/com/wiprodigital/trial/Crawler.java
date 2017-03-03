package com.wiprodigital.trial;

import static java.util.concurrent.TimeUnit.MINUTES;
import static com.fasterxml.jackson.databind.SerializationFeature.INDENT_OUTPUT;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.HttpClientBuilder;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wiprodigital.trial.exception.InvalidUrlException;
import com.wiprodigital.trial.exception.UnknowHostException;
import com.wiprodigital.trial.model.TNode;

public class Crawler {
	
	private static final String USER_AGENT = "Mozilla/5.0 (compatible; chibirovbot/2.0; +http://www.bing.com/bingbot.htm)";

	private Log log = LogFactory.getLog(getClass());
	
	private static final String URL_REGEX = "(https?:\\/\\/(?:www\\.|(?!www))[^\\s\\.]+\\.[^\\s]{2,}|www\\.[^\\s]+\\.[^\\s]{2,})";
	
	private static final int TIMEOUT_IN_MINUTES = 1;
	
	private static final int MAX_ATTEMPTS = 30;	

	private Set<String> visited = new HashSet<String>();
	
	private Set<String> allow = new HashSet<String>();
	
	private Set<String> disallow = new HashSet<String>();
	
	private static Set<String> specialChars = new HashSet<String>() {{
		add("\""); add("<"); add("'");
		add("\\?"); add("#"); add("\\)");
	}};
	
	private String normalize(String link) throws UnsupportedEncodingException {
		for (String special : specialChars)
			link = link.split(special)[0];
		if (link.contains("%")) link = URLDecoder.decode(link, "UTF-8");
		return link;
	}
	
	public Crawler(String startUrl) {
		if (startUrl != null && !startUrl.isEmpty()) {
			try {
				String robots = getData(startUrl + (startUrl.endsWith("/") ? "" : "/") + "robots.txt", 0);
				if (robots != null) {
					for (String line : robots.split(System.getProperty("line.separator"))) {
						if (line.contains(":")) {
							String value = line.split(":")[1].trim();
							if (line.startsWith("User-agent") && !(value.contains("*") || value.contains("chibirovbot"))) break; 
							else if (line.startsWith("Allow")) allow.add(value);
							else disallow.add(value);
						}
					}
				}
			} catch (ClientProtocolException e) {
				throw new InvalidUrlException(e.getMessage(), e);
			} catch (IOException e) {
				throw new UnknowHostException(e.getMessage(), e);
			}
		} else {
			throw new RuntimeException("Required parameter is missing");
		}
	}
	
	public String getData(String url, int attempt) throws ClientProtocolException, IOException {
		HttpClient client = HttpClientBuilder.create().build();
		HttpUriRequest request=new HttpGet(url);
	    request.addHeader(HttpHeaders.USER_AGENT, USER_AGENT);
	    HttpResponse response = client.execute(request);
	    if (response.getStatusLine().getStatusCode() == 200) {
	    	attempt = 0;
		    HttpEntity entitiy = response.getEntity();
		    String content = new String(IOUtils.toByteArray(entitiy.getContent()));
			return content;
	    } else if (response.getStatusLine().getStatusCode() == 429) {
			try {
				if (attempt <= MAX_ATTEMPTS) {
					log.debug(url + " inaccessible, retrying in " + TIMEOUT_IN_MINUTES + " minutes");
					attempt++;
					MINUTES.sleep(TIMEOUT_IN_MINUTES);						
					getData(url, attempt);
				}
			} catch (InterruptedException e) {
				log.fatal(e.getMessage(), e);
			}
	    }
	    return null;
	}
	
	public void crawl(TNode node) throws IOException {
		Pattern p = Pattern.compile(URL_REGEX);
		String content = getData(node.getUrl(), 0);
		if (content != null) {
			Matcher m = p.matcher(content);
			while (m.find()) {
				String link = normalize(m.group());
				URL url = new URL(node.getUrl());
				TNode child = new TNode(link.startsWith("http") ? link : url.getProtocol() + "://" + link);
				if (!(visited.contains(child.getUrl()) || visited.contains(child.getUrl() + "/"))) {
					node.addChild(child);
					visited.add(child.getUrl());
					if (link.contains(url.getHost()))
						crawl(child);
				}
			}
		}
	}
	
	public static void main(String[] args) throws IOException, URISyntaxException {
		String startUrl = args.length == 0 ? "http://wiprodigital.com/" : args[0];
		Crawler crawler = new Crawler(startUrl);
		TNode node = new TNode(startUrl);
		crawler.crawl(node);
		ObjectMapper mapper = new ObjectMapper();
		mapper.enable(INDENT_OUTPUT);
		mapper.setSerializationInclusion(Include.NON_NULL);
		File f = new File(new File(Crawler.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).getParentFile().getPath() + startUrl.split("/")[2] + ".json");
		OutputStream out = new FileOutputStream(f);
		mapper.writeValue(out, node);
		out.close();
	}
}
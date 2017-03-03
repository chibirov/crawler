package com.wiprodigital.trial.test;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.Assert.*;

import java.io.IOException;
import java.io.StringWriter;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.wiprodigital.trial.Crawler;
import com.wiprodigital.trial.exception.InvalidUrlException;
import com.wiprodigital.trial.exception.UnknowHostException;
import com.wiprodigital.trial.model.TNode;

public class CrawlerTest {
	
    WireMockServer wireMockServer = null;
    WireMock wireMock = null;
	
	private Crawler crawler;
	
    @After
    public void tearDown() {
    	 wireMockServer.stop();
    }
	
    @Before
    public void setup() {
    	 wireMockServer = new WireMockServer(wireMockConfig().port(8888));
    	 wireMockServer.start();
    	 wireMock = new WireMock("localhost", wireMockServer.port());
    }
	
	@Test(expected = InvalidUrlException.class) 
	public void testInvalUrl() {
		crawler = new Crawler("test");
	}
	
	@Test(expected = UnknowHostException.class) 
	public void testNonexistingUrl() {
		crawler = new Crawler("http://www.123123.test");
	}
	
	@Test(expected = RuntimeException.class) 
	public void testNull() {
		crawler = new Crawler(null);
	}
	
	@Test 
	public void test() throws IOException {
	    wireMock.register(get(urlEqualTo("/users/"))
	            .willReturn(aResponse().withStatus(200).withBody("Test")
	    ));
	    crawler = new Crawler("http://localhost:8888/users/");
	    TNode node = new TNode("http://localhost:8888/users/");
	    crawler.crawl(node);
	    StringWriter sw = new StringWriter();
		new ObjectMapper().writeValue(sw, node);
		assertEquals(sw.toString(), "{\"url\":\"http://localhost:8888/users/\",\"children\":null}");
	}
}
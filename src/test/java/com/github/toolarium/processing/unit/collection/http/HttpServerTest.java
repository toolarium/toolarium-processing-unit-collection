/*
 * HttpProcessUnitWebserverTest.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.processing.unit.collection.http;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.github.toolarium.network.server.service.EchoService;
import com.github.toolarium.network.server.util.HttpServerTestUtil;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.junit.jupiter.api.Test;


/**
 * Some basic tests of the http-server
 *  
 * @author patrick
 */
public class HttpServerTest {
    private static int port = 8080;

    
    /**
     * Test Echo server
     *
     * @throws Exception In case of an error
     */
    @Test
    public void echoSSLGetTest() throws Exception {
        HttpRequest request = HttpRequest
                .newBuilder(URI.create("https://localhost" + ":" + port + "/echo"))
                .GET()
                .build();
        
        HttpResponse<String> response = HttpServerTestUtil.getInstance().runHttps(new EchoService(), request);
        assertEquals(200, response.statusCode());
    }

    
    /**
     * Test Echo server
     *
     * @throws Exception In case of an error
     */
    @Test
    public void echoSSLPostTest() throws Exception {
        HttpRequest request = HttpRequest
                .newBuilder(URI.create("https://localhost" + ":" + port))
                .POST(HttpRequest.BodyPublishers.ofString("{\"action\":\"hello\"}"))
                .build();
        
        HttpResponse<String> response = HttpServerTestUtil.getInstance().runHttps(new EchoService(), request);
        assertEquals(200, response.statusCode());
    }

    
    /**
     * Test Echo server
     *
     * @throws Exception In case of an error
     */
    @Test
    public void echoPostTest() throws Exception {
        HttpRequest request = HttpRequest
                .newBuilder(URI.create("http://localhost" + ":" + port))
                .POST(HttpRequest.BodyPublishers.ofString("{\"action\":\"hello\"}"))
                .build();
        
        HttpResponse<String> response = HttpServerTestUtil.getInstance().runHttp(new EchoService(), request);
        assertEquals(200, response.statusCode());
    }
}

/*
 * HttpProcessUnitTest.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.processing.unit.collection.http;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.github.toolarium.network.server.HttpServerFactory;
import com.github.toolarium.network.server.IHttpServer;
import com.github.toolarium.network.server.service.EchoService;
import com.github.toolarium.processing.unit.dto.Parameter;
import com.github.toolarium.processing.unit.runtime.test.TestProcessingUnitRunner;
import com.github.toolarium.processing.unit.runtime.test.TestProcessingUnitRunnerFactory;
import com.github.toolarium.security.keystore.ISecurityManagerProvider;
import com.github.toolarium.security.keystore.SecurityManagerProviderFactory;
import com.github.toolarium.security.ssl.SSLContextFactory;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import javax.net.ssl.SSLContext;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Test the {@link HttpProcessingUnit}.
 *  
 * @author patrick
 */
public class HttpProcessUnitTest {
    private static final Logger LOG = LoggerFactory.getLogger(HttpProcessUnitTest.class);
    /*// debug
    static {
     System.setProperty("jdk.internal.httpclient.debug", "true");
    }*/

    
    /**
     * Echo test
     *
     * @throws Exception In case of an exception
     */
    @Test
    public void simpleGetRequestTest() throws Exception {
        int port = 8081;
        
        // create self signed certificate
        ISecurityManagerProvider securityManagerProvider = SecurityManagerProviderFactory.getInstance().getSecurityManagerProvider("toolarium", "changit");        
        assertNotNull(securityManagerProvider);

        // get ssl context from factory
        SSLContext sslContext = SSLContextFactory.getInstance().createSslContext(securityManagerProvider);

        // create server
        IHttpServer server = HttpServerFactory.getInstance().getServerInstance();
        server.start(new EchoService(), port, sslContext);
        Thread.sleep(100L);

        List<Parameter> parameterList = new ArrayList<Parameter>();
        parameterList.add(new Parameter(HttpProcessingUnitConstants.PORT_PARAMETER.getKey(), "" + port));
        parameterList.add(new Parameter(HttpProcessingUnitConstants.VERIFY_CERTIFICATE_PARAMETER.getKey(), "false"));
        
        //parameterList.add(new Parameter(HttpProcessingUnit.REQUEST_METHOD_PARAMETER.getKey(), "GET"));
        TestProcessingUnitRunner processRunner = TestProcessingUnitRunnerFactory.getInstance().getProcessingUnitRunner();
        assertEquals(processRunner.run(HttpProcessingUnit.class, parameterList), 1);

        assertEquals(processRunner.getSuspendCounter(), 0);
        assertNotNull(processRunner.getProcessingUnitProgress());
        assertEquals(processRunner.getProcessingUnitProgress().getNumberOfUnitsToProcess(), 1);
        assertEquals(processRunner.getProcessingUnitProgress().getNumberOfProcessedUnits(), 1);
        //assertEquals(processRunner.getProcessingUnitProgress().getNumberOfFailedUnits(), 0);
        assertNotNull(processRunner.getStatusMessageList());
        
        server.stop();
    }

    
    /**
     * Echo test
     *
     * @throws Exception In case of an exception
     */
    @Test
    public void simpleGetFromGoogle() throws Exception {
        //System.setProperty("jdk.internal.httpclient.debug", "true");

        List<Parameter> parameterList = new ArrayList<Parameter>();
        //parameterList.add(new Parameter(HttpProcessingUnitConstants.PROTOCOL_PARAMETER.getKey(), "https")); // default
        //parameterList.add(new Parameter(HttpProcessingUnitConstants.PORT_PARAMETER.getKey(), "443")); // default
        parameterList.add(new Parameter(HttpProcessingUnitConstants.DOMAIN_PARAMETER.getKey(), "www.google.com"));
        parameterList.add(new Parameter(HttpProcessingUnitConstants.PATH_PARAMETER.getKey(), "/search"));
        parameterList.add(new Parameter(HttpProcessingUnitConstants.REQUEST_QUERY_PARAMETER.getKey(), "q=abc"));
        
        //https://www.google.com/search?q=dd
        //parameterList.add(new Parameter(HttpProcessingUnit.REQUEST_METHOD_PARAMETER.getKey(), "GET"));
        TestProcessingUnitRunner processRunner = TestProcessingUnitRunnerFactory.getInstance().getProcessingUnitRunner();
        assertEquals(processRunner.run(HttpProcessingUnit.class, parameterList), 1);

        assertEquals(processRunner.getSuspendCounter(), 0);
        assertNotNull(processRunner.getProcessingUnitProgress());
        assertEquals(processRunner.getProcessingUnitProgress().getNumberOfUnitsToProcess(), 1);
        assertEquals(processRunner.getProcessingUnitProgress().getNumberOfProcessedUnits(), 1);
        // assertEquals(processRunner.getProcessingUnitProgress().getNumberOfFailedUnits(), 0); // TODO:
        assertNotNull(processRunner.getStatusMessageList());
    }

    

    /**
     * Test Echo server
     *
     * @throws Exception In case of an error
     */
    @Test
    public void echoSSLTest() throws Exception {
        int port = 8082;
        
        // create self signed certificate
        ISecurityManagerProvider securityManagerProvider = SecurityManagerProviderFactory.getInstance().getSecurityManagerProvider("toolarium", "changit");
        assertNotNull(securityManagerProvider);

        // get ssl context from factory
        SSLContext sslContext = SSLContextFactory.getInstance().createSslContext(securityManagerProvider);

        IHttpServer server = HttpServerFactory.getInstance().getServerInstance();
        server.start(new EchoService(), port, sslContext);
        Thread.sleep(100L);
        
        LOG.debug("Server hostname: " + server.getHttpServerInformation().getHostname());
        HttpRequest request = HttpRequest
                .newBuilder(URI.create("https://localhost" + ":" + port)) //  + server.getHttpServerInformation().getHostname()
                .GET()
                .build();
        
        HttpResponse<String> response = HttpClient
                .newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .connectTimeout(Duration.ofSeconds(10))
                .sslContext(sslContext)
                .build()
                .send(request, BodyHandlers.ofString());
        
        LOG.debug("Response: " + response.body());
        server.stop();
    }
    
}

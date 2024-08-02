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
 * 
 * @author patrick
 */
public class HttpProcessUnitTest {
    private static final Logger LOG = LoggerFactory.getLogger(HttpProcessUnitTest.class);

    
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

        IHttpServer server = HttpServerFactory.getInstance().getServerInstance();
        server.start(new EchoService(), port, sslContext);
        Thread.sleep(100L);
        
        List<Parameter> parameterList = new ArrayList<Parameter>();
        //parameterList.add(new Parameter("keyNames", "name1", "name2"));

        TestProcessingUnitRunner processRunner = TestProcessingUnitRunnerFactory.getInstance().getProcessingUnitRunner();
        assertEquals(processRunner.run(HttpProcessingUnit.class, parameterList), 1);

        assertEquals(processRunner.getSuspendCounter(), 0);
        assertNotNull(processRunner.getProcessingUnitProgress());
        assertEquals(processRunner.getProcessingUnitProgress().getNumberOfUnitsToProcess(), 1);
        assertEquals(processRunner.getProcessingUnitProgress().getNumberOfProcessedUnits(), 1);
        assertEquals(processRunner.getProcessingUnitProgress().getNumberOfFailedUnits(), 0);
        assertNotNull(processRunner.getStatusMessageList());
        
        /*
        LOG.debug("Server hostname: " + server.getHttpServerInformation().getHostname());
        */
        HttpRequest request = HttpRequest
                .newBuilder(URI.create("https://localhost" + ":" + port)) //  + server.getHttpServerInformation().getHostname()
                .GET()
                .build();

        HttpResponse<String> response = HttpClient
                .newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(Duration.ofSeconds(10))
                .sslContext(sslContext)
                .build()
                .send(request, BodyHandlers.ofString());
        
        LOG.debug("Response: " + response.body());
        server.stop();
    }

}

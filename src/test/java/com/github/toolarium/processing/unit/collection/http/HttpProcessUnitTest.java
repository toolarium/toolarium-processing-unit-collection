/*
 * HttpProcessUnitTest.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.processing.unit.collection.http;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.toolarium.network.server.HttpServerFactory;
import com.github.toolarium.network.server.IHttpServer;
import com.github.toolarium.network.server.service.EchoService;
import com.github.toolarium.processing.unit.collection.http.test.HttpTestProcessingUnitRunner;
import com.github.toolarium.processing.unit.dto.Parameter;
import com.github.toolarium.security.certificate.CertificateUtilFactory;
import com.github.toolarium.security.keystore.ISecurityManagerProvider;
import com.github.toolarium.security.keystore.SecurityManagerProviderFactory;
import com.github.toolarium.security.pki.KeyConverterFactory;
import com.github.toolarium.security.ssl.SSLContextFactory;
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
    private static int port = 8080;

    
    /**
     * Http processing test
     *
     * @throws Exception In case of an exception
     */
    @Test
    public void getHttpProcessingTest() throws Exception {
        List<Parameter> parameterList = new ArrayList<Parameter>();
        parameterList.add(new Parameter(HttpProcessingUnitConstants.PROTOCOL_PARAMETER.getKey(), "http")); // default is https
        parameterList.add(new Parameter(HttpProcessingUnitConstants.PORT_PARAMETER.getKey(), "" + ++port));
        parameterList.add(new Parameter(HttpProcessingUnitConstants.PATH_PARAMETER.getKey(), "/echo"));
        parameterList.add(new Parameter(HttpProcessingUnitConstants.REQUEST_QUERY_PARAMETER.getKey(), "q=abc"));
        parameterList.add(new Parameter(HttpProcessingUnitConstants.REQUEST_BODY_PARAMETER.getKey(), "TEST1"));
        
        HttpTestProcessingUnitRunner processRunner = new HttpTestProcessingUnitRunner();
        assertEquals(processRunner.run(HttpProcessingUnit.class, parameterList), 1);

        HttpProcessingUnit.HttpResultPersistence persistence = processRunner.getProcessingPersistence();
        assertEquals(1, persistence.getSize());
        assertEquals("echo", persistence.pop());
        assertEquals(0, persistence.getSize());
        
        LOG.debug(processRunner.toString());
        assertEquals(processRunner.getSuspendCounter(), 0);
        assertNotNull(processRunner.getProcessingUnitProgress());
        assertEquals(processRunner.getProcessingUnitProgress().getNumberOfUnitsToProcess(), 1);
        assertEquals(processRunner.getProcessingUnitProgress().getNumberOfProcessedUnits(), 1);
        assertEquals(processRunner.getProcessingUnitProgress().getNumberOfFailedUnits(), 0); 
        assertNotNull(processRunner.getStatusMessageList());
    }

    
    /**
     * Http processing test
     *
     * @throws Exception In case of an exception
     */
    @Test
    public void getHttpProcessingTestWithNoEchoPath() throws Exception {
        List<Parameter> parameterList = new ArrayList<Parameter>();
        parameterList.add(new Parameter(HttpProcessingUnitConstants.PROTOCOL_PARAMETER.getKey(), "http")); // default is https
        parameterList.add(new Parameter(HttpProcessingUnitConstants.PORT_PARAMETER.getKey(), "" + ++port));
        //parameterList.add(new Parameter(HttpProcessingUnitConstants.REQUEST_QUERY_PARAMETER.getKey(), "q=abc"));
        parameterList.add(new Parameter(HttpProcessingUnitConstants.REQUEST_BODY_PARAMETER.getKey(), "TEST2"));
        
        HttpTestProcessingUnitRunner processRunner = new HttpTestProcessingUnitRunner();
        assertEquals(processRunner.run(HttpProcessingUnit.class, parameterList), 1);

        HttpProcessingUnit.HttpResultPersistence persistence = processRunner.getProcessingPersistence();
        assertEquals(1, persistence.getSize());
        assertEquals("", persistence.pop());
        assertEquals(0, persistence.getSize());
        
        LOG.debug(processRunner.toString());
        assertEquals(processRunner.getSuspendCounter(), 0);
        assertNotNull(processRunner.getProcessingUnitProgress());
        assertEquals(processRunner.getProcessingUnitProgress().getNumberOfUnitsToProcess(), 1);
        assertEquals(processRunner.getProcessingUnitProgress().getNumberOfProcessedUnits(), 1);
        assertEquals(processRunner.getProcessingUnitProgress().getNumberOfFailedUnits(), 0); 
        assertNotNull(processRunner.getStatusMessageList());
    }

    
    /**
     * Http processing test
     *
     * @throws Exception In case of an exception
     */
    @Test
    public void postHttpProcessingTest() throws Exception {
        List<Parameter> parameterList = new ArrayList<Parameter>();
        parameterList.add(new Parameter(HttpProcessingUnitConstants.PROTOCOL_PARAMETER.getKey(), "http")); // default is https
        parameterList.add(new Parameter(HttpProcessingUnitConstants.PORT_PARAMETER.getKey(), "" + ++port));
        parameterList.add(new Parameter(HttpProcessingUnitConstants.PATH_PARAMETER.getKey(), "/echo"));
        //parameterList.add(new Parameter(HttpProcessingUnitConstants.REQUEST_QUERY_PARAMETER.getKey(), "q=abc"));
        parameterList.add(new Parameter(HttpProcessingUnitConstants.REQUEST_METHOD_PARAMETER.getKey(), "POST"));
        parameterList.add(new Parameter(HttpProcessingUnitConstants.REQUEST_BODY_PARAMETER.getKey(), "TEST3"));
        
        HttpTestProcessingUnitRunner processRunner = new HttpTestProcessingUnitRunner();
        assertEquals(processRunner.run(HttpProcessingUnit.class, parameterList), 1);

        HttpProcessingUnit.HttpResultPersistence persistence = processRunner.getProcessingPersistence();
        assertEquals(1, persistence.getSize());
        assertEquals("TEST3", persistence.pop());
        assertEquals(0, persistence.getSize());
        
        LOG.debug(processRunner.toString());
        assertEquals(processRunner.getSuspendCounter(), 0);
        assertNotNull(processRunner.getProcessingUnitProgress());
        assertEquals(processRunner.getProcessingUnitProgress().getNumberOfUnitsToProcess(), 1);
        assertEquals(processRunner.getProcessingUnitProgress().getNumberOfProcessedUnits(), 1);
        assertEquals(processRunner.getProcessingUnitProgress().getNumberOfFailedUnits(), 0); 
        assertNotNull(processRunner.getStatusMessageList());
    }

    
    /**
     * Https processing test
     *
     * @throws Exception In case of an exception
     */
    @Test
    public void getHttpsProcessingTest() throws Exception {
        List<Parameter> parameterList = new ArrayList<Parameter>();
        parameterList.add(new Parameter(HttpProcessingUnitConstants.PORT_PARAMETER.getKey(), "" + ++port));
        parameterList.add(new Parameter(HttpProcessingUnitConstants.PATH_PARAMETER.getKey(), "/echo"));
        parameterList.add(new Parameter(HttpProcessingUnitConstants.REQUEST_QUERY_PARAMETER.getKey(), "q=abc"));
        parameterList.add(new Parameter(HttpProcessingUnitConstants.VERIFY_CERTIFICATE_PARAMETER.getKey(), "false"));
        parameterList.add(new Parameter(HttpProcessingUnitConstants.REQUEST_BODY_PARAMETER.getKey(), "TEST4"));
        
        HttpTestProcessingUnitRunner processRunner = new HttpTestProcessingUnitRunner();
        assertEquals(processRunner.run(HttpProcessingUnit.class, parameterList), 1);

        HttpProcessingUnit.HttpResultPersistence persistence = processRunner.getProcessingPersistence();
        assertEquals(1, persistence.getSize());
        assertEquals("echo", persistence.pop());
        assertEquals(0, persistence.getSize());

        LOG.debug(processRunner.toString());
        assertEquals(processRunner.getSuspendCounter(), 0);
        assertNotNull(processRunner.getProcessingUnitProgress());
        assertEquals(processRunner.getProcessingUnitProgress().getNumberOfUnitsToProcess(), 1);
        assertEquals(processRunner.getProcessingUnitProgress().getNumberOfProcessedUnits(), 1);
        assertEquals(processRunner.getProcessingUnitProgress().getNumberOfFailedUnits(), 0); 
        assertNotNull(processRunner.getStatusMessageList());
    }

    
    /**
     * Http processing test
     *
     * @throws Exception In case of an exception
     */
    @Test
    public void getHttpsProcessingTestWithNoEchoPath() throws Exception {
        List<Parameter> parameterList = new ArrayList<Parameter>();
        parameterList.add(new Parameter(HttpProcessingUnitConstants.PORT_PARAMETER.getKey(), "" + ++port));
        //parameterList.add(new Parameter(HttpProcessingUnitConstants.REQUEST_QUERY_PARAMETER.getKey(), "q=abc"));
        parameterList.add(new Parameter(HttpProcessingUnitConstants.VERIFY_CERTIFICATE_PARAMETER.getKey(), "false"));
        parameterList.add(new Parameter(HttpProcessingUnitConstants.REQUEST_BODY_PARAMETER.getKey(), "TEST5"));
        
        HttpTestProcessingUnitRunner processRunner = new HttpTestProcessingUnitRunner();
        assertEquals(processRunner.run(HttpProcessingUnit.class, parameterList), 1);

        HttpProcessingUnit.HttpResultPersistence persistence = processRunner.getProcessingPersistence();
        assertEquals(1, persistence.getSize());
        assertEquals("", persistence.pop());
        assertEquals(0, persistence.getSize());
        
        LOG.debug(processRunner.toString());
        assertEquals(processRunner.getSuspendCounter(), 0);
        assertNotNull(processRunner.getProcessingUnitProgress());
        assertEquals(processRunner.getProcessingUnitProgress().getNumberOfUnitsToProcess(), 1);
        assertEquals(processRunner.getProcessingUnitProgress().getNumberOfProcessedUnits(), 1);
        assertEquals(processRunner.getProcessingUnitProgress().getNumberOfFailedUnits(), 0); 
        assertNotNull(processRunner.getStatusMessageList());
    }

    
    /**
     * Http processing test
     *
     * @throws Exception In case of an exception
     */
    @Test
    public void postHttpsProcessingTest() throws Exception {
        List<Parameter> parameterList = new ArrayList<Parameter>();
        parameterList.add(new Parameter(HttpProcessingUnitConstants.PORT_PARAMETER.getKey(), "" + ++port));
        parameterList.add(new Parameter(HttpProcessingUnitConstants.PATH_PARAMETER.getKey(), "/echo"));
        //parameterList.add(new Parameter(HttpProcessingUnitConstants.REQUEST_QUERY_PARAMETER.getKey(), "q=abc"));
        parameterList.add(new Parameter(HttpProcessingUnitConstants.REQUEST_METHOD_PARAMETER.getKey(), "POST"));
        parameterList.add(new Parameter(HttpProcessingUnitConstants.VERIFY_CERTIFICATE_PARAMETER.getKey(), "false"));
        parameterList.add(new Parameter(HttpProcessingUnitConstants.REQUEST_BODY_PARAMETER.getKey(), "TEST6"));
        
        HttpTestProcessingUnitRunner processRunner = new HttpTestProcessingUnitRunner();
        assertEquals(processRunner.run(HttpProcessingUnit.class, parameterList), 1);

        HttpProcessingUnit.HttpResultPersistence persistence = processRunner.getProcessingPersistence();
        assertEquals(1, persistence.getSize());
        assertEquals("TEST6", persistence.pop());
        assertEquals(0, persistence.getSize());
        
        LOG.debug(processRunner.toString());
        assertEquals(processRunner.getSuspendCounter(), 0);
        assertNotNull(processRunner.getProcessingUnitProgress());
        assertEquals(processRunner.getProcessingUnitProgress().getNumberOfUnitsToProcess(), 1);
        assertEquals(processRunner.getProcessingUnitProgress().getNumberOfProcessedUnits(), 1);
        assertEquals(processRunner.getProcessingUnitProgress().getNumberOfFailedUnits(), 0); 
        assertNotNull(processRunner.getStatusMessageList());
    }

    
    /**
     * Echo test
     *
     * @throws Exception In case of an exception
     */
    @Test
    public void simpleGetLocalServer() throws Exception {
        //System.setProperty("jdk.internal.httpclient.debug", "true");

        // create self signed certificate
        final ISecurityManagerProvider securityManagerProvider = SecurityManagerProviderFactory.getInstance().getSecurityManagerProvider("toolarium", "changit");
        SSLContext sslContext = SSLContextFactory.getInstance().createSslContext(securityManagerProvider);
        final IHttpServer httpServer = HttpServerFactory.getInstance().getServerInstance();
        httpServer.start(new EchoService(), 8084, sslContext);

        List<Parameter> parameterList = new ArrayList<Parameter>();
        //parameterList.add(new Parameter(HttpProcessingUnitConstants.PROTOCOL_PARAMETER.getKey(), "https")); // default
        parameterList.add(new Parameter(HttpProcessingUnitConstants.PORT_PARAMETER.getKey(), "" + ++port));
        //parameterList.add(new Parameter(HttpProcessingUnitConstants.PATH_PARAMETER.getKey(), "/echo"));
        //parameterList.add(new Parameter(HttpProcessingUnitConstants.REQUEST_QUERY_PARAMETER.getKey(), "q=abc"));
        parameterList.add(new Parameter(HttpProcessingUnitConstants.VERIFY_CERTIFICATE_PARAMETER.getKey(), "false"));
        parameterList.add(new Parameter(HttpProcessingUnitConstants.REQUEST_METHOD_PARAMETER.getKey(), "POST"));
        parameterList.add(new Parameter(HttpProcessingUnitConstants.REQUEST_BODY_PARAMETER.getKey(), "TEST7"));
        
        HttpTestProcessingUnitRunner processRunner = new HttpTestProcessingUnitRunner();
        assertEquals(processRunner.run(HttpProcessingUnit.class, parameterList), 1);

        HttpProcessingUnit.HttpResultPersistence persistence = processRunner.getProcessingPersistence();
        assertEquals(1, persistence.getSize());
        assertEquals("TEST7", persistence.pop());
        assertEquals(0, persistence.getSize());

        LOG.debug(processRunner.toString());
        assertEquals(processRunner.getSuspendCounter(), 0);
        assertNotNull(processRunner.getProcessingUnitProgress());
        assertEquals(processRunner.getProcessingUnitProgress().getNumberOfUnitsToProcess(), 1);
        assertEquals(processRunner.getProcessingUnitProgress().getNumberOfProcessedUnits(), 1);
        assertEquals(processRunner.getProcessingUnitProgress().getNumberOfFailedUnits(), 0); 
        assertNotNull(processRunner.getStatusMessageList());
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
        parameterList.add(new Parameter(HttpProcessingUnitConstants.DOMAIN_PARAMETER.getKey(), "www.google.com"));
        parameterList.add(new Parameter(HttpProcessingUnitConstants.PATH_PARAMETER.getKey(), "/search"));
        parameterList.add(new Parameter(HttpProcessingUnitConstants.REQUEST_QUERY_PARAMETER.getKey(), "q=abc"));

        //https://www.google.com/search?q=dd
        HttpTestProcessingUnitRunner processRunner = new HttpTestProcessingUnitRunner();
        assertEquals(processRunner.run(HttpProcessingUnit.class, parameterList), 1);

        HttpProcessingUnit.HttpResultPersistence persistence = processRunner.getProcessingPersistence();
        assertEquals(1, persistence.getSize());
        assertTrue(persistence.pop().indexOf("<html") > 0);
        assertEquals(0, persistence.getSize());

        assertEquals(processRunner.getSuspendCounter(), 0);
        assertNotNull(processRunner.getProcessingUnitProgress());
        assertEquals(processRunner.getProcessingUnitProgress().getNumberOfUnitsToProcess(), 1);
        assertEquals(processRunner.getProcessingUnitProgress().getNumberOfProcessedUnits(), 1);
        assertEquals(processRunner.getProcessingUnitProgress().getNumberOfFailedUnits(), 0);
        assertNotNull(processRunner.getStatusMessageList());
    }


    /**
     * Echo test
     *
     * @throws Exception In case of an exception
     */
    @Test
    public void simpleGetFromGoogleWithOwnTrustCertificate() throws Exception {
        //System.setProperty("jdk.internal.httpclient.debug", "true");
        List<Parameter> parameterList = new ArrayList<Parameter>();
        parameterList.add(new Parameter(HttpProcessingUnitConstants.DOMAIN_PARAMETER.getKey(), "www.google.com"));
        parameterList.add(new Parameter(HttpProcessingUnitConstants.PATH_PARAMETER.getKey(), "/search"));
        parameterList.add(new Parameter(HttpProcessingUnitConstants.REQUEST_QUERY_PARAMETER.getKey(), "q=abc"));

        String trustCertificate = KeyConverterFactory.getInstance().getConverter().formatPKCS7(CertificateUtilFactory.getInstance().getGenerator().createCreateCertificate("toolarium").getCertificates());
        parameterList.add(new Parameter(HttpProcessingUnitConstants.TRUST_CERTIFICATE_PARAMETER.getKey(), trustCertificate));

        //https://www.google.com/search?q=dd
        HttpTestProcessingUnitRunner processRunner = new HttpTestProcessingUnitRunner();
        assertEquals(processRunner.run(HttpProcessingUnit.class, parameterList), 1);

        HttpProcessingUnit.HttpResultPersistence persistence = processRunner.getProcessingPersistence();
        assertEquals(1, persistence.getSize());
        assertTrue(persistence.pop().indexOf("<html") > 0);
        assertEquals(0, persistence.getSize());

        assertEquals(processRunner.getSuspendCounter(), 0);
        assertNotNull(processRunner.getProcessingUnitProgress());
        assertEquals(processRunner.getProcessingUnitProgress().getNumberOfUnitsToProcess(), 1);
        assertEquals(processRunner.getProcessingUnitProgress().getNumberOfProcessedUnits(), 1);
        assertEquals(processRunner.getProcessingUnitProgress().getNumberOfFailedUnits(), 0);
        assertNotNull(processRunner.getStatusMessageList());
    }
}

/*
 * HttpTestProcessingUnitRunner.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.processing.unit.collection.http.test;

import com.github.toolarium.common.util.ExceptionWrapper;
import com.github.toolarium.network.server.HttpServerFactory;
import com.github.toolarium.network.server.IHttpServer;
import com.github.toolarium.network.server.service.EchoService;
import com.github.toolarium.network.server.service.IHttpService;
import com.github.toolarium.processing.unit.IProcessingUnit;
import com.github.toolarium.processing.unit.IProcessingUnitContext;
import com.github.toolarium.processing.unit.collection.http.HttpProcessingUnit;
import com.github.toolarium.processing.unit.collection.http.HttpProcessingUnitConstants;
import com.github.toolarium.processing.unit.dto.Parameter;
import com.github.toolarium.processing.unit.exception.ProcessingException;
import com.github.toolarium.processing.unit.exception.ValidationException;
import com.github.toolarium.processing.unit.runtime.runnable.IEmptyProcessingUnitHandler;
import com.github.toolarium.processing.unit.runtime.test.TestProcessingUnitRunner;
import com.github.toolarium.security.keystore.ISecurityManagerProvider;
import com.github.toolarium.security.keystore.SecurityManagerProviderFactory;
import com.github.toolarium.security.ssl.SSLContextFactory;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.net.ssl.SSLContext;


/**
 * Implements a http-server {@link TestProcessingUnitRunner}.
 * 
 * @author patrick
 */
public class HttpTestProcessingUnitRunner extends TestProcessingUnitRunner {
    private static final long serialVersionUID = 2829007892788097567L;
    private transient IHttpServer httpServer;
    private IHttpService httpService;

    
    /**
     * Constructor for HttpTestProcessingUnitRunner
     */
    public HttpTestProcessingUnitRunner() {
        this.httpService = new EchoService();
    }

    
    /**
     * Constructor for HttpTestProcessingUnitRunner
     *
     * @param httpService the http service
     */
    public HttpTestProcessingUnitRunner(IHttpService httpService) {
        this.httpService = httpService;
    }


    /**
     * @see com.github.toolarium.processing.unit.runtime.test.TestProcessingUnitRunner#run(java.lang.Class, java.util.List)
     */
    @Override
    public long run(Class<? extends IProcessingUnit> processingUnitClass, List<Parameter> parameterList) throws ValidationException, ProcessingException {
        List<Parameter> updatedParameters = startServer(parameterList);
        
        try {
            return super.run(processingUnitClass, updatedParameters);
        } finally {
            stopServer();
        }
    }


    /**
     * @see com.github.toolarium.processing.unit.runtime.test.TestProcessingUnitRunner#runAndAbort(java.lang.Class, java.util.List, java.lang.Integer)
     */
    @Override
    public long runAndAbort(Class<? extends IProcessingUnit> processingUnitClass, List<Parameter> parameterList, Integer numberOfCyclesBeforeStop) throws ValidationException, ProcessingException {
        List<Parameter> updatedParameters = startServer(parameterList);
        
        try {
            return super.runAndAbort(processingUnitClass, updatedParameters, numberOfCyclesBeforeStop);
        } finally {
            stopServer();
        }
    }


    /**
     * @see com.github.toolarium.processing.unit.runtime.test.TestProcessingUnitRunner#runWithThrottling(java.lang.Class, java.util.List, java.lang.Long)
     */
    @Override
    public long runWithThrottling(Class<? extends IProcessingUnit> processingUnitClass, List<Parameter> parameterList, Long maxNumberOfProcessingUnitCallsPerSecond) throws ValidationException, ProcessingException {
        List<Parameter> updatedParameters = startServer(parameterList);
        
        try {
            return super.runWithThrottling(processingUnitClass, updatedParameters, maxNumberOfProcessingUnitCallsPerSecond);
        } finally {
            stopServer();
        }
    }


    /**
     * @see com.github.toolarium.processing.unit.runtime.test.TestProcessingUnitRunner#runWithSuspendAndResume(java.lang.Class, java.util.List, long, long, int)
     */
    @Override
    public long runWithSuspendAndResume(Class<? extends IProcessingUnit> processingUnitClass,
                                        List<Parameter> parameterList,
                                        long suspendAfterCycles,
                                        long suspendSleepTime,
                                        int maxNumberOfSuspends) throws ValidationException, ProcessingException {
        List<Parameter> updatedParameters = startServer(parameterList);
        
        try {
            return super.runWithSuspendAndResume(processingUnitClass, updatedParameters, suspendAfterCycles, suspendSleepTime, maxNumberOfSuspends);
        } finally {
            stopServer();
        }
    }


    /**
     * @see com.github.toolarium.processing.unit.runtime.test.TestProcessingUnitRunner#runWithSuspendAndResume(java.lang.Class, java.util.List, long, long, int, java.lang.Long)
     */
    @Override
    public long runWithSuspendAndResume(Class<? extends IProcessingUnit> processingUnitClass,
                                        List<Parameter> parameterList,
                                        long suspendAfterCycles,
                                        long suspendSleepTime,
                                        int maxNumberOfSuspends,
                                        Long maxNumberOfProcessingUnitCallsPerSecond) throws ValidationException, ProcessingException {
        List<Parameter> updatedParameters = startServer(parameterList);
        
        try {
            return super.runWithSuspendAndResume(processingUnitClass, updatedParameters, suspendAfterCycles, suspendSleepTime, maxNumberOfSuspends, maxNumberOfProcessingUnitCallsPerSecond);
        } finally {
            stopServer();
        }
    }


    /**
     * @see com.github.toolarium.processing.unit.runtime.test.TestProcessingUnitRunner#processingUnitContext(com.github.toolarium.processing.unit.IProcessingUnitContext)
     */
    @Override
    public TestProcessingUnitRunner processingUnitContext(IProcessingUnitContext processingUnitContext) {
        super.processingUnitContext(processingUnitContext);
        return this;
    }

    
    /**
     * Get the processing persistence
     *
     * @return the processing persistence
     */
    public HttpProcessingUnit.HttpResultPersistence getProcessingPersistence() {
        return ((HttpProcessingUnit)getProcesingUnit()).getProcessingPersistence();
    }



    /**
     * @see com.github.toolarium.processing.unit.runtime.test.TestProcessingUnitRunner#setEmptyProcessingUnitHandler(com.github.toolarium.processing.unit.runtime.runnable.IEmptyProcessingUnitHandler)
     */
    @Override
    public void setEmptyProcessingUnitHandler(IEmptyProcessingUnitHandler emptyProcessingUnitHandler) {
        super.setEmptyProcessingUnitHandler(getEmptyProcessingUnitHandler());
    }

    
    /**
     * Start the http server
     *
     * @param parameterList the parameter list
     * @return the modified parameters
     * @throws ValidationException In case of validation issue
     * @throws ProcessingException In case of a process start issue
     */
    protected List<Parameter> startServer(List<Parameter> parameterList) throws ValidationException, ProcessingException {
        //System.setProperty("jdk.internal.httpclient.debug", "true");

        Map<String, Parameter> parameterMap = new LinkedHashMap<String, Parameter>();
        for (Parameter p : parameterList) {
            parameterMap.put(p.getKey(), p);
        }

        SSLContext sslContext = null;
        Parameter p = parameterMap.get(HttpProcessingUnitConstants.PROTOCOL_PARAMETER.getKey());
        if (p == null || "https".equals(p.getParameterValue().getValueAsString())) {
            // create self signed certificate
            final ISecurityManagerProvider securityManagerProvider = SecurityManagerProviderFactory.getInstance().getSecurityManagerProvider("toolarium", "changit");
            try {
                sslContext = SSLContextFactory.getInstance().createSslContext(securityManagerProvider);
            } catch (GeneralSecurityException e) {
                throw ExceptionWrapper.getInstance().convertException(e, ProcessingException.class);
            }
        }
        
        int port = (Integer)HttpProcessingUnitConstants.PORT_PARAMETER.getDefaultValue();
        p = parameterMap.get(HttpProcessingUnitConstants.PORT_PARAMETER.getKey());
        if (p != null) {
            port = p.getParameterValue().getValueAsInteger();
        }

        String hostname = "" + HttpProcessingUnitConstants.DOMAIN_PARAMETER.getDefaultValue();
        p = parameterMap.get(HttpProcessingUnitConstants.DOMAIN_PARAMETER.getKey());
        if (p != null) {
            hostname = p.getParameterValue().getValueAsString();
        }
        if (parameterMap.containsKey(HttpProcessingUnitConstants.DOMAIN_PARAMETER.getKey())) {
            parameterMap.remove(HttpProcessingUnitConstants.DOMAIN_PARAMETER.getKey());
        }
        parameterMap.put(HttpProcessingUnitConstants.DOMAIN_PARAMETER.getKey(), new Parameter(HttpProcessingUnitConstants.DOMAIN_PARAMETER.getKey(), hostname));

        httpServer = HttpServerFactory.getInstance().getServerInstance();
        try {
            httpServer.start(httpService, port, sslContext);
        } catch (IOException e) {
            httpServer = null;
            throw ExceptionWrapper.getInstance().convertException(e, ProcessingException.class);
        }
        
        return new ArrayList<Parameter>(parameterMap.values());
    }


    /**
     * Stop http server
     *
     * @throws ProcessingException the processing exception
     */
    protected void stopServer() throws ProcessingException {
        if (httpServer != null) {
            try {
                httpServer.stop();
            } catch (IOException e) {
                throw ExceptionWrapper.getInstance().convertException(e, ProcessingException.class);
            }
            
            httpServer = null;
        }
    }
}

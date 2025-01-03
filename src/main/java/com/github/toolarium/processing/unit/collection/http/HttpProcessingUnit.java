/*
 * HttpProcessingUnit.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.processing.unit.collection.http;


import com.github.toolarium.common.util.ThreadUtil;
import com.github.toolarium.processing.unit.IProcessingUnit;
import com.github.toolarium.processing.unit.IProcessingUnitContext;
import com.github.toolarium.processing.unit.IProcessingUnitPersistence;
import com.github.toolarium.processing.unit.IProcessingUnitStatus;
import com.github.toolarium.processing.unit.ProcessingUnitStatusBuilder;
import com.github.toolarium.processing.unit.base.AbstractProcessingUnitPersistenceImpl;
import com.github.toolarium.processing.unit.dto.Parameter;
import com.github.toolarium.processing.unit.exception.ProcessingException;
import com.github.toolarium.processing.unit.exception.ValidationException;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.LinkedList;
import java.util.List;
import javax.net.ssl.SSLContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Implements a http {@link IProcessingUnit}.
 * 
 * @author patrick
 */
public class HttpProcessingUnit extends AbstractProcessingUnitPersistenceImpl<HttpProcessingUnit.HttpResultPersistence>  implements HttpProcessingUnitConstants {
    private static final Logger LOG = LoggerFactory.getLogger(HttpProcessingUnit.class);
    private URI requestUri;
    private HttpClient httpClient;


    /**
     * @see com.github.toolarium.processing.unit.base.AbstractProcessingUnitImpl#initializeParameterDefinition()
     */
    public void initializeParameterDefinition() {
        getParameterRuntime().addParameterDefinition(PROTOCOL_PARAMETER);
        getParameterRuntime().addParameterDefinition(DOMAIN_PARAMETER);
        getParameterRuntime().addParameterDefinition(PORT_PARAMETER);
        getParameterRuntime().addParameterDefinition(PATH_PARAMETER);
        getParameterRuntime().addParameterDefinition(URL_PARAMETER);
        getParameterRuntime().addParameterDefinition(HTTP_VERSION_PARAMETER);
        getParameterRuntime().addParameterDefinition(VERIFY_CERTIFICATE_PARAMETER);
        getParameterRuntime().addParameterDefinition(TRUST_CERTIFICATE_PARAMETER);
        getParameterRuntime().addParameterDefinition(REQUEST_METHOD_PARAMETER);
        getParameterRuntime().addParameterDefinition(REQUEST_QUERY_PARAMETER);
        getParameterRuntime().addParameterDefinition(ENCODE_REQUEST_QUERY_PARAMETER);       
        getParameterRuntime().addParameterDefinition(REQUESTR_HEADER_PARAMETER);
        getParameterRuntime().addParameterDefinition(REQUEST_BODY_PARAMETER);
        getParameterRuntime().addParameterDefinition(NUMBER_OF_CALLS_PARAMTER);
        getParameterRuntime().addParameterDefinition(TIMEOUT_PARAMTER);
        getParameterRuntime().addParameterDefinition(RETRY_AFTER_TIMEOUT_PARAMTER);
        getParameterRuntime().addParameterDefinition(SLEEPTIME_BEFORE_RETRY_PARAMTER);
        getParameterRuntime().addParameterDefinition(FOLLOW_REDIRECT_PARAMETER);
        getParameterRuntime().addParameterDefinition(EXPECTED_RESPONSE_CODE_PARAMTER);
    }
    
    
    /**
     * @see com.github.toolarium.processing.unit.base.AbstractProcessingUnitImpl#validateParameterList(java.util.List)
     */
    @Override
    public void validateParameterList(List<Parameter> parameterList) throws ValidationException {
        super.validateParameterList(parameterList);
    }

    
    /**
     * @see com.github.toolarium.processing.unit.IProcessingUnit#initialize(java.util.List, com.github.toolarium.processing.unit.IProcessingUnitContext)
     */
    @Override
    public void initialize(List<Parameter> parameterList, IProcessingUnitContext processingUnitContext) throws ValidationException, ProcessingException {
        super.initialize(parameterList, processingUnitContext);

        requestUri = HttpProcessingUnitUtil.getInstance().getRequestUri(getParameterRuntime());
        if (LOG.isDebugEnabled()) {
            LOG.debug("Request uri [" + requestUri + "]");
        }
        
        SSLContext sslContext = null;
        if ("https".equals(requestUri.getScheme())) {
            sslContext = HttpProcessingUnitUtil.getInstance().getSSLContext(getParameterRuntime());
        }
        
        httpClient = HttpProcessingUnitUtil.getInstance().createHttpClient(getParameterRuntime(), sslContext);
    }


    /**
     * @see com.github.toolarium.processing.unit.base.AbstractProcessingUnitImpl#estimateNumberOfUnitsToProcess()
     */
    @Override
    public long estimateNumberOfUnitsToProcess() throws ProcessingException {
        return getParameterRuntime().getParameterValueList(NUMBER_OF_CALLS_PARAMTER).getValueAsInteger();
    }


    /**
     * @see com.github.toolarium.processing.unit.base.AbstractProcessingUnitImpl#processUnit(com.github.toolarium.processing.unit.ProcessingUnitStatusBuilder)
     */
    @Override
    public IProcessingUnitStatus processUnit(ProcessingUnitStatusBuilder processingUnitStatusBuilder) throws ProcessingException {
        
        try {
            final HttpRequest httpRequest = HttpProcessingUnitUtil.getInstance().createHttpRequest(getParameterRuntime(), requestUri);
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            String result = "";
            if (response.body() != null) {
                result = response.body();
            }

            if (response.statusCode() == 404) {
                if (getParameterRuntime().getParameterValueList(RETRY_AFTER_TIMEOUT_PARAMTER).getValueAsBoolean()) {
                    ThreadUtil.getInstance().sleep(1000 * getParameterRuntime().getParameterValueList(SLEEPTIME_BEFORE_RETRY_PARAMTER).getValueAsLong());
                }
            } else if (response.statusCode() >= 200 && response.statusCode() < 300) {
                getProcessingPersistence().add(result);
            }
            
            if (response.statusCode() != getParameterRuntime().getParameterValueList(EXPECTED_RESPONSE_CODE_PARAMTER).getValueAsInteger()) {
                // TODO
            }

        } catch (InterruptedException | IOException e) {
            LOG.warn("Error occured: " + e.getMessage(), e);
            processingUnitStatusBuilder.increaseNumberOfFailedUnits();
        } finally {
            processingUnitStatusBuilder.increaseNumberOfSuccessfulUnits();
        }

        // During a processing step status message can be returned, a status SUCCESSFUL, WARN or ERROR. Additional a message can be set
        //processingUnitStatusBuilder.warn("Warning sample");
        //processingUnitStatusBuilder.error("Error sample");
        //processingUnitStatusBuilder.message("Error sample");

        // Support of statistic:
        //processingUnitStatusBuilder.statistic("counter", 1);
        
        return processingUnitStatusBuilder.hasNextIfHasUnprocessedUnits().build();
    }

    
    /**
     * @see com.github.toolarium.processing.unit.base.AbstractProcessingUnitImpl#releaseResource()
     */
    @Override
    public void releaseResource() throws ProcessingException {
        if (httpClient != null) {
            try {
                httpClient.close();
            } catch (RuntimeException e) {
                // NOP
            }
        }
    }


    /**
     * @see com.github.toolarium.processing.unit.base.AbstractProcessingUnitPersistenceImpl#newPersistenceInstance()
     */
    @Override
    protected HttpResultPersistence newPersistenceInstance() {
        return new HttpResultPersistence();
    }


    /**
     * @see com.github.toolarium.processing.unit.base.AbstractProcessingUnitPersistenceImpl#getProcessingPersistence()
     */
    @Override
    public HttpResultPersistence getProcessingPersistence() {
        return super.getProcessingPersistence();
    }

    
    /**
     * Define the http persitense. It works like a fifo queue. 
     * 
     * @author patrick
     */
    public class HttpResultPersistence implements IProcessingUnitPersistence {
        private static final long serialVersionUID = -178680376384580300L;
        private LinkedList<String> responseQueue;
        
        
        /**
         * Constructor for HttpResultPersistence
         */
        HttpResultPersistence() {
            responseQueue = new LinkedList<String>();
        }
        
        
        /**
         * Add new response
         *
         * @param response the response
         */
        public void add(String response) {
            responseQueue.add(response);
        }

        
        /**
         * Get the response 
         *
         * @return the response
         */
        public String pop() {
            return responseQueue.pop();
        }
        
        
        /**
         * Get the size
         *
         * @return the size
         */
        public int getSize() {
            return responseQueue.size();
        }


        /**
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            return "HttpResultPersistence [responseQueue=" + responseQueue + "]";
        }
    }
}

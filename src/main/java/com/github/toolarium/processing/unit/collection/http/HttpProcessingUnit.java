/*
 * HttpProcessingUnit.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.processing.unit.collection.http;


import com.github.toolarium.common.util.ThreadUtil;
import com.github.toolarium.processing.unit.IProcessingUnit;
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
import java.util.List;
import javax.net.ssl.SSLContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Implements a http {@link IProcessingUnit}.
 * 
 * @author patrick
 */
public class HttpProcessingUnit extends AbstractProcessingUnitPersistenceImpl<HttpProcessingUnit.Persistence>  implements HttpProcessingUnitConstants {
    private static final Logger LOG = LoggerFactory.getLogger(HttpProcessingUnit.class);
    private URI requestUri;
    private HttpClient httpClient;
    private HttpRequest httpRequest;


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
        getParameterRuntime().addParameterDefinition(REQUEST_METHOD_PARAMETER);
        getParameterRuntime().addParameterDefinition(REQUEST_QUERY_PARAMETER);
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
     * @see com.github.toolarium.processing.unit.base.AbstractProcessingUnitImpl#estimateNumberOfUnitsToProcess()
     */
    @Override
    public long estimateNumberOfUnitsToProcess() throws ProcessingException {
        requestUri = HttpProcessingUnitUtil.getInstance().getRequestUri(getParameterRuntime());
        
        SSLContext sslContext = null;
        if ("https".equals(requestUri.getScheme())) {
            LOG.debug("Create self signed cert.");        
            sslContext = HttpProcessingUnitUtil.getInstance().getSSLContext(getParameterRuntime());
        }
        
        httpClient = HttpProcessingUnitUtil.getInstance().getRequestClient(getParameterRuntime(), sslContext);
        httpRequest = HttpProcessingUnitUtil.getInstance().getHttpRequest(getParameterRuntime(), requestUri);

        return getParameterRuntime().getParameterValueList(NUMBER_OF_CALLS_PARAMTER).getValueAsInteger();
    }


    /**
     * @see com.github.toolarium.processing.unit.base.AbstractProcessingUnitImpl#processUnit(com.github.toolarium.processing.unit.ProcessingUnitStatusBuilder)
     */
    @Override
    public IProcessingUnitStatus processUnit(ProcessingUnitStatusBuilder processingUnitStatusBuilder) throws ProcessingException {
        HttpResponse<String> response;
        
        try {
            
            
            response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 404) {
                if (getParameterRuntime().getParameterValueList(RETRY_AFTER_TIMEOUT_PARAMTER).getValueAsBoolean()) {
                    ThreadUtil.getInstance().sleep(1000 * getParameterRuntime().getParameterValueList(SLEEPTIME_BEFORE_RETRY_PARAMTER).getValueAsLong());
                }
            }
            
            if (response.statusCode() != getParameterRuntime().getParameterValueList(EXPECTED_RESPONSE_CODE_PARAMTER).getValueAsInteger()) {
                // TODO
                
            }

        } catch (InterruptedException | IOException e) {
            LOG.warn("Error occured: " + e.getMessage(), e);
            processingUnitStatusBuilder.increaseNumberOfSuccessfulUnits();
        } finally {
            processingUnitStatusBuilder.increaseNumberOfFailedUnits();
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
                //httpClient.close(); // TODO: Java 21
            } catch (RuntimeException e) {
                // NOP
            }
        }
    }


    /**
     * @see com.github.toolarium.processing.unit.base.AbstractProcessingUnitPersistenceImpl#newPersistenceInstance()
     */
    @Override
    protected Persistence newPersistenceInstance() {
        return new Persistence();
    }


    /**
     * Define sample an own persistence 
     * 
     * @author patrick
     */
    static class Persistence implements IProcessingUnitPersistence {
        private static final long serialVersionUID = -178680376384580300L;
        private String text;
        private int counter;
        
        /**
         * Get text
         *
         * @return the text
         */
        public String getText() {
            return text;
        }
        
        /**
         * Set text
         *
         * @param text the text
         */
        public void setText(String text) {
            this.text = text;
        }
        
        
        /**
         * Get the counter
         *
         * @return the counter
         */
        public int getCounter() {
            return counter;
        }
        
        
        /**
         * Set the counter
         *
         * @param counter the counter
         */
        public void setCounter(int counter) {
            this.counter = counter;
        }

        
        /**
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            return "SamplePersistence [text=" + text + ", counter=" + counter + "]";
        }
    }
}

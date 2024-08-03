/*
 * HttpProcessingUnitUtil.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.processing.unit.collection.http;

import com.github.toolarium.processing.unit.exception.ValidationException;
import com.github.toolarium.processing.unit.runtime.IParameterRuntime;
import com.github.toolarium.security.keystore.util.KeyStoreUtil;
import java.net.ProxySelector;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublisher;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Defines the http processing unit util
 * 
 * @author patrick
 */
public final class HttpProcessingUnitUtil {
    private static final Logger LOG = LoggerFactory.getLogger(HttpProcessingUnitUtil.class);

    /**
     * Private class, the only instance of the singelton which will be created by accessing the holder class.
     *
     * @author patrick
     */
    private static class HOLDER {
        static final HttpProcessingUnitUtil INSTANCE = new HttpProcessingUnitUtil();
    }

    
    /**
     * Constructor
     */
    private HttpProcessingUnitUtil() {
        // NOP
    }

    
    /**
     * Get the instance
     *
     * @return the instance
     */
    public static HttpProcessingUnitUtil getInstance() {
        return HOLDER.INSTANCE;
    }

    
    /**
     * Initialize the request uri
     *
     * @param parameterRuntime the parameter runtime
     * @return the request uri
     * @throws ValidationException In case of a validation error
     */
    public URI getRequestUri(IParameterRuntime parameterRuntime) throws ValidationException {
        try {
            String uri = getRequestUrl(parameterRuntime) + getRequestQueryParameter(parameterRuntime);
            return URI.create(uri);
        } catch (RuntimeException e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Could not initialize uri: " + e.getMessage(), e);
            }
            
            throw new ValidationException(e.getMessage(), e);
        }
    }
    

    /**
     * Get the request url
     *
     * @param parameterRuntime the parameter runtime
     * @return the request url
     */
    public String getRequestUrl(IParameterRuntime parameterRuntime) {
        
        if (parameterRuntime.existParameter(HttpProcessingUnitConstants.URL_PARAMETER)) {
            return parameterRuntime.getParameterValueList(HttpProcessingUnitConstants.URL_PARAMETER).getValueAsString().trim();
        } else {
            String protocol = parameterRuntime.getParameterValueList(HttpProcessingUnitConstants.PROTOCOL_PARAMETER).getValueAsString().trim();
            if (protocol != null && !protocol.isBlank()) {
                protocol += "://";
            }
            
            final String domain = parameterRuntime.getParameterValueList(HttpProcessingUnitConstants.DOMAIN_PARAMETER).getValueAsString().trim();
            final Integer port = parameterRuntime.getParameterValueList(HttpProcessingUnitConstants.PORT_PARAMETER).getValueAsInteger();
            
            String portStr = "";
            if (port > 0) {
                portStr = ":" + port;
            }
            
            final String path = parameterRuntime.getParameterValueList(HttpProcessingUnitConstants.PATH_PARAMETER).getValueAsString().trim();
            String result = protocol + domain + portStr + path;
            LOG.debug("Set url: " + result);
            return result;
        }
    }

    
    /**
     * Get the request query parameter
     *
     * @param parameterRuntime the parameter runtime
     * @return the request query parameter
     */
    public String getRequestQueryParameter(IParameterRuntime parameterRuntime) {
        String requestQuery = parameterRuntime.getParameterValueList(HttpProcessingUnitConstants.REQUEST_QUERY_PARAMETER).getValueAsString().trim();
        if (requestQuery != null && !requestQuery.isBlank()) {
            String queryParameter = "?";

            if (parameterRuntime.getParameterValueList(HttpProcessingUnitConstants.ENCODE_REQUEST_QUERY_PARAMETER).getValueAsBoolean()) {
                queryParameter += URLEncoder.encode(requestQuery, StandardCharsets.UTF_8).replaceAll("\\+", "%20");
            } else {
                queryParameter += requestQuery;
            }

            LOG.debug("Set query parameter: " + queryParameter);
            return queryParameter;
        }

        return "";
    }

    
    /**
     * Get the request query parameter
     *
     * @param parameterRuntime the parameter runtime
     * @return the request query parameter
     */
    public HttpClient.Version getHttpVersion(IParameterRuntime parameterRuntime) {
        final String version = parameterRuntime.getParameterValueList(HttpProcessingUnitConstants.HTTP_VERSION_PARAMETER).getValueAsString();
        HttpClient.Version httpVersion = HttpClient.Version.HTTP_2;
        if ("1.1".equals(version)) {
            httpVersion = HttpClient.Version.HTTP_1_1;
        }
        
        LOG.debug("Set http version: " + httpVersion);
        return httpVersion;
    }


    /**
     * Get the request header
     *
     * @param parameterRuntime the parameter runtime
     * @return the request header
     */
    protected String[] getRequestHeaders(IParameterRuntime parameterRuntime) {
        List<String> list = new ArrayList<String>();
        if (parameterRuntime.existParameter(HttpProcessingUnitConstants.REQUESTR_HEADER_PARAMETER)) {
            list = parameterRuntime.getParameterValueList(HttpProcessingUnitConstants.REQUESTR_HEADER_PARAMETER).getValueAsStringList();
        } else {
            // TODO:
        }
        
        if (!list.isEmpty()) {
            LOG.debug("Set request headers: " + list);
        }
        return list.stream().toArray(String[]::new);
    }

    
    /**
     * Get the ssl context
     *
     * @param parameterRuntime the parameter runtime
     * @return the ssl context
     * @throws ValidationException In case of a validation error
     */
    public SSLContext getSSLContext(IParameterRuntime parameterRuntime) throws ValidationException {
        SSLContext sslContext = null;
        try {
            //sslContext = SSLContext.getDefault();
            sslContext = SSLContext.getInstance("TLS");
            
            TrustManager[] trustManager = null;
            if (!parameterRuntime.getParameterValueList(HttpProcessingUnitConstants.VERIFY_CERTIFICATE_PARAMETER).getValueAsBoolean()) {
                trustManager = KeyStoreUtil.getInstance().getTrustAllCertificateManager(); 
            } else {
                /*
                TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                try {
                    tmf.init(KeyStoreUtil.getInstance().getDefaultTrustKeyStore());
                    trustManager = tmf.getTrustManagers();                
                } catch (GeneralSecurityException | IOException e) {
                    LOG.warn("Error occured: " + e.getMessage(), e);
                }
                */
            }
            sslContext.init(null, trustManager, SecureRandom.getInstanceStrong()); 
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Could not initialize ssl context: " + e.getMessage(), e);
            }
            
            throw new ValidationException("Could not initialize ssl context: " + e.getMessage(), e);
        }
        
        return sslContext;
    }

    
    /**
     * Initialize the request client
     *
     * @param parameterRuntime the parameter runtime
     * @param sslContext the ssl context
     * @return the http client
     * @throws ValidationException In case of a validation error
     */
    public HttpClient getRequestClient(IParameterRuntime parameterRuntime, SSLContext sslContext) throws ValidationException {
        HttpClient httpClient;
        
        HttpClient.Redirect redirect = HttpClient.Redirect.NORMAL;
        if (parameterRuntime.existParameter(HttpProcessingUnitConstants.FOLLOW_REDIRECT_PARAMETER)) {
            final String follow = parameterRuntime.getParameterValueList(HttpProcessingUnitConstants.FOLLOW_REDIRECT_PARAMETER).getValueAsString();
            if (HttpClient.Redirect.NEVER.toString().equalsIgnoreCase(follow)) {
                redirect = HttpClient.Redirect.NEVER;
            } else if (HttpClient.Redirect.ALWAYS.toString().equalsIgnoreCase(follow)) {
                redirect = HttpClient.Redirect.ALWAYS;
            }
        }        

        if (sslContext != null) {
            httpClient = HttpClient.newBuilder()
                    //.authenticator(null)
                    .connectTimeout(Duration.ofSeconds(parameterRuntime.getParameterValueList(HttpProcessingUnitConstants.TIMEOUT_PARAMTER).getValueAsInteger()))
                    //.cookieHandler(null)
                    .followRedirects(redirect)
                    //.localAddress(null)
                    //.priority(0)
                    .sslContext(sslContext)
                    //.sslParameters(null)
                    .version(getHttpVersion(parameterRuntime))
                    .proxy(ProxySelector.getDefault())
                    .build();
        } else {
            httpClient = HttpClient.newBuilder()
                    //.authenticator(null)
                    .connectTimeout(Duration.ofSeconds(parameterRuntime.getParameterValueList(HttpProcessingUnitConstants.TIMEOUT_PARAMTER).getValueAsInteger()))
                    //.cookieHandler(null)
                    .followRedirects(redirect)
                    //.localAddress(null)
                    //.priority(0)
                    .version(getHttpVersion(parameterRuntime))
                    .proxy(ProxySelector.getDefault())
                    .build();
        }

        return httpClient;
    }

    
    /**
     * Initialize the request client
     *
     * @param parameterRuntime the parameter runtime
     * @param requestUri the request uri
     * @return the http request
     * @throws ValidationException In case of a validation error
     */
    public HttpRequest getHttpRequest(IParameterRuntime parameterRuntime, URI requestUri) throws ValidationException {
        String requestMethod = parameterRuntime.getParameterValueList(HttpProcessingUnitConstants.REQUEST_METHOD_PARAMETER).getValueAsString().trim();
        HttpRequest.Builder builder = HttpRequest.newBuilder();
        builder.uri(requestUri);
      
        //BodyPublisher requestBody = HttpRequest.BodyPublishers.ofString("");

        if (requestMethod.equalsIgnoreCase("GET")) {
            builder.GET();
        } else if (requestMethod.equalsIgnoreCase("DELETE")) {
            builder.DELETE();
        } else if (requestMethod.equalsIgnoreCase("POST")) {
            BodyPublisher requestBody = HttpRequest.BodyPublishers.ofString(parameterRuntime.getParameterValueList(HttpProcessingUnitConstants.REQUEST_BODY_PARAMETER).getValueAsString().trim()); 
            builder.POST(requestBody);
        } else if (requestMethod.equalsIgnoreCase("PUT")) {
            BodyPublisher requestBody = HttpRequest.BodyPublishers.ofString(parameterRuntime.getParameterValueList(HttpProcessingUnitConstants.REQUEST_BODY_PARAMETER).getValueAsString().trim()); 
            builder.PUT(requestBody);
        } else {
            BodyPublisher requestBody = HttpRequest.BodyPublishers.ofString(parameterRuntime.getParameterValueList(HttpProcessingUnitConstants.REQUEST_BODY_PARAMETER).getValueAsString().trim()); 
            requestMethod = requestMethod.toUpperCase();
            builder.method(requestMethod, requestBody);
        }

        String[] headers = HttpProcessingUnitUtil.getInstance().getRequestHeaders(parameterRuntime);
        if (headers != null && headers.length > 0) {
            builder.headers(headers);
        }
        
        Duration duration = Duration.ofSeconds(parameterRuntime.getParameterValueList(HttpProcessingUnitConstants.TIMEOUT_PARAMTER).getValueAsInteger());
        if (duration != null) {
            builder.timeout(duration);
        }
        
        return builder.version(HttpProcessingUnitUtil.getInstance().getHttpVersion(parameterRuntime)).build();
    }
    
    /*
        final String requestMethod = getParameterRuntime().getParameterValueList(REQUEST_METHOD_PARAMETER).getValueAsString().trim();
        final BodyPublisher requestBody = null;
        if (requestMethod.equalsIgnoreCase("GET") || requestMethod.equalsIgnoreCase("DELETE")) {
            requestMethod = requestMethod.toUpperCase();
        } else if (requestMethod.equalsIgnoreCase("POST") || requestMethod.equalsIgnoreCase("PUT") || requestMethod.equalsIgnoreCase("PATCH")) {
            requestMethod = requestMethod.toUpperCase();
            requestBody = HttpRequest.BodyPublishers.ofString(getParameterRuntime().getParameterValueList(REQUEST_BODY_PARAMETER).getValueAsString().trim()); 
        }
        
        httpRequest = HttpRequest.newBuilder()
                .method(requestMethod, requestBody)
                .uri(requestUri)
                .headers(HttpProcessingUnitUtil.getInstance().getRequestHeaders(getParameterRuntime()))
                .timeout(Duration.ofSeconds(getParameterRuntime().getParameterValueList(TIMEOUT_PARAMTER).getValueAsInteger()))
                .version(HttpProcessingUnitUtil.getInstance().getHttpVersion(getParameterRuntime()))
                .build();
 
     */
}

/*
 * HttpProcessingUnitConstants.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.processing.unit.collection.http;

import com.github.toolarium.processing.unit.ParameterDefinitionBuilder;
import com.github.toolarium.processing.unit.dto.ParameterDefinition;


/**
 * Defines the parameters for the {@link HttpProcessingUnit}.
 * 
 * @author patrick
 */
public interface HttpProcessingUnitConstants {
    
    /** PROTOCOL_PARAMETER: the protocol parameter. It is optional. */
    ParameterDefinition PROTOCOL_PARAMETER = new ParameterDefinitionBuilder().name("protocol").defaultValue("http").description("The protocol (default: https).").build();
    
    /** DOMAIN_PARAMETER: the domain parameter. It is optional. */
    ParameterDefinition DOMAIN_PARAMETER = new ParameterDefinitionBuilder().name("domain").defaultValue("localhost").description("The domain (default: localhost).").build();

    /** PORT_PARAMETER: the port parameter. It is optional. */
    ParameterDefinition PORT_PARAMETER = new ParameterDefinitionBuilder().name("port").defaultValue(8080).description("The port (default: 8080).").build();

    /** PATH_PARAMETER: the path parameter. It is optional. */
    ParameterDefinition PATH_PARAMETER = new ParameterDefinitionBuilder().name("path").defaultValue("/").description("The path (default: /).").build();

    /** URL_PARAMETER: the url parameter. It is optional. */
    ParameterDefinition URL_PARAMETER = new ParameterDefinitionBuilder().name("url").emptyValueIsAllowed().description("The url: if this is defined than the parameters protcol, domain, port and port are ignored.").build();

    /** HTTP_VERSION_PARAMETER: define the http version. */
    ParameterDefinition HTTP_VERSION_PARAMETER = new ParameterDefinitionBuilder().name("httpVersion").defaultValue("2").description("Define the http version: 2, 1.1, (default: 2).").build();

    /** VERIFY_CERTIFICATE_PARAMETER: the verify certificate parameter in case of a https call. */
    ParameterDefinition VERIFY_CERTIFICATE_PARAMETER = new ParameterDefinitionBuilder().name("verifyCertificate").defaultValue(true).description("The verify certificate parameter in case of a https call (default: true).").build();

    /** METHOD_PARAMETER: the method parameter. */
    ParameterDefinition REQUEST_METHOD_PARAMETER = new ParameterDefinitionBuilder().name("method").defaultValue("GET").description("The request method to use (default: GET).").build();

    /** REQUEST_QUERY_PARAMETER: the method parameter. */
    ParameterDefinition REQUEST_QUERY_PARAMETER = new ParameterDefinitionBuilder().name("query").defaultValue("").emptyValueIsAllowed().description("The request query parameter (default is empty).").build();

    /** REQUESTR_HEADER_PARAMETER: the header parameter. */
    ParameterDefinition REQUESTR_HEADER_PARAMETER = new ParameterDefinitionBuilder().name("header").emptyValueIsAllowed().maxOccurs(Integer.MAX_VALUE).description("The request header parameter.").build();

    /** REQUEST_BODY_PARAMETER: the request body. */
    ParameterDefinition REQUEST_BODY_PARAMETER = new ParameterDefinitionBuilder().name("body").defaultValue("").emptyValueIsAllowed().description("The request body parameter (default is empty).").build();

    /** NUMBER_OF_CALLS_PARAMTER: the number of calls. */
    ParameterDefinition NUMBER_OF_CALLS_PARAMTER = new ParameterDefinitionBuilder().name("numberOfCalls").defaultValue(1).description("The number of calls to execute.").build();

    /** TIMEOUT_PARAMTER: the timeout. */
    ParameterDefinition TIMEOUT_PARAMTER = new ParameterDefinitionBuilder().name("timeout").defaultValue(60).description("The timeout in seconds of the request.").build();

    /** RETRY_AFTER_TIMEOUT_PARAMTER: the timeout. */
    ParameterDefinition RETRY_AFTER_TIMEOUT_PARAMTER = new ParameterDefinitionBuilder().name("retryAfterTimeout").defaultValue(true).description("The retry after timeout (default: true).").build();

    /** SLEEPTIME_BEFORE_RETRY_PARAMTER: the timeout. */
    ParameterDefinition SLEEPTIME_BEFORE_RETRY_PARAMTER = new ParameterDefinitionBuilder().name("sleeptimeBeforeRetry").defaultValue(3).description("The sleeptime before retry in seconds (default: 3).").build();

    /** FOLLOW_REDIRECT_PARAMETER: define it redirect will be followed. */
    ParameterDefinition FOLLOW_REDIRECT_PARAMETER = new ParameterDefinitionBuilder().name("followRedirect").defaultValue(true).description("Define it redirect will be followed.").build();

    /** EXPECTED_RESPONSE_CODE_PARAMTER: the expected response code parameter. */
    ParameterDefinition EXPECTED_RESPONSE_CODE_PARAMTER = new ParameterDefinitionBuilder().name("expectedResponseCode").defaultValue(200).description("The expected response code (default: 200).").build();
}

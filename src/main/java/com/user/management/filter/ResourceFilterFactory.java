package com.user.management.filter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.Priorities;
import javax.ws.rs.container.DynamicFeature;
import javax.ws.rs.ext.Provider;


/**
 * Add the SecurityContextFilter to the list of Filters to apply to requests
 *
 * This factory is registered with the Web Context:
 *
 * <code>
 *     <init-param>
            <param-name>com.sun.jersey.spi.container.ResourceFilters</param-name>
            <param-value>com.porterhead.com.porterhead.rest.filter.ResourceFilterFactory</param-value>
        </init-param>
 * </code>
 *
 *
 * @author: Iain Porter
 */
@Component
@Provider
public class ResourceFilterFactory implements DynamicFeature  {
    @Autowired
    private SecurityContextFilter securityContextFilter;

    @Override
    public void configure(javax.ws.rs.container.ResourceInfo resourceInfo, javax.ws.rs.core.FeatureContext configuration) {
        configuration.register(securityContextFilter, Priorities.AUTHORIZATION);
    };

}

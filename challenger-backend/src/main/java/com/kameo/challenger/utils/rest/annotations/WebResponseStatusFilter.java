package com.kameo.challenger.utils.rest.annotations;


import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.lang.annotation.Annotation;

@Provider
public class WebResponseStatusFilter implements ContainerResponseFilter {

    @Override
    public void filter(ContainerRequestContext containerRequestContext, ContainerResponseContext containerResponseContext) throws IOException {
        if (containerResponseContext.getStatus() == 200) {
            for (Annotation annotation : containerResponseContext.getEntityAnnotations()) {
                if(annotation instanceof WebResponseStatus){
                    containerResponseContext.setStatus(((WebResponseStatus) annotation).value());
                    break;
                }
            }
        }
    }
}
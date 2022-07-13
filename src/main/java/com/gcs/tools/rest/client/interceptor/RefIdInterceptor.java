package com.gcs.tools.rest.client.interceptor;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

import javax.ws.rs.client.Invocation;

import static com.gcs.tools.rest.client.RestClient.X_CORRELATION_ID;
import static java.util.Optional.ofNullable;

@Slf4j
public class RefIdInterceptor implements RequestInterceptor
{
    @Override
    public void intercept(Invocation.Builder invocationBuilder_, String url_)
    {

        String refId = ofNullable(MDC.get(X_CORRELATION_ID)).orElseGet(() -> String.valueOf(System.nanoTime()));
        invocationBuilder_.header(X_CORRELATION_ID, null);
        invocationBuilder_.header(X_CORRELATION_ID, refId);
        MDC.put(X_CORRELATION_ID, refId);


        if (_logger.isTraceEnabled())
        {
            _logger.trace("[{}] sending request to:{}", refId, url_);
        }
    }
}

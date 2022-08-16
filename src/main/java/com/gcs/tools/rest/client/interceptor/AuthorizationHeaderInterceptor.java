package com.gcs.tools.rest.client.interceptor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.ws.rs.client.Invocation;

import static java.lang.String.format;

@Slf4j
@RequiredArgsConstructor
public class AuthorizationHeaderInterceptor implements RequestInterceptor
{
    public static final String AUTHORIZATION = "Authorization";

    private final String _jwtToken;





    @Override
    public void intercept(Invocation.Builder invocationBuilder_, String url_)
    {
        String jwtheader = format("Bearer %s", _jwtToken);

        invocationBuilder_.header(AUTHORIZATION, jwtheader);
        if (_logger.isTraceEnabled())
        {
            _logger.trace("adding header [{}] to request URL: {}", jwtheader, url_);
        }
    }
}

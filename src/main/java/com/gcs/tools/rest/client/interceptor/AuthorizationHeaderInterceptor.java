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
        invocationBuilder_.header(AUTHORIZATION, format("Bearer %s", _jwtToken));
    }
}

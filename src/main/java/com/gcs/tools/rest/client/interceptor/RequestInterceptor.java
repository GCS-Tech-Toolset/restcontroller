package com.gcs.tools.rest.client.interceptor;

import javax.ws.rs.client.Invocation;

public interface RequestInterceptor
{
    void intercept(Invocation.Builder invocationBuilder_, String url_);
}

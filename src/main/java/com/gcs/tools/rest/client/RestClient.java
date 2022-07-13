/****************************************************************************
 * FILE: HttpClient.java
 * DSCRPT: 
 ****************************************************************************/


package com.gcs.tools.rest.client;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import com.gcs.tools.rest.client.exception.RestClientException;
import com.gcs.tools.rest.client.interceptor.RefIdInterceptor;
import com.gcs.tools.rest.client.interceptor.RequestInterceptor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.glassfish.jersey.apache.connector.ApacheClientProperties;
import org.glassfish.jersey.apache.connector.ApacheConnectorProvider;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;
import org.slf4j.MDC;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static com.gcs.tools.rest.client.RestClient.CustomRestClientBuilder.createClientConfig;
import static com.gcs.tools.rest.client.RestClient.CustomRestClientBuilder.prepareDefaultInterceptors;
import static java.util.Optional.ofNullable;
import static org.apache.commons.collections.CollectionUtils.isEmpty;


@Slf4j
@Builder
@AllArgsConstructor
public class RestClient
{
    public static final String X_CORRELATION_ID = "X-Correlation-ID";

    @Builder.Default
    private int _connectionTimeout = DEFAULT_CONNECTION_TIMEOUT;
    @Builder.Default
    private int _readTimeout = DEFAULT_READ_TIMEOUT;
    private PoolingHttpClientConnectionManager _connectionManager;
    @Getter
    private Client _httpClient;
    private Collection<RequestInterceptor> _interceptors;





    public static RestClientBuilder builder()
    {
        return new CustomRestClientBuilder();
    }





    public static class CustomRestClientBuilder extends RestClientBuilder
    {
        @Override
        public RestClient build()
        {
            final var restClient = super.build();
            ClientConfig config = createClientConfig(restClient._connectionTimeout, restClient._readTimeout, restClient._connectionManager);
            restClient._httpClient = ClientBuilder.newClient(config);
            if (isEmpty(restClient._interceptors)) {
                restClient._interceptors = prepareDefaultInterceptors();
            } else {
                Collection<RequestInterceptor> mergedInterceptors = new ArrayList<>();
                mergedInterceptors.addAll(prepareDefaultInterceptors());
                mergedInterceptors.addAll(restClient._interceptors);
                restClient._interceptors = mergedInterceptors;
            }
            return restClient;
        }





        static ClientConfig createClientConfig(int connectionTimeout_, int readTimout_, PoolingHttpClientConnectionManager cm_)
        {
            final ClientConfig config = new ClientConfig();
            int connectionTimeout = connectionTimeout_ > 0 ? connectionTimeout_ : DEFAULT_CONNECTION_TIMEOUT;
            int readTimeout = readTimout_ > 0 ? readTimout_ : DEFAULT_READ_TIMEOUT;
            config.property(ClientProperties.CONNECT_TIMEOUT, connectionTimeout);
            config.property(ClientProperties.READ_TIMEOUT, readTimeout);
            config.register(JacksonJsonProvider.class);
            if (cm_ != null)
            {
                config.property(ApacheClientProperties.CONNECTION_MANAGER, cm_);
                config.connectorProvider(new ApacheConnectorProvider());
            }
            return config;
        }

        static Collection<RequestInterceptor> prepareDefaultInterceptors()
        {
            List<RequestInterceptor> defaultInterceptors = new ArrayList<>();
            defaultInterceptors.add(new RefIdInterceptor());
            return defaultInterceptors;
        }
    }


    @Builder.Default
    private ObjectMapper _objectMapper = new ObjectMapper();


    public static final int DEFAULT_CONNECTION_TIMEOUT = 1_000;
    public static final int DEFAULT_READ_TIMEOUT = 4_000;





    public RestClient()
    {
        this(DEFAULT_CONNECTION_TIMEOUT, DEFAULT_READ_TIMEOUT, null);
    }





    public RestClient(int connectionTimeout_, int readTimeout_)
    {
        this(connectionTimeout_, readTimeout_, null);
    }





    public RestClient(int connectionTimeout_, int readTimeout_, PoolingHttpClientConnectionManager cm_)
    {
        ClientConfig config = createClientConfig(connectionTimeout_, readTimeout_, cm_);
        _objectMapper = new ObjectMapper();
        _httpClient = ClientBuilder.newClient(config);
        _interceptors = prepareDefaultInterceptors();
    }





    public Response getEntity(String url_)
    {
        return getProcessedInvocation(url_).get();
    }





    public Response getEntity(String url_, String refId_)
    {
        return getProcessedInvocation(url_, refId_).get();
    }





    public <T> T getEntity(String url_, Class<T> responseClass_) throws RestClientException
    {
        return processResponse(getProcessedInvocation(url_)::get, responseClass_);
    }





    public <T> T getEntity(String url_, Class<T> responseClass_, String refId_) throws RestClientException
    {
        return processResponse(getProcessedInvocation(url_, refId_)::get, responseClass_);
    }





    public <T> T getEntityFromString(String url_, Class<T> responseClass_) throws IOException, RestClientException
    {
        return processResponseFromString(getProcessedInvocation(url_)::get, responseClass_);
    }





    public <T> T getEntityFromString(String url_, Class<T> responseClass_, String refId_) throws IOException, RestClientException
    {
        return processResponseFromString(getProcessedInvocation(url_, refId_)::get, responseClass_);
    }





    public final Response postEntity(String url_, Object out_) throws IOException
    {
        return getProcessedInvocation(url_).post(Entity.json(out_));
    }





    public final Response postEntity(String url_, Object out_, String refId_) throws IOException
    {
        return getProcessedInvocation(url_, refId_).post(Entity.json(out_));
    }





    public <T> T postEntity(String url_, Object out_, Class<T> responseClass_) throws RestClientException
    {
        return processResponse(() -> getProcessedInvocation(url_).post(Entity.json(out_)), responseClass_);
    }





    public <T> T postEntity(String url_, Object out_, Class<T> responseClass_, String refId_) throws RestClientException
    {
        return processResponse(() -> getProcessedInvocation(url_, refId_).post(Entity.json(out_)), responseClass_);
    }





    public <T> T postEntityFromString(String url_, Object out_, Class<T> responseClass_) throws IOException, RestClientException
    {
        return processResponseFromString(() -> getProcessedInvocation(url_).post(Entity.json(out_)), responseClass_);
    }





    public <T> T postEntityFromString(String url_, Object out_, Class<T> responseClass_, String refId_) throws IOException, RestClientException
    {
        return processResponseFromString(() -> getProcessedInvocation(url_, refId_).post(Entity.json(out_)), responseClass_);
    }





    private <T> T processResponse(Supplier<Response> responseSupplier_, Class<T> responseClass_) throws RestClientException
    {
        T responseObj = null;
        Response rsps = null;
        String refId = resolveRefId();
        try
        {
            rsps = responseSupplier_.get();
            if (rsps.getStatusInfo().getFamily().equals(Response.Status.Family.SUCCESSFUL))
            {
                responseObj = rsps.readEntity(responseClass_);
                if (_logger.isTraceEnabled())
                {
                    _logger.trace("[{}] {} rsps: {}", refId, rsps.getStatus(), responseObj);
                }
                return responseObj;
            }
            else
            {
                _logger.error("[{}] not-ok, error:[{}:{}]",
                    refId,
                    rsps.getStatus(),
                    Response.Status.fromStatusCode(rsps.getStatus()).getReasonPhrase());
                throw new RestClientException("[{}] not-ok, error:[{}:{}]");
            }
        }
        finally
        {
            ofNullable(rsps).ifPresent(Response::close);
        }
    }





    private <T> T processResponseFromString(Supplier<Response> responseSupplier_, Class<T> responseClass_) throws IOException, RestClientException
    {
        Response rsps = null;
        String refId = resolveRefId();
        try
        {
            rsps = responseSupplier_.get();
            if (rsps.getStatusInfo().getFamily().equals(Response.Status.Family.SUCCESSFUL))
            {
                String response = rsps.readEntity(String.class);
                if (_logger.isTraceEnabled())
                {
                    _logger.trace("[{}] {} rsps: {}", refId, rsps.getStatus(), response);
                }
                return _objectMapper.readValue(response, responseClass_);
            }
            else
            {
                _logger.error("[{}] not-ok, error:[{}:{}]",
                    refId,
                    rsps.getStatus(),
                    Response.Status.fromStatusCode(rsps.getStatus()).getReasonPhrase());
                throw new RestClientException("[{}] not-ok, error:[{}:{}]");
            }
        }
        finally
        {
            ofNullable(rsps).ifPresent(Response::close);
        }
    }





    private Invocation.Builder getProcessedInvocation(String url_)
    {
        final var builder = _httpClient
            .target(url_)
            .request()
            .accept(MediaType.APPLICATION_JSON);
        Stream.ofNullable(_interceptors)
            .flatMap(Collection::stream)
            .forEach(requestInterceptor_ -> requestInterceptor_.intercept(builder, url_));
        return builder;
    }





    private Invocation.Builder getProcessedInvocation(String url_, String refId_)
    {
        ofNullable(refId_)
            .ifPresent(refId -> MDC.put(X_CORRELATION_ID, refId));
        final var builder = _httpClient
            .target(url_)
            .request()
            .accept(MediaType.APPLICATION_JSON);
        Stream.ofNullable(_interceptors)
            .flatMap(Collection::stream)
            .forEach(requestInterceptor_ -> requestInterceptor_.intercept(builder, url_));
        return builder;
    }





    private String resolveRefId()
    {
        return ofNullable(MDC.get(X_CORRELATION_ID)).orElseGet(() -> String.valueOf(System.nanoTime()));
    }


}

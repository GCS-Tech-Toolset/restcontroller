/****************************************************************************
 * FILE: HttpClient.java
 * DSCRPT: 
 ****************************************************************************/





package com.gcs.tools.rest.client;





import java.io.IOException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.glassfish.jersey.apache.connector.ApacheClientProperties;
import org.glassfish.jersey.apache.connector.ApacheConnectorProvider;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import lombok.extern.slf4j.Slf4j;





@Slf4j
public class RestClient
{
    private static final String                 X_CORRELATION_ID = "X-Correlation-ID";


    private Client                              _httpClient;
    private final ObjectMapper                  _objectMapper;


    public static final int                    DEFAULT_CONNECTION_TIMEOUT = 1_000;
    public static final int                    DEFAULT_READ_TIMEOUT = 4_000;





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
    }





    public Response getEntity(String url_) throws IOException
    {
        final long refId = System.nanoTime();
        return getEntity(url_, Long.toString(refId));
    }





    public Response getEntity(String url_, String refId_) throws IOException
    {
        logRequest(url_, refId_);
        return _httpClient
                .target(url_)
                .request()
                .header(X_CORRELATION_ID, refId_)
                .accept(MediaType.APPLICATION_JSON)
                .get();
    }





    public  <T> T getEntity(String url_, Class<T> responseClass_) throws IOException
    {
        final long refId = System.nanoTime();
        return getEntity(url_, responseClass_, Long.toString(refId));
    }





    public <T> T getEntity(String url_, Class<T> responseClass_, String refId_) throws IOException
    {
        T responseObj = null;
        logRequest(url_, refId_);
        final Response rsps = _httpClient
                .target(url_)
                .request()
                .header(X_CORRELATION_ID, refId_)
                .accept(MediaType.APPLICATION_JSON)
                .get();
        if (rsps.getStatusInfo().getFamily().equals(Response.Status.Family.SUCCESSFUL))
        {
            responseObj = rsps.readEntity(responseClass_);
            if (_logger.isTraceEnabled())
            {
                _logger.trace("[{}] {} rsps: {}", refId_, rsps.getStatus(), responseObj);
            }
        }
        else
        {
            _logger.error("[{}] not-ok, error:[{}:{}]",
                    refId_,
                    rsps.getStatus(),
                    Response.Status.fromStatusCode(rsps.getStatus()).getReasonPhrase());
        }
        rsps.close();
        return responseObj;
    }





    public <T> T getEntityFromString(String url_, Class<T> responseClass_) throws IOException
    {
        final long refId = System.nanoTime();
        return getEntityFromString(url_, responseClass_, Long.toString(refId));
    }





    public <T> T getEntityFromString(String url_, Class<T> responseClass_, String refId_) throws IOException
    {
        T responseObj = null;
        logRequest(url_, refId_);
        final Response rsps = _httpClient
                .target(url_)
                .request()
                .accept(MediaType.APPLICATION_JSON)
                .header(X_CORRELATION_ID, refId_)
                .get();
        if (rsps.getStatusInfo().getFamily().equals(Response.Status.Family.SUCCESSFUL))
        {

            final String str = rsps.readEntity(String.class);
            if (_logger.isTraceEnabled())
            {
                _logger.trace("[{}] {} rsps: {}", refId_, rsps.getStatus(), responseObj);
            }
            responseObj = _objectMapper.readValue(str, responseClass_);
        }
        else
        {
            _logger.error("[{}] not-ok, error:[{}:{}]",
                    refId_,
                    rsps.getStatus(),
                    Response.Status.fromStatusCode(rsps.getStatus()).getReasonPhrase());
        }
        rsps.close();
        return responseObj;
    }





    public final Response postEntity(String url_, Object out_) throws IOException
    {
        final Long refId = System.nanoTime();
        return postEntity(url_, out_, refId.toString());
    }





    public final Response postEntity(String url_, Object out_, String refId_) throws IOException
    {
        logRequest(url_, refId_);
        return _httpClient
                .target(url_)
                .request()
                .accept(MediaType.APPLICATION_JSON)
                .header(X_CORRELATION_ID, refId_)
                .post(Entity.json(out_));
    }





    public <T> T postEntity(String url_, Object out_, Class<T> responseClass_) throws IOException
    {
        final long refid = System.nanoTime();
        return postEntity(url_, out_, responseClass_, Long.toString(refid));
    }





    public <T> T postEntity(String url_, Object out_, Class<T> responseClass_, String refId_) throws IOException
    {
        T responseObj = null;
        logRequest(url_, refId_);
        final Response rsps = _httpClient
                .target(url_)
                .request()
                .accept(MediaType.APPLICATION_JSON)
                .header(X_CORRELATION_ID, refId_)
                .post(Entity.json(out_));
        if (rsps.getStatusInfo().getFamily().equals(Response.Status.Family.SUCCESSFUL))
        {
            responseObj = rsps.readEntity(responseClass_);
            if (_logger.isTraceEnabled())
            {
                _logger.trace("[{}] {} rsps: {}", refId_, rsps.getStatus(), responseObj);
            }
            rsps.close();
            return responseObj;
        }

        rsps.close();
        _logger.error("[{}] not-ok, error:[{}:{}]",
                refId_,
                rsps.getStatus(),
                Response.Status.fromStatusCode(rsps.getStatus()).getReasonPhrase());
        throw new IOException(rsps.getStatusInfo().getReasonPhrase());
    }





    public <T> T postEntityFromString(String url_, Object out_, Class<T> responseClass_) throws IOException
    {
        final long refid = System.nanoTime();
        return postEntityFromString(url_, out_, responseClass_, Long.toString(refid));
    }





    public <T> T postEntityFromString(String url_, Object out_, Class<T> responseClass_, String refId) throws IOException
    {
        T responseObj = null;
        logRequest(url_, refId);
        final Response rsps = _httpClient
                .target(url_)
                .request()
                .accept(MediaType.APPLICATION_JSON)
                .header(X_CORRELATION_ID, refId)
                .post(Entity.json(out_));
        if (rsps.getStatusInfo().getFamily().equals(Response.Status.Family.SUCCESSFUL))
        {

            final String str = rsps.readEntity(String.class);
            if (_logger.isTraceEnabled())
            {
                _logger.trace("[{}] {} rsps: {}", refId, rsps.getStatus(), responseObj);
            }
            responseObj = _objectMapper.readValue(str, responseClass_);
            rsps.close();
            return responseObj;
        }
        else
        {
            rsps.close();
            _logger.error("[{}] not-ok, error:[{}:{}]",
                    refId,
                    rsps.getStatus(),
                    Response.Status.fromStatusCode(rsps.getStatus()).getReasonPhrase());
            throw new IOException(rsps.getStatusInfo().getReasonPhrase());
        }
    }





    private void logRequest(String url_, String refid_)
    {
        if (_logger.isTraceEnabled())
        {
            _logger.trace("[{}] sending request to:{}", refid_, url_);
        }
    }





    private ClientConfig createClientConfig(int connectionTimeout_, int readTimout_, PoolingHttpClientConnectionManager cm_)
    {
        final ClientConfig config = new ClientConfig();
        int connectionTimeout = connectionTimeout_ > 0 ? connectionTimeout_ : DEFAULT_CONNECTION_TIMEOUT;
        int readTimeout = readTimout_ > 0 ? readTimout_ : DEFAULT_READ_TIMEOUT;
        config.property(ClientProperties.CONNECT_TIMEOUT, connectionTimeout);
        config.property(ClientProperties.READ_TIMEOUT, readTimeout);
        config.register(JacksonJsonProvider.class);
        if(cm_ != null)
        {
            config.property(ApacheClientProperties.CONNECTION_MANAGER, cm_);
            config.connectorProvider(new ApacheConnectorProvider());
        }
        return config;
    }





    //This method has been added for testing purposes. Need to be able to check client configuration in unit tests.
    public Configuration getClientConfig()
    {
        return _httpClient.getConfiguration();
    }
}

/****************************************************************************
 * FILE: HttpClient.java
 * DSCRPT: 
 ****************************************************************************/





package com.gcs.tools.rest.client;





import java.io.IOException;



import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;



import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;



import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;



import lombok.extern.slf4j.Slf4j;





@Slf4j
public class RestClient
{
    private static final String X_CORRELATION_ID = "X-Correlation-ID";


    private Client             _httpClient;
    private final ObjectMapper _objectMapper;


    private int _connectionTimeout = 1_000;
    private int _readTimout        = 4_000;





    public RestClient()
    {
        _objectMapper = new ObjectMapper();
        initializeHttpClient();
    }





    public RestClient(int connectionTimeout_, int readTimeout_)
    {
        _connectionTimeout = connectionTimeout_;
        _readTimout = readTimeout_;
        _objectMapper = new ObjectMapper();
        initializeHttpClient();
    }





    public Response getEntity(String url_) throws IOException
    {
        final long refid = System.nanoTime();
        logRequest(url_, refid);
        return _httpClient
                .target(url_)
                .request()
                .header(X_CORRELATION_ID, refid)
                .accept(MediaType.APPLICATION_JSON)
                .get();
    }





    public <T> T getEntity(String url_, Class<T> responseClass_) throws IOException
    {
        T responseObj = null;
        final long refid = System.nanoTime();
        logRequest(url_, refid);
        final Response rsps = _httpClient
                .target(url_)
                .request()
                .header(X_CORRELATION_ID, refid)
                .accept(MediaType.APPLICATION_JSON)
                .get();
        if (rsps.getStatusInfo().getFamily().equals(Response.Status.Family.SUCCESSFUL))
        {
            responseObj = rsps.readEntity(responseClass_);
            if (_logger.isTraceEnabled())
            {
                _logger.trace("[{}] {} rsps: {}", refid, rsps.getStatus(), responseObj);
            }
        }
        else
        {
            _logger.error("[{}] not-ok, error:[{}:{}]",
                    refid,
                    rsps.getStatus(),
                    Response.Status.fromStatusCode(rsps.getStatus()).getReasonPhrase());
        }
        rsps.close();
        return responseObj;
    }





    public <T> T getEntityFromString(String url_, Class<T> responseClass_) throws IOException
    {
        T responseObj = null;
        final long refid = System.nanoTime();
        logRequest(url_, refid);
        final Response rsps = _httpClient
                .target(url_)
                .request()
                .accept(MediaType.APPLICATION_JSON)
                .header(X_CORRELATION_ID, refid)
                .get();
        if (rsps.getStatusInfo().getFamily().equals(Response.Status.Family.SUCCESSFUL))
        {

            final String str = rsps.readEntity(String.class);
            if (_logger.isTraceEnabled())
            {
                _logger.trace("[{}] {} rsps: {}", refid, rsps.getStatus(), responseObj);
            }
            responseObj = _objectMapper.readValue(str, responseClass_);
        }
        else
        {
            _logger.error("[{}] not-ok, error:[{}:{}]",
                    refid,
                    rsps.getStatus(),
                    Response.Status.fromStatusCode(rsps.getStatus()).getReasonPhrase());
        }
        rsps.close();
        return responseObj;
    }





    public final Response postEntity(String url_, Object out_) throws IOException
    {
        final long refid = System.nanoTime();
        logRequest(url_, refid);
        return _httpClient
                .target(url_)
                .request()
                .accept(MediaType.APPLICATION_JSON)
                .header(X_CORRELATION_ID, refid)
                .post(Entity.json(out_));
    }





    public <T> T postEntity(String url_, Object out_, Class<T> responseClass_) throws IOException
    {
        T responseObj = null;
        final long refid = System.nanoTime();
        logRequest(url_, refid);
        final Response rsps = _httpClient
                .target(url_)
                .request()
                .accept(MediaType.APPLICATION_JSON)
                .header(X_CORRELATION_ID, refid)
                .post(Entity.json(out_));
        if (rsps.getStatusInfo().getFamily().equals(Response.Status.Family.SUCCESSFUL))
        {
            responseObj = rsps.readEntity(responseClass_);
            if (_logger.isTraceEnabled())
            {
                _logger.trace("[{}] {} rsps: {}", refid, rsps.getStatus(), responseObj);
            }
        }
        else
        {
            _logger.error("[{}] not-ok, error:[{}:{}]",
                    refid,
                    rsps.getStatus(),
                    Response.Status.fromStatusCode(rsps.getStatus()).getReasonPhrase());
        }
        rsps.close();
        return responseObj;
    }





    public <T> T postEntityFromString(String url_, Object out_, Class<T> responseClass_) throws IOException
    {
        T responseObj = null;
        final long refid = System.nanoTime();
        logRequest(url_, refid);
        final Response rsps = _httpClient
                .target(url_)
                .request()
                .accept(MediaType.APPLICATION_JSON)
                .header(X_CORRELATION_ID, refid)
                .post(Entity.json(out_));
        if (rsps.getStatusInfo().getFamily().equals(Response.Status.Family.SUCCESSFUL))
        {

            final String str = rsps.readEntity(String.class);
            if (_logger.isTraceEnabled())
            {
                _logger.trace("[{}] {} rsps: {}", refid, rsps.getStatus(), responseObj);
            }
            responseObj = _objectMapper.readValue(str, responseClass_);
        }
        else
        {
            _logger.error("[{}] not-ok, error:[{}:{}]",
                    refid,
                    rsps.getStatus(),
                    Response.Status.fromStatusCode(rsps.getStatus()).getReasonPhrase());
        }
        rsps.close();
        return responseObj;
    }





    private final void logRequest(String url_, final long refid_)
    {
        if (_logger.isTraceEnabled())
        {
            _logger.trace("[{}] sending request to:{}", refid_, url_);
        }
    }





    private void initializeHttpClient()
    {
        final ClientConfig config = new ClientConfig();
        config.property(ClientProperties.CONNECT_TIMEOUT, _connectionTimeout);
        config.property(ClientProperties.READ_TIMEOUT, _readTimout);
        config.register(JacksonJsonProvider.class);
        _httpClient = ClientBuilder.newClient(config);
    }


}

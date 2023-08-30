/**
 * Author: kgoldstein
 * Date: Feb 24, 2023
 * Terms: Expressly forbidden for use without written consent from the author
 */





package com.gcs.tools.rest;





import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;



import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;



import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;



import lombok.extern.slf4j.Slf4j;





@Slf4j
public class HttpClientBuilder
{
    public static int DEFAULT_CONNECTION_TIMEOUT = 1_000;
    public static int DEFAULT_READ_TIMEOUT = 4_000;




    public static ClientConfig createClientConfig()
    {
        return createClientConfig(DEFAULT_CONNECTION_TIMEOUT, DEFAULT_READ_TIMEOUT);
    }





    public static ClientConfig createClientConfig(int connectionTimeout_, int readTimout_)
    {
        final ClientConfig config = new ClientConfig();

        final int connectionTimeout = connectionTimeout_ > 0 ? connectionTimeout_ : DEFAULT_CONNECTION_TIMEOUT;
        _logger.debug("{}={}", ClientProperties.CONNECT_TIMEOUT, connectionTimeout);
        config.property(ClientProperties.CONNECT_TIMEOUT, connectionTimeout);

        final int readTimeout = readTimout_ > 0 ? readTimout_ : DEFAULT_READ_TIMEOUT;
        _logger.debug("{}={}", ClientProperties.READ_TIMEOUT, readTimeout);
        config.property(ClientProperties.READ_TIMEOUT, readTimeout);

        final int cPoolSz = 5;
        _logger.debug("{}={}", ClientProperties.ASYNC_THREADPOOL_SIZE, cPoolSz);
        config.property(ClientProperties.ASYNC_THREADPOOL_SIZE, cPoolSz);

        final boolean keepalive = true;
        _logger.debug("{}={}", "http.keepalive", Boolean.toString(keepalive));
        System.setProperty("http.keepalive", Boolean.toString(keepalive));

        _logger.debug("{}={}", "http.maxConnections", Integer.toString(cPoolSz));
        System.setProperty("http.maxConnections", Integer.toString(cPoolSz));

        _logger.debug("regestering jackson-json provider...");
        config.register(JacksonJsonProvider.class);

        // done
        return config;
    }





    public static Client createClient()
    {
        return createClient(DEFAULT_CONNECTION_TIMEOUT, DEFAULT_READ_TIMEOUT);
    }



    public static Client createClient(int connectTimeout_, int readTimeout_)
    {
        ClientConfig cfg = createClientConfig(connectTimeout_, readTimeout_);
        return ClientBuilder.newClient(cfg);
    }


}

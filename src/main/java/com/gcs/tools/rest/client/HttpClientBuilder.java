/**
 * Author: kgoldstein
 * Date: Feb 24, 2023
 * Terms: Expressly forbidden for use without written consent from the author
 */





package com.gcs.tools.rest.client;





import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;



import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;



import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;



import lombok.extern.slf4j.Slf4j;





@Slf4j
public class HttpClientBuilder
{
    public static final int DEFAULT_CONNECTION_TIMEOUT = 1_000;
    public static final int DEFAULT_READ_TIMEOUT       = 4_000;




    static ClientConfig createClientConfig()
    {
        return createClientConfig(DEFAULT_CONNECTION_TIMEOUT, DEFAULT_READ_TIMEOUT);
    }





    static ClientConfig createClientConfig(int connectionTimeout_, int readTimout_)
    {
        final ClientConfig config = new ClientConfig();
        int connectionTimeout = connectionTimeout_ > 0 ? connectionTimeout_ : DEFAULT_CONNECTION_TIMEOUT;
        int readTimeout = readTimout_ > 0 ? readTimout_ : DEFAULT_READ_TIMEOUT;

        _logger.debug("{}={}", ClientProperties.CONNECT_TIMEOUT, connectionTimeout);
        config.property(ClientProperties.CONNECT_TIMEOUT, connectionTimeout);

        _logger.debug("{}={}", ClientProperties.READ_TIMEOUT, readTimeout);
        config.property(ClientProperties.READ_TIMEOUT, readTimeout);

        _logger.debug("regestering jackson-json provider...");
        config.register(JacksonJsonProvider.class);
        return config;
    }





    static Client createClient()
    {
        ClientConfig cfg = createClientConfig(DEFAULT_READ_TIMEOUT, DEFAULT_CONNECTION_TIMEOUT);
        return ClientBuilder.newClient(cfg);
    }


}

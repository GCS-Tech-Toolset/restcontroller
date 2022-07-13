/****************************************************************************
 * FILE: HttpClientTest.java
 * DSCRPT: 
 ****************************************************************************/


package com.gcs.tools.rest.client;


import com.gcs.tools.rest.restcontroller.HttpRestController;
import com.gcs.tools.rest.restcontroller.HttpRestException;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.Hashtable;

import static java.lang.String.format;
import static java.util.Optional.ofNullable;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;


@Slf4j
public class HttpClientTest
{

    private HttpRestController _httpSimulator;


    @Path("simulator")
    public static class Simulator
    {
        @POST
        @Path("listener")
        @Consumes(MediaType.APPLICATION_JSON)
        @Produces(MediaType.APPLICATION_JSON)
        public Response listen(
            @HeaderParam("X-Correlation-ID") String refID_,
            String str_)
        {
            _logger.info("[{}] val:{}", refID_, str_);
            return Response.ok(new TestResponse(refID_, str_, null)).build();
        }





        @POST
        @Path("listenerWithException")
        @Consumes(MediaType.APPLICATION_JSON)
        @Produces(MediaType.APPLICATION_JSON)
        public Response listenWithException(
            @HeaderParam("X-Correlation-ID") String refID_,
            String str_)
        {
            _logger.info("[{}] val:{}. Exception will be thrown right away, this is expected.", refID_, str_);
            return Response.serverError().build();
        }





        @GET
        @Path("somepath/{somestring}")
        @Produces(MediaType.APPLICATION_JSON)
        public Response get(
            @HeaderParam("X-Correlation-ID") String refID_, @PathParam("somestring") String param_)
        {
            _logger.info("[{}] val:{}", refID_, param_);
            return Response.ok(new TestResponse(refID_, param_, null)).build();
        }





        @GET
        @Path("authorizedpath/{somepathparam}")
        @Produces(MediaType.APPLICATION_JSON)
        public Response getWithAuthorization(
            @HeaderParam("Authorization") String authorization,
            @HeaderParam("X-Correlation-ID") String refID_,
            @PathParam("somepathparam") String param_)
        {
            return Response.ok(new TestResponse(refID_, param_, authorization)).build();
        }


    }





    @Before
    public void setupHttp()
    {
        try
        {
            if (_httpSimulator == null)
            {
                _httpSimulator = new HttpRestController("junit", 8000);
                _httpSimulator.register(new Simulator());
                _httpSimulator.start();
            }
        }
        catch (HttpRestException ex_)
        {
            _logger.error(ex_.toString(), ex_);
        }
        catch (Exception ex_)
        {
            _logger.error(ex_.toString(), ex_);
        }
    }





    @After
    public void shutdownHttp()
    {
        if (_httpSimulator != null)
        {
            _httpSimulator.stop();
        }
    }





    @Test
    public void testPostEntity()
    {
        try
        {
            RestClient clnt = new RestClient();
            Hashtable<String, String> body = new Hashtable<>();
            body.put("test1", "value1");
            body.put("test2", "value2");
            body.put("test3", "value3");
            TestResponse st = clnt.postEntity("http://localhost:8000/junit/simulator/listener", body, TestResponse.class);
            assertNotNull(st._refId);
            assertNotNull(st._bodyAsString);
        }
        catch (Exception ex_)
        {
            fail(ex_.toString());
        }
    }





    @Test
    public void testPostEntity_refId()
    {
        try
        {
            RestClient clnt = new RestClient();
            String body = "hello from unit tests";
            TestResponse st = clnt.postEntity("http://localhost:8000/junit/simulator/listener", body, TestResponse.class, "1234");
            assertEquals(st._refId, "1234");
            assertNotNull(st._bodyAsString);
        }
        catch (Exception ex_)
        {
            fail(ex_.toString());
        }
    }





    @Test(expected = IOException.class)
    public void testPostEntity_exception() throws IOException
    {
        RestClient clnt = new RestClient();
        Hashtable<String, String> body = new Hashtable<>();
        body.put("test1", "value1");
        TestResponse st = clnt.postEntity("http://localhost:8000/junit/simulator/listenerWithException", body, TestResponse.class);
    }





    @Test
    public void testGetEntity()
    {
        try
        {
            RestClient clnt = new RestClient();
            String param = "test_parameter";
            TestResponse st = clnt.getEntity("http://localhost:8000/junit/simulator/somepath/" + param, TestResponse.class);
            assertNotNull(st._refId);
            assertEquals(st._bodyAsString, param);
        }
        catch (Exception ex_)
        {
            fail(ex_.toString());
        }
    }





    @Test
    public void testGetEntity_withJwt()
    {
        try
        {
            RestClient clnt = new RestClient("SomeFancyToken");
            String param = "test_parameter";
            TestResponse st = clnt.getEntity(format("http://localhost:8000/junit/simulator/authorizedpath/%s", param), TestResponse.class);
            assertNotNull(st._refId);
            assertEquals(param, st._bodyAsString);
            assertEquals("Bearer SomeFancyToken", st._authorization);
        }
        catch (Exception ex_)
        {
            fail(ex_.toString());
        }
    }





    @Test
    public void testGetEntity_RefId()
    {
        try
        {
            RestClient clnt = new RestClient();
            String param = "test_parameter";
            String refId = "5678";
            TestResponse st = clnt.getEntity("http://localhost:8000/junit/simulator/somepath/" + param, TestResponse.class, refId);
            assertEquals(st._refId, refId);
            assertEquals(st._bodyAsString, param);
        }
        catch (Exception ex_)
        {
            fail(ex_.toString());
        }
    }





    @Test
    public void testRestClientConstructor_default() throws NoSuchFieldException, IllegalAccessException
    {
        RestClient client = new RestClient();
        Configuration configuration = client.getClientConfig();
        Assert.assertEquals(configuration.getProperty("jersey.config.client.connectTimeout"), 1000);
        Assert.assertEquals(configuration.getProperty("jersey.config.client.readTimeout"), 4000);
        Assert.assertNull(configuration.getProperty("jersey.config.apache.client.connectionManager"));
        Assert.assertNull(getToken(client));
    }





    @Test
    public void testRestClientConstructor_jwtToken() throws NoSuchFieldException, IllegalAccessException
    {
        RestClient client = new RestClient("SomeFancyToken");
        Configuration configuration = client.getClientConfig();
        Assert.assertEquals(configuration.getProperty("jersey.config.client.connectTimeout"), 1000);
        Assert.assertEquals(configuration.getProperty("jersey.config.client.readTimeout"), 4000);
        Assert.assertNull(configuration.getProperty("jersey.config.apache.client.connectionManager"));
        Assert.assertEquals("Bearer SomeFancyToken", getToken(client));

    }





    private String getToken(RestClient client) throws NoSuchFieldException, IllegalAccessException
    {
        var declaredField = RestClient.class.getDeclaredField("_jwtToken");
        declaredField.setAccessible(true);
        var tokenValue = declaredField.get(client);
        return ofNullable(tokenValue)
            .map(String::valueOf)
            .orElse(null);
    }





    @Test
    public void testRestClientConstructor_timeout()
    {
        RestClient client = new RestClient(2000, 5000);
        Configuration configuration = client.getClientConfig();
        Assert.assertEquals(configuration.getProperty("jersey.config.client.connectTimeout"), 2000);
        Assert.assertEquals(configuration.getProperty("jersey.config.client.readTimeout"), 5000);
        Assert.assertEquals(configuration.getProperty("jersey.config.apache.client.connectionManager"), null);
    }





    @Test
    public void testRestClientConstructor_connectionPool()
    {
        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
        RestClient client = new RestClient(3000, 6000, cm);
        Configuration configuration = client.getClientConfig();
        Assert.assertEquals(configuration.getProperty("jersey.config.client.connectTimeout"), 3000);
        Assert.assertEquals(configuration.getProperty("jersey.config.client.readTimeout"), 6000);
        Assert.assertEquals(configuration.getProperty("jersey.config.apache.client.connectionManager"), cm);
    }
}

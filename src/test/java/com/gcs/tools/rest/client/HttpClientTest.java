/****************************************************************************
 * FILE: HttpClientTest.java
 * DSCRPT: 
 ****************************************************************************/


package com.gcs.tools.rest.client;


import com.gcs.tools.rest.client.exception.RestClientException;
import com.gcs.tools.rest.client.interceptor.AuthorizationHeaderInterceptor;
import com.gcs.tools.rest.client.interceptor.RequestInterceptor;
import com.gcs.tools.rest.restcontroller.HttpRestController;
import com.gcs.tools.rest.restcontroller.HttpRestException;
import lombok.RequiredArgsConstructor;
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
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Hashtable;

import static com.gcs.tools.rest.client.RestClient.X_CORRELATION_ID;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
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
            return Response.ok(new TestResponse(refID_, str_)).build();
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
            return Response.ok(new TestResponse(refID_, param_)).build();
        }





        @GET
        @Path("withauthorization")
        @Produces(MediaType.APPLICATION_JSON)
        public Response getWithAuthorization(
            @HeaderParam("X-Correlation-ID") String refID_,
            @HeaderParam("Authorization") String authorization_)
        {
            _logger.info("[{}] val:{}", refID_, authorization_);
            return Response.ok(new TestAuthResponse(refID_, authorization_)).build();
        }


    }


    @RequiredArgsConstructor
    public static class TestRefIdInterceptor implements RequestInterceptor
    {

        private final String _refId;





        @Override
        public void intercept(Invocation.Builder invocationBuilder_, String url_)
        {
            invocationBuilder_.header(X_CORRELATION_ID, _refId);
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
            String refId = "1463415";
            TestResponse st = clnt.postEntity("http://localhost:8000/junit/simulator/listener", body, TestResponse.class, refId);
            assertEquals(refId, st.getRefId());
            assertNotNull(st.getBodyAsString());
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
            TestResponse st = clnt.postEntity("http://localhost:8000/junit/simulator/listener", body, TestResponse.class);
            assertNotNull(st.getRefId());
            assertEquals(body, st.getBodyAsString());
        }
        catch (Exception ex_)
        {
            fail(ex_.toString());
        }
    }





    @Test(expected = RestClientException.class)
    public void testPostEntity_exception() throws RestClientException
    {
        RestClient clnt = new RestClient();
        Hashtable<String, String> body = new Hashtable<>();
        body.put("test1", "value1");
        clnt.postEntity("http://localhost:8000/junit/simulator/listenerWithException", body, TestResponse.class);
    }





    @Test
    public void testGetEntity()
    {
        try
        {
            RestClient clnt = new RestClient();
            String param = "test_parameter";
            TestResponse st = clnt.getEntity("http://localhost:8000/junit/simulator/somepath/" + param, TestResponse.class);
            assertNotNull(st.getRefId());
            assertEquals(param, st.getBodyAsString());
        }
        catch (Exception ex_)
        {
            fail(ex_.toString());
        }
    }





    @Test
    public void testGetEntityWithInterceptor_RefId()
    {
        try
        {
            String param = "test_parameter";
            String refId = "5678";
            RestClient clnt = RestClient.builder()
                .interceptors(asList(new TestRefIdInterceptor(refId)))
                .build();
            TestResponse st = clnt.getEntity("http://localhost:8000/junit/simulator/somepath/" + param, TestResponse.class);
            assertTrue(st.getRefId().contains(refId));
            assertEquals(param, st.getBodyAsString());
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
            String param = "test_parameter";
            String refId = "5678";
            RestClient clnt = new RestClient();
            TestResponse st = clnt.getEntity("http://localhost:8000/junit/simulator/somepath/" + param, TestResponse.class, refId);
            assertEquals(refId, st.getRefId());
            assertEquals(param, st.getBodyAsString());
        }
        catch (Exception ex_)
        {
            fail(ex_.toString());
        }
    }





    @Test
    public void testGetEntity_Authorization()
    {
        try
        {
            String auth = "test_auth";
            String refId = "5678";
            RestClient clnt = RestClient.builder()
                .interceptors(asList(new AuthorizationHeaderInterceptor(auth)))
                .build();
            TestAuthResponse st = clnt.getEntity("http://localhost:8000/junit/simulator/withauthorization", TestAuthResponse.class, refId);
            assertEquals(refId, st.getRefId());
            assertEquals("Bearer " + auth, st.getAuthorization());
        }
        catch (Exception ex_)
        {
            fail(ex_.toString());
        }
    }





    @Test
    public void testRestClientConstructor_default()
    {
        RestClient client = new RestClient();
        Configuration configuration = client.getHttpClient().getConfiguration();
        Assert.assertEquals(configuration.getProperty("jersey.config.client.connectTimeout"), 1000);
        Assert.assertEquals(configuration.getProperty("jersey.config.client.readTimeout"), 4000);
        Assert.assertEquals(configuration.getProperty("jersey.config.apache.client.connectionManager"), null);
    }





    @Test
    public void testRestClientConstructor_timeout()
    {
        RestClient client = new RestClient(2000, 5000);
        Configuration configuration = client.getHttpClient().getConfiguration();
        Assert.assertEquals(configuration.getProperty("jersey.config.client.connectTimeout"), 2000);
        Assert.assertEquals(configuration.getProperty("jersey.config.client.readTimeout"), 5000);
        Assert.assertEquals(configuration.getProperty("jersey.config.apache.client.connectionManager"), null);
    }





    @Test
    public void testRestClientConstructor_connectionPool()
    {
        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
        RestClient client = new RestClient(3000, 6000, cm);
        Configuration configuration = client.getHttpClient().getConfiguration();
        Assert.assertEquals(configuration.getProperty("jersey.config.client.connectTimeout"), 3000);
        Assert.assertEquals(configuration.getProperty("jersey.config.client.readTimeout"), 6000);
        Assert.assertEquals(configuration.getProperty("jersey.config.apache.client.connectionManager"), cm);
    }





    @Test
    public void testRestClientBuilder_default()
    {
        RestClient client = RestClient.builder().build();
        Configuration configuration = client.getHttpClient().getConfiguration();
        Assert.assertEquals(1000, configuration.getProperty("jersey.config.client.connectTimeout"));
        Assert.assertEquals(4000, configuration.getProperty("jersey.config.client.readTimeout"));
        Assert.assertNull(configuration.getProperty("jersey.config.apache.client.connectionManager"));
    }





    @Test
    public void testRestClientBuilder_timeout()
    {
        RestClient client = RestClient.builder()
            .connectionTimeout(2000)
            .readTimeout(5000)
            .build();
        Configuration configuration = client.getHttpClient().getConfiguration();
        Assert.assertEquals(2000, configuration.getProperty("jersey.config.client.connectTimeout"));
        Assert.assertEquals(5000, configuration.getProperty("jersey.config.client.readTimeout"));
        Assert.assertNull(configuration.getProperty("jersey.config.apache.client.connectionManager"));
    }





    @Test
    public void testRestClientBuilder_connectionPool()
    {
        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
        RestClient client = RestClient.builder()
            .connectionTimeout(3000)
            .readTimeout(6000)
            .connectionManager(cm)
            .build();
        Configuration configuration = client.getHttpClient().getConfiguration();
        Assert.assertEquals(3000, configuration.getProperty("jersey.config.client.connectTimeout"));
        Assert.assertEquals(6000, configuration.getProperty("jersey.config.client.readTimeout"));
        Assert.assertEquals(cm, configuration.getProperty("jersey.config.apache.client.connectionManager"));
    }
}

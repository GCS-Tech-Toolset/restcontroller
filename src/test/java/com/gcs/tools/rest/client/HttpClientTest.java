/****************************************************************************
 * FILE: HttpClientTest.java
 * DSCRPT: 
 ****************************************************************************/





package com.gcs.tools.rest.client;





import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;



import java.util.Hashtable;



import javax.ws.rs.Consumes;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;



import org.junit.After;
import org.junit.Before;
import org.junit.Test;



import com.gcs.tools.rest.restcontroller.HttpRestController;
import com.gcs.tools.rest.restcontroller.HttpRestException;



import lombok.extern.slf4j.Slf4j;





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
        public String listen(
                @HeaderParam("X-Correlation-ID") String refID_,
                String str_)
        {
            _logger.info("[{}] val:{}", refID_, str_);
            return str_;
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
    public void test()
    {
        try
        {
            HttpClient clnt = new HttpClient();
            Hashtable<String, String> body = new Hashtable<>();
            body.put("test1", "value1");
            body.put("test2", "value2");
            body.put("test3", "value3");
            String st = clnt.postEntity("http://localhost:8000/junit/simulator/listener", body, String.class);
            assertNotNull(st);
        }
        catch (Exception ex_)
        {
            fail(ex_.toString());
        }
    }

}

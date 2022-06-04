/**
 * Author: kgoldstein
 * Date: Feb 21, 2022
 * Terms: Expressly forbidden for use without written consent from the author
 */





package com.gcs.tools.rest.restcontroller;





import static org.junit.Assert.assertTrue;



import java.util.HashMap;
import java.util.HashSet;



import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;



import org.junit.Test;



import com.fasterxml.jackson.jaxrs.yaml.YAMLMediaTypes;



import lombok.extern.slf4j.Slf4j;





@Slf4j
public class HttpRestControllerTest
{

    @Test
    public void test()
    {
        try
        {
            String app = "junit";
            int port = 12345;

            HttpRestController restCtrl = new HttpRestController(app, port);
            restCtrl.register(new RestTest());
            restCtrl.start();



            Client clnt = ClientBuilder.newBuilder().build();
            WebTarget target = buildHttp(clnt, app, port, "ftest");
            Response rsps = target.request().get();
            assertTrue(Response.Status.OK == Response.Status.fromStatusCode(rsps.getStatus()));
            if (_logger.isDebugEnabled())
            {
                String str = rsps.readEntity(String.class);
                _logger.debug(str.toString());
            }



            target = buildHttp(clnt, app, port, "larger");
            rsps = target.request().get();
            assertTrue(Response.Status.OK == Response.Status.fromStatusCode(rsps.getStatus()));
            if (_logger.isDebugEnabled())
            {
                String str = rsps.readEntity(String.class);
                _logger.debug(str.toString());
            }



            target = buildHttp(clnt, app, port, "larger2");
            rsps = target.request().get();
            assertTrue(Response.Status.OK == Response.Status.fromStatusCode(rsps.getStatus()));
            if (_logger.isDebugEnabled())
            {
                String str = rsps.readEntity(String.class);
                _logger.debug(str.toString());
            }


            target = buildHttp(clnt, app, port, "larger3");
            rsps = target.request().get();
            assertTrue(Response.Status.OK == Response.Status.fromStatusCode(rsps.getStatus()));
            if (_logger.isDebugEnabled())
            {
                String str = rsps.readEntity(String.class);
                _logger.debug(str.toString());
            }



            restCtrl.stop();
        }
        catch (Exception ex_)
        {
            _logger.error(ex_.toString(), ex_);
        }
    }





    private WebTarget buildHttp(Client clnt_, String app_, int port_, String target_)
    {
        var wt = clnt_.target("http://localhost:" + port_).path(app_).path("junit").path(target_);
        if (_logger.isTraceEnabled())
        {
            _logger.trace("web-target:{}", wt.toString());
        }
        return wt;
    }

}

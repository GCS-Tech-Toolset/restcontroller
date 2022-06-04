/****************************************************************************
 * FILE: HttpAsyncResponseManagerTest.java
 * DSCRPT: 
 ****************************************************************************/





package com.gcs.tools.rest.restcontroller;





import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;



import java.util.Optional;
import java.util.concurrent.TimeUnit;



import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.Response;



import org.junit.Test;



import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;





@Slf4j
public class HttpAsyncResponseManagerTest
{
    @Test
    public void test()
    {
        try
        {
            String app = "junit-async-test";
            int port = 12345;
            HttpRestController.setMAX_THREADS(11);
            HttpRestController.setMIN_THREADS(11);
            HttpRestController ctrl = new HttpRestController(app, port);
            var asyncMgr = new HttpAsyncResponseManager<Long>(1, TimeUnit.SECONDS);
            ctrl.register(new HttpTestAsyc(asyncMgr));
            ctrl.start();


            Client clnt = ClientBuilder.newBuilder().build();
            WebTarget target = buildHttp(clnt, app, port, "asyncnormal");
            Response rsps = target.request().get();
            rsps.close();
            assertTrue(Response.Status.OK == Response.Status.fromStatusCode(rsps.getStatus()));


            // expect a timeout
            target = buildHttp(clnt, app, port, "asynctimeout");
            rsps = target.request().get();
            rsps.close();
            assertEquals(Response.Status.REQUEST_TIMEOUT, Response.Status.fromStatusCode(rsps.getStatus()));


            ctrl.stop();

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


    @RequiredArgsConstructor
    @Path("/junit")
    public class HttpTestAsyc
    {
        private static final String REFID = "X-Correlation-ID";



        @NonNull private HttpAsyncResponseManager<Long> _asyncResponseManager;


        @GET
        @Path("asyncnormal")
        public void testAsyncFinish(@HeaderParam(REFID) String refid_, @NonNull @Suspended final AsyncResponse rsps_)
        {

            if (_logger.isTraceEnabled())
            {
                _logger.trace("received async request");
            }




            long refId = Long.parseLong(Optional.ofNullable(refid_).orElse(Long.toString(System.nanoTime())));
            _asyncResponseManager.registerAsyncResponse(refId, rsps_);
            assertEquals(1, _asyncResponseManager.getPendingAsyncSize());
            rsps_.resume(Response.status(Response.Status.OK).header(REFID, System.nanoTime()).build());
            _asyncResponseManager.removeAsyncResposne(refId);
            assertEquals(0, _asyncResponseManager.getPendingAsyncSize());
        }





        @GET
        @Path("asynctimeout")
        public void testAsyncTimeout(@HeaderParam(REFID) String refid_, @NonNull @Suspended final AsyncResponse rsps_)
        {

            if (_logger.isTraceEnabled())
            {
                _logger.trace("received async request");
            }



            long refId = Long.parseLong(Optional.ofNullable(refid_).orElse(Long.toString(System.nanoTime())));
            _asyncResponseManager.registerAsyncResponse(refId, rsps_);
            assertEquals(1, _asyncResponseManager.getPendingAsyncSize());
            try
            {
                Thread.sleep(4000);
            }
            catch (InterruptedException ex_)
            {
                _logger.error(ex_.toString(), ex_);
            }


            if (rsps_.isSuspended())
            {
                fail("should never get here, since this timedout");
                rsps_.resume(Response.status(Response.Status.OK).header(REFID, System.nanoTime()).build());
                _asyncResponseManager.removeAsyncResposne(refId);
            }


            // this is useless, but should not cause an error...
            rsps_.resume(Response.status(Response.Status.OK).header(REFID, System.nanoTime()).build());
            assertEquals(0, _asyncResponseManager.getPendingAsyncSize());
        }
    }




}

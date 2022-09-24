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



import org.glassfish.jersey.process.internal.RequestScoped;
import org.junit.Test;
import org.slf4j.MDC;



import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;





@Slf4j
public class HttpAsyncResponseManagerTest
{
	class TestProps implements IRestControllerProperties
	{

		@Override
		public int getQueueThreadPoolMaxThreads()
		{
			return 13;
		}





		@Override
		public int getQueueThreadPoolMinThreads()
		{
			return 13;
		}





		@Override
		public int getHttpPort()
		{
			return 12345;
		}





		@Override
		public String getAppName()
		{
			return "junit-async-test";
		}



	}

	@Test
	public void test()
	{
		try
		{
			TestProps props = new TestProps();
			HttpRestController ctrl = new HttpRestController(props);
			var asyncMgr = new HttpAsyncResponseManager<Long>(1, TimeUnit.SECONDS);
			ctrl.register(new HttpTestAsyc(asyncMgr));
			ctrl.start();


			Client clnt = ClientBuilder.newBuilder().build();
			WebTarget target = buildHttp(clnt, props.getAppName(), props.getHttpPort(), "asyncnormal");
			Response rsps = target.request().header("X-Correlation-ID", "1111111111").get();
			rsps.close();
			assertTrue(Response.Status.OK == Response.Status.fromStatusCode(rsps.getStatus()));
			_logger.info("[{}] completed successfully", rsps.getHeaderString("X-Correlation-ID"));


			// expect a timeout
			target = buildHttp(clnt, props.getAppName(), props.getHttpPort(), "asynctimeout");
			rsps = target.request().header("X-Correlation-ID", "2222222222").get();
			rsps.close();
			assertEquals(Response.Status.REQUEST_TIMEOUT, Response.Status.fromStatusCode(rsps.getStatus()));
			_logger.info("[{}] timed out as expected", rsps.getHeaderString("X-Correlation-ID"));


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



		@NonNull
		private HttpAsyncResponseManager<Long> _asyncResponseManager;


		@GET @Path("asyncnormal")
		public void testAsyncFinish(@HeaderParam(REFID) String refid_, @NonNull @Suspended final AsyncResponse rsps_)
		{
			MDC.put(REFID, refid_);
			if (_logger.isDebugEnabled())
			{
				_logger.debug("received async request");
			}




			long refId = Long.parseLong(Optional.ofNullable(refid_).orElse(Long.toString(System.nanoTime())));
			_asyncResponseManager.registerAsyncResponse(refId, rsps_);
			assertEquals(1, _asyncResponseManager.getPendingAsyncSize());
			rsps_.resume(Response.status(Response.Status.OK).header(REFID, refId).build());
			_asyncResponseManager.removeAsyncResposne(refId);
			assertEquals(0, _asyncResponseManager.getPendingAsyncSize());
			MDC.clear();
		}





		@GET @Path("asynctimeout")
		public void testAsyncTimeout(@HeaderParam(REFID) String refid_, @NonNull @Suspended final AsyncResponse rsps_)
		{
			MDC.put(REFID, refid_);
			if (_logger.isDebugEnabled())
			{
				_logger.debug("received async request");
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
				rsps_.resume(Response.status(Response.Status.OK).header(REFID, refId).build());
				_asyncResponseManager.removeAsyncResposne(refId);
			}


			// this is useless, but should not cause an error...
			rsps_.resume(Response.status(Response.Status.OK).header(REFID, refId).build());
			assertEquals(0, _asyncResponseManager.getPendingAsyncSize());
			MDC.clear();
		}
	}




}

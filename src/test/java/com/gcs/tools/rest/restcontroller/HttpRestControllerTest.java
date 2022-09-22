/**
 * Author: kgoldstein
 * Date: Feb 21, 2022
 * Terms: Expressly forbidden for use without written consent from the author
 */





package com.gcs.tools.rest.restcontroller;





import static org.junit.Assert.assertTrue;





import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;



import org.junit.Test;



import com.gcs.tools.rest.restcontroller.impl.DefaultRestControllerProperties;



import lombok.extern.slf4j.Slf4j;





@Slf4j
public class HttpRestControllerTest
{

	@Test
	public void testWithParms()
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
			rsps.close();



			target = buildHttp(clnt, app, port, "larger");
			rsps = target.request().get();
			assertTrue(Response.Status.OK == Response.Status.fromStatusCode(rsps.getStatus()));
			if (_logger.isDebugEnabled())
			{
				String str = rsps.readEntity(String.class);
				_logger.debug(str.toString());
			}
			rsps.close();


			target = buildHttp(clnt, app, port, "larger2");
			rsps = target.request().get();
			assertTrue(Response.Status.OK == Response.Status.fromStatusCode(rsps.getStatus()));
			if (_logger.isDebugEnabled())
			{
				String str = rsps.readEntity(String.class);
				_logger.debug(str.toString());
			}
			rsps.close();

			target = buildHttp(clnt, app, port, "larger3");
			rsps = target.request().get();
			assertTrue(Response.Status.OK == Response.Status.fromStatusCode(rsps.getStatus()));
			if (_logger.isDebugEnabled())
			{
				String str = rsps.readEntity(String.class);
				_logger.debug(str.toString());
			}
			rsps.close();



			restCtrl.stop();
		}
		catch (Exception ex_)
		{
			_logger.error(ex_.toString(), ex_);
		}
	}





	@Test
	public void testWithProps()
	{
		try
		{
			String app = "junit";
			int port = 12345;
			DefaultRestControllerProperties props = DefaultRestControllerProperties.getInstance();
			props.setAppName(app);
			props.setHttpPort(port);


			HttpRestController restCtrl = new HttpRestController(props);
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
			rsps.close();



			target = buildHttp(clnt, app, port, "larger");
			rsps = target.request().get();
			assertTrue(Response.Status.OK == Response.Status.fromStatusCode(rsps.getStatus()));
			if (_logger.isDebugEnabled())
			{
				String str = rsps.readEntity(String.class);
				_logger.debug(str.toString());
			}
			rsps.close();


			target = buildHttp(clnt, app, port, "larger2");
			rsps = target.request().get();
			assertTrue(Response.Status.OK == Response.Status.fromStatusCode(rsps.getStatus()));
			if (_logger.isDebugEnabled())
			{
				String str = rsps.readEntity(String.class);
				_logger.debug(str.toString());
			}
			rsps.close();

			target = buildHttp(clnt, app, port, "larger3");
			rsps = target.request().get();
			assertTrue(Response.Status.OK == Response.Status.fromStatusCode(rsps.getStatus()));
			if (_logger.isDebugEnabled())
			{
				String str = rsps.readEntity(String.class);
				_logger.debug(str.toString());
			}
			rsps.close();



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

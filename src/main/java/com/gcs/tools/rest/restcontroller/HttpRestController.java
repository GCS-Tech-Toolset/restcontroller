/****************************************************************************
 * FILE: TxFileParserEntryPoint.java
 * DSCRPT: 
 ****************************************************************************/





package com.gcs.tools.rest.restcontroller;





import java.io.IOException;
import java.io.Writer;
import java.net.BindException;
import java.util.EnumSet;



import javax.servlet.DispatcherType;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Response;



import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.ErrorHandler;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.servlets.CrossOriginFilter;
import org.eclipse.jetty.util.component.LifeCycle;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;



import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;
import com.fasterxml.jackson.jaxrs.xml.JacksonXMLProvider;
import com.fasterxml.jackson.jaxrs.yaml.JacksonJaxbYAMLProvider;



import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;





@Slf4j
public class HttpRestController implements LifeCycle.Listener
{

	private static final String REFID = "X-Correlation-ID";

	@Getter @Setter private boolean	_canRegister;
	private ResourceConfig			_resourceConfig;
	private Server					_server;

	private int _port;

	@Getter @Setter private static int	_MIN_THREADS	= 11;
	@Getter @Setter private static int	_MAX_THREADS	= 25;





	public HttpRestController(String appName_, int httpPort_) throws Exception
	{
		QueuedThreadPool threadPool = new QueuedThreadPool(_MAX_THREADS, _MIN_THREADS);
		_server = new Server(threadPool);
		_port = httpPort_;

		ServerConnector connector = new ServerConnector(_server);
		connector.setPort(httpPort_);
		connector.setAcceptQueueSize(100);



		/*if (getClass().getResource("/keystore.jks") != null)
		{
		    HttpConfiguration https = new HttpConfiguration();
		    https.addCustomizer(new SecureRequestCustomizer());
		    https.setSecureScheme("https");
		    SslContextFactory sslContextFactory = new SslContextFactory();
		    sslContextFactory.setKeyStorePath(HttpRestController.class.getResource("/keystore.jks").toExternalForm());
		    sslContextFactory.setKeyStorePassword("123456");
		    sslContextFactory.setKeyManagerPassword("123456");
		    ServerConnector sslConnector = new ServerConnector(_server,
		            new SslConnectionFactory(sslContextFactory, HttpVersion.HTTP_1_1.asString()),
		            new HttpConnectionFactory(https));
		    sslConnector.setPort(httpPort_ + 1);
		}*/

		_server.setConnectors(new Connector[]
		{
				connector//,
				//sslConnector
		});


		ServletContextHandler ctx = new ServletContextHandler(ServletContextHandler.NO_SESSIONS);
		ctx.setContextPath("/");


		FilterHolder filterHolder = new FilterHolder(new CrossOriginFilter());
		filterHolder.setInitParameter(CrossOriginFilter.ALLOWED_ORIGINS_PARAM, "*");
		filterHolder.setInitParameter(CrossOriginFilter.ACCESS_CONTROL_ALLOW_CREDENTIALS_HEADER, "true");
		filterHolder.setInitParameter(CrossOriginFilter.ACCESS_CONTROL_ALLOW_METHODS_HEADER, "POST,OPTIONS,GET");
		filterHolder.setInitParameter(CrossOriginFilter.ACCESS_CONTROL_ALLOW_HEADERS_HEADER, "Content-Type");
		ctx.addFilter(filterHolder, "/*", EnumSet.of(DispatcherType.REQUEST));


		_server.setHandler(ctx);
		_server.addLifeCycleListener(this);

		_resourceConfig = new ResourceConfig();
		registerProviders();
		registerSwagger();
		setCanRegister(true);



		String pathSpec = "/" + appName_ + "/*";
		ServletContainer container = new ServletContainer(_resourceConfig);
		ServletHolder servletHolder = new ServletHolder(container);
		servletHolder.setInitParameter("idleTimout", "1");
		ctx.addServlet(servletHolder, pathSpec);



		ctx.setErrorHandler(new ErrorHandler()
		{


			@Override
			public void doError(String target_, org.eclipse.jetty.server.Request baseRequest_, HttpServletRequest request_, HttpServletResponse response_) throws IOException
			{
				_logger.error("[{}] general-error-target:{}, response code:{}",
						baseRequest_.getHeader(REFID),
						baseRequest_.getOriginalURI(),
						Response.Status.fromStatusCode(response_.getStatus()));
				baseRequest_.getParameterMap().forEach((x, y) -> _logger.info("[{}] param:{}, val:{}", response_.getHeader(REFID), x, y));
				super.doError(target_, baseRequest_, request_, response_);
			}





			@Override
			protected void handleErrorPage(HttpServletRequest request_, Writer writer_, int code_, String message_) throws IOException
			{
				_logger.error("[{}] errort-page-target:{}, generating error page",
						request_.getHeader(REFID),
						request_.getRequestURI());
				super.handleErrorPage(request_, writer_, code_, message_);
			}





			@Override
			public boolean isShowStacks()
			{
				return true;
			}





			@Override
			public boolean getShowMessageInTitle()
			{
				return true;

			}

		});

	}





	private void registerSwagger()
	{
	}





	private void registerProviders()
	{
		if (_resourceConfig == null)
		{
		}

		_resourceConfig.register(JacksonXMLProvider.class);
		_resourceConfig.register(JacksonJaxbJsonProvider.class);
		_resourceConfig.register(JacksonJaxbYAMLProvider.class);
	}





	public void start() throws HttpRestException
	{
		if (_server == null)
		{
			throw new HttpRestException("Server not initialized");
		}

		try
		{
			_server.start();
			if (_logger.isTraceEnabled())
			{
				_logger.trace(_server.dump());
				var itr = _resourceConfig.getResources().iterator();
				while (itr.hasNext())
				{
					_logger.info(itr.next().toString());
				}
			}
		}
		catch (BindException ex_)
		{
			_logger.error("port:{}", _port);
			_logger.error(ex_.toString(), ex_);
		}
		catch (Exception ex_)
		{
			_logger.error(ex_.toString(), ex_);
		}
	}





	public void stop() throws HttpRestException
	{
		if (_server == null)
		{
			throw new HttpRestException("Server not initialized");
		}

		try
		{
			if (_logger.isTraceEnabled())
			{
				_logger.trace(_server.dump());
			}
			_server.stop();
		}
		catch (Exception ex_)
		{
			_logger.error(ex_.toString(), ex_);
		}
	}





	public void register(Object o_) throws HttpRestException
	{
		if (_server == null)
		{
			throw new HttpRestException("server is NULL, cannot register");
		}
		else if (!_canRegister)
		{
			throw new HttpRestException("no longer accepting registration of new end-points");
		}

		_resourceConfig.register(o_);

	}





	@Override
	public void lifeCycleStarting(LifeCycle event_)
	{
		if (_logger.isInfoEnabled())
		{
			_logger.info("HttpRestController::lifeCycleStarting(event_)");
		}
	}





	@Override
	public void lifeCycleStarted(LifeCycle event_)
	{
		if (_logger.isInfoEnabled())
		{
			_logger.info("HttpRestController::lifeCycleStarted(enclosing_method_arguments)");
		}
	}





	@Override
	public void lifeCycleFailure(LifeCycle event_, Throwable cause_)
	{
		_logger.error("LifeCycleFailure:{}, cause:{}", event_.toString(), cause_.toString());
		_canRegister = false;
		_server = null;
	}





	@Override
	public void lifeCycleStopping(LifeCycle event_)
	{
		_logger.info("LifeCycleStopping:{}, cause:{}", event_.toString());
		_canRegister = false;
	}





	@Override
	public void lifeCycleStopped(LifeCycle event_)
	{
		_logger.info("LifeCycleStopping:{}, cause:{}", event_.toString());
		_server = null;
	}





	public void join() throws InterruptedException
	{
		if (_server == null)
		{
			_logger.error("can't join.. the internal server varaible (_server) is NULL... This is most likely due to the server not starting correctly");
			return;
		}
		_server.join();
	}

}

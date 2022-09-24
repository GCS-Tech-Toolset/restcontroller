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



import org.eclipse.jetty.http.HttpVersion;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.server.handler.ErrorHandler;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.servlets.CrossOriginFilter;
import org.eclipse.jetty.util.component.LifeCycle;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;
import org.slf4j.MDC;



import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;
import com.fasterxml.jackson.jaxrs.xml.JacksonXMLProvider;
import com.fasterxml.jackson.jaxrs.yaml.JacksonJaxbYAMLProvider;
import com.gcs.tools.rest.restcontroller.impl.DefaultRestControllerProperties;



import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;





@Slf4j
public class HttpRestController implements LifeCycle.Listener
{

	private static final String REFID = "X-Correlation-ID";

	@Getter @Setter private boolean _canRegister;

	private IRestControllerProperties	_props;
	private ResourceConfig				_resourceConfig;
	private Server						_server;





	public HttpRestController(String appName_, int httpPort_) throws Exception
	{
		DefaultRestControllerProperties.getInstance().setAppName(appName_);
		DefaultRestControllerProperties.getInstance().setHttpPort(httpPort_);
		_props = DefaultRestControllerProperties.getInstance();
		startServices();
	}





	public HttpRestController(IRestControllerProperties props_) throws Exception
	{
		if (props_ == null)
		{
			_logger.error("specified properties are null, loading default properties instead");
			_props = DefaultRestControllerProperties.getInstance();
		}


		_props = props_;
		startServices();
	}





	private void startServices()
	{
		initThreadPoolAndServer();
		initConnectors();
		initContextHandler();
	}





	private void initThreadPoolAndServer()
	{
		QueuedThreadPool threadPool = new QueuedThreadPool(
				_props.getQueueThreadPoolMaxThreads(),
				_props.getQueueThreadPoolMinThreads());
		_server = new Server(threadPool);
		if (_logger.isTraceEnabled())
		{
			_logger.trace("QueuedThreadPool, size-max:{}, size-min:{}",
					_props.getQueueThreadPoolMaxThreads(),
					_props.getQueueThreadPoolMinThreads());
		}
	}





	private void initConnectors()
	{
		ServerConnector connector = new ServerConnector(_server);
		connector.setPort(_props.getHttpPort());
		connector.setAcceptQueueSize(_props.getAcceptQueueSize());


		if (_props.getEnableSsl())
		{
			if (getClass().getResource("/keystore.jks") != null)
			{
				HttpConfiguration https = new HttpConfiguration();
				https.addCustomizer(new SecureRequestCustomizer());
				https.setSecureScheme("https");
				SslContextFactory sslContextFactory = new SslContextFactory();
				sslContextFactory.setKeyStorePath(HttpRestController.class.getResource(_props.getKeystore()).toExternalForm());
				sslContextFactory.setKeyStorePassword("123456");
				sslContextFactory.setKeyManagerPassword("123456");
				ServerConnector sslConnector = new ServerConnector(_server,
						new SslConnectionFactory(sslContextFactory, HttpVersion.HTTP_1_1.asString()),
						new HttpConnectionFactory(https));
				sslConnector.setPort(_props.getHttpPort() + 1);
				_server.setConnectors(new Connector[]
				{
						connector,
						sslConnector
				});

				if (_logger.isDebugEnabled())
				{
					_logger.debug("Started server with connectors[RAW:{} AND SSL:{}]", connector.getPort(), sslConnector.getPort());
				}


				return;
			}
			else
			{
				_logger.error("SSL is configured as TRUE, but unable to load resource... ignoring");
			}

		}



		//
		// I can make it here by one of 2 conditions:
		// 1. SSL is *NOT* enabled
		// 2. SSL is enabbled, but the keystore was not located - essentially disabeling SSL
		//
		_server.setConnectors(new Connector[]
		{
				connector
		});


		if (_logger.isDebugEnabled())
		{
			_logger.debug("Started server with connectors[RAW:{}]", connector.getPort());
		}

	}





	private void initContextHandler()
	{
		ServletContextHandler ctx = new ServletContextHandler(ServletContextHandler.NO_SESSIONS);
		ctx.setContextPath("/");


		FilterHolder filterHolder = new FilterHolder(new CrossOriginFilter());
		filterHolder.setInitParameter(CrossOriginFilter.ALLOWED_ORIGINS_PARAM, "*");
		filterHolder.setInitParameter(CrossOriginFilter.ACCESS_CONTROL_ALLOW_CREDENTIALS_HEADER, "true");
		filterHolder.setInitParameter(CrossOriginFilter.ACCESS_CONTROL_ALLOW_METHODS_HEADER, "POST,OPTIONS,GET");
		filterHolder.setInitParameter(CrossOriginFilter.ACCESS_CONTROL_ALLOW_HEADERS_HEADER, "Content-Type");
		filterHolder.setInitParameter(CrossOriginFilter.ACCESS_CONTROL_ALLOW_CREDENTIALS_HEADER, "false");
		filterHolder.setInitParameter(CrossOriginFilter.ACCESS_CONTROL_ALLOW_CREDENTIALS_HEADER, "false");
		ctx.addFilter(filterHolder, "/*", EnumSet.of(DispatcherType.REQUEST));


		_server.setHandler(ctx);
		_server.addLifeCycleListener(this);

		_resourceConfig = new ResourceConfig();
		registerProviders();
		registerSwagger();
		registerBasicEndpoints(_props);
		setCanRegister(true);




		String pathSpec = "/" + _props.getAppName() + "/*";
		ServletContainer container = new ServletContainer(_resourceConfig);
		ServletHolder servletHolder = new ServletHolder(container);
		servletHolder.setInitParameter("idleTimout", "1");
		ctx.addServlet(servletHolder, pathSpec);



		ctx.setErrorHandler(new ErrorHandler()
		{


			@Override
			public void doError(String target_, org.eclipse.jetty.server.Request baseRequest_, HttpServletRequest request_, HttpServletResponse response_) throws IOException
			{
				MDC.put(REFID, baseRequest_.getHeader(REFID));
				_logger.error("header:{}", baseRequest_.getHeader(REFID));
				_logger.error("[{}] general-error-target:{}, response code:{}",
						baseRequest_.getHeader(REFID),
						baseRequest_.getOriginalURI(),
						Response.Status.fromStatusCode(response_.getStatus()));
				super.doError(target_, baseRequest_, request_, response_);



				if (_logger.isDebugEnabled())
				{
					baseRequest_.getParameterMap().forEach((x, y) -> _logger.info("[{}] param:{}, val:{}", response_.getHeader(REFID), x, y));
					var itr = baseRequest_.getHeaderNames().asIterator();
					while (itr.hasNext())
					{
						final String header = itr.next();
						_logger.debug("{}={}", header, baseRequest_.getHeader(header));
					}
				}

				MDC.clear();
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





	private void registerBasicEndpoints(IRestControllerProperties props_)
	{
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
			}
		}
		catch (BindException ex_)
		{
			_logger.error("port:{}", Integer.toString(_props.getHttpPort()));
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
			_logger.error("can't join, server=NULL");
			return;
		}
		_server.join();
	}

}

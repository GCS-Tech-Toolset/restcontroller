/****************************************************************************
 * FILE: IRestControllerProperties.java
 * DSCRPT: 
 ****************************************************************************/





package com.gcs.tools.rest.restcontroller;





public interface IRestControllerProperties
{
	default public boolean getEnableSsl()
	{
		return false;
	}





	default public boolean getRegisterBasicEndpoints()
	{
		return false;
	}





	default public int getHttpPort()
	{
		return 8000;
	}





	default public int getQueueThreadPoolMaxThreads()
	{
		return 25;
	}





	default public int getQueueThreadPoolMinThreads()
	{
		return 11;
	}





	default public int getAcceptQueueSize()
	{
		return 100;
	}





	public default String getAppName()
	{
		return "HttpRestContrtoller";
	}





	public default String getKeystore()
	{
		return "/keystore.jks";
	}
}

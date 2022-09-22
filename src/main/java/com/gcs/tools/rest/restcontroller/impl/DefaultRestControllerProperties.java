/****************************************************************************
 * FILE: DefaultRestControllerProperties.java
 * DSCRPT: 
 ****************************************************************************/





package com.gcs.tools.rest.restcontroller.impl;





import com.gcs.tools.rest.restcontroller.IRestControllerProperties;



import lombok.Getter;
import lombok.Setter;





public class DefaultRestControllerProperties implements IRestControllerProperties
{

	@Getter @Setter
	private String _appName;

	@Getter @Setter
	private int _httpPort;



	private DefaultRestControllerProperties()
	{
	}





	///////////////////////////////
	//
	////////////////////////////////
	public static final DefaultRestControllerProperties getInstance()
	{
		return SingletonDefaultRestControllerProperties._instance;
	}

	private static final class SingletonDefaultRestControllerProperties
	{
		private static final DefaultRestControllerProperties _instance = new DefaultRestControllerProperties();
	}
}

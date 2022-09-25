/**
 * Author: kgoldstein
 * Date: Feb 22, 2022
 * Terms: Expressly forbidden for use without written consent from the author
 */





package com.gcs.tools.rest.restcontroller;





public class HttpRestException extends RuntimeException
{

	private static final long serialVersionUID = 1L;

	public HttpRestException()
	{
		super();

	}





	public HttpRestException(String message_, Throwable cause_, boolean enableSuppression_, boolean writableStackTrace_)
	{
		super(message_, cause_, enableSuppression_, writableStackTrace_);

	}





	public HttpRestException(String message_, Throwable cause_)
	{
		super(message_, cause_);

	}





	public HttpRestException(String message_)
	{
		super(message_);

	}





	public HttpRestException(Throwable cause_)
	{
		super(cause_);

	}


}

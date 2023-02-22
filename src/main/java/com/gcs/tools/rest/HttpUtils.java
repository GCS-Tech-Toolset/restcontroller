/**
 * Author: kgoldstein
 * Date: Feb 20, 2023
 * Terms: Expressly forbidden for use without written consent from the author
 */





package com.gcs.tools.rest;





import java.util.Optional;



import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;



import lombok.extern.slf4j.Slf4j;





@Slf4j
public class HttpUtils
{
    public static final String REFID = "X-Correlation-ID";

    public static String getRefId(final String refId_)
    {
        return Optional.ofNullable(refId_).orElse(Long.toString(System.nanoTime()));
    }





    public static ResponseBuilder respond(
            Status status_,
            String refId_)
    {
        return Response.status(status_).header(REFID, refId_);
    }

}

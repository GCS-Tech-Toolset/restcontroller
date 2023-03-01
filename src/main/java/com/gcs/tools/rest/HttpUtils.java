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



import lombok.NonNull;
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
            @NonNull Status status_,
            @NonNull String refId_)
    {
        return Response.status(status_).header(REFID, refId_);
    }





    public static boolean isSuccess(@NonNull final Response rsps_)
    {
        return rsps_.getStatusInfo().getFamily().equals(Status.Family.SUCCESSFUL);
    }





    public static boolean isNotSuccess(@NonNull final Response rsps_)
    {
        return !isSuccess(rsps_);
    }
}

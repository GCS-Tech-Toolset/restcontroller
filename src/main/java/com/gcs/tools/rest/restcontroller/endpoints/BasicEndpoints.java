/****************************************************************************
 * FILE: BasicEnpoints.java
 * DSCRPT: 
 ****************************************************************************/





package com.gcs.tools.rest.restcontroller.endpoints;





import static com.gcs.tools.rest.HttpUtils.REFID;
import static com.gcs.tools.rest.HttpUtils.getRefId;



import java.util.HashMap;
import java.util.Map;



import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;



import org.slf4j.MDC;



import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;





@Slf4j
//@Path("/basic")
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public abstract class BasicEndpoints
{



    protected abstract Map<String,String> getPropsAsMap();





    public abstract String getVersion();





    @GET
    @Path("healthcheck")
    @Produces(MediaType.APPLICATION_JSON)
    public final Response healthcheck(final @HeaderParam(REFID) String refId_)
    {
        if (_logger.isTraceEnabled())
        {
            _logger.trace("healthcheck(refId_)");
        }

        final String refid = getRefId(refId_);
        try
        {
            MDC.put(REFID, refid);
            if (_logger.isInfoEnabled())
            {
                _logger.info("BasicEndpoints::healthcheck(refId_)");
            }
            return Response.ok().header(REFID, refid).build();
        }
        finally
        {
            MDC.clear();
        }
    }





    @GET
    @Path("version")
    @Produces(MediaType.APPLICATION_JSON)
    public Response version(final @HeaderParam(REFID) String refId_)
    {
        try
        {
            final String refid = getRefId(refId_);
            MDC.put(REFID, refid);
            if (_logger.isInfoEnabled())
            {
                _logger.info("BasicEndpoints::version(refId_)");
            }

            var ht = new HashMap<String, String>();
            ht.put("version", getVersion());
            return Response.ok().header(REFID, refid).entity(ht).build();
        }
        finally
        {
            MDC.clear();
        }
    }





    @GET
    @Path("info")
    @Produces(MediaType.APPLICATION_JSON)
    public final Response info(final @HeaderParam(REFID) String refId_)
    {
        final String refid = getRefId(refId_);
        try
        {
            MDC.put(REFID, refid);
            if (_logger.isInfoEnabled())
            {
                _logger.info("BasicEndpoints::info(refId_)");
            }


            var props = getPropsAsMap();
            return Response.ok().header(REFID, refid).entity(props).build();
        }
        finally
        {
            MDC.clear();
        }
    }





}

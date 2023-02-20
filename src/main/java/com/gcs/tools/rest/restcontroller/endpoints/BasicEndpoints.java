/****************************************************************************
 * FILE: BasicEnpoints.java
 * DSCRPT: 
 ****************************************************************************/





package com.gcs.tools.rest.restcontroller.endpoints;





import static com.gcs.tools.rest.HttpUtils.REFID;
import static com.gcs.tools.rest.HttpUtils.getRefId;



import java.util.HashMap;



import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;



import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;





@Slf4j
@Path("/")
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public abstract class BasicEndpoints
{





    @GET
    @Path("healthcheck")
    @Produces(MediaType.APPLICATION_JSON)
    public final Response healthcheck(final @HeaderParam(REFID) String refId_)
    {
        if (_logger.isTraceEnabled())
        {
            _logger.trace("healthcheck(refId_)");
        }

        String refid = getRefId(refId_);
        if (_logger.isTraceEnabled())
        {
            _logger.trace("refid");
        }
        return Response.ok().header(REFID, refid).build();
    }





    @GET
    @Path("version")
    @Produces(MediaType.APPLICATION_JSON)
    public final Response version(final @HeaderParam(REFID) String refId_)
    {
        if (_logger.isTraceEnabled())
        {
            _logger.trace("version(refId_)");
        }

        final String refid = getRefId(refId_);
        var ht = new HashMap<String, String>();
        ht.put("version", getVersion());
        return Response.ok().header(REFID, refid).entity(ht).build();
    }





    @GET
    @Path("info")
    @Produces(MediaType.APPLICATION_JSON)
    public final Response info(final @HeaderParam(REFID) String refId_)
    {
        if (_logger.isTraceEnabled())
        {
            _logger.trace("info(refId_)");
        }

        final String refid = getRefId(refId_);
        String props = getPropsAsMap();
        return Response.ok().header(REFID, refid).entity(props.toString()).build();
    }





    protected abstract String getPropsAsMap();





    public abstract String getVersion();





}

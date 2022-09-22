/****************************************************************************
 * FILE: BasicEnpoints.java
 * DSCRPT: 
 ****************************************************************************/

package com.gcs.tools.rest.restcontroller.endpoints;

import com.gcs.tools.rest.version.VersionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Optional;

@Slf4j
@Path("/")
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class BasicEndpoints
{
    public static final String REFID = "X-Correlation-ID";





    private void logRefId(String refId_)
    {
        if (_logger.isTraceEnabled())
        {
            _logger.trace("new refid: [{}]", refId_);
        }
    }





    @GET
    @Path("healthcheck")
    @Produces(MediaType.APPLICATION_JSON)
    public final Response healthcheck(final @HeaderParam(REFID) String refId_)
    {
        if (_logger.isTraceEnabled())
        {
            _logger.trace("healthcheck(refId_)");
        }

        String refid = Optional.ofNullable(refId_).orElse(Long.toString(System.nanoTime()));
        if (_logger.isTraceEnabled())
        {
            _logger.trace("refid");
        }
        return Response.ok().header(REFID, refid).entity(VersionService.getInstance().getVersion()).build();
    }





    @GET
    @Path("version")
    @Produces(MediaType.APPLICATION_JSON)
    public final String version(final @HeaderParam(REFID) String refId_)
    {
        if (_logger.isTraceEnabled())
        {
            _logger.trace("version(refId_)");
        }

        final String refid = Optional.ofNullable(refId_).orElse(Long.toString(System.nanoTime()));
        logRefId(refid);
        var ht = new HashMap<String, String>();
        ht.put("version", VersionService.getInstance().getVersion());
        Response.ok().header(REFID, refid).entity(ht).build();
        return VersionService.getInstance().getVersion();
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

        final String refid = Optional.ofNullable(refId_).orElse(Long.toString(System.nanoTime()));
        logRefId(refid);
        VersionService props = VersionService.getInstance();
        return Response.ok().header(REFID, refid).entity(props.toString()).build();
    }

}

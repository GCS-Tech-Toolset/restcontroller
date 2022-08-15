/**
 * Author: kgoldstein
 * Date: Feb 21, 2022
 * Terms: Expressly forbidden for use without written consent from the author
 */





package com.gcs.tools.rest.restcontroller;





import java.util.HashMap;
import java.util.HashSet;



import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;



import com.fasterxml.jackson.jaxrs.yaml.YAMLMediaTypes;





@Path("junit")
public class RestTest
{




    public static final MediaType YAML_MEDIA_TYPE = YAMLMediaTypes.APPLICATION_JACKSON_YAML_TYPE;




    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("halt")
    public Response halt()
    {
        Thread r = new Thread()
        {

            @Override
            public void run()
            {
                try
                {
                    Thread.sleep(10000);
                    System.exit(0);
                }
                catch (InterruptedException ex_)
                {
                    ex_.printStackTrace();
                }
            }

        };
        r.start();


        return Response.status(Response.Status.OK).entity(new String("shutting down system in: 10 sec")).build();
    }





    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("ftest")
    public Response test()
    {
        return Response.status(Response.Status.OK).entity(new String("test")).build();

    }





    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("larger")
    public Response larger()
    {
        Rsps rsps = new Rsps();
        rsps.setA(1);
        rsps.setB("b");
        var map = new HashMap<String, String>();
        map.put("abc", "def");
        map.put("fed", "zuy");
        HashSet<Object> o = new HashSet<Object>();
        o.add(map);
        o.add(rsps);
        return Response.status(Response.Status.OK).entity(o).build();
    }





    @GET
    @Produces(MediaType.APPLICATION_XML)
    @Path("larger2")
    public Response larger2()
    {
        Rsps rsps = new Rsps();
        rsps.setA(1);
        rsps.setB("b");
        var map = new HashMap<String, String>();
        map.put("abc", "def");
        map.put("fed", "zuy");
        HashSet<Object> o = new HashSet<Object>();
        o.add(map);
        o.add(rsps);
        return Response.status(Response.Status.OK).entity(o).build();
    }





    @GET
    @Produces(YAMLMediaTypes.APPLICATION_JACKSON_YAML)
    @Path("larger3")
    public Response largerYaml()
    {
        Rsps rsps = new Rsps();
        rsps.setA(1);
        rsps.setB("b");
        var map = new HashMap<String, String>();
        map.put("abc", "def");
        map.put("fed", "zuy");
        HashSet<Object> o = new HashSet<Object>();
        o.add(map);
        o.add(rsps);
        return Response.status(Response.Status.OK).entity(o).build();
    }



}

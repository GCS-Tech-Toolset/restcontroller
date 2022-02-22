# restcontroller

Example usage:

```java
HttpRestController restCtrl = new HttpRestController("exmaple", 8081);
restCtrl.register(new RestTest());
restCtrl.start();
restCtrl.join();
```

Here is an example endpoint:
```
@Path("rtest")
public class RestTest
{
    public static final MediaType YAML_MEDIA_TYPE = YAMLMediaTypes.APPLICATION_JACKSON_YAML_TYPE;


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
```

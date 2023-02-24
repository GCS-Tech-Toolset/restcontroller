# restcontroller

Example usage:

```java
HttpRestController restCtrl = new HttpRestController("exmaple", 8081);
restCtrl.register(new RestTest());
restCtrl.start();
restCtrl.join();
```

Here is an example endpoint, calls:

```bash
http://127.0.0.1:8081/junit/rtest/larger1
http://127.0.0.1:8081/junit/rtest/larger2
http://127.0.0.1:8081/junit/rtest/larger3
http://127.0.0.1:8081/junit/rtest/halt <--> shuts down server
```

Here is the code for the above.

```java
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
```



# Releases
### 1.10

  * added `BasicEndpoint` with abstract classes
  * added `HttpUtils` class
  * added `HttpClientBuilder` to help generate `configured` http clients (`Client`)

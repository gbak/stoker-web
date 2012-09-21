package sweb.server.rest;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import com.sun.jersey.spi.resource.Singleton;

@Singleton
@Produces(MediaType.APPLICATION_JSON)
@Path("/test")
public class JerseyTest {
    @POST
    public String handleFooPost(@QueryParam("bar") String bar, @QueryParam("quux") int quux) {
       return "{\"yay\":\"hooray\"}";
    }
}
package at.htl.leoenergy.boundary;

import at.htl.leoenergy.influxdb.InfluxDbRepository;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/floors")
@Produces(MediaType.APPLICATION_JSON)
public class FloorResource {
    @Inject
    InfluxDbRepository influxDbRepository;


    @GET
    public Response getAllFloors() {
        var floors = influxDbRepository.getAllFloors();
        return floors.isEmpty()
                ? Response.noContent().entity("No floors in database").build()
                : Response.ok(floors).build();
    }
}

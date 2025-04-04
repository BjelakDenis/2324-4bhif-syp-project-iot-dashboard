package at.htl.leoenergy.boundary;

import at.htl.leoenergy.influxdb.InfluxDbRepository;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/rooms")
@Produces(MediaType.APPLICATION_JSON)
public class RoomResource {

    @Inject
    InfluxDbRepository influxDbRepository;

    @GET
    public Response getAllRooms() {
        var rooms = influxDbRepository.getAllRooms();
        return rooms.isEmpty()
                ? Response.noContent().entity("No rooms in database").build()
                : Response.ok(rooms).build();
    }
}

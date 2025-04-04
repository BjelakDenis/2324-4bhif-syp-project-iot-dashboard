package at.htl.leoenergy.boundary;

import at.htl.leoenergy.entity.SensorBoxDTO;
import at.htl.leoenergy.influxdb.InfluxDbRepository;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/sensorbox")
@Produces(MediaType.APPLICATION_JSON)
public class SensorBoxResource {

    @Inject
    InfluxDbRepository influxDbRepository;

    @GET
    @Path("/latest-values/{room}")
    public Response getLatestValues(@PathParam("room") String room) {
        if (!influxDbRepository.getAllRooms().contains(room)) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Room not found").build();
        }

        var dto = influxDbRepository.getLatestSensorBoxDataForRoom(room);
        return Response.ok(SensorBoxDTO.deleteFaultyValues(dto)).build();
    }
}

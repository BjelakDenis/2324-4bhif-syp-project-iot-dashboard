package at.htl.leoenergy.influxdb;

import at.htl.leoenergy.entity.SensorBoxDTO;
import at.htl.leoenergy.entity.SensorBoxValue;
import at.htl.leoenergy.entity.SensorValue;
import at.htl.leoenergy.influxdb.mapper.SensorBoxMapper;
import at.htl.leoenergy.mqtt.InfluxDbConnectionManager;
import com.influxdb.query.FluxRecord;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@ApplicationScoped
public class InfluxDbRepository {


    @Inject
    InfluxDbConnectionManager influxDBClient;


    @ConfigProperty(name = "influxdb.org")
    String org;

    @ConfigProperty(name = "influxdb.bucket")
    String bucket;


    public void insert(SensorValue sensorValue) {
        var writeApi = influxDBClient.getInfluxDBClient().getWriteApiBlocking();
        var valueTimestampInMs = TimeUnit.MILLISECONDS.toMillis(sensorValue.getTime());
        var point = new PointBuilder("sensor_values")
                .deviceName(sensorValue.getDeviceName())
                .valueTypeId(sensorValue.getValueTypeId())
                .relation(sensorValue.getRelation())
                .valueType(sensorValue.getValueType())
                .site(sensorValue.getSite())
                .value(sensorValue.getValue())
                .unit(sensorValue.getUnit())
                .timestamp(valueTimestampInMs)
                .build();

        writeApi.writePoint(bucket, org, point);

    }


    public void insertSensorBoxMeasurement(SensorBoxValue sensorBox) {

        var writeApi = influxDBClient.getInfluxDBClient().getWriteApiBlocking();
        long currentTimeInNanoseconds = TimeUnit.MILLISECONDS.toMillis(sensorBox.getTime());


        var point = new PointBuilder("sensor_box")
                .room(sensorBox.getRoom())
                .parameter(sensorBox.getParameter())
                .floor(sensorBox.getFloor())
                .value(sensorBox.getValue())
                .timestamp(currentTimeInNanoseconds)
                .build();

        writeApi.writePoint(bucket, org, point);
    }

    public List<String> getAllFloors() {
        Set<String> floors = new HashSet<>();


        var fluxQuery = String.format("""
                from(bucket: "%s")
                  |> range(start: 0)
                  |> distinct(column: "floor")
                  |> keep(columns: ["floor"])
                """, bucket);

        influxDBClient.getInfluxDBClient()
                .getQueryApi()
                .query(fluxQuery, org)
                .forEach(table -> table.getRecords()
                        .forEach(record -> floors.add(record.getValueByKey("floor")
                                .toString())));

        return floors.stream().toList();
    }

    public List<String> getAllRooms() {
        Set<String> rooms = new HashSet<>();


        var queryApi = influxDBClient.getInfluxDBClient().getQueryApi();

        var fluxQuery = String.format("""
                from(bucket: "%s")
                  |> range(start: 0)
                  |> distinct(column: "room")
                  |> keep(columns: ["room"])
                """, bucket);

        queryApi.query(fluxQuery, org).forEach(table -> table.getRecords().forEach(record -> rooms.add(record.getValueByKey("room").toString())));


        return rooms.stream().toList();

    }

    public SensorBoxDTO getLatestSensorBoxDataForRoom(String room) {
        String fluxQuery = String.format("""
        from(bucket: "%s")
        |> range(start: 0)
        |> filter(fn: (r) => r["_measurement"] == "sensor_box")
        |> filter(fn: (r) => r["room"] == "%s")
        |> group(columns: ["parameter"])
        |> last()
        """, bucket, room);

        var records = new ArrayList<FluxRecord>();

        influxDBClient.getInfluxDBClient().getQueryApi()
                .query(fluxQuery, org)
                .forEach(table -> records.addAll(table.getRecords()));

        return SensorBoxMapper.mapToSensorBoxDTO(room, records);
    }

}
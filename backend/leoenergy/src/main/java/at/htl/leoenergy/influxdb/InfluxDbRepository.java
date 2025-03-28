package at.htl.leoenergy.influxdb;

import at.htl.leoenergy.entity.SensorBoxDTO;
import at.htl.leoenergy.entity.SensorBoxValue;
import at.htl.leoenergy.entity.SensorValue;
import at.htl.leoenergy.mqtt.InfluxDbConnectionManager;
import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.QueryApi;
import com.influxdb.client.WriteApiBlocking;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import io.quarkus.logging.Log;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
        var point = new PointBuilder()
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
        try {

            WriteApiBlocking writeApi = influxDBClient.getInfluxDBClient().getWriteApiBlocking();

            long currentTimeInNanoseconds = TimeUnit.MILLISECONDS.toMillis(sensorBox.getTime());

            Point point = Point.measurement("sensor_box").addTag("room", sensorBox.getRoom()) // Room: e72
                    .addTag("parameter", sensorBox.getParameter()) // Parameter: humidity
                    .addTag("floor", sensorBox.getFloor()) // Floor: eg
                    .addField("value", sensorBox.getValue()).time(currentTimeInNanoseconds, WritePrecision.MS); // Zeitstempel des Werts (in Millisekunden), z.B. 1732874651000 (nach Konvertierung)

            writeApi.writePoint(bucket, org, point);

        } catch (Exception e) {
            System.err.println("Error writing CO2 data to InfluxDB: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public List<String> getAllFloors() {
        Set<String> floors = new HashSet<>();

        try {


            var fluxQuery = String.format("from(bucket: \"%s\") " + "|> range(start: 0) " + "|> distinct(column: \"floor\") " + "|> keep(columns: [\"floor\"])", bucket);

            influxDBClient.getInfluxDBClient().getQueryApi().query(fluxQuery, org).forEach(table -> table.getRecords().forEach(record -> floors.add(record.getValueByKey("floor").toString())));
        } catch (Exception e) {
            Log.error("Error retrieving distinct floors: ", e);
        }

        return floors.stream().toList();
    }

    public List<String> getAllRooms() {
        Set<String> rooms = new HashSet<>();


        QueryApi queryApi = influxDBClient.getInfluxDBClient().getQueryApi();

        String fluxQuery = String.format("from(bucket: \"%s\") " + "|> range(start: 0) " + "|> distinct(column: \"room\") " + "|> keep(columns: [\"room\"])", bucket);

        queryApi.query(fluxQuery, org).forEach(table -> table.getRecords().forEach(record -> rooms.add(record.getValueByKey("room").toString())));


        return rooms.stream().toList();

    }

    public SensorBoxDTO getLatestSensorBoxDataForRoom(String room) {
        final SensorBoxDTO sensorBoxDTO = new SensorBoxDTO();
        sensorBoxDTO.setRoom(room);
        InfluxDBClient client = influxDBClient.getInfluxDBClient();
        QueryApi queryApi = client.getQueryApi();

        String fluxQuery = String.format("from(bucket: \"%s\") " + "|> range(start: 0) " + "|> filter(fn: (r) => r[\"_measurement\"] == \"sensor_box\") " + "|> filter(fn: (r) => r[\"room\"] == \"%s\") " + "|> group(columns: [\"parameter\"]) " + "|> last()", bucket, room);

        queryApi.query(fluxQuery, org).forEach(table -> table.getRecords()
                .forEach(record -> {
                    String parameter = record.getValueByKey("parameter").toString();
                    Double value = Double.parseDouble(record.getValueByKey("_value").toString());
                    long timestamp = record.getTime().toEpochMilli();

                    sensorBoxDTO.setTimestamp(timestamp); // Set timestamp from any parameter (latest)
                    sensorBoxDTO.setFloor(record.getValueByKey("floor").toString());

                    switch (parameter) {
                        case "co2":
                            sensorBoxDTO.setCo2(value);
                            break;
                        case "humidity":
                            sensorBoxDTO.setHumidity(value);
                            break;
                        case "motion":
                            sensorBoxDTO.setMotion(value);
                            break;
                        case "neopixel":
                            sensorBoxDTO.setNeopixel(value);
                            break;
                        case "noise":
                            sensorBoxDTO.setNoise(value);
                            break;
                        case "pressure":
                            sensorBoxDTO.setPressure(value);
                            break;
                        case "rssi":
                            sensorBoxDTO.setRssi(value);
                            break;
                        case "temperature":
                            sensorBoxDTO.setTemperature(value);
                            break;
                        default:
                            Log.warnf("Unhandled parameter: %s", parameter);
                            break;
                    }
                }));


        return sensorBoxDTO;
    }
    /*
    //TODO: Check if angular can handle records
    public List<Map<String,Object>> getLatestRoomValues(String room) {

        InfluxDBClient client = influxDBClient.getInfluxDBClient();
        QueryApi queryApi = client.getQueryApi();

        String fluxQuery = String.format("from(bucket: \"%s\") " + "|> range(start: 0) " + "|> filter(fn: (r) => r[\"_measurement\"] == \"sensor_box\") " + "|> filter(fn: (r) => r[\"room\"] == \"%s\") " + "|> group(columns: [\"parameter\"]) " + "|> last()", bucket, room);

        return queryApi.query(fluxQuery, org).stream()
                .map(fluxTable -> fluxTable.getRecords())
                        .map(records -> records.stream()
                                .map(record -> record.getValue()))
                        .toList();



    }*/
}
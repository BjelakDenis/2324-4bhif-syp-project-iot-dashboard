package at.htl.leoenergy.influxdb;

import at.htl.leoenergy.entity.SensorBoxDTO;
import at.htl.leoenergy.entity.SensorBoxValue;
import at.htl.leoenergy.entity.SensorValue;
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
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@ApplicationScoped
public class InfluxDbRepository {
    @ConfigProperty(name = "influxdb.url")
    String influxUrl;

    @ConfigProperty(name = "influxdb.token")
    String token;

    @ConfigProperty(name = "influxdb.org")
    String org;

    @ConfigProperty(name = "influxdb.bucket")
    String bucket;

    public void startUp(@Observes StartupEvent ev) {
        Log.infof("InfluxUrl: %s", influxUrl);
    }

    public void insertMeasurementFromJSON(SensorValue sensorValue) {
        try {
            InfluxDBClient client = InfluxDBClientFactory.create(influxUrl, token.toCharArray());
            WriteApiBlocking writeApi = client.getWriteApiBlocking();

            long currentTimeInNanoseconds = TimeUnit.MILLISECONDS.toMillis(sensorValue.getTime());

            Point point = Point.measurement("sensor_values")
                    // Beispiel: Name des Geräts, z.B. "PV-Energie"
                    .addTag("device_name", sensorValue.getDeviceName()) // device_name: PV-Energie
                    // Beispiel: ID des Wertetyps, z.B. "56"
                    .addTag("value_type_id", String.valueOf(sensorValue.getValueTypeId())) // value_type_id: 56
                    // Beispiel: Typ des gemessenen Werts, z.B. "Energy"
                    .addTag("value_type", String.valueOf(sensorValue.getValueType())) // value_type: Energy
                    // Beispiel: Gemessener Wert, z.B. "7572564"
                    .addField("value", sensorValue.getValue()) // value: 7572564
                    // Beispiel: Beziehung/Relation des Werts, z.B. "consumption/production"
                    .addTag("relation", sensorValue.getRelation()) // relation: generated
                    // Beispiel: Einheit des gemessenen Werts, z.B. "Wh"
                    .addTag("unit", sensorValue.getUnit()) // unit: Wh
                    // Beispiel: Standort des Geräts, z.B. "PV-Raum-Keller"
                    .addTag("site", sensorValue.getSite()) // site: PV-Raum-Keller
                    // Zeitstempel des Werts (in Millisekunden), z.B. 1732873810000
                    .time(currentTimeInNanoseconds, WritePrecision.MS);

            writeApi.writePoint(bucket, org, point);
            client.close();
        } catch (Exception e) {
            System.err.println("Error writing SensorValue data to InfluxDB: " + e.getMessage());
            e.printStackTrace();
        }
    }


    public void insertSensorBoxMeasurement(SensorBoxValue sensorBox) {
        try {
            InfluxDBClient client = InfluxDBClientFactory.create(influxUrl, token.toCharArray());
            WriteApiBlocking writeApi = client.getWriteApiBlocking();

            long currentTimeInNanoseconds = TimeUnit.MILLISECONDS.toMillis(sensorBox.getTime());

            Point point = Point.measurement("sensor_box").addTag("room", sensorBox.getRoom()) // Room: e72
                    .addTag("parameter", sensorBox.getParameter()) // Parameter: humidity
                    .addTag("floor", sensorBox.getFloor()) // Floor: eg
                    .addField("value", sensorBox.getValue()).time(currentTimeInNanoseconds, WritePrecision.MS); // Zeitstempel des Werts (in Millisekunden), z.B. 1732874651000 (nach Konvertierung)

            writeApi.writePoint(bucket, org, point);
            client.close();
        } catch (Exception e) {
            System.err.println("Error writing CO2 data to InfluxDB: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public List<String> getAllFloors() {
        Set<String> floors = new HashSet<>();

        try (InfluxDBClient client = InfluxDBClientFactory.create(influxUrl, token.toCharArray())) {
            QueryApi queryApi = client.getQueryApi();

            String fluxQuery = String.format("from(bucket: \"%s\") " + "|> range(start: 0) " + "|> distinct(column: \"floor\") " + "|> keep(columns: [\"floor\"])", bucket);

            queryApi.query(fluxQuery, org).forEach(table -> table.getRecords().forEach(record -> floors.add(record.getValueByKey("floor").toString())));
        } catch (Exception e) {
            Log.error("Error retrieving distinct floors: ", e);
        }

        return floors.stream().toList();
    }

    public List<String> getAllRooms() {
        Set<String> rooms = new HashSet<>();

        try (InfluxDBClient client = InfluxDBClientFactory.create(influxUrl, token.toCharArray())) {
            QueryApi queryApi = client.getQueryApi();

            String fluxQuery = String.format("from(bucket: \"%s\") " + "|> range(start: 0) " + "|> distinct(column: \"room\") " + "|> keep(columns: [\"room\"])", bucket);

            queryApi.query(fluxQuery, org).forEach(table -> table.getRecords().forEach(record -> rooms.add(record.getValueByKey("room").toString())));

        } catch (Exception e) {
            Log.error("Error retrieving distinct rooms: ", e);
        }

        return rooms.stream().toList();
    }

    public SensorBoxDTO getLatestSensorBoxDataForRoom(String room) {
        final SensorBoxDTO sensorBoxDTO = new SensorBoxDTO();
        sensorBoxDTO.setRoom(room);

        try (InfluxDBClient client = InfluxDBClientFactory.create(influxUrl, token.toCharArray())) {
            QueryApi queryApi = client.getQueryApi();

            String fluxQuery = String.format("from(bucket: \"%s\") " + "|> range(start: 0) " + "|> filter(fn: (r) => r[\"_measurement\"] == \"sensor_box\") " + "|> filter(fn: (r) => r[\"room\"] == \"%s\") " + "|> group(columns: [\"parameter\"]) " + "|> last()", bucket, room);

            queryApi.query(fluxQuery, org).forEach(table -> table.getRecords().forEach(record -> {
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

        } catch (Exception e) {
            Log.error("Error retrieving latest sensor box data for room: " + room, e);
        }

        return sensorBoxDTO;
    }
}
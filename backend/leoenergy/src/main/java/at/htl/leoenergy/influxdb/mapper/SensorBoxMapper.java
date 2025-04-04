package at.htl.leoenergy.influxdb.mapper;

import at.htl.leoenergy.entity.SensorBoxDTO;
import com.influxdb.query.FluxRecord;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

public class SensorBoxMapper {

    public static SensorBoxDTO mapToSensorBoxDTO(String room, List<FluxRecord> records) {
        Map<String, Double> values = new HashMap<>();
        AtomicReference<String> floor = new AtomicReference<>(null);
        AtomicLong timestamp = new AtomicLong(0);

        for (FluxRecord record : records) {
            String parameter = String.valueOf(record.getValueByKey("parameter"));
            Double value = Double.parseDouble(record.getValueByKey("_value").toString());
            values.put(parameter, value);

            floor.set(String.valueOf(record.getValueByKey("floor")));
            timestamp.set(record.getTime().toEpochMilli());
        }

        return new SensorBoxDTO(
                room,
                floor.get(),
                values.get("co2"),
                values.get("humidity"),
                values.get("motion"),
                values.get("neopixel"),
                values.get("noise"),
                values.get("pressure"),
                values.get("rssi"),
                values.get("temperature"),
                timestamp.get()
        );
    }
}

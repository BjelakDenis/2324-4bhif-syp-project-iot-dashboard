package at.htl.leoenergy.mqtt;

import at.htl.leoenergy.entity.SensorValue;
import at.htl.leoenergy.influxdb.UnitConverter;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class SensorValueMapper {
    @Inject
    UnitConverter converter;
    ObjectMapper objectMapper = new ObjectMapper();


    public SensorValue fromJson(String json) {
        try {
            var sensorValue = objectMapper.readValue(json, SensorValue.class);
            converter.setTypeOfDevice(sensorValue);
            return sensorValue;
        } catch (Exception e) {
            Log.error("Error during converting json string to SensorValue object!");
            throw new RuntimeException(e);
        }
    }
}

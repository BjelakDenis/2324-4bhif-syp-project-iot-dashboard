package at.htl.leoenergy.entity;

import at.htl.leoenergy.entity.SensorBoxDTO;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SensorBoxDTOTest {

    @Test
    void deleteFaultyValues_shouldHandleValidAndInvalidInputs() {
        // --- Valid values ---
        SensorBoxDTO validDto = new SensorBoxDTO(
                "room1", "EG", 500.0, 50.0, 1.0, 1.0, 100.0, 1013.25, -50.0, 20.0, 123456789L
        );

        SensorBoxDTO resultValid = SensorBoxDTO.deleteFaultyValues(validDto);

        assertEquals(500.0, resultValid.co2());
        assertEquals(50.0, resultValid.humidity());
        assertEquals(100.0, resultValid.noise());
        assertEquals(1013.25, resultValid.pressure());
        assertEquals(20.0, resultValid.temperature());

        // --- Invalid low values ---
        SensorBoxDTO invalidLowDto = new SensorBoxDTO(
                "room1", "EG", -10.0, -5.0, 0.0, 0.0, -1.0, -100.0, 0.0, -300.0, 123456789L
        );

        SensorBoxDTO resultLow = SensorBoxDTO.deleteFaultyValues(invalidLowDto);

        assertTrue(Double.isNaN(resultLow.co2()));
        assertTrue(Double.isNaN(resultLow.humidity()));
        assertTrue(Double.isNaN(resultLow.noise()));
        assertTrue(Double.isNaN(resultLow.pressure()));
        assertTrue(Double.isNaN(resultLow.temperature()));

        // --- Invalid high values (only co2, humidity, noise are limited) ---
        SensorBoxDTO invalidHighDto = new SensorBoxDTO(
                "room1", "EG", 1_000_001.0, 101.0, 0.0, 0.0, 195.0, Double.MAX_VALUE, 0.0, 1000.0, 123456789L
        );

        SensorBoxDTO resultHigh = SensorBoxDTO.deleteFaultyValues(invalidHighDto);

        assertTrue(Double.isNaN(resultHigh.co2()));
        assertTrue(Double.isNaN(resultHigh.humidity()));
        assertTrue(Double.isNaN(resultHigh.noise()));
        assertEquals(Double.MAX_VALUE, resultHigh.pressure());
        assertEquals(1000.0, resultHigh.temperature());
    }
}

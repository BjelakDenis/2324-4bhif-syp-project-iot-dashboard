package at.htl.leoenergy.entity;

public record SensorBoxDTO(
        String room,
        String floor,
        Double co2,
        Double humidity,
        Double motion,
        Double neopixel,
        Double noise,
        Double pressure,
        Double rssi,
        Double temperature,
        long timestamp
) {

    @Override
    public String toString() {
        return String.format(
                "SensorBoxDTO { Room: %s, Floor: %s, Timestamp: %d, CO2: %.2f, Humidity: %.2f, Motion: %.2f, Noise: %.2f, Pressure: %.2f, RSSI: %.2f, Temperature: %.2f }",
                room, floor, timestamp,
                co2 != null ? co2 : Double.NaN,
                humidity != null ? humidity : Double.NaN,
                motion != null ? motion : Double.NaN,
                noise != null ? noise : Double.NaN,
                pressure != null ? pressure : Double.NaN,
                rssi != null ? rssi : Double.NaN,
                temperature != null ? temperature : Double.NaN
        );
    }

    public static SensorBoxDTO deleteFaultyValues(SensorBoxDTO dto) {
        return new SensorBoxDTO(
                dto.room(),
                dto.floor(),
                isValid(dto.co2(), 0, 1_000_000) ? dto.co2() : Double.NaN,
                isValid(dto.humidity(), 0, 100) ? dto.humidity() : Double.NaN,
                dto.motion(),
                dto.neopixel(),
                isValid(dto.noise(), 0, 194) ? dto.noise() : Double.NaN,
                dto.pressure() != null && dto.pressure() >= 0 ? dto.pressure() : Double.NaN,
                dto.rssi(),
                dto.temperature() != null && dto.temperature() >= -273.15 ? dto.temperature() : Double.NaN,
                dto.timestamp()
        );
    }

    private static boolean isValid(Double value, double min, double max) {
        return value != null && value >= min && value <= max;
    }
}

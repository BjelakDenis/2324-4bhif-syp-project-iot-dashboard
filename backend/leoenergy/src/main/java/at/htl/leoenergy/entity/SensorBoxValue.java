package at.htl.leoenergy.entity;

public class SensorBoxValue extends BaseSensorValue {
    private String floor;
    private String room;
    private String parameter;

    public SensorBoxValue() {}

    public SensorBoxValue(String floor, double value, String room, String parameter, long time) {
        super(value, time);
        this.floor = floor;
        this.room = room;
        this.parameter = parameter;
    }

    public String getFloor() {
        return floor;
    }

    public void setFloor(String floor) {
        this.floor = floor;
    }

    public String getRoom() {
        return room;
    }

    public void setRoom(String room) {
        this.room = room;
    }

    public String getParameter() {
        return parameter;
    }

    public void setParameter(String parameter) {
        this.parameter = parameter;
    }

    @Override
    public String toString() {
        return String.format(
                "SensorBoxValue { Floor: %s, Value: %.2f, Room: %s, Parameter: %s, Time: %d }",
                floor, value, room, parameter, time
        );
    }
}

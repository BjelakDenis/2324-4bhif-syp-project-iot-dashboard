package at.htl.leoenergy.entity;

public abstract class BaseSensorValue {
    protected double value;
    protected long time;

    public BaseSensorValue() {}

    public BaseSensorValue(double value, long time) {
        this.value = value;
        this.time = time;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }
}

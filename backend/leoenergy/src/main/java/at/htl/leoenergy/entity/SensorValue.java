package at.htl.leoenergy.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigInteger;
@JsonIgnoreProperties(ignoreUnknown = true)
public class SensorValue extends BaseSensorValue {

    private BigInteger id;
    private Long valueTypeId;
    private String deviceName;
    private long deviceId;
    private String site;
    private String unit;
    private String valueType;
    private String relation;

    public SensorValue() {}

    public SensorValue(long deviceId, long time, double value, Long measurementId, String description, String unit, String relation, String deviceName) {
        super(value, time);
        this.valueTypeId = measurementId;
        this.deviceId = deviceId;
        this.site = description;
        this.unit = unit;
        this.relation = relation;
        this.deviceName = deviceName;
    }

    public BigInteger getId() {
        return id;
    }

    public void setId(BigInteger id) {
        this.id = id;
    }

    public Long getValueTypeId() {
        return valueTypeId;
    }

    public void setValueTypeId(Long valueTypeId) {
        this.valueTypeId = valueTypeId;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public long getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(long deviceId) {
        this.deviceId = deviceId;
    }

    public String getSite() {
        return site;
    }

    public void setSite(String site) {
        this.site = site;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getValueType() {
        return valueType;
    }

    public void setValueType(String valueType) {
        this.valueType = valueType;
    }

    public String getRelation() {
        return relation;
    }

    public void setRelation(String relation) {
        this.relation = relation;
    }

    @Override
    public String toString() {
        return String.format(
                "SensorValue { Device ID: %d, Device Name: %s, Value: %.2f, Value Type: %s, Value Type ID: %d, " +
                        "Unit: %s, Site: %s, Relation: %s, Time: %d }",
                deviceId, deviceName, value, valueType, valueTypeId, unit, site, relation, time
        );
    }
}

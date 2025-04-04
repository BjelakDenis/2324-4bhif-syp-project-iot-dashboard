package at.htl.leoenergy.influxdb;

import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;

public class PointBuilder {
    final Point point;

    public PointBuilder deviceName(String name) {
        this.point.addTag("device_name", name);
        return this;
    }

    public PointBuilder valueTypeId(Long valueTypeId) {
        this.point.addTag("value_type_id", String.valueOf(valueTypeId));
        return this;
    }

    public PointBuilder valueType(String valueType) {
        this.point.addTag("value_type", valueType);
        return this;
    }

    public PointBuilder value(Number value) {
        this.point.addField("value", value);
        return this;
    }

    public PointBuilder relation(String relation) {
        this.point.addTag("relation", relation);
        return this;
    }

    public PointBuilder unit(String unit) {
        this.point.addTag("unit", unit);
        return this;
    }

    public PointBuilder site(String site) {
        this.point.addTag("site", site);
        return this;
    }

    public PointBuilder room(String room) {
        this.point.addTag("room", room);
        return this;
    }

    public PointBuilder parameter(String parameter) {
        this.point.addTag("parameter", parameter);
        return this;
    }

    public PointBuilder floor(String floor) {
        this.point.addTag("floor", floor);
        return this;
    }

    public PointBuilder timestamp(long timestampInMillis) {
        this.point.time(timestampInMillis, WritePrecision.MS);
        return this;
    }

    PointBuilder(String measurmentName) {
        this.point = new Point(measurmentName);
    }


    Point build() {
        return point;
    }

}

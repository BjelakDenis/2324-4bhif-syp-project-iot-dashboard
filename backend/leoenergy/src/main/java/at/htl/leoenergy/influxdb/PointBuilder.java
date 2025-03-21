package at.htl.leoenergy.influxdb;

import com.influxdb.client.write.Point;

public class PointBuilder {

    final Point point;

    PointBuilder deviceName(String name) {
        this.point.addTag("device_name", name);
        return this;
    }

    PointBuilder valueTypeId(Long valueTypeId) {
        this.point.addTag("value_type_id", String.valueOf(valueTypeId));
        return this;
    }

    PointBuilder valueType(String valueType) {
        this.point.addTag("value_type", valueType);
        return this;
    }

    PointBuilder(String measurementName) {
        this.point = new Point(measurementName);
    }

    Point build() {
        return point;
    }

}

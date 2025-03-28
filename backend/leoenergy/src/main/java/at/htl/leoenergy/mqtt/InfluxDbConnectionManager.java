package at.htl.leoenergy.mqtt;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import io.quarkus.runtime.StartupEvent;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class InfluxDbConnectionManager {
    @ConfigProperty(name = "influxdb.url")
    String influxUrl;
    @ConfigProperty(name = "influxdb.token")
    String token;
    InfluxDBClient influxDBClient;
    @PostConstruct
    void init() {
        influxDBClient = InfluxDBClientFactory.create(influxUrl, token.toCharArray());
    }
    public InfluxDBClient getInfluxDBClient() { return influxDBClient; }
    @PreDestroy
    void cleanUp(){
        influxDBClient.close();
        influxDBClient = null;
    }
}

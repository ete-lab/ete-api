package br.inpe.resource;

import com.github.tomakehurst.wiremock.WireMockServer;
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;

import java.util.Map;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

public class DacWireMockTestResource implements QuarkusTestResourceLifecycleManager {
    private static WireMockServer server;

    @Override
    public Map<String, String> start() {
        server = new WireMockServer(options().dynamicPort());
        server.start();
        return Map.of("device.processor.url", server.baseUrl() + "/dac");
    }

    @Override
    public void stop() {
        if (server != null) {
            server.stop();
            server = null;
        }
    }

    static WireMockServer server() {
        return server;
    }
}

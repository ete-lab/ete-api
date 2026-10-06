package br.inpe;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/up")
public class GreetingResource {

    @ConfigProperty(name = "device.processor.url")
    String processorUrl;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String hello() {
        return "UP";
    }

    @GET
    @Path("health")
    @Produces(MediaType.TEXT_PLAIN)
    public String health() {
        return "Socket url em DEVICE_PROCESSOR_URL\": " + processorUrl + "\n" +
                "Socket url em prod: " + System.getProperty("device.processor.url") + "\n" +
                "Socket url: em dev" + System.getProperty("dev.device.processor.url") ;
    }
}

package org.example.azure.services.iotHub.simulator;

import com.codahale.metrics.annotation.Timed;
import com.microsoft.azure.sdk.iot.device.exceptions.IotHubClientException;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.net.URISyntaxException;

@Path("simulator")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class DeviceSimulatorResource {

    private final DeviceBA deviceBA;

    public DeviceSimulatorResource(DeviceBA deviceBA) {
        this.deviceBA = deviceBA;
    }

    @POST
    @Timed
    public void createStorageAccount() throws IotHubClientException, URISyntaxException, IOException {
        System.out.println("asdasd");
        deviceBA.initializeDeviceClient();
    }

}

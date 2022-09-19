package org.example.azure.resources.iotHub.devicemanagement.service;

import com.codahale.metrics.annotation.Timed;
import org.example.azure.resources.iotHub.devicemanagement.business.DeviceManagementBA;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Path("iothub/{iotHubName}/devices")
@Produces(MediaType.APPLICATION_JSON)
public class DeviceManagementService {

    private final DeviceManagementBA deviceManagementBA;

    public DeviceManagementService(DeviceManagementBA deviceManagementBA) {
        this.deviceManagementBA = deviceManagementBA;
    }

    @POST
    @Timed
    public void registerDevicesInBulk(@PathParam("iotHubName") String iotHubName,
                                      @QueryParam("devicePrefix") String devicePrefix,
                                      @QueryParam("deviceCount") Integer deviceCount,
                                      @QueryParam("authenticationType") String authenticationType) throws Exception {
        deviceManagementBA.createAndImportDevicesToIotHub(iotHubName,devicePrefix, deviceCount, authenticationType);
    }

    @GET
    @Timed
    public void getDevices(@PathParam("iotHubName") String iotHubName) throws Exception {
        deviceManagementBA.getDevicesFromIotHubToBlob(iotHubName);
    }
}
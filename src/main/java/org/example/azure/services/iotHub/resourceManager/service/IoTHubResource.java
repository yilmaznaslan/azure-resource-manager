package org.example.azure.services.iotHub.resourceManager.service;

import com.azure.resourcemanager.iothub.fluent.models.IotHubDescriptionInner;
import com.codahale.metrics.annotation.Timed;
import org.example.azure.services.iotHub.resourceManager.business.IoTHubBA;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Path("/iothub")
@Produces(MediaType.APPLICATION_JSON)
public class IoTHubResource {

    private final IoTHubBA ioTHubBA;

    public IoTHubResource(IoTHubBA ioTHubBA) {
        this.ioTHubBA = ioTHubBA;
    }



    @GET
    @Timed
    public List<IotHubDescriptionInner> getAllIoTHubs(){
       return ioTHubBA.getAllIotHubs();
    }


    @DELETE
    @Timed
    public void deleteIoTHub(@QueryParam("iotHubName") String iotHubName){
        ioTHubBA.deleteIotHub(iotHubName);
    }


}
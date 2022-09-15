package org.example.azure.resources.iotHub.resourceManager.service;

import com.azure.resourcemanager.iothub.fluent.models.IotHubDescriptionInner;
import com.azure.resourcemanager.iothub.models.IotHubDescription;
import com.azure.resourcemanager.iothub.models.IotHubProperties;
import com.codahale.metrics.annotation.Timed;
import org.example.azure.resources.iotHub.resourceManager.business.IoTHubBA;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("/iothub")
@Produces(MediaType.APPLICATION_JSON)
public class IoTHubResource {

    private final IoTHubBA ioTHubBA;

    public IoTHubResource(IoTHubBA ioTHubBA) {
        this.ioTHubBA = ioTHubBA;
    }

    @POST
    @Timed
    public Response createIoTHub(@QueryParam("iotHubName") String iotHubName){
        ioTHubBA.createIoTHub(iotHubName);
        return Response.accepted().build();
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
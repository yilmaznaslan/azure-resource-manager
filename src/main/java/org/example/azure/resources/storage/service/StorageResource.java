package org.example.azure.resources.storage.service;

import com.codahale.metrics.annotation.Timed;
import org.example.azure.resources.storage.business.StorageBA;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Path("storage")
@Produces(MediaType.APPLICATION_JSON)
public class StorageResource {

    private final StorageBA storageBA;

    public StorageResource(StorageBA storageBA) {
        this.storageBA = storageBA;
    }

    @POST
    @Timed
    public void createStorageAccount(@QueryParam("storageAccountName") String accountName) {
        storageBA.createStorageAccount(accountName);
    }

}

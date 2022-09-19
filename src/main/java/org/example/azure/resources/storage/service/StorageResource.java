package org.example.azure.resources.storage.service;

import com.codahale.metrics.annotation.Timed;
import com.microsoft.azure.storage.StorageException;
import org.example.azure.resources.storage.business.StorageBA;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;

@Path("storage")
@Produces(MediaType.APPLICATION_JSON)
public class StorageResource {

    private final StorageBA storageBA;

    public StorageResource(StorageBA storageBA) {
        this.storageBA = storageBA;
    }

    @POST
    @Timed
    public void createStorageAccount(@QueryParam("storageAccountName") String accountName,
                                     @QueryParam("containerName") String containerNAme) throws URISyntaxException, InvalidKeyException, StorageException {
        storageBA.createStorageAccountAndContainer(accountName, containerNAme);
    }

}

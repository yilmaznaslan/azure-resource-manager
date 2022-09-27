package org.example.azure.resources.storage.service;

import com.codahale.metrics.annotation.Timed;
import com.microsoft.azure.storage.StorageException;
import org.example.azure.resources.storage.business.StorageResourceManager;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;

@Path("/azure/resources/storage")
@Produces(MediaType.APPLICATION_JSON)
public class StorageResource {

    private final StorageResourceManager storageResourceManager;

    public StorageResource(StorageResourceManager storageResourceManager) {
        this.storageResourceManager = storageResourceManager;
    }

    @POST
    @Timed
    public void createStorageAccount(@QueryParam("storageAccountName") String accountName,
                                     @QueryParam("containerName") String containerNAme) throws URISyntaxException, InvalidKeyException, StorageException {
        storageResourceManager.createStorageAccountAndContainer(accountName, containerNAme);
    }

}

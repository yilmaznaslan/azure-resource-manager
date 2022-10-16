package org.example.azure.services.storage.business;

import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.SharedAccessBlobPermissions;
import com.microsoft.azure.storage.blob.SharedAccessBlobPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.EnumSet;

public class StorageBA {

    private static Logger LOGGER = LoggerFactory.getLogger(StorageBA.class);
    private CloudBlobContainer container;
    private final String storageConnectionString;

    public StorageBA(String storageConnectionString ) {
        this.storageConnectionString = storageConnectionString;
        try {
            createStorageContainer("iothubcontainer");
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        } catch (InvalidKeyException e) {
            throw new RuntimeException(e);
        } catch (StorageException e) {
            throw new RuntimeException(e);
        }
    }

    public void createStorageContainer(String containerName) throws URISyntaxException, InvalidKeyException, StorageException {
        CloudStorageAccount cloudStorageAccount = CloudStorageAccount.parse(storageConnectionString);
        CloudBlobClient blobClient = cloudStorageAccount.createCloudBlobClient();
        this.container = blobClient.getContainerReference(containerName);

        if ( container.exists()){
            LOGGER.info("Container already exists, no need to create again");
        }else {
            LOGGER.info("Container does not exist, creating it");
            container.create();
        }
    }

    public String getContainerSasUri() throws InvalidKeyException, StorageException {
        // Set the expiry time and permissions for the container.
        // In this case no start time is specified, so the shared access signature becomes valid immediately.
        SharedAccessBlobPolicy sasConstraints = new SharedAccessBlobPolicy();
        Date expirationDate = Date.from(Instant.now().plus(Duration.ofDays(1)));
        sasConstraints.setSharedAccessExpiryTime(expirationDate);
        EnumSet<SharedAccessBlobPermissions> permissions = EnumSet.of(
                SharedAccessBlobPermissions.WRITE,
                SharedAccessBlobPermissions.LIST,
                SharedAccessBlobPermissions.READ,
                SharedAccessBlobPermissions.DELETE);
        sasConstraints.setPermissions(permissions);

        // Generate the shared access signature on the container, setting the constraints directly on the signature.
        String sasContainerToken = container.generateSharedAccessSignature(sasConstraints, null);

        // Return the URI string for the container, including the SAS token.
        return container.getUri() + "?" + sasContainerToken;
    }

    public CloudBlobContainer getContainer() {
        return container;
    }
}

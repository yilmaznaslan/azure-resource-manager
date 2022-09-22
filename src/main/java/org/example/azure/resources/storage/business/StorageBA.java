package org.example.azure.resources.storage.business;

import com.azure.core.management.Region;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.storage.models.StorageAccount;
import com.azure.resourcemanager.storage.models.StorageAccountKey;
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
import java.util.List;

public class StorageBA {

    private static Logger LOGGER = LoggerFactory.getLogger(StorageBA.class);
    public static String storageConnectionString;
    private final AzureResourceManager azureResourceManager;
    private final String resourceGroupName;
    private String storageAccountName = "storagetestyilmaz";
    private String containerName = "devicecontainer";
    private CloudBlobContainer container;

    public StorageBA(String resourceGroupName, AzureResourceManager azureResourceManager) throws URISyntaxException, InvalidKeyException, StorageException {
        this.azureResourceManager = azureResourceManager;
        this.resourceGroupName = resourceGroupName;
        createStorageAccountAndContainer(this.storageAccountName, this.containerName);
    }

    public void createStorageAccountAndContainer(String storageAccountName, String containerName) throws StorageException, URISyntaxException, InvalidKeyException {
        LOGGER.info("Creating a storage account");
        this.storageAccountName = storageAccountName;
        this.containerName = containerName;
        StorageAccount storageAccount = azureResourceManager.storageAccounts().define(storageAccountName)
                .withRegion(Region.US_EAST)
                .withNewResourceGroup(resourceGroupName)
                .create();


        // get a list of storage account keys related to the account
        List<StorageAccountKey> storageAccountKeys = storageAccount.getKeys();
        for (StorageAccountKey key : storageAccountKeys) {
            System.out.println("Key name: " + key.keyName() + " with value " + key.value());
        }



        this.storageConnectionString = "DefaultEndpointsProtocol=https;AccountName="+storageAccountName+";AccountKey="+storageAccountKeys.get(0).value()+";EndpointSuffix=core.windows.net";

        createStorageContainer(containerName);
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

package org.example.azure.resources.storage.business;

import com.azure.core.management.Region;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.storage.models.StorageAccount;
import com.azure.resourcemanager.storage.models.StorageAccountKey;
import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import org.example.azure.resources.iotHub.devicemanagement.business.DeviceManagementBA;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.util.List;

public class StorageBA {

    private static Logger LOGGER = LoggerFactory.getLogger(StorageBA.class);
    public static String storageConnectionString;
    private final AzureResourceManager azureResourceManager;
    private final String resourceGroupName;
    private CloudBlobContainer container;

    public StorageBA(String resourceGroupName, AzureResourceManager azureResourceManager) {
        this.azureResourceManager = azureResourceManager;
        this.resourceGroupName = resourceGroupName;
    }

    public void createStorageAccountAmdContainer(String storageAccountName, String containerName) throws StorageException, URISyntaxException, InvalidKeyException {
        LOGGER.info("Creating a storage account");
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

    public CloudBlobContainer getContainer() {
        return container;
    }
}

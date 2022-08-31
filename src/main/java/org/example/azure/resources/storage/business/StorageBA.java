package org.example.azure.resources.storage.business;

import com.azure.core.management.Region;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.storage.models.StorageAccount;
import com.azure.resourcemanager.storage.models.StorageAccountKey;

import java.util.List;

public class StorageBA {

    private final AzureResourceManager azureResourceManager;
    private final String resourceGroupName;

    public StorageBA(String resourceGroupName, AzureResourceManager azureResourceManager) {
        this.azureResourceManager = azureResourceManager;
        this.resourceGroupName = resourceGroupName;
    }

    public void createStorageAccount(String storageAccountName){
        StorageAccount storageAccount = azureResourceManager.storageAccounts().define(storageAccountName)
                .withRegion(Region.US_EAST)
                .withNewResourceGroup(resourceGroupName)
                .create();

        // get a list of storage account keys related to the account
        List<StorageAccountKey> storageAccountKeys = storageAccount.getKeys();
        for (StorageAccountKey key : storageAccountKeys) {
            System.out.println("Key name: " + key.keyName() + " with value " + key.value());
        }
    }
}

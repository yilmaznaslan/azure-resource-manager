package org.example.azure.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;

import javax.validation.constraints.NotEmpty;

public class DefaultConfiguration extends Configuration {

    @NotEmpty
    private String iotHubResourceGroupName;

    @NotEmpty
    private String storageAccountsResourceGroupName;


    public String getIotHubResourceGroupName(){
        return this.iotHubResourceGroupName;
    }

    public void setIotHubResourceGroupName(String iotHubResourceGroupName) {
        this.iotHubResourceGroupName = iotHubResourceGroupName;
    }

    public String getStorageAccountsResourceGroupName() {
        return storageAccountsResourceGroupName;
    }

    public void setStorageAccountsResourceGroupName(String storageAccountsResourceGroupName) {
        this.storageAccountsResourceGroupName = storageAccountsResourceGroupName;
    }
}
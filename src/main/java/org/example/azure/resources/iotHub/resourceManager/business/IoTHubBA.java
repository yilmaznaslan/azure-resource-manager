package org.example.azure.resources.iotHub.resourceManager.business;

import com.azure.core.credential.TokenCredential;
import com.azure.core.management.profile.AzureProfile;
import com.azure.core.util.Context;
import com.azure.resourcemanager.iothub.IotHubManager;
import com.azure.resourcemanager.iothub.fluent.models.IotHubDescriptionInner;
import com.azure.resourcemanager.iothub.models.IotHubDescription;
import com.azure.resourcemanager.iothub.models.SharedAccessSignatureAuthorizationRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

import static org.example.azure.MainApplication.RESOURCE_GROUP_NAME;

public class IoTHubBA {

    private static Logger LOGGER = LoggerFactory.getLogger(IoTHubBA.class);

    private String SHARED_ACCESS_POLICY_NAME = "iothubowner";

    private IotHubManager iotHubManager;

    public IoTHubBA(TokenCredential tokenCredential, AzureProfile profile) {
        this.iotHubManager = IotHubManager.authenticate(tokenCredential, profile);

    }

    // ToDO All the methods here first should check if the iothub is in active state
    public String getPrimarySharedAccessSignature(String iotHubName){
        SharedAccessSignatureAuthorizationRule sasRule = this.iotHubManager.iotHubResources().getKeysForKeyName(RESOURCE_GROUP_NAME, iotHubName, SHARED_ACCESS_POLICY_NAME);
        String primarySharedAccessSignature = sasRule.primaryKey();
        return primarySharedAccessSignature;
    }

    public String getIotHubConnectionString(String iotHubName){
        String sas = getPrimarySharedAccessSignature(iotHubName);
        String iotHubConnectionString = "HostName="+iotHubName+".azure-devices.net;SharedAccessKeyName=iothubowner;SharedAccessKey="+sas;
        return iotHubConnectionString;
    }

    public List<IotHubDescriptionInner> getAllIotHubs() {
        return iotHubManager.iotHubResources().list().stream().map(asd -> asd.innerModel()).collect(Collectors.toList());
    }

    public IotHubDescription deleteIotHub(String resourceName){
        LOGGER.info("Deleting IoTHubId: " + resourceName);
        return iotHubManager.iotHubResources().delete(RESOURCE_GROUP_NAME, resourceName, Context.NONE);
    }

    public IotHubDescription getIotHub(String iotHubName){
        LOGGER.info("Getting IoTHub: " + iotHubName);
        return iotHubManager.iotHubResources().getByResourceGroup(RESOURCE_GROUP_NAME, iotHubName);
    }

}

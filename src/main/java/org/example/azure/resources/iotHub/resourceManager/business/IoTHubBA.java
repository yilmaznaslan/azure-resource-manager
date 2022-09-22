package org.example.azure.resources.iotHub.resourceManager.business;

import com.azure.core.credential.TokenCredential;
import com.azure.core.management.Region;
import com.azure.core.management.profile.AzureProfile;
import com.azure.core.util.Context;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.iothub.IotHubManager;
import com.azure.resourcemanager.iothub.fluent.models.IotHubDescriptionInner;
import com.azure.resourcemanager.iothub.models.IotHubDescription;
import com.azure.resourcemanager.iothub.models.SharedAccessSignatureAuthorizationRule;
import com.azure.resourcemanager.resources.models.Deployment;
import com.azure.resourcemanager.resources.models.DeploymentMode;
import org.apache.commons.io.IOUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class IoTHubBA {

    private static Logger LOGGER = LoggerFactory.getLogger(IoTHubBA.class);
    private String RESOURCE_GROUP_NAME;
    private String SHARED_ACCESS_POLICY_NAME = "iothubowner";

    private AzureResourceManager azureResourceManager;

    private IotHubManager iotHubManager;

    public IoTHubBA(String resourceGroupName, TokenCredential tokenCredential, AzureResourceManager azureResourceManager, AzureProfile profile) {
        this.azureResourceManager = azureResourceManager;
        this.iotHubManager = IotHubManager.authenticate(tokenCredential, profile);
        RESOURCE_GROUP_NAME = resourceGroupName;
    }

    public void createIoTHub(String iotHubName){

        LOGGER.info("Creating IoTHubResource");

        String pathToIotHubResourceTemplate = "org/example/azure/iotHub/ResourceManager/iotHubResourceTemplate.json";
        File filePath = new File(Objects.requireNonNull(this.getClass().getClassLoader().getResource(pathToIotHubResourceTemplate)).getPath());
        try(InputStream templatein = new BufferedInputStream(new FileInputStream( filePath));
            StringWriter templateWriter = new StringWriter();
        ){
            // Read the template.json file
            IOUtils.copy(templatein, templateWriter);

            // Convert template to JSON object
            JSONObject templateNode = new JSONObject(templateWriter.toString());

            // Add default value for parameters
            JSONObject parameterValue = templateNode.optJSONObject("parameters");
            parameterValue.optJSONObject("sku_name").put("defaultValue","B1");
            parameterValue.optJSONObject("sku_units").put("defaultValue","1");
            parameterValue.optJSONObject("d2c_partitions").put("defaultValue","4");
            parameterValue.optJSONObject("location").put("defaultValue", Region.US_EAST.toString());
            parameterValue.optJSONObject("features").put("defaultValue","None");
            parameterValue.optJSONObject("name").put("defaultValue",iotHubName);

            // Deploy
            Deployment deployment = azureResourceManager.deployments().define("CreateIOTHub")
                    .withExistingResourceGroup(RESOURCE_GROUP_NAME)
                    .withTemplate(templateNode.toString())
                    .withParameters("{}")
                    .withMode(DeploymentMode.INCREMENTAL)
                    .create();

        } catch (FileNotFoundException e) {
            LOGGER.warn("Couldn't find resourceTemplate file");
        } catch (IOException e) {
            LOGGER.warn("IOException occured. Reason: ",e.getCause(), e);
        }
    }

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

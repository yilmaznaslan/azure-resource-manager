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
import io.dropwizard.Configuration;
import org.apache.commons.io.IOUtils;
import org.example.azure.config.DefaultConfiguration;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class IoTHubBA {

    private static Logger LOGGER = LoggerFactory.getLogger(IoTHubBA.class);

    private final String RESOURCE_GROUP_NAME;
    public static String KEY_NAME = "iothubowner";
    private static String PATH_TO_IOTHUB_RESOURCE_TEMPLATE = "org/example/azure/iotHub/ResourceManager/iotHubResourceTemplate.json";

    private AzureResourceManager azureResourceManager;

    private IotHubManager iotHubManager;

    public IoTHubBA(String resourceGroupName, TokenCredential tokenCredential, AzureResourceManager azureResourceManager, AzureProfile profile) {
        this.azureResourceManager = azureResourceManager;
        this.iotHubManager = IotHubManager.authenticate(tokenCredential, profile);
        RESOURCE_GROUP_NAME = resourceGroupName;
    }



    public void createIoTHub(String iotHubName){

        LOGGER.info("Creating IoTHubResource");

        File filePath = new File(Objects.requireNonNull(this.getClass().getClassLoader().getResource(PATH_TO_IOTHUB_RESOURCE_TEMPLATE)).getPath());
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

            String sas = getPrimarySharedAccessKey(iotHubName);
            LOGGER.info("Primary Shared Access Key: " + sas);
            //iotHubConnectionString = iotHubConnectionString+sharedAccessKey;


        } catch (FileNotFoundException e) {
            LOGGER.warn("Couldn't find resourceTemplate file");
        } catch (IOException e) {
            LOGGER.warn("",e.getCause(), e);
        }
    }

    public String getPrimarySharedAccessKey(String iotHubName){
        SharedAccessSignatureAuthorizationRule sasRule = this.iotHubManager.iotHubResources().getKeysForKeyName(RESOURCE_GROUP_NAME, iotHubName, KEY_NAME);
        String primarySharedAccessKey = sasRule.primaryKey();
        return primarySharedAccessKey;
    }


    public List<IotHubDescriptionInner> getAllIotHubs() {
        return iotHubManager.iotHubResources().list().stream().map(asd -> asd.innerModel()).collect(Collectors.toList());
    }


    public IotHubDescription deleteIotHub(String resourceName){
        LOGGER.info("Deleting IoTHubId: " + resourceName);
        return iotHubManager.iotHubResources().delete(RESOURCE_GROUP_NAME, resourceName, Context.NONE);
    }

    public String getIotHubConnectionString(String iotHubName){
       String sas = getPrimarySharedAccessKey(iotHubName);
  //      LOGGER.info("Primary Shared Access Key: " + sas);
//        public static String iotHubConnectionString = "HostName=IoTHubForSmartMobility.azure-devices.net;SharedAccessKeyName=iothubowner;SharedAccessKey=";

        String iotHubConnectionString = "HostName="+iotHubName+".azure-devices.net;SharedAccessKeyName=iothubowner;SharedAccessKey="+sas;
        //iotHubConnectionString = iotHubConnectionString+sharedAccessKey;
    //this.getIotHub(iotHubName).properties()

    return iotHubConnectionString;
    }

    public IotHubDescription getIotHub(String iotHubName){
        return iotHubManager.iotHubResources().getByResourceGroup(RESOURCE_GROUP_NAME, iotHubName);
    }


}

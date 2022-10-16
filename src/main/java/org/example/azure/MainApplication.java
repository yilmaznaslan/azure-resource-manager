package org.example.azure;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.profile.AzureProfile;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.iothub.IotHubManager;
import io.dropwizard.Application;
import io.dropwizard.setup.Environment;
import org.example.azure.config.DefaultConfiguration;
import org.example.azure.services.iotHub.resourceManager.business.IoTHubResourceManager;
import org.example.azure.services.iotHub.devicemanagement.business.DeviceManagementBA;
import org.example.azure.services.iotHub.devicemanagement.service.DeviceManagementService;
import org.example.azure.services.iotHub.resourceManager.business.IoTHubBA;
import org.example.azure.services.iotHub.resourceManager.service.IoTHubResource;
import org.example.azure.services.storage.business.StorageBA;
import org.example.azure.services.storage.business.StorageResourceManager;
import org.example.azure.services.storage.service.StorageResource;
import org.example.azure.services.iotHub.simulator.DeviceBA;
import org.example.azure.services.iotHub.simulator.DeviceSimulatorResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MainApplication extends Application<DefaultConfiguration> {

    private static Logger LOGGER = LoggerFactory.getLogger(MainApplication.class);
    public static String RESOURCE_GROUP_NAME;
    public static String STORAGE_ACCOUNT_CONNECTION_STRING;
    public static String IOTHUB_CONNECTION_STRING;
    public static void main(String[] args) throws Exception {
        STORAGE_ACCOUNT_CONNECTION_STRING = System.getenv("STORAGE_ACCOUNT_CONNECTION_STRING");
        IOTHUB_CONNECTION_STRING = System.getenv("IOTHUB_CONNECTION_STRING");
        new MainApplication().run(args);
    }


    @Override
    public void run(DefaultConfiguration configuration, Environment environment) throws Exception {

        RESOURCE_GROUP_NAME = configuration.getIotHubResourceGroupName();
        final AzureProfile profile = new AzureProfile(AzureEnvironment.AZURE);
        final TokenCredential credential = new DefaultAzureCredentialBuilder()
                .authorityHost(profile.getEnvironment().getActiveDirectoryEndpoint())
                .build();


        AzureResourceManager azureResourceManager = AzureResourceManager
                .configure()
                .withLogLevel(HttpLogDetailLevel.BASIC)
                .authenticate(credential, profile)
                .withDefaultSubscription();

        IotHubManager iotHubManager = IotHubManager.authenticate(credential, profile);

        String resourceGroupName = configuration.getIotHubResourceGroupName();
        IoTHubBA ioTHubBA = new IoTHubBA(credential, profile);
        StorageBA storageBA = new StorageBA(STORAGE_ACCOUNT_CONNECTION_STRING);
        DeviceManagementBA deviceManagementBA = new DeviceManagementBA(iotHubManager, storageBA, resourceGroupName, IOTHUB_CONNECTION_STRING);
        DeviceBA deviceBA = new DeviceBA();

        StorageResourceManager storageResourceManager = new StorageResourceManager(resourceGroupName, azureResourceManager);


        // Create resources
        DeviceSimulatorResource deviceSimulatorResource = new DeviceSimulatorResource(deviceBA);
        IoTHubResourceManager defaultResource = new IoTHubResourceManager(azureResourceManager);
        IoTHubResource ioTHubResource = new IoTHubResource(ioTHubBA);
        DeviceManagementService deviceManagementService = new DeviceManagementService(deviceManagementBA);
        StorageResource storageResource = new StorageResource(storageResourceManager);

        // Register resources
        environment.jersey().register(deviceSimulatorResource);
        environment.jersey().register(defaultResource);
        environment.jersey().register(ioTHubResource);
        environment.jersey().register(deviceManagementService);
        environment.jersey().register(storageResource);

    }

    private boolean isIoTHubConnectionStringValidated(){
        if (IOTHUB_CONNECTION_STRING == null){
            return false;
        }
        return true;
    }
}

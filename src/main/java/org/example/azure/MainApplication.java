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
import org.example.azure.resources.resourceManager.DefaultResource;
import org.example.azure.resources.iotHub.devicemanagement.business.DeviceManagementBA;
import org.example.azure.resources.iotHub.devicemanagement.service.DeviceManagementService;
import org.example.azure.resources.iotHub.resourceManager.business.IoTHubBA;
import org.example.azure.resources.iotHub.resourceManager.service.IoTHubResource;
import org.example.azure.resources.storage.business.StorageBA;
import org.example.azure.resources.storage.service.StorageResource;
import org.example.azure.resources.iotHub.simulator.DeviceBA;
import org.example.azure.resources.iotHub.simulator.DeviceSimulatorResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MainApplication extends Application<DefaultConfiguration> {

    private static Logger LOGGER = LoggerFactory.getLogger(MainApplication.class);
    public static String RESOURCE_GROUP_NAME = "yilmaz_ResourceGroup";
    public static void main(String[] args) throws Exception {
        new MainApplication().run(args);
    }


    @Override
    public void run(DefaultConfiguration configuration, Environment environment) throws Exception {

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

        String resourceGroupName = configuration.getResourceGroupName();
        IoTHubBA ioTHubBA = new IoTHubBA(credential, profile);
        StorageBA storageBA = new StorageBA(resourceGroupName, azureResourceManager);
        DeviceManagementBA deviceManagementBA = new DeviceManagementBA(iotHubManager, ioTHubBA, storageBA, resourceGroupName);

        DeviceBA deviceBA = new DeviceBA();
        DeviceSimulatorResource deviceSimulatorResource = new DeviceSimulatorResource(deviceBA);

        final DefaultResource defaultResource = new DefaultResource(azureResourceManager);
        final IoTHubResource ioTHubResource = new IoTHubResource(ioTHubBA);
        final DeviceManagementService deviceManagementService = new DeviceManagementService(deviceManagementBA);
        StorageResource storageResource = new StorageResource(storageBA);


        environment.jersey().register(deviceSimulatorResource);
        environment.jersey().register(defaultResource);
        environment.jersey().register(ioTHubResource);
        environment.jersey().register(deviceManagementService);
        environment.jersey().register(storageResource);

    }
}

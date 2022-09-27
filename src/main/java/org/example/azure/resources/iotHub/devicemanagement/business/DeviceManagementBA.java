package org.example.azure.resources.iotHub.devicemanagement.business;

import com.azure.core.util.Context;
import com.azure.resourcemanager.iothub.IotHubManager;
import com.azure.resourcemanager.iothub.models.ExportDevicesRequest;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.microsoft.azure.sdk.iot.service.twin.Twin;
import com.microsoft.azure.sdk.iot.service.twin.TwinClient;
import com.microsoft.azure.sdk.iot.service.twin.TwinCollection;
import com.microsoft.azure.sdk.iot.service.auth.AuthenticationMechanism;
import com.microsoft.azure.sdk.iot.service.auth.AuthenticationType;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import com.microsoft.azure.sdk.iot.service.registry.*;
import com.microsoft.azure.storage.blob.CloudBlob;
import com.microsoft.azure.storage.blob.CloudBlockBlob;
import com.microsoft.azure.storage.blob.ListBlobItem;
import org.example.azure.resources.iotHub.resourceManager.business.IoTHubBA;
import org.example.azure.resources.storage.business.StorageBA;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;


public class DeviceManagementBA {

    private static Logger LOGGER = LoggerFactory.getLogger(DeviceManagementBA.class);

    public String relativePathForImportedDevices = "org/example/azure/iotHub/DeviceManager/exportedDevices.json";
    private static final Gson gson = new GsonBuilder().disableHtmlEscaping().create();
    private static boolean excludeKeys = false;
    private static String importBlobName = "devices.txt";
    private final IotHubManager iotHubManager;
    private final IoTHubBA ioTHubBA;

    private final String iotHubConnectionString;
    private final StorageBA storageBA;
    private final String resourceGroupName;

    public DeviceManagementBA(IotHubManager iotHubManager, IoTHubBA ioTHubBA, StorageBA storageBA, String resourceGroupName, String iotHubConnectionString) {
        this.iotHubManager = iotHubManager;
        this.ioTHubBA = ioTHubBA;
        this.storageBA = storageBA;
        this.resourceGroupName = resourceGroupName;
        this.iotHubConnectionString = iotHubConnectionString;
    }

    //ToDO The exportDevicesWithResponse might not work. Check if it works
    public Object iotHubResourceExportDevices(String iotHubName) {
        return iotHubManager
                .iotHubResources()
                .exportDevicesWithResponse(
                        resourceGroupName,
                        iotHubName,
                        new ExportDevicesRequest().withExportBlobContainerUri("testBlob").withExcludeKeys(true),
                        Context.NONE);
    }


    public void patchDeviceTwin(String deviceId) throws IOException, IotHubException {

        TwinClient twinClient = new TwinClient(iotHubConnectionString);
        Twin twin = twinClient.get(deviceId);
        TwinCollection twinCollection = new TwinCollection();
        twinCollection.put("customerId", "null");
        twinCollection.put("country", "germany");
        twinCollection.put("SoftwareId", "xe123");
        twin.setTags(twinCollection);
        twinClient.patch(twin);

    }

    public void registerSingleDevice(String deviceId) {
        RegistryClient registryClient = new RegistryClient(iotHubConnectionString);
        Device device = new Device(deviceId);

        try {
            device = registryClient.addDevice(device);
            System.out.println("Device created: " + device.getDeviceId());
            System.out.println("Device key: " + device.getPrimaryKey());
            HashMap<String, String> tags  = new HashMap<String, String>();
            tags.put("countery", "germany");
            patchDeviceTwin(device.getDeviceId());

        } catch (IotHubException | IOException iote) {
            iote.printStackTrace();
        }
    }

    public void getDevicesFromIotHubToBlob(String iotHubName) throws Exception {
        LOGGER.info("Exporting devices from IoTHub to blob");

        RegistryClient registryClient = new RegistryClient(iotHubConnectionString);
        String containerSasUri = storageBA.getContainerSasUri();
        RegistryJob exportJob = registryClient.exportDevices(containerSasUri, excludeKeys);

        while (true) {
            exportJob = registryClient.getJob(exportJob.getJobId());
            if (exportJob.getStatus() == RegistryJob.JobStatus.COMPLETED) {
                break;
            }
            Thread.sleep(500);
        }
        LOGGER.info("The job for exporting devices from IoTHub to blob is completed");

        File importedDevicePath = new File(DeviceManagementBA.class.getClassLoader().getResource(relativePathForImportedDevices).getPath());
        InputStream is = this.getClass()
                .getClassLoader()
                .getResourceAsStream(relativePathForImportedDevices);
        for (ListBlobItem blobItem : storageBA.getContainer().listBlobs()) {
            if (blobItem instanceof CloudBlob) {
                CloudBlob blob = (CloudBlob) blobItem;
                //blob.download(new FileOutputStream(exportFileLocation + blob.getName()));
                String blobName = blob.getName();
                if (blobName.equals("devices.txt")) {
                    blob.downloadToFile(importedDevicePath.getAbsolutePath());
                    LOGGER.info("Downloading file to the path: " + importedDevicePath.getAbsolutePath());
                    break;
                }
            }
        }

    }

    public void createAndRegisterDevicesToIotHub(String iotHubName, String devicePrefix, int deviceCount, String authenticationType) throws Exception {
        createDevicesInBlob(devicePrefix, deviceCount, authenticationType);
        registerDevicesFromBlobToIoTHub(iotHubName);
    }

    private void createDevicesInBlob(String devicePrefix, int deviceCount, String authenticationType) throws Exception {
        LOGGER.debug("Starting to create devices in blob. ");

        // Creating the list of devices to be submitted for import
        StringBuilder devicesToImport = new StringBuilder();
        for (int i = 0; i < deviceCount; i++) {
            ExportImportDevice deviceToAdd = new ExportImportDevice();
            TwinCollection twinCollection = new TwinCollection();
            twinCollection.put("customerId", "null");
            twinCollection.put("country", "germany");
            twinCollection.put("SoftwareId", "xe123");
            deviceToAdd.setTags(twinCollection);
            String deviceId = devicePrefix + "_" + i;


            if (authenticationType.equals(AuthenticationType.SAS.name())) {
                Device device = new Device(deviceId);
                AuthenticationMechanism authentication = new AuthenticationMechanism(device.getSymmetricKey());
                deviceToAdd.setAuthentication(authentication);
            }
            if (authenticationType.equals(AuthenticationType.SELF_SIGNED.name())) {
                String primaryThumbprint = "DE89B7BBD215E7E47ECD372F61205712D71DD521";
                String secondaryThumbprint = "DE89B7BBD215E7E47ECD372F61205712D71DD521";
                AuthenticationMechanism authentication = new AuthenticationMechanism(primaryThumbprint, secondaryThumbprint);
                deviceToAdd.setAuthentication(authentication);
            }
            if (authenticationType.equals(AuthenticationType.CERTIFICATE_AUTHORITY.name())) {
                AuthenticationMechanism authentication = new AuthenticationMechanism(AuthenticationType.CERTIFICATE_AUTHORITY);
                deviceToAdd.setAuthentication(authentication);
            }

            //deviceToAdd.setDesiredProperties();
            deviceToAdd.setId(deviceId);
            deviceToAdd.setStatus(DeviceStatus.Enabled);
            deviceToAdd.setImportMode(ImportMode.CreateOrUpdate);
            devicesToImport.append(gson.toJson(deviceToAdd));
            if (i < deviceCount - 1) {
                devicesToImport.append("\r\n");
            }
        }

        byte[] blobToImport = devicesToImport.toString().getBytes(StandardCharsets.UTF_8);

        // Creating the Azure storage blob and uploading the serialized string of devices
        LOGGER.info("Uploading " + blobToImport.length + " bytes into Azure storage.");
        InputStream stream = new ByteArrayInputStream(blobToImport);
        CloudBlockBlob importBlob = storageBA.getContainer().getBlockBlobReference(importBlobName);
        importBlob.deleteIfExists();
        importBlob.upload(stream, blobToImport.length);
    }

    private void registerDevicesFromBlobToIoTHub(String iotHubName) throws Exception {
        LOGGER.info("Registering devices from blob to iothub : {}", iotHubName);

        // Create and start the import job
        RegistryClient registryClient = new RegistryClient(iotHubConnectionString);
        String containerSasUri = storageBA.getContainerSasUri();
        RegistryJob importJob = registryClient.importDevices(containerSasUri, containerSasUri);

        // Waiting for the import job to complete
        while (true) {
            importJob = registryClient.getJob(importJob.getJobId());
            if (importJob.getStatus() == RegistryJob.JobStatus.COMPLETED
                    || importJob.getStatus() == RegistryJob.JobStatus.FAILED) {
                break;
            }
            Thread.sleep(500);
        }

        // Checking the result of the import job
        if (importJob.getStatus() == RegistryJob.JobStatus.COMPLETED) {
            LOGGER.info("Import job completed. The new devices are now added to the hub.");
        } else {
            LOGGER.error("Import job failed. Failure reason: " + importJob.getFailureReason());
        }

        //Cleaning up the blob
        /*
        for (ListBlobItem blobItem : container.listBlobs()) {
            if (blobItem instanceof CloudBlob) {
                CloudBlob blob = (CloudBlockBlob) blobItem;
                blob.deleteIfExists();
            }
        }
         */

    }

}

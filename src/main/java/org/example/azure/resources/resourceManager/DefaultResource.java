package org.example.azure.resources.resourceManager;

import com.azure.core.management.Region;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.resources.models.Deployment;
import com.azure.resourcemanager.resources.models.DeploymentMode;
import com.azure.resourcemanager.resources.models.ResourceGroup;
import com.codahale.metrics.annotation.Timed;
import org.apache.commons.io.IOUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.example.azure.MainApplication.RESOURCE_GROUP_NAME;

@Path("/azure/resources")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class DefaultResource {
    private static Logger LOGGER = LoggerFactory.getLogger(DefaultResource.class);

    private final AzureResourceManager azureResourceManager;


    public DefaultResource(AzureResourceManager azureResourceManager) {
        this.azureResourceManager = azureResourceManager;
    }

    public List<ResourceGroup> getResourceGroups(){
        LOGGER.info("Getting resource groups");

        List<ResourceGroup> resourceGroups = new ArrayList<ResourceGroup>();
        for (ResourceGroup rGroup : azureResourceManager.resourceGroups().list()) {
            LOGGER.info("Resource group: " + rGroup.name());
            resourceGroups.add(rGroup);
        }
        return resourceGroups;
    }

    @Path("/iothub")
    @POST
    public Response createIoTHubS(@QueryParam("iotHubName") String iotHubName){
        createIoTHub(iotHubName);
        return Response.accepted().build();
    }

    private void createIoTHub(String iotHubName){
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
            parameterValue.optJSONObject("sku_name").put("defaultValue","s1");
            //parameterValue.optJSONObject("sku_name").put("defaultValue","B1");
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
}
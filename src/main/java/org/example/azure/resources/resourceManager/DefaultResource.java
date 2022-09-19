package org.example.azure.resources.resourceManager;

import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.resources.models.ResourceGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.List;

@Path("/azure/resources")
@Produces(MediaType.APPLICATION_JSON)
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
}
package org.example.azure.resources.iotHub.devicemanagement.model;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DeviceModel {

    private final String id;
    private final String eTag;
    private final String status;

    @JsonCreator
    public DeviceModel(String id, String eTag, String status) {
        this.id = id;
        this.eTag = eTag;
        this.status = status;
    }
}

package org.example.azure.resources.iotHub.simulator;

import com.microsoft.azure.sdk.iot.device.DeviceClient;
import com.microsoft.azure.sdk.iot.device.IotHubClientProtocol;
import com.microsoft.azure.sdk.iot.device.IotHubMessageResult;
import com.microsoft.azure.sdk.iot.device.Message;
import com.microsoft.azure.sdk.iot.device.exceptions.IotHubClientException;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static com.microsoft.azure.sdk.iot.device.IotHubStatusCode.OK;

public class DeviceBA {
    private static Logger LOGGER = LoggerFactory.getLogger(DeviceBA.class);

    private DeviceClient deviceClient;
    // Plug and play features are available over MQTT, MQTT_WS, AMQPS, and AMQPS_WS.
    private static final IotHubClientProtocol protocol = IotHubClientProtocol.MQTT;

    /**
     * Initialize the device client instance over Mqtt protocol, setting the ModelId into ClientOptions.
     * This method also sets a connection status change callback, that will get triggered any time the device's connection status changes.
     */
    public void initializeDeviceClient() throws IotHubClientException
    {
        System.out.println("You hey");
        // ToDo this should be also automated !
        String deviceConnectionString = "HostName=smartIoTHubYilmaz.azure-devices.net;DeviceId=evehicle_1;SharedAccessKey=vHJyf+qGsNCxavyl1pV6IA==";
        this.deviceClient = new DeviceClient(deviceConnectionString, protocol);

        deviceClient.setConnectionStatusChangeCallback((context) -> {
            LOGGER.debug("Connection status change registered: status={}, reason={}", context.getNewStatus(), context.getNewStatusReason());

            Throwable throwable = context.getCause();
            if (throwable != null) {
                LOGGER.debug("The connection status change was caused by the following Throwable: {}", throwable.getMessage());
                throwable.printStackTrace();
            }
        }, deviceClient);

        deviceClient.open(false);
        asd();
    }

    private void asd (){
        new Thread(new Runnable() {
            @SneakyThrows({InterruptedException.class, IOException.class})
            @Override
            public void run() {
                while (true) {
                    sendTemperatureTelemetry();
                    try {
                        Thread.sleep(5 * 1000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }).start();
    }

    private void sendTemperatureTelemetry() {
        String telemetryName = "temperature";
        double temperature = 20.0d;
        String telemetryPayload = String.format("{\"%s\": %f}", telemetryName, temperature);

        Message message = new Message(telemetryPayload);
        message.setContentEncoding(StandardCharsets.UTF_8.name());
        message.setContentType("application/json");

        deviceClient.sendEventAsync(message, new MessageSentCallback(), message);
        MessageReceivedCallback callback = new MessageReceivedCallback();
        deviceClient.setMessageCallback(callback, null);
        //LOGGER.info("Telemetry: Sent - {\"{}\": {}°C} with message Id {}.", telemetryName, temperature, message.getMessageId());
        //temperatureReadings.put(new Date(), temperature);
    }

    /**
     * The callback to be invoked when a telemetry response is received from IoT Hub.
     */
    private static class MessageSentCallback implements com.microsoft.azure.sdk.iot.device.MessageSentCallback {
        @Override
        public void onMessageSent(Message sentMessage, IotHubClientException exception, Object callbackContext) {
            Message msg = (Message) callbackContext;
            LOGGER.info("Telemetry - Response from IoT Hub: message Id={}, status={}", msg.getMessageId(), exception == null ? OK : exception.getStatusCode());
        }
    }


    /**
     * The callback to be invoked when a telemetry response is received from IoT Hub.
     */
    private static class MessageReceivedCallback implements com.microsoft.azure.sdk.iot.device.MessageCallback {

        @Override
        public IotHubMessageResult onCloudToDeviceMessageReceived(Message message, Object callbackContext) {
            Message msg = (Message) callbackContext;
            LOGGER.info("Message recevied from cloud: message Id={}, status={}", msg.getMessageId());
            System.out.println("Received message with content: " + new String(msg.getBytes(), Message.DEFAULT_IOTHUB_MESSAGE_CHARSET));
            return IotHubMessageResult.COMPLETE;
        }
    }


}

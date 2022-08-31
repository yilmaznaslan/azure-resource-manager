# Azure IoT Development Guide


`java -cp ":build/libs/all-in-one-jar-1.0-SNAPSHOT.jar" -jar build/libs/all-in-one-jar-1.0-SNAPSHOT.jar`

java -cp ":build/classes/java/main" -jar build/libs/all-in-one-jar-1.0-SNAPSHOT.jar

java -cp ":build/classes/java/main:build/libs/all-in-one-jar-1.0-SNAPSHOT.jar" com/maibornwolff/azure/iot/ResourceManager

java -cp ":build/classes/java/main:build/libs/all-in-one-jar-1.0-SNAPSHOT.jar" com.maibornwolff.azure.iotHub.ResourceManager



## IoT Hub

### Device Management

#### Device Identity Management

##### Importing device identities to IoT HUB a.k.a Device Registry in Bulk




##
![Azure Role Assignment](https://docs.microsoft.com/en-us/azure/includes/role-based-access-control/media/scope-levels.png)


## How to run
./gradlew run --args='server build/resources/main/serverConfig.yml'

## Refernece
- https://docs.microsoft.com/en-us/java/api/overview/azure/resourcemanager-iothub-readme?view=azure-java-stable
- https://www.inkoop.io/blog/how-to-get-azure-api-credentials/
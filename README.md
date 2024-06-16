# Spring Kafka deep dive

## Local setup

**_pre required:_** Docker

### Build
```
./mvnw clean package
```

### Run

```
docker-compose up -d
```

Start application in your favorite IDE.

open http://localhost:8080/ui/docker-kafka-server/schema. Add the following two schemas:

| Subject               | Compatibility Level | Schema Type | Schema                                                              |
|-----------------------|---------------------|-------------|---------------------------------------------------------------------|
| document-create-key   | Backward            | AVRO        | [document-info-key.avsc](avro-schemas%2Fdocument-info-key.avsc)     |
| document-create-value | Backward            | AVRO        | [document-info-value.avsc](avro-schemas%2Fdocument-info-value.avsc) |

open http://localhost:8080/ui/docker-kafka-server/topic/documentInfo/data. Produce to topic:

| Key schema          | Key                   | Value schema          | Value                                 |
|---------------------|-----------------------|-----------------------|---------------------------------------|
| document-create-key | {"referenceId":"123"} | document-create-value | {"orderId": "200","product": "HOUSE"} |

This will produce a message in http://localhost:8080/ui/docker-kafka-server/topic/document-generated/data meaning the application 
consumed and produced messages correctly.

## Error handling

open http://localhost:8080/ui/docker-kafka-server/topic/documentInfo/data. Produce to topic:

| Key schema          | Key                   | Value schema          | Value                                 |
|---------------------|-----------------------|-----------------------|---------------------------------------|
| document-create-key | {"referenceId":"123"} | document-create-value | {"orderId": "500","product": "HOUSE"} |

An orderId 500 value will make the documents endpoint in DocumentService return a 500 http response. 
The message will be retried 9 time and will be logged. This is the default behavior.
Only logging the failure is in most cases not enough, because the message is lost.
To change this, it's possible to send the message to the dead letter topic (DLT). 
To do so stop the application, uncomment the beans on line 25 and 31 in ExceptionConfiguration and start the application again. 
Produce the message again.

open http://localhost:8080/ui/docker-kafka-server/topic/documentInfo.custom.DLT<br>
The message will be in the DLT

## None-blocking retries

Stop the application, uncomment the RetryableTopic annotation in DocumentInfoConsumer and start the application again. 
This will enable the retry mechanism. 

open http://localhost:8080/ui/docker-kafka-server/topic/documentInfo/data. Produce to topic:

| Key schema          | Key                   | Value schema          | Value                                 |
|---------------------|-----------------------|-----------------------|---------------------------------------|
| document-create-key | {"referenceId":"123"} | document-create-value | {"orderId": "500","product": "HOUSE"} |

open: http://localhost:8080/ui/docker-kafka-server/topic/documentInfo-darkside-1000/data<br>
open: http://localhost:8080/ui/docker-kafka-server/topic/documentInfo-darkside-2000/data<br>
open: http://localhost:8080/ui/docker-kafka-server/topic/documentInfo-darkside-3000/data<br>
open: http://localhost:8080/ui/docker-kafka-server/topic/documentInfo-darkside.DLT/data<br>
In the 3000 topic there will be two messages (attempt 4 and 5), because of the 5 attempts and 3000 maxDelay.

## Blocking vs non-blocking

For this one there is no message example, but the code can be found in NonBlockingConfiguration.<br>
On this that has been added extra is the configureDeadLetterPublishingContainerFactory method. When the Configuration annotation
is uncommented a custom header with the name 'customExceptionHeader' will be added on every retry.

## Poison pull

open http://localhost:8080/ui/docker-kafka-server/topic/documentInfo/data. Produce to topic:

| Key schema          | Key                   | Value schema          | Value       |
|---------------------|-----------------------|-----------------------|-------------|
| document-create-key | {"referenceId":"123"} | document-create-value | poison pill |

Then you look at the logging error will be thrown and this will not stop. Stop the application.

In ExceptionConfiguration uncomment bean on line 41 and comment bean on line 25. Start the application.

open http://localhost:8080/ui/docker-kafka-server/topic/documentInfo/data. Produce to topic:

| Key schema          | Key                   | Value schema          | Value                |
|---------------------|-----------------------|-----------------------|----------------------|
| document-create-key | {"referenceId":"123"} | document-create-value | Yes! I'm invincible! |

This time there will be no error logs.

open http://localhost:8080/ui/docker-kafka-server/topic/documentInfo.DLT/data<br>
Here the unreadable message will be shown.

## Annotations vs programmatically 

Stop the application, uncomment the configuration annotation in KafkaConsumerConfiguration and start the application.

open http://localhost:8080/ui/docker-kafka-server/topic/documentInfo/data. Produce to topic:

| Key schema          | Key                   | Value schema          | Value                                 |
|---------------------|-----------------------|-----------------------|---------------------------------------|
| document-create-key | {"referenceId":"123"} | document-create-value | {"orderId": "200","product": "HOUSE"} |

In the logs you will see "Received Message:..." 

## Testing

Under test there is one integration test in DocumentGeneratorTest named testSuccessfulEvent. This test mocks out the 
Avro schema registry and and does a check on the document-generated topic




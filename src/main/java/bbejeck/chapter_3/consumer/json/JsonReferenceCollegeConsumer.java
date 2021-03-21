package bbejeck.chapter_3.consumer.json;

import bbejeck.chapter_3.consumer.BaseConsumer;
import bbejeck.chapter_3.json.CollegeJson;
import bbejeck.clients.ConsumerRecordsHandler;
import io.confluent.kafka.serializers.json.KafkaJsonSchemaDeserializerConfig;
import io.confluent.kafka.serializers.protobuf.KafkaProtobufDeserializer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * User: Bill Bejeck
 * Date: 10/11/20
 * Time: 6:35 PM
 */
public class JsonReferenceCollegeConsumer extends BaseConsumer {
    private static final Logger LOG = LogManager.getLogger(JsonReferenceCollegeConsumer.class);

    public JsonReferenceCollegeConsumer() {
        super(StringDeserializer.class, KafkaProtobufDeserializer.class);
    }

    public static void main(String[] args) {
        JsonReferenceCollegeConsumer collegeConsumer = new JsonReferenceCollegeConsumer();
        Map<String, Object> overrideConfigs = new HashMap<>();
        overrideConfigs.put(ConsumerConfig.GROUP_ID_CONFIG,"json-schema-college-ref-group");
        overrideConfigs.put(KafkaJsonSchemaDeserializerConfig.JSON_VALUE_TYPE, CollegeJson.class);

        ConsumerRecordsHandler<String, CollegeJson> processFunction = (consumerRecords ->
                consumerRecords.forEach(cr -> {
                    CollegeJson collegeRecord = cr.value();
                    LOG.info("Found JSON Schema college record {}", collegeRecord);
                }));

        collegeConsumer.runConsumer(overrideConfigs,"json-schema-college", processFunction);
    }
}

package bbejeck.chapter_9;

import bbejeck.BaseStreamsApplication;
import bbejeck.serializers.JsonDeserializer;
import bbejeck.serializers.JsonSerializer;
import bbejeck.serializers.SerializationConfig;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.kstream.SessionWindows;
import org.apache.kafka.streams.kstream.Windowed;
import org.apache.kafka.streams.kstream.WindowedSerdes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * User: Bill Bejeck
 * Date: 9/18/23
 * Time: 6:12 PM
 */
public class PageViewSessionWindows extends BaseStreamsApplication {
      private static final Logger LOG = LoggerFactory.getLogger(PageViewSessionWindows.class);
    @Override
    public Topology topology(Properties streamProperties) {
        Serde<Windowed<String>> sessionWindowSerde =
                WindowedSerdes.sessionWindowedSerdeFrom(String.class);

        JsonSerializer<Map<String, Integer>> serializer = new JsonSerializer<>();

        final Map<String, Object> configs = new HashMap<>();
        configs.put(SerializationConfig.VALUE_CLASS_NAME, Map.class);
        JsonDeserializer<Map<String, Integer>> deserializer = new JsonDeserializer<>();
        deserializer.configure(configs, false);

        Serde<Map<String, Integer>> pageViewCountSerde = Serdes.serdeFrom(serializer, deserializer);
        PageViewAggregator sessionAggregator = new PageViewAggregator();
        Serde<String> stringSerde = Serdes.String();
        PageViewSessionMerger sessionMerger = new PageViewSessionMerger();
        StreamsBuilder builder = new StreamsBuilder();

        KStream<String, String> pageViewStream = builder.stream("page-view",
                Consumed.with(stringSerde, stringSerde));
        pageViewStream.groupByKey()
                .windowedBy(SessionWindows.ofInactivityGapWithNoGrace(Duration.ofMinutes(2)))
                .aggregate(HashMap::new,
                        sessionAggregator,
                        sessionMerger)
                .toStream()
                .to("page-view-session-aggregates",
                        Produced.with(sessionWindowSerde, pageViewCountSerde));
                

        return builder.build(streamProperties);
    }

    public static void main(String[] args) throws Exception {
        PageViewSessionWindows pageViewSessionWindows = new PageViewSessionWindows();
        Properties properties = new Properties();
        properties.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        properties.put(StreamsConfig.APPLICATION_ID_CONFIG, "page-view-session-windows");
        Topology topology = pageViewSessionWindows.topology(properties);
        try (KafkaStreams streams = new KafkaStreams(topology, properties)) {
            streams.start();
            CountDownLatch countDownLatch = new CountDownLatch(1);
            countDownLatch.await(60, TimeUnit.SECONDS);
        }
    }
}

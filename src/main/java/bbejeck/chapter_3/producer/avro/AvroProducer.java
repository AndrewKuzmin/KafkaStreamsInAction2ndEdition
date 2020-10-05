package bbejeck.chapter_3.producer.avro;

import bbejeck.chapter_3.avro.AvengerAvro;
import bbejeck.chapter_3.producer.BaseProducer;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.List;

/**
 * User: Bill Bejeck
 * Date: 10/3/20
 * Time: 3:21 PM
 */
public class AvroProducer extends BaseProducer<String, AvengerAvro> {

    static final Logger LOG = LogManager.getLogger(AvroProducer.class);

    public AvroProducer() {
        super(StringSerializer.class, KafkaAvroSerializer.class);
    }

    @Override
    public List<AvengerAvro> getRecords() {
        final AvengerAvro blackWidow = AvengerAvro.newBuilder().setName("Black Widow")
                .setRealName("Natasha Romanova")
                .setMovies(Arrays.asList("Avengers", "Infinity Wars", "End Game")).build();

        final AvengerAvro hulk = AvengerAvro.newBuilder().setName("Hulk")
                .setRealName("Dr. Bruce Banner")
                .setMovies(Arrays.asList("Avengers", "Ragnarok", "Infinity Wars")).build();

        final AvengerAvro thor = AvengerAvro.newBuilder().setName("Thor")
                .setRealName("Thor")
                .setMovies(Arrays.asList("Dark Universe","Ragnarok", "Avengers" )).build();
        
        List<AvengerAvro> avengers = Arrays.asList(blackWidow, hulk, thor);
        LOG.info("Created avengers {}", avengers);
        return avengers;
    }

    public static void main(String[] args) {
        AvroProducer avroProducer = new AvroProducer();
        LOG.info("Sending avengers in version one format");
        avroProducer.send("avro-avengers");
        LOG.info("Done sending avengers, closing down now");
    }
}

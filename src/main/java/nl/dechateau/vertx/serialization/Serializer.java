package nl.dechateau.vertx.serialization;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Serializer that can write JSON objects from Java to their String equivalent.
 */
public final class Serializer {
    private static final Logger LOG = LoggerFactory.getLogger(Serializer.class);

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .configure(SerializationFeature.WRITE_EMPTY_JSON_ARRAYS, false)
            .setSerializationInclusion(JsonInclude.Include.NON_DEFAULT);

    public static <T> String write(T object) throws SerializationException {
        try {
            return MAPPER.writeValueAsString(object);
        } catch (JsonProcessingException jpEx) {
            LOG.trace("Problem serializing JSON object to string:", jpEx);
            throw new SerializationException("Problem serializing JSON object to string; Object: " + object, jpEx);
        }
    }
}

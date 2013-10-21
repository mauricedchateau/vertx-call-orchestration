package nl.dechateau.vertx.serialization;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

/**
 * Deserializer that can write JSON objects from Strings to their Java equivalent.
 */
public final class Deserializer {
    private static final Logger LOG = LoggerFactory.getLogger(Deserializer.class);

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    public static <T> T read(String jsonString, Class<T> clazz) throws SerializationException {
        T result = null;

        try {
            result = MAPPER.readValue(jsonString, clazz);
        } catch (IOException ioEx) {
            LOG.trace("Problem deserializing JSON object from string:", ioEx);
            throw new SerializationException("Problem deserializing JSON string to object; Expected type: " + clazz
                    .getName() + "; JSON: " + jsonString, ioEx);

        }

        return result;
    }

    public static <T> List<T> read(String jsonString,
                                   TypeReference<List<T>> listTypeRef) throws SerializationException {
        List<T> result = null;

        try {
            result = MAPPER.readValue(jsonString, listTypeRef);
        } catch (IOException ioEx) {
            LOG.trace("Problem deserializing list of JSON objects from string:", ioEx);
            throw new SerializationException("Problem deserializing JSON string to list of objects; Expected list of type: "
                    + listTypeRef.getType() + "; JSON: " + jsonString, ioEx);

        }

        return result;
    }
}

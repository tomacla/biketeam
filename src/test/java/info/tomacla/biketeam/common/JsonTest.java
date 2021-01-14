package info.tomacla.biketeam.common;

import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JsonTest {

    @Test
    public void testJson() {

        Map<String, Object> test = Map.of("test", 1.0, "test2", "test");
        Map<String, Object> result = Json.parse(Json.serialize(test), new TypeReference<Map<String, Object>>() {
        });

        assertEquals(test, result);

    }

}

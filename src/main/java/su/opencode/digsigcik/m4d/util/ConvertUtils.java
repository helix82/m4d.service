package su.opencode.digsigcik.m4d.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ConvertUtils {
    static ObjectMapper objectMapper = new ObjectMapper();

    public static <T> T convertStringToClass(String response, Class<T> clazz) throws JsonProcessingException {
        return objectMapper.readValue(response, clazz);
    }
}

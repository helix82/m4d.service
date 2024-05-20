package su.opencode.digsigcik.synapi.util;

import lombok.Getter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Getter
public class CastUtils {
    public static <T> List<T> castList(Class<? extends T> clazz, Collection<?> rawCollection)
            throws ClassCastException {
        List<T> result = new ArrayList<>(rawCollection.size());
        rawCollection.forEach(o -> result.add(clazz.cast(o)));
        return result;
    }
}

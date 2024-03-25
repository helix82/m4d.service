package su.opencode.digsigcik.synapi.dscp;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

final class InvariantUtils {

	private static final Map<Pair<Class<? extends Enum>, String>, Enum> ENUM_BY_CODE = new HashMap<>();
	private static final Log LOG = LogFactory.getLog(InvariantUtils.class);

	static {
		LOG.trace("Number of " + ContainerTypeEnum.class.getSimpleName() + " " + ContainerTypeEnum.values().length);
		LOG.trace("Number of " + SignatureTypeEnum.class.getSimpleName() + " " + SignatureTypeEnum.values().length);
		LOG.trace("Number of " + EnhanceTypeEnum.class.getSimpleName() + " " + EnhanceTypeEnum.values().length);
		LOG.trace("Number of " + MetaObjectEnum.class.getSimpleName() + " " + MetaObjectEnum.values().length);
		LOG.trace("Number of " + ResultTypeEnum.class.getSimpleName() + " " + ResultTypeEnum.values().length);
		LOG.trace("Number of " + AttachedSignatureTypeEnum.class.getSimpleName() + " " + AttachedSignatureTypeEnum.values().length);
	}

	private InvariantUtils() {
		throw new UnsupportedOperationException();
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	static void fillCodes(Class<? extends Enum> enumClass) {
		if (!HasCode.class.isAssignableFrom(enumClass))
			throw new IllegalArgumentException("Parameter class should be an implementation of HasCode interface");

		EnumSet<? extends Enum> enumSet = EnumSet.allOf(enumClass);
		for (Enum item : enumSet) {
			Pair<Class<? extends Enum>, String> key = new ImmutablePair<>(enumClass, ((HasCode)item).getCode());
			ENUM_BY_CODE.put(key, item);
		}
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	public static <E extends Enum<E>> E getValueByCode(String code, Class<E> enumClass, boolean tolerant) {
		Pair<Class<? extends Enum>, String> key = new ImmutablePair<>(enumClass, code);
		E res = (E)ENUM_BY_CODE.get(key);
		if (res != null) return res;
		if (tolerant) return null;
		else throw new IllegalArgumentException("Cannot find code '" + code + "' for enumClass=" + enumClass);
	}

	public static <E extends Enum<E>> E getValueByCode(String code, Class<E> enumClass) {
		return getValueByCode(code, enumClass, false);
	}

}

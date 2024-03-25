package su.opencode.digsigcik.synapi.dscp;

public interface HasCode<E extends Enum<E>> {
	String getCode();

	default E byCode(String code) {
		@SuppressWarnings("unchecked")
		Class<E> enumClass = (Class<E>)getClass();
		return InvariantUtils.getValueByCode(code, enumClass);
	}
}

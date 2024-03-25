package su.opencode.digsigcik.synapi.dscp;

import su.opencode.digsigcik.synapi.crypto.BasicSecurityProvider;
import su.opencode.digsigcik.synapi.crypto.SecurityProvider;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ContainerUtils {

	private static final BasicSecurityProvider BASIC_SECURITY_PROVIDER = new SimpleSecurityProvider();

	private ContainerUtils() {
		throw new UnsupportedOperationException();
	}

	public static Container createContainer(File filePath) throws DscpContainerException {
		try(InputStream in = new FileInputStream(filePath)) {
			return new Container(in);
		} catch (IOException e) {
			throw DscpContainerException.ioCause(e, true);
		}
	}

	public static Container createContainer(String filePath) throws DscpContainerException {
		return createContainer(new File(filePath));
	}

	public static Container createContainer(byte[] containerContent) throws DscpContainerException {
		try(InputStream in = new ByteArrayInputStream(containerContent)) {
			return new Container(in);
		} catch (IOException e) {
			throw DscpContainerException.ioCause(e, true);
		}
	}

	public static Container createContainer(InputStream in) throws DscpContainerException {
		return new Container(in);
	}

	public static List<ResultFileDescription> getFromContainer(
		Container container, List<ResultTypeEnum> returnParts,
		boolean mandatory, SecurityProvider provider) throws DscpContainerException {

		if (returnParts == null || returnParts.isEmpty()) return Collections.emptyList();
		List<ResultFileDescription> res = new ArrayList<>(returnParts.size());
		for (ResultTypeEnum returnPart : returnParts) {
			if (returnPart == null) {
				if (mandatory) throw new IllegalStateException("This is an empty returnPart (maybe there is no such a code)");
				res.add(null);
				continue;
			}

			ResultFileDescription resFile = BASIC_SECURITY_PROVIDER.provideSecurityAction(container, returnPart, provider);
			if (resFile != null) {
				res.add(resFile);
			} else if (!mandatory) {
				res.add(null);
			} else {
				throw DscpContainerException.resultCauseException("Cannot find mandatory part of result " + returnPart, returnPart, null);
			}
		}
		return res;
	}

	public static void writeContainer(OutputStream out, Container container) throws DscpContainerException {
		container.write(out);
	}

	public static byte[] getContainerContent(Container container) throws DscpContainerException {
		try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
			writeContainer(out, container);
			return out.toByteArray();
		} catch (IOException e) {
			throw DscpContainerException.ioCause(e, false);
		}
	}

}

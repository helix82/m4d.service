package su.opencode.digsigcik.synapi.crypto;

import org.apache.commons.lang3.time.FastDateFormat;
import su.opencode.digsigcik.synapi.dscp.Container;
import su.opencode.digsigcik.synapi.dscp.ContainerUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Date;

public class TestApplication {
	public static void main(String[] args) throws Exception {
		String filePath = args[0];
		Container container = ContainerUtils.createContainer(new File(filePath));
		System.out.println(container);

		int pointIndex = filePath.lastIndexOf('.');
		String fileName = filePath.substring(0, pointIndex);
		String fileExtension = filePath.substring(pointIndex);
		FastDateFormat format = FastDateFormat.getInstance("yyyy_MM_dd_HH_mm_ss");
		String filePostfix = format.format(new Date());
		OutputStream out = new FileOutputStream(fileName + "_" + filePostfix + fileExtension);
		ContainerUtils.writeContainer(out, container);
	}
}

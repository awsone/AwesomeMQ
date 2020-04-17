package omq.common.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * This class enables the compression of the information sent through the
 * rabbitmq server.
 * 
 * @author Sergi Toda <sergi.toda@estudiants.urv.cat>
 * 
 */
public class Zipper {

	public static byte[] zip(byte[] b) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		GZIPOutputStream zos = null;
		try {
			zos = new GZIPOutputStream(baos);
			zos.write(b);
		} finally {
			if (zos != null) {
				zos.close();
			}

			baos.close();
		}

		return baos.toByteArray();
	}

	public static byte[] unzip(byte[] b) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ByteArrayInputStream bais = new ByteArrayInputStream(b);

		GZIPInputStream zis = null;
		try {
			zis = new GZIPInputStream(bais);

			byte[] tmpBuffer = new byte[256];
			int n;
			while ((n = zis.read(tmpBuffer)) >= 0) {
				baos.write(tmpBuffer, 0, n);
			}
		} finally {
			if (zis != null) {
				zis.close();
			}

			bais.close();
			baos.close();
		}

		return baos.toByteArray();
	}
}

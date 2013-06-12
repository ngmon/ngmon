package cz.muni.fi.xtovarn.heimdall.commons.util.test;

import java.io.*;

public class TestUtil {

	public static String readInputStreamToString(InputStream inputStream) throws IOException {
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
		StringBuilder stringBuilder = new StringBuilder();

		String line;
		while ((line = bufferedReader.readLine()) != null) {
			stringBuilder.append(line);
		}

		bufferedReader.close();
		return stringBuilder.toString();
	}

	public static boolean recursiveDelete(File file) {
		File[] files = file.listFiles();
		if (files != null)
			for (File each : files)
				recursiveDelete(each);
		return file.delete();
	}
}

package cz.muni.fi.xtovarn.heimdall.client.debug;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class SimpleFileWriter {

	private static BufferedWriter out;

	public SimpleFileWriter(String filename) {
        try {
			FileWriter fstream = new FileWriter(filename, false); //true tells to append data.
			out = new BufferedWriter(fstream);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void write(String string) {
		try {
			out.write(string);
			out.newLine();
			out.flush();
		} catch (IOException e) {

			e.printStackTrace();
		}
	}

	public void close() {
		if (out != null) {
			try {
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
package edu.kit.aquaplanning.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class CSVWriter {

	public static final String SEPARATOR = ",";
	public static String folderName = null;

	public static void setFolder(String folderName) {
		CSVWriter.folderName = folderName;
	}

	public static void writeFile(String fileName, List<List<String>> lines) {

		BufferedWriter bw = null;

		try {
			String directoryName = "../CSVOutput/";
			if (folderName != null)
				directoryName += folderName + "/";
			File directory = new File(directoryName);
			if (!directory.exists()) {
				directory.mkdirs();
				// If you require it to make the entire directory path including parents,
				// use directory.mkdirs(); here instead.
			}

			bw = new BufferedWriter(new FileWriter(directoryName + fileName, true));

			for (List<String> list : lines) {
				bw.write(String.join(SEPARATOR, list));
				bw.newLine();
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} finally { // always close the file
			if (bw != null) {
				try {
					bw.close();
				} catch (IOException ioe2) {
					ioe2.printStackTrace();
				}
			}
		}
	}
}

package com.accenture.liquidstudio;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class WriteToFileUtil {
	private String LOC = "CSV";
	private String fileName;

	private BufferedWriter bw = null;
	private FileWriter fw = null;

	private File file;

	public WriteToFileUtil(String fileName) {
		this.fileName = fileName;

		file = new File(LOC + "\\" + fileName);

		setup();
	}

	public void setup() {
		try {
		// if file or directory doesnt exists, then create it
		if (!file.exists()) {
			file.createNewFile();
		}

		// true = append file
		if(fw==null)
			fw = new FileWriter(file.getAbsoluteFile(), true);
		if(bw==null)
			bw = new BufferedWriter(fw);
		} catch (IOException e)
		{
			e.getMessage();
		}
	}

	public void writeToFile(String data) {

		try {

			bw.write(data);

//			System.out.println("Done");

		} catch (IOException e) {

			e.printStackTrace();

		} 

	}
	
	public void closeWriter() {
		try {

			if (bw != null)
				bw.close();

			if (fw != null)
				fw.close();

		} catch (IOException ex) {

			ex.printStackTrace();

		}
	}
}

/**
 * Get music files from folder
 */

import java.io.File;

public class ReadMusicFile {
	private String[] listFiles;
	
	public ReadMusicFile(File folder) {
		listFiles = new String[folder.listFiles().length];
		
		int counter = 0;
		for(File file : folder.listFiles()) {
			listFiles[counter] = file.getName();
			counter++;
		}
	}
	
	public String[] getListFiles() {
		return listFiles;
	}
}

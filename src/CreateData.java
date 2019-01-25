import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;

/**
 * Enters training data into files
 * @author micha
 *
 */
public class CreateData {
	File xFile;
	File yFile;

	ArrayList<int[]> xData;
	ArrayList<Integer> yData;

	public CreateData(ArrayList<int[]> xData, ArrayList<Integer> yData) throws IOException {
		this.xData = xData;
		this.yData = yData;

		// Initialize files with paths
		xFile = new File("C:\\Users\\micha\\Desktop\\xFile.mat");
		yFile = new File("C:\\Users\\micha\\Desktop\\yFile.mat");

		// Write to files
		writeFile();
	}

	private void writeFile() throws IOException {
		// xFile
		FileWriter fileWriterX = new FileWriter(xFile);
		// BufferedWriter bufferedWriter = new BufferedWriter(fileWriterX, 8192);

		PrintWriter printWriterX = new PrintWriter(fileWriterX);

		// Get training example and add to file
		System.out.println(xData.size());
		System.out.println(xData.get(0).length);
		for(int[] example : xData) {
			
			String currentExample = "";
			for(int num : example) {
				currentExample += num + " ";
			}
			
			printWriterX.println(currentExample);
			
			/*String currentExample = "";
			for(int num : example) {
				currentExample += num + " ";
			}
			for(int i = 0; i < example.length; i++) {
				currentExample += example[i] + " ";
				// System.out.println(currentExample);
			}
			fileWriterX.write(currentExample);
			fileWriterX.write(System.getProperty("line.separator"));*/

			/*for(int i = 0; i < example.length - 1; i++) {
				String currentExample = example[i] + " ";
				fileWriterX.write(currentExample);
			}
			fileWriterX.write(example[example.length - 1] + "%n");*/
			// fileWriterX.write("%n");
		}

		// yFile
		FileWriter fileWriterY = new FileWriter(yFile);

		// Get training example and add to file
		System.out.println(yData.size());
		for(int i = 0; i < yData.size(); i++) {
			String currentExample = yData.get(i).intValue() + " ";
			// System.out.println(currentExample);
			fileWriterY.write(currentExample);
			fileWriterY.write(System.getProperty("line.separator"));
		}

		fileWriterY.close();
	}

}

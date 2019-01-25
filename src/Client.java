import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.mp3.LyricsHandler;
import org.apache.tika.parser.mp3.Mp3Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.SAXException;
/**
 * Client to run Audio Data training creation
 * @author micha
 *
 */
public class Client {

	public static void main(String[] args) throws IOException, SAXException, TikaException {
		// Initialize path to songs that want to train with
		String path = "C:\\Users\\micha\\Desktop\\Octave\\AudioGenreClassification\\DeepAudioClassification-master\\Data\\Raw";
		File musicFolder = new File(path);

		// Read music
		ReadMusicFile reader = new ReadMusicFile(musicFolder);
		String[] listOfFiles = reader.getListFiles();

		// Read Audio Data
		int BufferElements2Rec = 128;
		int sampleRate = 44100;
		// Want sample sizes of 56448 frequencies or 16385
		int sampleSize = 5000;
		// Create matrix to hold frequencies for different songs
		ArrayList<int[]> freqMatrix = new ArrayList<>(); 
		// Create array to hold genre type for snippet 
		ArrayList<Integer> yData = new ArrayList<>();

		// Testing
		int numShortList = 1;
		String[] shortList = new String[numShortList];
		for(int l = 0; l < numShortList; l++) {
			shortList[l] = listOfFiles[l];
		}
		for(String file : listOfFiles) {
			ReadAudio audioReader = new ReadAudio(path + "\\" + file);
			int genre = getMeta(path + "\\" + file);

			int counter = 0; // Count number of snippets for song

			for(int i = 0; i < 10; i++) {
				int[] sample = new int[sampleSize];

				// Get a full sample size 
				for(int t = 0; t < sampleSize; t++) {

					// Fill a buffer to get frequency
					int[] bin = new int[BufferElements2Rec];
					for(int j = 0; j < BufferElements2Rec; j++) {
						bin[j] = audioReader.getDin().read();
						//System.out.println(bin[j]);
					}

					// Calculate frequency from bin
					double frequency = getFrequency(bin, BufferElements2Rec, sampleRate);
					// System.out.println("Frequency: " + frequency);
					// Add frequency to snippet
					sample[t] = (int)(frequency);

					audioReader.getDin().skip(BufferElements2Rec);
				}

				freqMatrix.add(sample);
				//System.out.println("Added");
				counter++;
			}

			// Record number of snippets for same song so that can add that many to yData
			for(int n = 0; n < counter; n++) {
				//System.out.println("Adding genre");
				yData.add(genre);
			}
			counter = 0;
		}
		/*for(int[] array : freqMatrix)
			for(int num : array)
				System.out.print(num + " ");
		System.out.println();
		*/
		/*for(int c : yData) 
			System.out.print(c);*/

		CreateData createData = new CreateData(freqMatrix, yData);
	}
	public static int getMeta(String file) throws IOException, SAXException, TikaException {
		//detecting the file type
		BodyContentHandler handler = new BodyContentHandler();
		Metadata metadata = new Metadata();
		FileInputStream inputstream = new FileInputStream(file);
		ParseContext pcontext = new ParseContext();

		//Mp3 parser
		Mp3Parser  Mp3Parser = new  Mp3Parser();
		Mp3Parser.parse(inputstream, handler, metadata, pcontext);

		String genre = metadata.get("xmpDM:genre");
		System.out.println(genre);
		if(genre.equals("Hip-Hop/Rap")) {
			return 1;
		} else if(genre.equals("Electronic")) {
			return 2;
		} else if(genre.equals("R&B/Soul")) {
			return 3;
		} else {
			return -1;
		}
	}

	public static double getFrequency(int[] bin, int BufferElements2Rec, int sampleRate) {
		// FFT Data
		double[] real = new double[bin.length];
		double[] imag = new double[bin.length];
		for (int i = 0; i < real.length; i++) {
			real[i] = bin[i];
			imag[i] = 0;
		}
		transform(real, imag);
		String fftString = "";
		double max = 0;
		int max_bin = 0;
		for (int i = 1; i < real.length / 2; i++) {
			if (Math.sqrt(Math.pow(real[i], 2) + Math.pow(imag[i], 2)) > max) {
				max = Math.sqrt(Math.pow(real[i], 2) + Math.pow(imag[i], 2));
				max_bin = i;
			}
		}

		double frequency = 2 * max_bin * sampleRate / BufferElements2Rec;
		return frequency;
	}

	public static void transform(double[] real, double[] imag) {
		int n = real.length;
		if (n != imag.length)
			throw new IllegalArgumentException("Mismatched lengths");
		if (n == 0)
			return;
		else if ((n & (n - 1)) == 0)  // Is power of 2
			transformRadix2(real, imag);
		else  // More complicated algorithm for arbitrary sizes
			transformBluestein(real, imag);
	}

	/*
	 * Computes the discrete Fourier transform (DFT) of the given complex vector, storing the result back into the vector.
	 * The vector's length must be a power of 2. Uses the Cooley-Tukey decimation-in-time radix-2 algorithm.
	 */
	public static void transformRadix2(double[] real, double[] imag) {
		// Length variables
		int n = real.length;
		if (n != imag.length)
			throw new IllegalArgumentException("Mismatched lengths");
		int levels = 31 - Integer.numberOfLeadingZeros(n);  // Equal to floor(log2(n))
		if (1 << levels != n)
			throw new IllegalArgumentException("Length is not a power of 2");

		// Trigonometric tables
		double[] cosTable = new double[n / 2];
		double[] sinTable = new double[n / 2];
		for (int i = 0; i < n / 2; i++) {
			cosTable[i] = Math.cos(2 * Math.PI * i / n);
			sinTable[i] = Math.sin(2 * Math.PI * i / n);
		}

		// Bit-reversed addressing permutation
		for (int i = 0; i < n; i++) {
			int j = Integer.reverse(i) >>> (32 - levels);
		if (j > i) {
			double temp = real[i];
			real[i] = real[j];
			real[j] = temp;
			temp = imag[i];
			imag[i] = imag[j];
			imag[j] = temp;
		}
		}

		// Cooley-Tukey decimation-in-time radix-2 FFT
		for (int size = 2; size <= n; size *= 2) {
			int halfsize = size / 2;
			int tablestep = n / size;
			for (int i = 0; i < n; i += size) {
				for (int j = i, k = 0; j < i + halfsize; j++, k += tablestep) {
					int l = j + halfsize;
					double tpre = real[l] * cosTable[k] + imag[l] * sinTable[k];
					double tpim = -real[l] * sinTable[k] + imag[l] * cosTable[k];
					real[l] = real[j] - tpre;
					imag[l] = imag[j] - tpim;
					real[j] += tpre;
					imag[j] += tpim;
				}
			}
			if (size == n)  // Prevent overflow in 'size *= 2'
				break;
		}
	}

	/*
	 * Computes the discrete Fourier transform (DFT) of the given complex vector, storing the result back into the vector.
	 * The vector can have any length. This requires the convolution function, which in turn requires the radix-2 FFT function.
	 * Uses Bluestein's chirp z-transform algorithm.
	 */
	public static void transformBluestein(double[] real, double[] imag) {
		// Find a power-of-2 convolution length m such that m >= n * 2 + 1
		int n = real.length;
		if (n != imag.length)
			throw new IllegalArgumentException("Mismatched lengths");
		if (n >= 0x20000000)
			throw new IllegalArgumentException("Array too large");
		int m = Integer.highestOneBit(n) * 4;

		// Trignometric tables
		double[] cosTable = new double[n];
		double[] sinTable = new double[n];
		for (int i = 0; i < n; i++) {
			int j = (int) ((long) i * i % (n * 2));  // This is more accurate than j = i * i
			cosTable[i] = Math.cos(Math.PI * j / n);
			sinTable[i] = Math.sin(Math.PI * j / n);
		}

		// Temporary vectors and preprocessing
		double[] areal = new double[m];
		double[] aimag = new double[m];
		for (int i = 0; i < n; i++) {
			areal[i] = real[i] * cosTable[i] + imag[i] * sinTable[i];
			aimag[i] = -real[i] * sinTable[i] + imag[i] * cosTable[i];
		}
		double[] breal = new double[m];
		double[] bimag = new double[m];
		breal[0] = cosTable[0];
		bimag[0] = sinTable[0];
		for (int i = 1; i < n; i++) {
			breal[i] = breal[m - i] = cosTable[i];
			bimag[i] = bimag[m - i] = sinTable[i];
		}

		// Convolution
		double[] creal = new double[m];
		double[] cimag = new double[m];
		convolve(areal, aimag, breal, bimag, creal, cimag);

		// Postprocessing
		for (int i = 0; i < n; i++) {
			real[i] = creal[i] * cosTable[i] + cimag[i] * sinTable[i];
			imag[i] = -creal[i] * sinTable[i] + cimag[i] * cosTable[i];
		}
	}

	/*
	 * Computes the circular convolution of the given complex vectors. Each vector's length must be the same.
	 */
	public static void convolve(double[] xreal, double[] ximag,
			double[] yreal, double[] yimag, double[] outreal, double[] outimag) {

		int n = xreal.length;
		if (n != ximag.length || n != yreal.length || n != yimag.length
				|| n != outreal.length || n != outimag.length)
			throw new IllegalArgumentException("Mismatched lengths");

		xreal = xreal.clone();
		ximag = ximag.clone();
		yreal = yreal.clone();
		yimag = yimag.clone();
		transform(xreal, ximag);
		transform(yreal, yimag);

		for (int i = 0; i < n; i++) {
			double temp = xreal[i] * yreal[i] - ximag[i] * yimag[i];
			ximag[i] = ximag[i] * yreal[i] + xreal[i] * yimag[i];
			xreal[i] = temp;
		}
		inverseTransform(xreal, ximag);

		for (int i = 0; i < n; i++) {  // Scaling (because this FFT implementation omits it)
			outreal[i] = xreal[i] / n;
			outimag[i] = ximag[i] / n;
		}
	}

	/*
	 * Computes the inverse discrete Fourier transform (IDFT) of the given complex vector, storing the result back into the vector.
	 * The vector can have any length. This is a wrapper function. This transform does not perform scaling, so the inverse is not a true inverse.
	 */
	public static void inverseTransform(double[] real, double[] imag) {
		transform(imag, real);
	}


}

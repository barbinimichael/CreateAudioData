import java.io.File;
import java.io.IOException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

/**
 * Reads raw audio data from mp3 files
 * @author micha
 *
 */
public class ReadAudio {
	private File file; // Current mp3 file
	private AudioInputStream in;
	private AudioInputStream din = null;
	private AudioFormat baseFormat;
	private AudioFormat decodedFormat;
	
	public ReadAudio(String file) {
		this.file = new File(file);		
		// Get Input Stream
		try {
			in = AudioSystem.getAudioInputStream(this.file);
		} catch (UnsupportedAudioFileException e) {
			System.out.println("Unsupported audio format");
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("Error");
			e.printStackTrace();
		}
		baseFormat = in.getFormat();
		decodedFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 
				baseFormat.getSampleRate(), 
				16, 
				baseFormat.getChannels(), 
				baseFormat.getChannels() * 2,
				baseFormat.getSampleRate(), 
				false);
		din = AudioSystem.getAudioInputStream(decodedFormat, in);		
	}
	
	public AudioInputStream getDin() {
		return din;
	}
}

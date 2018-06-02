import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;



public class AudioThread extends Thread
{

	OutputStream out;

	public AudioThread(Socket c)
	{
		try {
			out = c.getOutputStream();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	
	public void run()
	{
		while (true) {
			AudioInputStream ain = testPlay("music/mix.wav");
			if (ain != null) {
				try {
					AudioSystem.write(ain, AudioFileFormat.Type.WAVE, out);
				} catch (IOException e) {
					break;
				}
			}
		}
		this.interrupt();
	}
	
	public static AudioInputStream testPlay(String filename) {
		AudioInputStream din = null;
		try {
			File file = new File(filename);
			AudioInputStream in = AudioSystem.getAudioInputStream(file);
			System.out.println("Before :: " + in.available());

			AudioFormat baseFormat = in.getFormat();
			AudioFormat decodedFormat =
					new AudioFormat(AudioFormat.Encoding.PCM_UNSIGNED, baseFormat.getSampleRate(),
							8, baseFormat.getChannels(), baseFormat.getChannels(),
							baseFormat.getSampleRate(), false);
			din = AudioSystem.getAudioInputStream(decodedFormat, in);
			System.out.println("After :: " + din.available());
			return din;
		} catch (Exception e) {
			// Handle exception.
			e.printStackTrace();
		}
		return din;
	}
}

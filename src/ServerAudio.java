import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;


public class ServerAudio extends Thread{
	/**
	 * @param args
	 */
	public void run()  {
		// TODO Auto-generated method stub
		ServerSocket serverAudio = null;
		try {
			serverAudio = new ServerSocket(6666);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		Socket clientaudio = null;
		while(true)
		{	
			try {

				//connexion musique
				clientaudio = serverAudio.accept();
				new AudioThread(clientaudio).start();


			}catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
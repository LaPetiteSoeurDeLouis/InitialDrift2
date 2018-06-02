import java.io.*;
import java.net.*;

public class Server {
	ServerSocket serversocket;
	Socket client;
	int bytesRead;
	Connect c = new Connect();
	BufferedReader input;
	PrintWriter output;
	public static int nbjoueurs=0;



	public void start() throws IOException{
		System.out.println("Connection Starting on port:" + c.getPort());
		//make connection to client on port specified
		serversocket = new ServerSocket(c.getPort());

		while(true)
		{	
			try {

				//accept connection from client
				client = serversocket.accept();
				System.out.println("Nouveau joueur sur le serveur.");
				nbjoueurs++;

			} catch (Exception e) {
				e.printStackTrace();
			}
			new EchoThread(client).start();
		}
	}



	public static void main(String[] args){
		Server server = new Server();
		new ServerAudio().start();
		try {
			server.start();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}

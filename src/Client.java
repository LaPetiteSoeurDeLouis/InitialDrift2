import java.io.*;
import java.net.*;
import javax.swing.JOptionPane;

public class Client {
    Connect c = new Connect();
    Socket socket;
    BufferedReader read;
    PrintWriter output;

    public void startClient() throws UnknownHostException, IOException{
        //Create socket connection
        socket = new Socket(c.gethostName(), c.getPort());

        //create printwriter for sending login to server
        output = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));

        //prompt for user name
        String username = JOptionPane.showInputDialog(null, "Enter User Name:");

        //send user name to server
        output.println(username);

        //prompt for password
        String password = JOptionPane.showInputDialog(null, "Enter Password");

        //send password to server
        output.println(password);
        output.flush();

        //create Buffered reader for reading response from server
        read = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        //read response from server
        String response = read.readLine();
        System.out.println("Message du serveur :"+response);

        //display response
        JOptionPane.showMessageDialog(null, response);
    }

    public void fileInfo(){

    }

    public static void main(String args[]){
        Client client = new Client();
        try {
            client.startClient();
        } catch (UnknownHostException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
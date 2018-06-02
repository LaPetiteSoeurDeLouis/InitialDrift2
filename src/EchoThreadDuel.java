import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.sql.SQLException;


public class EchoThreadDuel extends Thread {
    protected Socket socket;
    protected InputStream input = null;
    protected BufferedReader brinp = null;
    protected PrintWriter output = null;
    public volatile String pseudo;
    private ServerDuel serveurDuel;
    
    public EchoThreadDuel(Socket clientSocket,ServerDuel s) {
        this.socket = clientSocket;
        this.serveurDuel = s;
    }

    public void run() {

        try {
            input = socket.getInputStream();
            brinp = new BufferedReader(new InputStreamReader(input));
            output = new PrintWriter(socket.getOutputStream());
            
        } catch (IOException e) {
			e.printStackTrace();
            return;
        }
        

    	
    	while(serveurDuel.getNbjoueurs() != 2)
    	{

    	}

		output.println("Adversaire trouvé !");
		output.flush();
    	
    	
    	
    	
    	String line = null;
    	
    	//-------------------------------------
    	
    	while (true) {
            try {

            	line = brinp.readLine();
            	
            	if (line.equalsIgnoreCase("QUIT")) {
                    socket.close();
                    serveurDuel.setNbjoueurs(serveurDuel.getNbjoueurs() - 1);
                    System.out.println(pseudo+" s'est déconnecté");
                    return;
                } 
                else {
          		
                	
                	//pseudos de la partie
                	if (line.equalsIgnoreCase("Duel/Get/Pseudos")) {
                		output.println(serveurDuel.getPseudoj1());
                		output.println(serveurDuel.getPseudoj2());
                		output.flush();
                    }
                	
                	if (line.startsWith("Pos/X/")) {
                		if(line.split("/")[2].equals(serveurDuel.getPseudoj2()))
                		{	
                			/* J2 POSX*/
							serveurDuel.setJ2PosX(Integer.parseInt(line.split("/")[3]));
                		}
                		else
                		{
                			/* J1 POSX*/
							serveurDuel.setJ1PosX(Integer.parseInt(line.split("/")[3]));
                		}
                	}
                	
                	if (line.startsWith("Pos/Y/")) {
                		if(line.split("/")[2].equals(serveurDuel.getPseudoj2()))
                 		{	
                			/* J2 POSY*/
							serveurDuel.setJ2PosY(Integer.parseInt(line.split("/")[3]));
                		}
                		else
                		{
                			/* J1 POSY*/
							serveurDuel.setJ1PosY(Integer.parseInt(line.split("/")[3]));
                		}
                    }
                	if (line.startsWith("Win/")) 
                	{
                		serveurDuel.setFin(true);
                		try{
                    		EchoThread.addResultatDuel(line.split("/")[1], true);
                    		output.println("Nouveau résultat enregistré sur le serveur. Consulte le leaderboard pour connaître ton classement");
        					output.flush();                			
                		}
                		catch(SQLException e)
                		{
        					//Handle errors for JDBC
        					output.println("Erreur dans l'enregistrement du score...");
        					output.flush();
                		}
                    }
                	if (line.startsWith("Lose/")) 
                	{
                		serveurDuel.setFin(true);
                		try{
                    		EchoThread.addResultatDuel(line.split("/")[1], false);
                    		output.println("Nouveau résultat enregistré sur le serveur. Consulte le leaderboard pour connaître ton classement");
        					output.flush();                			
                		}
                		catch(SQLException e)
                		{
        					//Handle errors for JDBC
        					output.println("Erreur dans l'enregistrement du score...");
        					output.flush();
                		}

                	}
                	if (line.startsWith("Fin/")) 
                	{
                		if(!serveurDuel.isFin())
                		{
                    		serveurDuel.setFin(Boolean.parseBoolean(line.split("/")[1]));                			
                		}
                    }

                }
            } 
            catch (SocketException e) {
            	try {
					socket.close();
	                serveurDuel.setNbjoueurs(serveurDuel.getNbjoueurs() - 1);
				} catch (IOException e1) {
					e1.printStackTrace();
				}
                e.printStackTrace();
                serveurDuel.setNbjoueurs(serveurDuel.getNbjoueurs() - 1);
                return;
            }            
            catch (IOException e) {
                e.printStackTrace();
                serveurDuel.setNbjoueurs(serveurDuel.getNbjoueurs() - 1);
                return;
            }
            catch (NullPointerException e) {
            	try {
					socket.close();
	                serveurDuel.setNbjoueurs(serveurDuel.getNbjoueurs() - 1);
				} catch (IOException e1) {
					e1.printStackTrace();
				}
                e.printStackTrace();
                serveurDuel.setNbjoueurs(serveurDuel.getNbjoueurs() - 1);
                return;
            }

        }
    }
    
    public void update()
    {		
    	output.println(String.valueOf(serveurDuel.isFin()));
    	
    	if(serveurDuel.isFin())
    	{
    		serveurDuel.seti42(1);
    		serveurDuel.seti43(1);
   		}
    	output.println(serveurDuel.geti42());
    	output.println(serveurDuel.geti43());    		

		output.println(serveurDuel.getPseudoj1()+"/"+serveurDuel.getJ1PosX()+"/"+serveurDuel.getJ1PosY());		
		output.println(serveurDuel.getPseudoj2()+"/"+serveurDuel.getJ2PosX()+"/"+serveurDuel.getJ2PosY());
		
    	output.println(String.valueOf(serveurDuel.isFin()));
		if(serveurDuel.geti42() == 15){
			output.println(serveurDuel.getDecorPosX());
			output.println(serveurDuel.getDecorType());
			output.println(serveurDuel.getEnnemiPosX());
			output.println(serveurDuel.getEnnemiType());
		}
		if(serveurDuel.geti43() == 25)
		{
			output.println(serveurDuel.getViePosX());
		}
    	output.println(String.valueOf(serveurDuel.isFin()));
		output.flush();

    }
    
    
}
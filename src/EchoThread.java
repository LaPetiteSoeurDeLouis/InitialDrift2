import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.DatatypeConverter;


public class EchoThread extends Thread {
    protected Socket socket;
    protected InputStream input = null;
    protected BufferedReader brinp = null;
    protected PrintWriter output = null;
	ServerDuel s;

    public EchoThread(Socket clientSocket) {
        this.socket = clientSocket;
    }

    public void run() {

        try {
            input = socket.getInputStream();
            brinp = new BufferedReader(new InputStreamReader(input));
            output = new PrintWriter(socket.getOutputStream());
            
        } catch (IOException e) {
            return;
        }
        String line;
        try {
			logInfo();
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
        while (true) {
            try {
            	line = brinp.readLine();
                if (line.equalsIgnoreCase("QUIT")) {
                    socket.close();
                    Server.nbjoueurs--;
                    System.out.println("Un joueur s'est déconnecté");
                    return;
                }
                else {
                	if(line.startsWith("HighScore/Add/"))
                	{                
                		System.out.println("Ajout highscore détecté");

                		
                		String[] parties = line.split("/");
                		String nomJoueur = parties[2];
                		String score = parties[3];
                		String date = parties[4];
                		try {
							addHighScore(nomJoueur,score,date);
						} catch (SQLException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
                	}
                	if(line.startsWith("HighScores/Get"))
                	{
                		getHighScores();
                	}
                	if(line.startsWith("Leaderboard/Get"))
                	{
                		getLeaderboard();
                	}
                	if(line.startsWith("HighScores/TopScore"))
                	{
                		getTop();
                	}
                	if(line.startsWith("Logs/NombreJoueurs"))
                	{
                		output.println(Server.nbjoueurs);
                		output.flush();
                	}
                	if(line.startsWith("Duel/Start"))
                	{
                		int port_serveur_a_rejoindre = 0;
                		int num_ligne=0;
                		boolean serveurDuelEnAttente = false;
                		
                		Path p = Paths.get("duelEnAttente.txt");
                		List<String> fileContent = new ArrayList<String>(Files.readAllLines(p, StandardCharsets.UTF_8));
                		int i=0;
                		for(String ligne : fileContent)
                		{
                			System.out.println(ligne);
                			if(ligne.contains("true"))
                			{
                				port_serveur_a_rejoindre = Integer.parseInt(ligne.split(" ")[0]);
                				serveurDuelEnAttente = true;
                    			num_ligne = i;
                				break;
                			}
                			i++;
                		}
                		
                		
                		System.out.println("Serveur Duel En Attente : "+serveurDuelEnAttente);
                		System.out.println("Ligne : "+num_ligne);
                		System.out.println("Port : "+port_serveur_a_rejoindre);
                		if(serveurDuelEnAttente)
                		{
                		    fileContent.remove(num_ligne);
                		    Files.write(p, fileContent, StandardCharsets.UTF_8);
                		    
                		    output.println(port_serveur_a_rejoindre);
                			output.flush();
                		}
                		else{
                		    System.out.println("Création du serveur de duel");
                			s = new ServerDuel();               
                			s.start();
                			output.println(s.portServeur);
                			output.flush();
                		    fileContent.add(s.portServeur+" true");
                		    Files.write(p, fileContent, StandardCharsets.UTF_8,StandardOpenOption.APPEND);
                		    s.run();              		    
                		}
                	}
                	
                	

                }
            } catch (IOException e) {
                e.printStackTrace();
                Server.nbjoueurs--;
                return;
            }catch (Exception e) {
            	e.printStackTrace();
                System.out.println("Un joueur a quitté le serveur");
                Server.nbjoueurs--;
                return;
            }
        }
    }
    
	/**
	 * Récupère les identifiants du client dans la db. <br/> Gère la connexion et l'inscription. 
	 * @throws Exception
	 */
	public void logInfo() throws Exception{
		String username = "";
		byte[] digest;
		String password;
		boolean inscription = false;
		//open buffered reader for reading data from client
		MessageDigest md = MessageDigest.getInstance("MD5");
		
		username = brinp.readLine();
		if(username.startsWith("inscription/"))
		{
			username = username.split("/")[1];
			inscription = true;
		}
		password = brinp.readLine();
		md.update(password.getBytes());
		digest = md.digest();
		String myHash = DatatypeConverter.printHexBinary(digest).toUpperCase();
		
		
		
		// Appel à la bdd pour savoir si le compte existe
		Connection conn = null;
		try {
			Class.forName("org.postgresql.Driver");
			conn = DriverManager.getConnection(
					"jdbc:postgresql://localhost:5432/initialdrift",
					"loanubuntu", "Caecke11");
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println(e.getClass().getName()+": "+e.getMessage());
			System.exit(0);
		}
		System.out.println("Opened database successfully");

		Statement stmt = conn.createStatement();

		if(inscription)
		{
			System.out.println(username);
			System.out.println(password);
			boolean ok = false;
			try{
				username.length();
				password.length();
				ok = true;
				
			}
			catch(NullPointerException e)
			{
				output.println("Pseudo ou mot de passe non renseigné, annulation de l'inscription " + username);
				output.flush();
        //		output.close();
			}
			catch(IndexOutOfBoundsException e)
			{
				output.println("Pseudo ou mot de passe non renseigné, annulation de l'inscription " + username);
				output.flush();
        	//	output.close();
			}
			
			
			if(ok){

				try{
					output = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
					String sql = "INSERT INTO logs " +
							"VALUES ('"+username+"','"+myHash+"')";
					stmt.executeUpdate(sql);
					//open printwriter for writing data to client

					output.println("Bienvenue sur Initial Drift 2.0, " + username);
					output.flush();
        //    		output.close();
				}catch(SQLException se){
					//Handle errors for JDBC
					output.println("Impossible de t'inscrire sous ce nom, le pseudo est déjà utilisé...");
					output.flush();
         //   		output.close();
					se.printStackTrace();
				}catch(Exception e){
					//Handle errors for Class.forName
					e.printStackTrace();
				}
			}
		}
		else{	
			ResultSet rs = stmt.executeQuery("SELECT * FROM logs WHERE pseudo = '"+username+"' AND password = '"+myHash+"'");
			//open printwriter for writing data to client
			output = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
			boolean isExist = rs.next();

			if(isExist){
				output.println("Bon retour sur Initial Drift 2.0, " + username);
				output.flush();
        	//	output.close();
				
				output.println(Server.nbjoueurs);
				output.flush();
        	//	output.close();
			}else{
				output.println("Login Failed. Mauvais mot de passe ?");
				output.flush();
        	//	output.close();
			}
		}

	}

	
	/**
	 * Ajoute un record à la base de données
	 * @param nomJoueur Pseudo du joueur
	 * @param victoire True si gagné <br/> False si perdu
	 * @throws SQLException
	 */
	public static void addResultatDuel(String nomJoueur, boolean victoire) throws SQLException {
				// Connexion à la BDD
				Connection conn = null;
				try {
					Class.forName("org.postgresql.Driver");
					conn = DriverManager.getConnection(
							"jdbc:postgresql://localhost:5432/initialdrift",
							"loanubuntu", "Caecke11");
				} catch (Exception e) {
					e.printStackTrace();
					System.err.println(e.getClass().getName()+": "+e.getMessage());
					System.exit(0);
				}
				System.out.println("Accès à la base OK");

				Statement stmt = conn.createStatement();
				
				try{
					String sql = "INSERT INTO leaderboard VALUES ('"+nomJoueur+"',"+victoire+")";
					stmt.executeUpdate(sql);
					System.out.println("Insertion dans la base OK");

				}catch(SQLException se){
					se.printStackTrace();
				}catch(Exception e){
					e.printStackTrace();
				}
	}
	
	/**
	 * Ajoute un record à la base de données
	 * @param nomJoueur
	 * @param score
	 * @param date
	 * @throws SQLException
	 */
	private void addHighScore(String nomJoueur, String score, String date) throws SQLException {
				// Connexion à la BDD
				Connection conn = null;
				try {
					Class.forName("org.postgresql.Driver");
					conn = DriverManager.getConnection(
							"jdbc:postgresql://localhost:5432/initialdrift",
							"loanubuntu", "Caecke11");
				} catch (Exception e) {
					e.printStackTrace();
					System.err.println(e.getClass().getName()+": "+e.getMessage());
					System.exit(0);
				}
				System.out.println("Accès à la base OK");

				Statement stmt = conn.createStatement();
				
				try{
					String sql = "INSERT INTO highscores VALUES ('"+nomJoueur+"',"+score+","+"'"+date+"')";
					stmt.executeUpdate(sql);
					System.out.println("Insertion dans la base OK");

					output.println("Nouveau score enregistré sur le serveur. Consulte les highscores pour connaître ton classement");
					output.flush();
				}catch(SQLException se){
					//Handle errors for JDBC
					output.println("Erreur dans l'enregistrement du score...");
					output.flush();
					se.printStackTrace();
				}catch(Exception e){
					e.printStackTrace();
				}
	}
	
	
	/**
	 * Récupère les 10 meilleurs records de la table des highscores 
	 */
	private void getHighScores() {
		// Connexion à la BDD
		Connection conn = null;
		try {
			Class.forName("org.postgresql.Driver");
			conn = DriverManager.getConnection(
					"jdbc:postgresql://localhost:5432/initialdrift",
					"loanubuntu", "Caecke11");
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println(e.getClass().getName()+": "+e.getMessage());
			System.exit(0);
		}
		System.out.println("Accès à la base OK");

		Statement stmt = null;
		try {
			stmt = conn.createStatement();
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		try{
			ResultSet rs = stmt.executeQuery("SELECT * FROM highscores ORDER BY score DESC LIMIT 10");
			String records="";
			while(rs.next()){
				records+=rs.getString(1)+"_"+rs.getString(2)+"_"+rs.getString(3)+"&";
			}			
			
			output.println(records);
			output.flush();				

		}catch(SQLException se){
			//Handle errors for JDBC
			output.println("Erreur dans la récupération des scores...");
			output.flush();
			se.printStackTrace();
		}catch(Exception e){
			e.printStackTrace();
		}

	}

	
	/**
	 * Récupère les 10 meilleurs duelistes du serveur 
	 */
	private void getLeaderboard() {
		// Connexion à la BDD
		Connection conn = null;
		try {
			Class.forName("org.postgresql.Driver");
			conn = DriverManager.getConnection(
					"jdbc:postgresql://localhost:5432/initialdrift",
					"loanubuntu", "Caecke11");
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println(e.getClass().getName()+": "+e.getMessage());
			System.exit(0);
		}
		System.out.println("Accès à la base OK");

		Statement stmt = null;
		try {
			stmt = conn.createStatement();
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		try{
			ResultSet rs = stmt.executeQuery("SELECT COUNT(resultat) AS victoires, pseudo FROM leaderboard WHERE resultat = 't' GROUP BY pseudo ORDER BY COUNT(resultat) DESC;");
			String records = "";
			while(rs.next()){
				// Format : nbvictoires - pseudo
				records+=rs.getString(1)+"_"+rs.getString(2)+"&";
			}
			output.println(records);
			output.flush();				

		}catch(SQLException se){
			//Handle errors for JDBC
			output.println("Erreur dans la récupération des scores...");
			output.flush();
			se.printStackTrace();
		}catch(Exception e){
			e.printStackTrace();
		}

	}
	
	private void getTop()
	{
		// Connexion à la BDD
		Connection conn = null;
		try {
			Class.forName("org.postgresql.Driver");
			conn = DriverManager.getConnection(
					"jdbc:postgresql://localhost:5432/initialdrift",
					"loanubuntu", "Caecke11");
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println(e.getClass().getName()+": "+e.getMessage());
			System.exit(0);
		}
		System.out.println("Accès à la base OK");

		Statement stmt = null;
		try {
			stmt = conn.createStatement();
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		try{
			ResultSet rs = stmt.executeQuery("SELECT * FROM highscores ORDER BY score DESC LIMIT 10");
			String records="";
			while(rs.next()){
				records+=rs.getString(1)+" - "+rs.getString(2)+"pts";
				break;
			}			
			
			output.println(records);
			output.flush();				

		}catch(SQLException se){
			//Handle errors for JDBC
			output.println("Erreur dans la récupération des scores...");
			output.flush();
			se.printStackTrace();
		}catch(Exception e){
			e.printStackTrace();
		}

	}
	
}
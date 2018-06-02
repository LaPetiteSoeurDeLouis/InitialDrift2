import java.io.*;
import java.net.*;
import java.util.concurrent.atomic.AtomicBoolean;


public class ServerDuel {
	public ServerSocket serversocket;
	public volatile int portServeur;
	public Socket client;
	int bytesRead;
	protected InputStream input = null;
	protected BufferedReader brinp = null;
	protected PrintWriter output = null;

	private volatile int nbjoueurs=0;

	public volatile AtomicBoolean ServeurDuelEnAttente = new AtomicBoolean();


	private volatile EchoThreadDuel joueur1;
	private volatile EchoThreadDuel joueur2;

	private volatile String pseudoj1;
	private volatile String pseudoj2;
	
	
	private volatile boolean fin=false;
	private volatile boolean blocage=false;

	private volatile int i42=0;
	private volatile int i43=0;

	private volatile int decorPosX=0;
	private volatile int decorType=0;
	
	private volatile int ennemiPosX=0;
	private volatile int ennemiType=0;	
	
	private volatile int viePosX=0;


	private volatile int j1PosX;
	private volatile int j1PosY;	
	
	private volatile int j2PosX;
	private volatile int j2PosY;	
	

	public void start() throws IOException{
		//make connection to client on port specified
		serversocket = new ServerSocket(0);
		portServeur = serversocket.getLocalPort();
		System.out.println("Connection Starting on port: "+portServeur);
	}

	public void run(){
		try{

			connectJ1();
			connectJ2();
			System.out.println("Le duel commence.");
			Thread.sleep(800);
		} catch (Exception e) {
			e.printStackTrace();
		}

		while(getNbjoueurs()==2)
		{
			try {
				Thread.sleep(40);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (i42 == 15) {
				GenererDecor();
				GenererEnnemi();
				i42 = 0;
				i43++;
			}
			if (i43 == 25) {
				i43 = 0;
				GenererVie();
			}
			i42++;
			joueur1.update();
			joueur2.update();
		}
	}

	public void connectJ2(){
		//accept connection from client
		try {
			client = serversocket.accept();

			input = client.getInputStream();
			brinp = new BufferedReader(new InputStreamReader(input));
			output = new PrintWriter(client.getOutputStream());

		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Joueur 2 sur le serveur de duel.");
		setNbjoueurs(getNbjoueurs() + 1);
		joueur2 = new EchoThreadDuel(client,this);
		//--Récupération du pseudo du joueur
		try {
			joueur2.pseudo = brinp.readLine();
			setPseudoj2(joueur2.pseudo);
		} catch (IOException e1) {
			e1.printStackTrace();
		}	
		
		joueur2.start();
	}

	public void connectJ1() {
		//accept connection from client

		try {
			client = serversocket.accept();

			input = client.getInputStream();
			brinp = new BufferedReader(new InputStreamReader(input));
			output = new PrintWriter(client.getOutputStream());		
		} catch (IOException e) {
			e.printStackTrace();
		}

		System.out.println("Joueur 1 sur le serveur de duel.");
		setNbjoueurs(getNbjoueurs() + 1);

		joueur1 = new EchoThreadDuel(client,this);


		//--Récupération du pseudo du joueur
		try {
			joueur1.pseudo = brinp.readLine();
			setPseudoj1(joueur1.pseudo);
		} catch (IOException e1) {
			e1.printStackTrace();
		}	


		joueur1.start();
	}


	public ServerDuel()
	{
	}


	//Générer Décors
	public void GenererDecor()
	{
		setDecorPosX((int)(Math.random()*(3-1))+1); // gauche ou droite
		setDecorType((int)(Math.random()*(5-1))+1); // 4 possibilités de décor 
	}

	//Générer Ennemis
	public void GenererEnnemi()
	{
		setEnnemiPosX((int)(Math.random()*(6-1))+1);
		setEnnemiType((int)(Math.random()*(4-1))+1);
	}

	//Générer Vies
	public void GenererVie()
	{
		setViePosX((int)(Math.random()*(6-1))+1);
	}
	
	
	public int geti42()
	{
		return i42;
	}

	public int geti43()
	{
		return i43;
	}

	public String getPseudoj1() {
		return pseudoj1;
	}

	public void setPseudoj1(String pseudoj1) {
		this.pseudoj1 = pseudoj1;
	}

	public String getPseudoj2() {
		return pseudoj2;
	}

	public void setPseudoj2(String pseudoj2) {
		this.pseudoj2 = pseudoj2;
	}

	public boolean isFin() {
		return fin;
	}

	public void setFin(boolean fin) {
		this.fin = fin;
	}

	public boolean isBlocage() {
		return blocage;
	}

	public void setBlocage(boolean blocage) {
		this.blocage = blocage;
	}

	public int getDecorPosX() {
		return decorPosX;
	}

	public void setDecorPosX(int decorPosX) {
		this.decorPosX = decorPosX;
	}

	public int getDecorType() {
		return decorType;
	}

	public void setDecorType(int decorType) {
		this.decorType = decorType;
	}

	public int getEnnemiPosX() {
		return ennemiPosX;
	}

	public void setEnnemiPosX(int ennemiPosX) {
		this.ennemiPosX = ennemiPosX;
	}

	public int getEnnemiType() {
		return ennemiType;
	}

	public void setEnnemiType(int ennemiType) {
		this.ennemiType = ennemiType;
	}

	public int getViePosX() {
		return viePosX;
	}

	public void setViePosX(int viePosX) {
		this.viePosX = viePosX;
	}

	public int getJ1PosX() {
		return j1PosX;
	}

	public void setJ1PosX(int j1PosX) {
		this.j1PosX = j1PosX;
	}

	public int getJ1PosY() {
		return j1PosY;
	}

	public void setJ1PosY(int j1PosY) {
		this.j1PosY = j1PosY;
	}

	public int getJ2PosX() {
		return j2PosX;
	}

	public void setJ2PosX(int j2PosX) {
		this.j2PosX = j2PosX;
	}

	public int getJ2PosY() {
		return j2PosY;
	}

	public void setJ2PosY(int j2PosY) {
		this.j2PosY = j2PosY;
	}

	public int getNbjoueurs() {
		return nbjoueurs;
	}

	public void setNbjoueurs(int nbjoueurs) {
		this.nbjoueurs = nbjoueurs;
	}

	public void seti42(int i) {
		i42 = i;		
	}
	public void seti43(int i) {
		i43 = i;		
	}

}


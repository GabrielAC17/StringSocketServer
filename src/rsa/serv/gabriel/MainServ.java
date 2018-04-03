package rsa.serv.gabriel;

public class MainServ {


	public static void main(String[] args) {
		System.out.println("Iniciando conexão com Arduíno");
		iniciarArduino();
		System.out.println("Iniciando servidor");
		iniciarServidor(8888);
	}

	
	public static void iniciarServidor(int port){
		Server server = new Server(port);
		System.out.println("Porta "+ port);
		server.start();
	}
	public static void iniciarArduino() {
		 TwoWaySerialComm com = new TwoWaySerialComm("COM3");
	}
	
}


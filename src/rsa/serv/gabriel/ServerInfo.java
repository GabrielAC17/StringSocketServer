package rsa.serv.gabriel;

import java.util.ArrayList;


public class ServerInfo {
	private static ArrayList<ClientConnection> clientes = new ArrayList<ClientConnection>();
	private static String currentInfo = "No data";

	public static String getCurrentInfo() {
		return currentInfo;
	}

	public static void setCurrentInfo(String currentInfo) {
		ServerInfo.currentInfo = currentInfo;
	}

	public static ArrayList<ClientConnection> getClientes() {
		return clientes;
	}

	public static void setClientes(ArrayList<ClientConnection> c) {
		clientes = c;
	}
}
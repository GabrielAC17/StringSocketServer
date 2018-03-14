package model;

import java.util.ArrayList;
import java.util.List;

import controller.ClientConnection;

public class ServerInfo {
	private static ArrayList<ClientConnection> clientes = new ArrayList<ClientConnection>();
	private static String currentInfo = "";

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

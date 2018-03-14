package controller;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import javax.swing.JOptionPane;

import model.ServerInfo;

public class Server extends Thread{
	private ServerSocket server;
	
	public Server(int port)
	{
		try
		{
			server = new ServerSocket(port);
		}
		catch (IOException e)
		{
			JOptionPane.showMessageDialog(null,"Porta indisponível:  "+ e.getMessage(),"Erro!",JOptionPane.WARNING_MESSAGE);
			Thread.currentThread().interrupt();
		}
	}
	
	@Override
	public void run(){
		while (true){
			Socket socket;
			ClientConnection cli = null;
			try {
				System.out.println("Servidor ouvindo");
				socket = server.accept();
				cli = new ClientConnection(socket);
				System.out.println("Novo cliente conectado! " + socket.getInetAddress());
				ServerInfo.getClientes().add(cli);
				cli.start();
			} catch (IOException e) {
				JOptionPane.showMessageDialog(null,"Erro de socket:  "+ e.getMessage(),"Erro!",JOptionPane.WARNING_MESSAGE);
				Thread.currentThread().interrupt();
			}
		}
	}
}

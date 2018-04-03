package rsa.client;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.util.Base64;
import java.util.Scanner;

import javax.crypto.Cipher;

import com.fasterxml.jackson.databind.ObjectMapper;

public class HashCliente {
	
	 private static final String PONG = "pong";

	private static final String PING = "ping";

	/**
     * Local da chave pública no sistema de arquivos.
     */
    public static final String PATH_CHAVE_PUBLICA = "C:/keys/public.key";
	
    /**
     * Constante RSA
     */
    public static final String ALGORITHM = "RSA";
	
	public static void main(String[] args) throws UnknownHostException, IOException, NoSuchAlgorithmException, ClassNotFoundException {
		// dispara cliente
//		new HashCliente("192.168.43.27", 8888).executa();
		new HashCliente("localhost", 8888).executa();
	}

	private String host;
	private int porta;
	private PrintStream printCliente;
	public Socket cliente;
	private String nome = "matheus";
	private String grupo = "G1";

	public HashCliente (Socket cliente) {
		this.cliente = cliente;
	}

	public HashCliente(String host, int porta) {
		this.host = host;
		this.porta = porta;
	}
	public void executa() throws UnknownHostException, IOException, NoSuchAlgorithmException, ClassNotFoundException {
		this.cliente = new Socket(this.host, this.porta);
		System.out.println("O cliente se conectou ao servidor!");

		// thread para receber mensagens do servidor
		 HashRecebedor r = new HashRecebedor(this);
		 new Thread(r).start();

		// lê msgs do teclado e manda pro servidor
		Scanner teclado = new Scanner(System.in);
		PrintStream saida = new PrintStream(cliente.getOutputStream());
		StringBuilder msgEnv = new StringBuilder();
		while (teclado.hasNextLine()) {
			
			msgEnv.append(teclado.nextLine());	
			
			JsonMessage json = new JsonMessage();
			
	        json.setText(msgEnv.toString().getBytes());
			
	        
	        ObjectMapper mapper = new ObjectMapper();
	        String strJson = mapper.writeValueAsString(json);
	        
	        int length = strJson.getBytes().length;

			byte[] valuelength = new byte[4];
			valuelength[0] = (byte)(length & 0xff);
	        valuelength[1] = (byte)((length >> 8) & 0xff);
	        valuelength[2] = (byte)((length >> 16) & 0xff);
	        valuelength[3] = (byte)((length >> 24) & 0xff);

	        cliente.getOutputStream().write(valuelength);
	        cliente.getOutputStream().write(strJson.getBytes());

			msgEnv = new StringBuilder();
		}

		saida.close();
		teclado.close();
		cliente.close();
	}
	

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public String getGrupo() {
		return grupo;
	}

	public void setGrupo(String grupo) {
		this.grupo = grupo;
	}

	public PrintStream getPrintCliente() {
		return printCliente;
	}

	public void setPrintCliente(PrintStream printCliente) {
		this.printCliente = printCliente;
	}

	public Socket getCliente() {
		return cliente;
	}

	public void setCliente(Socket cliente) {
		this.cliente = cliente;
	}

}

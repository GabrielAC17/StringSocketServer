package controller;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.swing.JOptionPane;

import com.fasterxml.jackson.databind.ObjectMapper;

import model.Message;
import model.ServerInfo;

public class ClientConnection extends Thread{
	public static final String ALGORITHM = "RSA";
	
	/**
     * Local da chave privada no sistema de arquivos.
     */
    public static final String PATH_CHAVE_PRIVADA = "C:/keys/private.key";
   
    /**
     * Local da chave pública no sistema de arquivos.
     */
    public static final String PATH_CHAVE_PUBLICA = "C:/keys/public.key";
	
	private Socket socket;
	private BufferedReader input;
	private BufferedWriter output;
	private boolean stopThread = false;
	//private MessageDigest digest;
	private ObjectMapper map = new ObjectMapper();

	public ClientConnection(Socket socket)
	{
		this.socket = socket;
		try
		{

			output = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
			input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		}
		catch (IOException e)
		{
			JOptionPane.showMessageDialog(null,"Erro ao conectar com o cliente:  "+ e.getMessage(),"Erro!",JOptionPane.WARNING_MESSAGE);
			ServerInfo.getClientes().remove(this);
			this.interrupt();
		}
		
	}

	public void send(String msg, PublicKey key)
	{
		int cont = 0;
		while (cont <= 2) {
			try
			{
				Message m = new Message();
				
//				ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(PATH_CHAVE_PUBLICA));
//		        final PublicKey chavePublica = (PublicKey) inputStream.readObject();
		        final byte[] textoCriptografado = criptografa(msg, key);
//		        inputStream.close();
		        
		        m.setMessage(textoCriptografado);
		        
		        //hash
				MessageDigest digest = MessageDigest.getInstance("SHA-256");
				byte[] hash = digest.digest(msg.toString().getBytes(StandardCharsets.UTF_8));
				m.setHash(hash);
				
				String obj = map.writeValueAsString(m);
				
				String valueencoded = Base64.getEncoder().encodeToString(obj.getBytes());
				
				output.write(valueencoded + "\n");
				//output.write(msg + "\n");
				break;
			}
			catch (IOException e)
			{
				System.out.println("Conexão com o cliente perdida, fechando conexão.");
				ServerInfo.getClientes().remove(this);
				//this.interrupt();
				stopThread = true;
			} catch (ClassNotFoundException e) {
				System.out.println("Problema ao ler a chave pública de criptografia, verifique a chave e tente novamente.");
				System.out.println("Tentativa " + Integer.toString(cont+1) + "de 3");
				cont++;
			} catch (NoSuchAlgorithmException e) {
				System.out.println("Erro ao gerar o hash.");
				System.out.println("Tentativa " + Integer.toString(cont+1) + "de 3");
				cont++;
			} catch (Exception e) {
				System.out.println("Erro ao processar a mensagem, tentando novamente");
				System.out.println("Tentativa " + Integer.toString(cont+1) + "de 3");
				cont++;
			}		
		}
		
	}
	
	@Override
	public void run()
	{
		while(true)
		{
			if (stopThread){
				return;
			}
			try
			{
				String m = input.readLine();
				if (m == null || m == "" || m == "pong"){
					continue;
				}
				
				byte[] valueDecoded = Base64.getDecoder().decode(m.getBytes());
				
				Message response = map.readValue(new String(valueDecoded), Message.class);
				
//				ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(PATH_CHAVE_PRIVADA));
//		        final PrivateKey chavePrivada = (PrivateKey) inputStream.readObject();
//		        final String textoPuro = decriptografa(response.getMessage(), chavePrivada);
//		        inputStream.close();
		        
		        //hash
				MessageDigest digest = MessageDigest.getInstance("SHA-256");
				byte[] hash = digest.digest(response.getMessage());
				
				String hashGerado = new String(hash);
				String hashCliente = new String(response.getHash());
				
				if (hashGerado.equals(hashCliente)) {
					X509EncodedKeySpec spec = new X509EncodedKeySpec(response.getChave().getBytes());
					KeyFactory keyFactory = KeyFactory.getInstance("RSA");
					PublicKey chave = keyFactory.generatePublic(spec);
					if (new String().toUpperCase().equals("MANDAINFO")) {
						
						send(ServerInfo.getCurrentInfo(),chave);
						System.out.println("Cliente " + socket.getInetAddress()+": " + response.getMessage());
					}
						
				}
				else {
					System.out.println("Cliente " + socket.getInetAddress()+": " + response.getMessage());
					System.out.println("Perda de dados detectada!");
				}
					
				
				output.flush();
				//output.reset();
			}
			catch (IOException e)
			{
				System.out.println("Conexão com o cliente perdida, fechando conexão.");
				ServerInfo.getClientes().remove(this);
				return;
			}
			catch(Exception e) {
				System.out.println("Erro ao processar a mensagem recebida do cliente " + socket.getInetAddress());
			}
		}
	}
	
	/**
     * Criptografa o texto puro usando chave pública.
	 * @throws Exception 
     */
    public static byte[] criptografa(String texto, PublicKey chave) throws Exception {
      byte[] cipherText = null;
      
      try {
        final Cipher cipher = Cipher.getInstance(ALGORITHM);
        // Criptografa o texto puro usando a chave Púlica
        cipher.init(Cipher.ENCRYPT_MODE, chave);
        cipherText = cipher.doFinal(texto.getBytes());
      } catch (Exception e) {
        throw e;
      }
      
      return cipherText;
    }
   
    /**
     * Decriptografa o texto puro usando chave privada.
     * @throws Exception 
     */
    public static String decriptografa(byte[] texto, PrivateKey chave) throws Exception {
      byte[] dectyptedText = null;
      
      try {
        final Cipher cipher = Cipher.getInstance(ALGORITHM);
        // Decriptografa o texto puro usando a chave Privada
        cipher.init(Cipher.DECRYPT_MODE, chave);
        dectyptedText = cipher.doFinal(texto);
   
      } catch (Exception ex) {
        throw ex;
      }
   
      return new String(dectyptedText);
    }
	
}

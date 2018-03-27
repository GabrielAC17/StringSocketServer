package controller;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.EncodedKeySpec;
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
	//private BufferedReader input;
	//private BufferedWriter output;
	private InputStream input;
	private OutputStream output;
	private boolean stopThread = false;
	//private MessageDigest digest;
	private ObjectMapper map = new ObjectMapper();

	public ClientConnection(Socket socket)
	{
		this.socket = socket;
		try
		{

			//output = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
			//input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			input = socket.getInputStream();
			output = socket.getOutputStream();
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
		        
		        m.setText(textoCriptografado);
		        
		        //hash
				MessageDigest digest = MessageDigest.getInstance("SHA-256");
				byte[] hash = digest.digest(msg.toString().getBytes(StandardCharsets.UTF_8));
				m.setHash(hash);
				
				String obj = map.writeValueAsString(m);
				
				String valueencoded = Base64.getEncoder().encodeToString(obj.getBytes());
				
				byte[] valueAsBytes = valueencoded.getBytes();
				
				int length = valueAsBytes.length;
				
				byte[] valuelength = new byte[4];
				
				valuelength[0] = (byte)(length & 0xff);
		        valuelength[1] = (byte)((length >> 8) & 0xff);
		        valuelength[2] = (byte)((length >> 16) & 0xff);
		        valuelength[3] = (byte)((length >> 24) & 0xff);
		        
		        output.write(valuelength);
		        
		        output.write(valueAsBytes);
				
				//output.write((valueencoded + "\n").getBytes());
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
				byte[] lenBytes = new byte[4];
				input.read(lenBytes);
				int length =  (((lenBytes[3] & 0xff) << 24) | ((lenBytes[2] & 0xff) << 16) |
		                  ((lenBytes[1] & 0xff) << 8) | (lenBytes[0] & 0xff));
				
				String m = "";
				byte[] b = new byte[length];
				
				if(input.read(b) > 0) {
					 m = new String(b, 0, length);
 					if (m == null || m == "" || m == "pong"){
						continue;
					}
				}
				else {
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
				byte[] hash = digest.digest(response.getText());
				
				String hashGerado = new String(hash);
				String hashCliente = new String(response.getHash());
				
				if (hashGerado.equals(hashCliente)) {
					EncodedKeySpec spec = new X509EncodedKeySpec(response.getChave());
					PublicKey chave = KeyFactory.getInstance("RSA").generatePublic(spec);
					if (new String(response.getText()).toUpperCase().equals("MANDAINFO")) {
						
						send(ServerInfo.getCurrentInfo(),chave);
						System.out.println("Cliente " + socket.getInetAddress()+": " + response.getText());
					}
						
				}
				else {
					System.out.println("Cliente " + socket.getInetAddress()+": " + response.getText());
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

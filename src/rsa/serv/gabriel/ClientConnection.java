package rsa.serv.gabriel;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import javax.swing.JOptionPane;
import com.fasterxml.jackson.databind.ObjectMapper;
import rsa.client.JsonMessage;
import rsa.client.SecurityManager;



public class ClientConnection extends Thread{
	public static final String ALGORITHM = "RSA";
	
	/**
     * Local da chave privada no sistema de arquivos.
     */
    public static final String PATH_CHAVE_PRIVADA = "C:/keys/private.key";
    
	private static final String MANDAINFO = "MANDAINFO";
   
    /**
     * Local da chave pública no sistema de arquivos.
     */
    public static final String PATH_CHAVE_PUBLICA = "C:/keys/public.key";
	
	private Socket socket;
	private InputStream input;
	private OutputStream output;
	private boolean stopThread = false;
	private ObjectMapper map = new ObjectMapper();

	public ClientConnection(Socket socket)
	{
		this.socket = socket;
		try
		{
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

	public void send(int size, Boolean valid)
	{
		int cont = 0;
		while (cont <= 2) {
			try
			{
				String strJson = "MENSAGEM INVÁLIDA. . .";
				int length = size;
				if (valid) {
					// mensagem de resposta do servidor
					JsonMessage resp = new JsonMessage();
					String texto = ServerInfo.getCurrentInfo();
					resp.setText(texto.getBytes());

					// hash assinado
					MessageDigest digest;
					digest = MessageDigest.getInstance("SHA-256");
					byte[] hashGerado = digest.digest(texto.getBytes(StandardCharsets.UTF_8));
					resp.setHash(Base64.getEncoder().encode(hashGerado));
					
					//testar erro
//					byte[] hashGeradoTextFalse = digest.digest("Resposta Serv False".getBytes(StandardCharsets.UTF_8));
//					resp.setHash(Base64.getEncoder().encode(hashGeradoTextFalse));

					SecurityManager sm = new SecurityManager();
					byte[] hashAssinado = sm.signData(hashGerado);
					resp.setSign(Base64.getEncoder().encode(hashAssinado));

					// public key
					
					resp.setChave(Base64.getEncoder().encode(sm.getPublicKey().getEncoded()));
					 
					strJson = map.writeValueAsString(resp);
				}
				
				
				//Enia os dados ao cliente
				length = strJson.getBytes().length;

				byte[] valuelength = new byte[4];
				valuelength[0] = (byte) (length & 0xff);
				valuelength[1] = (byte) ((length >> 8) & 0xff);
				valuelength[2] = (byte) ((length >> 16) & 0xff);
				valuelength[3] = (byte) ((length >> 24) & 0xff);

				output.write(valuelength);
				output.write(strJson.getBytes());
				output.flush();
				// output.reset();
				
				break;
			}
			catch (IOException e)
			{
				System.out.println("Conexão com o cliente perdida, fechando conexão.");
				ServerInfo.getClientes().remove(this);
				//this.interrupt();
				stopThread = true;
			} catch (NoSuchAlgorithmException e) {
				System.out.println("Erro ao gerar o hash.");
				System.out.println("Tentativa " + Integer.toString(cont+1) + "de 3");
				cont++;
			} catch (Exception e) {
				System.out.println("Erro ao processar o envio mensagem, tentando novamente");
				System.out.println("Tentativa " + Integer.toString(cont+1) + "de 3");
				cont++;
			}		
		}
		
	}

	@Override
	public void run() {
		while (true) {
			if (stopThread) {
				return;
			}
			try {
				//Lê o tamanho da mensagem a ser recebida através de um array de bytes convertido para int no formato usado pelo java
				byte[] lenBytes = new byte[4];
				input.read(lenBytes);
				int length = (((lenBytes[3] & 0xff) << 24) | ((lenBytes[2] & 0xff) << 16) | ((lenBytes[1] & 0xff) << 8)
						| (lenBytes[0] & 0xff));

				String m = "";
				byte[] b = new byte[length];
				
				//Lê a mensagem
				if (input.read(b) > 0) {
					m = new String(b, 0, length);
					if (m == null || m == "" || m == "pong") {
						continue;
					}
				} else {
					continue;
				}
				
				//Lê objeto JSON
				JsonMessage json = map.readValue(m, JsonMessage.class);
				
				if (new String(json.getText()).toUpperCase().equals(MANDAINFO)) {

					send(length, true);
				}
				else {
					send(length, false);
				}
				
			} catch (IOException e) {
				System.out.println("Conexão com o cliente perdida, fechando conexão.");
				ServerInfo.getClientes().remove(this);
				return;
			} catch (Exception e) {
				System.out.println("Erro ao processar a mensagem recebida do cliente " + socket.getInetAddress());
			}
		}
	}
}
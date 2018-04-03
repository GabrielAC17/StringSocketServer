package rsa.client;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.EncodedKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Scanner;

import com.fasterxml.jackson.databind.ObjectMapper;

public class HashRecebedor implements Runnable {
	
	
	private static final String PATH_CHAVE_PRIVADA = "C:/keys/private.key";
	 /**
     * Constante RSA
     */
    public static final String ALGORITHM = "RSA";
	private HashCliente input;
	private String grupo;
	private boolean stopThread = false;
	private Signature sign;

	public HashRecebedor(HashCliente input/*, String grupo*/) throws NoSuchAlgorithmException {
		sign = Signature.getInstance("SHA1withRSA");
		this.input = input;
//		this.grupo = grupo;
	}

	public void run() {
		Scanner s;
		ObjectMapper mapper = new ObjectMapper();
		InputStream inputStream = null;
		try {
			inputStream = this.input.getCliente().getInputStream();
		} catch (IOException e1) {
			System.out.println(e1.getMessage());
		}
		while (true) {
			if (stopThread) {
				return;
			}
			try {
				byte[] lenBytes = new byte[4];
				inputStream.read(lenBytes);
				int length = (((lenBytes[3] & 0xff) << 24) | ((lenBytes[2] & 0xff) << 16) | ((lenBytes[1] & 0xff) << 8)
						| (lenBytes[0] & 0xff));

				String m = "";
				byte[] b = new byte[length];

				if (inputStream.read(b) > 0) {
					m = new String(b, 0, length);
					if (m == null || m == "" || m == "pong") {
						continue;
					}
				} else {
					continue;
				}
				JsonMessage json = mapper.readValue(m, JsonMessage.class);
				byte[] publicKeyByte = Base64.getDecoder().decode(new String(json.getChave()));
				KeyFactory keyFactory = KeyFactory.getInstance("RSA");
				EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(publicKeyByte);
				PublicKey publicKey = keyFactory.generatePublic(publicKeySpec);
				
			
				
				boolean verify = verifySignatureExtern(Base64.getDecoder().decode(json.getSign()), Base64.getDecoder().decode(json.getHash()), publicKey);
				
				if (verify) {
					MessageDigest digest;
					digest = MessageDigest.getInstance("SHA-256");
					byte[] hashCriado = digest.digest(new String(json.getText()).getBytes((StandardCharsets.UTF_8)));
					if (new String(hashCriado).equals(new String(Base64.getDecoder().decode(json.getHash())))){
						System.out.println("Hash Iguais, texto identicos: " + hashCriado);
					}
				}
			} catch (Exception e) {
				System.out.println(e.getMessage());
			}
		}
	}
	
	private boolean verifySignatureExtern(byte[] signature, byte[] data, PublicKey key) {
		try {
			sign.initVerify(key);
			sign.update(data);
			return sign.verify(signature);
		} catch (SignatureException e) {
			e.printStackTrace();
		} catch (InvalidKeyException e) {
		}

		return false;
	}
}

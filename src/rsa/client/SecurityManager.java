package rsa.client;
/**
 * <p>Title: RSA Security</p>
 * Description: This class generates a RSA private and public key, reinstantiates
 * the keys from the corresponding key files.It also generates compatible .Net Public Key,
 * which we will read later in C# program using .Net Securtiy Framework
 * The reinstantiated keys are used to sign and verify the given data.</p>
 *
 * @author Shaheryar
 * @version 1.0
 */

import java.security.*;
import java.security.spec.*;
import java.util.Base64;
import java.io.*;
import java.security.interfaces.*;
import javax.xml.transform.stream.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.*;
import org.w3c.dom.*;
import javax.xml.parsers.*;

public class SecurityManager {

	private KeyPairGenerator keyGen; // Key pair generator for RSA
	private PrivateKey privateKey; // Private Key Class
	private PublicKey publicKey; // Public Key Class
	private KeyPair keypair; // KeyPair Class
	private Signature sign; // Signature, used to sign the data
	private String PRIVATE_KEY_FILE; // Private key file.
	private String PUBLIC_KEY_FILE; // Public key file.
	private String DOT_NET_PUBLIC_KEY_FILE; // File to store .Net Compatible Key Data

	/**
	 * Default Constructor. Instantiates the key paths and signature algorithm.
	 */
	public SecurityManager() {
		try {

			// Get the instance of Signature Engine.
			sign = Signature.getInstance("SHA1withRSA");
			PRIVATE_KEY_FILE = "c:\\keys\\private.rsa"; // Location of private key file
			PUBLIC_KEY_FILE = "c:\\keys\\public.rsa"; // Location of Public key file
			DOT_NET_PUBLIC_KEY_FILE = "c:\\keys\\netpublic.key"; // Location of generated .Net Public Key File
		} catch (NoSuchAlgorithmException nsa) {
			System.out.println("" + nsa.getMessage());
		}
	}

	/**
	 * Generates the keys for given size.
	 * 
	 * @param size
	 *            - Key Size [512|1024]
	 */
	public void generateKeys(int size) {
		try {
			System.out.println("Generatign Keys");
			// Get Key Pair Generator for RSA.
			keyGen = KeyPairGenerator.getInstance("RSA");
			keyGen.initialize(size);
			keypair = keyGen.genKeyPair();
			privateKey = keypair.getPrivate();
			publicKey = keypair.getPublic();

			// Get the bytes of the public and private keys
			byte[] privateKeyBytes = privateKey.getEncoded();
			byte[] publicKeyBytes = publicKey.getEncoded();

			// write bytes to corresponding files.
			writeKeyBytesToFile(Base64.getEncoder().encode(privateKeyBytes), PRIVATE_KEY_FILE);
			byte[] encodedValue = Base64.getEncoder().encode(publicKeyBytes);
			writeKeyBytesToFile(encodedValue, PUBLIC_KEY_FILE);

			// Generate the Private Key, Public Key and Public Key in XML format.
			PrivateKey privateKey = KeyFactory.getInstance("RSA")
					.generatePrivate(new PKCS8EncodedKeySpec(privateKeyBytes));
			PublicKey publicKey = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(publicKeyBytes));
			RSAPublicKey rsaPublicKey = (RSAPublicKey) KeyFactory.getInstance("RSA")
					.generatePublic(new X509EncodedKeySpec(publicKeyBytes));
			// store the public key in XML string to make compatible .Net public key file
			String xml = getRSAPublicKeyAsXMLString(rsaPublicKey);
			// Store the XML (Generated .Net public key file) in file
			writeKeyBytesToFile(xml.getBytes(), DOT_NET_PUBLIC_KEY_FILE);
		} catch (java.security.NoSuchAlgorithmException e) {
			System.out.println("No such algorithm. Please check the JDK version." + e.getCause());
		} catch (java.security.spec.InvalidKeySpecException ik) {
			System.out.println("Invalid Key Specs. Not valid Key files." + ik.getCause());
		} catch (UnsupportedEncodingException ex) {
			System.out.println(ex);
		} catch (ParserConfigurationException ex) {
			System.out.println(ex);
		} catch (TransformerException ex) {
			System.out.println(ex);
		} catch (IOException ioe) {
			System.out.println("Files not found on specified path. " + ioe.getCause());
		}

	}

	/**
	 * Reads private key file
	 * 
	 * @return byte[] private key
	 */
	private byte[] readPrivateKeyFile() throws IOException {
		return readKeyBytesFromFile(PRIVATE_KEY_FILE);
	}

	/**
	 * Reads Public key
	 * 
	 * @return byte[] public key
	 */
	private byte[] readPublicKeyFile() throws IOException {
		return readKeyBytesFromFile(PUBLIC_KEY_FILE);
	}

	/**
	 * Initialize only the private key.
	 */
	private void initializePrivateKey() {
		try {
			// Read key files back and decode them from BASE64
			// BASE64Decoder decoder = new BASE64Decoder();
			byte[] privateKeyBytes = Base64.getDecoder().decode(new String(readPrivateKeyFile()));
			// Convert back to public and private key objects
			KeyFactory keyFactory = KeyFactory.getInstance("RSA");
			EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
			privateKey = keyFactory.generatePrivate(privateKeySpec);

		} catch (IOException io) {
			System.out.println("Private Key File Not found." + io.getCause());
		} catch (InvalidKeySpecException e) {
			System.out.println("Invalid Key Specs. Not valid Key files." + e.getCause());
		} catch (NoSuchAlgorithmException e) {
			System.out.println("There is no such algorithm. Please check the JDK ver." + e.getCause());
		}

	}

	/**
	 * Initializes the public and private keys.
	 */
	private void initializeKeys() {
		try {
			// Read key files back and decode them from BASE64
			// BASE64Decoder decoder = new BASE64Decoder();

			byte[] privateKeyBytes = Base64.getDecoder().decode(new String(readPrivateKeyFile()));
			byte[] publicKeyBytes = Base64.getDecoder().decode(new String(readPublicKeyFile()));

			// Convert back to public and private key objects
			KeyFactory keyFactory = KeyFactory.getInstance("RSA");
			EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
			privateKey = keyFactory.generatePrivate(privateKeySpec);

			EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(publicKeyBytes);
			publicKey = keyFactory.generatePublic(publicKeySpec);

		} catch (IOException io) {
			System.out.println("Public/ Private Key File Not found." + io.getCause());
		} catch (InvalidKeySpecException e) {
			System.out.println("Invalid Key Specs. Not valid Key files." + e.getCause());
		} catch (NoSuchAlgorithmException e) {
			System.out.println("There is no such algorithm. Please check the JDK ver." + e.getCause());
		}
	}

	/**
	 * Signs the data and return the signature for a given data.
	 * 
	 * @param toBeSigned
	 *            Data to be signed
	 * @return byte[] Signature
	 */
	public byte[] signData(byte[] toBeSigned) {
		if (privateKey == null) {
			initializePrivateKey();
		}
		try {
			Signature rsa = Signature.getInstance("SHA1withRSA");
			rsa.initSign(privateKey);
			rsa.update(toBeSigned);
			return rsa.sign();
		} catch (NoSuchAlgorithmException ex) {
			System.out.println(ex);
		} catch (InvalidKeyException in) {
			System.out.println("Invalid Key file.Please check the key file path" + in.getCause());
		} catch (SignatureException se) {
			System.out.println(se);
		}
		return null;
	}

	/**
	 * Verifies the signature for the given bytes using the public key.
	 * 
	 * @param signature
	 *            Signature
	 * @param data
	 *            Data that was signed
	 * @return boolean True if valid signature else false
	 */
	public boolean verifySignature(byte[] signature, byte[] data) {
		try {
			initializeKeys();
			sign.initVerify(publicKey);
			sign.update(data);
			return sign.verify(signature);
		} catch (SignatureException e) {
			e.printStackTrace();
		} catch (InvalidKeyException e) {
		}

		return false;
	}
	
	public boolean verifySignatureExtern(byte[] signature, byte[] data, PublicKey key) {
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

	/**
	 * Gets the RSA Public Key as XML. The key idea is to make the key readable for
	 * .Net platform.
	 * 
	 * @param key
	 *            RSAPublicKey
	 * @return Document XML document.
	 * @throws ParserConfigurationException
	 * @throws UnsupportedEncodingException
	 */
	private Document getRSAPublicKeyAsXML(RSAPublicKey key)
			throws ParserConfigurationException, UnsupportedEncodingException {
		Document result = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
		Element rsaKeyValue = result.createElement("RSAKeyValue");
		result.appendChild(rsaKeyValue);
		Element modulus = result.createElement("Modulus");
		rsaKeyValue.appendChild(modulus);

		byte[] modulusBytes = key.getModulus().toByteArray();
		modulusBytes = stripLeadingZeros(modulusBytes);
		modulus.appendChild(result.createTextNode(new String(Base64.getEncoder().encode(modulusBytes))));

		Element exponent = result.createElement("Exponent");
		rsaKeyValue.appendChild(exponent);

		byte[] exponentBytes = key.getPublicExponent().toByteArray();
		exponent.appendChild(result.createTextNode(new String(Base64.getEncoder().encode(exponentBytes))));

		return result;
	}

	/**
	 * Gets the RSA Public key as XML string.
	 * 
	 * @param key
	 *            RSAPublicKey
	 * @return String XML representation of RSA Public Key.
	 * @throws UnsupportedEncodingException
	 * @throws ParserConfigurationException
	 * @throws TransformerException
	 */
	private String getRSAPublicKeyAsXMLString(RSAPublicKey key)
			throws UnsupportedEncodingException, ParserConfigurationException, TransformerException {
		Document xml = getRSAPublicKeyAsXML(key);
		Transformer transformer = TransformerFactory.newInstance().newTransformer();
		StringWriter sw = new StringWriter();
		transformer.transform(new DOMSource(xml), new StreamResult(sw));
		return sw.getBuffer().toString();
	}

	/**
	 * Utility method to delete the leading zeros from the modulus.
	 * 
	 * @param a
	 *            modulus
	 * @return modulus
	 */
	private byte[] stripLeadingZeros(byte[] a) {
		int lastZero = -1;
		for (int i = 0; i < a.length; i++) {
			if (a[i] == 0) {
				lastZero = i;
			} else {
				break;
			}
		}
		lastZero++;
		byte[] result = new byte[a.length - lastZero];
		System.arraycopy(a, lastZero, result, 0, result.length);
		return result;
	}

	/**
	 * Writes the bytes of the key in a file.
	 * 
	 * @param key
	 *            byte array of key data.
	 * @param file
	 *            File Name
	 */
	private void writeKeyBytesToFile(byte[] key, String file) throws IOException {
		OutputStream out = new FileOutputStream(file);
		out.write(key);
		out.close();
	}

	/**
	 * Returns the contents of the file in a byte array.
	 * 
	 * @param fileName
	 *            File Name
	 * @return byte[] Teh data read from a given file as a byte array.
	 */
	private byte[] readKeyBytesFromFile(String fileName) throws IOException {
		File file = new File(fileName);
		InputStream is = new FileInputStream(file);

		// Get the size of the file
		long length = file.length();

		// You cannot create an array using a long type.
		// It needs to be an int type.
		// Before converting to an int type, check
		// to ensure that file is not larger than Integer.MAX_VALUE.
		if (length > Integer.MAX_VALUE) {
			// File is too large
		}

		// Create the byte array to hold the data
		byte[] bytes = new byte[(int) length];

		// Read in the bytes
		int offset = 0;
		int numRead = 0;
		while (offset < bytes.length && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
			offset += numRead;
		}

		// Ensure all the bytes have been read in
		if (offset < bytes.length) {
			throw new IOException("Key File Error: Could not completely read file " + file.getName());
		}

		// Close the input stream and return bytes
		is.close();
		return bytes;

	}

	public static void main(String args[]) {
		SecurityManager sm = new SecurityManager();

		/*
		 * Uncomment next line for first time when you run the code,it will generate the
		 * keys. Afterwards,the application will read the generated key files from the
		 * given location. If you want to generate the key files each time, then you
		 * should keep it uncommented always.
		 * 
		 * Note: Location of key files have been hardocded i.e. c:\\public.rsa... etc
		 * You can change the location to what ever you want.
		 * 
		 */

		 sm.generateKeys(512);

//		String dataToBesigned = "This data will be signed .... you can provide any data here.";
//		byte[] signature = sm.signData(dataToBesigned.getBytes());
//
//		byte[] signedB64 = Base64.getEncoder().encode(signature);// You may store this signature in a text file.
//		byte[] dataB64 = Base64.getEncoder().encode(dataToBesigned.getBytes());// You may store the data as well in a
//																				// text file.
//
//		// writing data to file, which will be read later by C# program for verification
//		sm.writeBytesToFile(dataB64, "C:\\keys\\data.dat");
//		// writing signature to file, which will be read later by C# program for
//		// verification
//		sm.writeBytesToFile(signedB64, "C:\\keys\\signature.dat");
//
//		System.out.println("Signature: \n" + signedB64 + "\n\n\n\n" + "Data that was signed : \n" + dataB64);
//		System.out.println("Verifying the data using Public Key:\n Data is Verified : "
//				+ sm.verifySignature(signature, dataToBesigned.getBytes()));

	}

	public void writeBytesToFile(byte[] data, String file) {
		try {
			OutputStream out = new FileOutputStream(file);
			out.write(data);
			out.close();
		} catch (IOException ioe) {
			System.out.println("Exception occured while writing file" + ioe.getMessage());
		}
	}

	public PublicKey getPublicKey() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
		byte[] publicKeyBytes = Base64.getDecoder().decode(new String(readPublicKeyFile()));

		// Convert back to public and private key objects
		KeyFactory keyFactory = KeyFactory.getInstance("RSA");
	
		EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(publicKeyBytes);
		publicKey = keyFactory.generatePublic(publicKeySpec);
		return publicKey;
	}

	

}
package model;

public class Message{
	
	private byte[] message;
	private byte[] hash;
	private String chave;

	public byte[] getMessage() {
		return message;
	}

	public void setMessage(byte[] message) {
		this.message = message;
	}

	public byte[] getHash() {
		return hash;
	}

	public String getChave() {
		return chave;
	}

	public void setChave(String chave) {
		this.chave = chave;
	}

	public void setHash(byte[] hash) {
		this.hash = hash;
	}
	
	public Message() {
		super();
		// TODO Auto-generated constructor stub
	}
}

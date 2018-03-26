package model;

public class Message{
	
	private byte[] text;
	private byte[] hash;
	private String chave;

	public byte[] getText() {
		return text;
	}

	public void setText(byte[] text) {
		this.text = text;
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

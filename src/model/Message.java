package model;

public class Message{
	
	private byte[] text;
	private byte[] hash;
	private byte[] chave;

	public byte[] getText() {
		return text;
	}

	public void setText(byte[] text) {
		this.text = text;
	}

	public byte[] getHash() {
		return hash;
	}

	public byte[] getChave() {
		return chave;
	}

	public void setChave(byte[] chave) {
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

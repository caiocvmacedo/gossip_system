package gossip;

import java.net.DatagramSocket;
import java.net.InetAddress;

public class mensagem {

	//	Classe de mensagem, os atributos são listados abaixo:
	private String mensagem;
	private String tipo;
	private InetAddress destIp;
	private DatagramSocket serverSocket;
	private int destPort;
	private InetAddress remIp;
	private int remPort;
	private InetAddress orgIp;
	private int orgPort;
//	Construtor
	public mensagem(String mensagem, String tipo, InetAddress destIp, int destPort, 
			InetAddress remIp, int remPort, InetAddress orgIp, int orgPort) {
		super();
		this.destIp = destIp;
		this.remIp = remIp;
		this.destPort = destPort;
		this.remPort = remPort;
		this.mensagem = mensagem;
		this.tipo = tipo;
		this.orgIp = orgIp;
		this.orgPort = orgPort;
	}
//	Métodos Getters and Setters
	protected InetAddress getRemIp() {
		return remIp;
	}

	protected int getRemPort() {
		return remPort;
	}
	
	protected void setMensagem(String mensagem) {
		this.mensagem = mensagem;
	}

	protected DatagramSocket getServerSocket() {
		return serverSocket;
	}

	protected InetAddress getDestIp() {
		return destIp;
	}

	protected int getDestPort() {
		return destPort;
	}

	protected InetAddress getOrgIp() {
		return orgIp;
	}
	
	protected int getOrgPort() {
		return orgPort;
	}
	
	protected String getMsg() {
		return mensagem;
	}
	
	protected String getTipo() {
		return tipo;
	}

	public int length() {
		int length = getMsg().length();
		return length;
	}
	
}

package gossip;

import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Random;
import java.util.Scanner;
import com.google.gson.Gson;

public class peer {
//	Estabalecendo as variáveis do peer
	private static InetAddress ip;
	private static InetAddress ip1;
	private static InetAddress ip2;
	private static int port;
	private static int port1;
	private static int port2;
	private static String path;
	private static String queryOrMsgGson = "null";
	private static String pesquisar = "null";
	private static mensagem queryOrMsg = new mensagem("null", "null", ip, port, ip1, port1, ip, port);
	private static boolean encontrouAlgo;
	private static String repeatSearch = "null";
	private static String repeatResponse = "null";
	private static String repeatPesquisar = "null";
	private static String responseSaved = "null";
	
//	Thread main do peer, nela será executado as perguntas para o peer
	public static void main(String[] args) throws Exception {
		Scanner scan = new Scanner(System.in);
		boolean repetir = true;
		System.out.println("\nBem-vindo ao sistema de buscas de arquivos!\n");
//		Laço de repetição para solicitar a inicialização e perguntar o que o peer deseja buscar
		while (repetir == true) {
			System.out.println("\nDeseja inicializar, caso nao tenha iniciado,\n ou deseja pesquisar um arquivo?\n");
			System.out.println("Inicializar (1) // Pesquisar (2)\n");
			int escolha = scan.nextInt();
			if (escolha == 1) {
				boolean verificar = false;
				while (verificar == false) {
					System.out.println("Inicializando!\n");
					System.out.println("Informe seu IP\n");
					ip = InetAddress.getByName(scan.next());
					System.out.println("Informe sua porta\n");
					port = scan.nextInt();
					System.out.println("Informe o caminho da sua pasta de arquivos\n");
//					Insira o path da pasta dos arquivos do peer em questão
					path = scan.next();					
					System.out.println("Para se conectar necessota-se de outros 2 peers\n");
					System.out.println("Informe o IP do primeiro peer\n");
					ip1 = InetAddress.getByName(scan.next());
					System.out.println("Informe a porta desse primeiro peer\n");
					port1 = scan.nextInt();
					System.out.println("Informe o IP do segundo peer\n");
					ip2 = InetAddress.getByName(scan.next());
					System.out.println("Informe a porta desse segundo peer\n");
					port2 = scan.nextInt();
					System.out.println("Confirma os dados da inicializacao?\n sim(1) // nao(2)\n");
//					Após a solicitação de todas as variáveis para o funcionamento do peer
//					O sistema irá perguntar se o usuário confirma as informações adicionadas
//					Caso haja um erro, as informações serão coletadas novamente!
					int confirma = scan.nextInt();
					if (confirma == 1) {
						verificar = true;
//						Aqui o sistema irá mostrar na tela os arquivos atuais da pasta do peer em questão
						System.out.println("Realizando listagem de arquivos do peer\n");
						String[] arquivos = verificaArquivos();
						System.out.println("arquivos da pasta: ");
						for (int i = 0; i<arquivos.length; i++) {
							if (arquivos[i] != null) {
							System.out.println(arquivos[i]);
							}
						}
//						Inicializando a Thread que irá repetir na tela, a cada 90s, os arquivos dos peers vizinhos
						ThreadVerificaArquivos Verifica = new ThreadVerificaArquivos();
						Verifica.start(); 
//						Inicializando a Thread de "servidor", onde o peer estará apto a receber mensagens de outros peers
						ThreadServidor servidor = new ThreadServidor();
						servidor.start();
					} 
					else {
						System.out.println("Insira novamente as informacoes\n");
					}
				}
			}
//			Caso o peer queira buscar um arquivo, será perguntado o nome deste arquivo e em seguida,
//			um objeto da classe "mensagem" será gerado, para o envio desta Query aos outros peers
			else if (escolha == 2) {
				System.out.println("Qual arquivo deseja pesquisar?\n");
				System.out.println("Digite sem espacos e insira a extensao do arquivo\n");
				pesquisar = search(scan);
//				Inicializando a Thread de Time Out, para dizer, após 2s, se o arquivo foi encontrado!
				ThreadTime t = new ThreadTime();
				t.start();
			} 
			else {
				System.out.println("Digite uma opcao valida!\n");
			}
		}
	}
//	Thread "servidor", responsável por receber mensagens alheias!
	public static class ThreadServidor extends Thread {
		public void run() {
			try {
				ThreadServidor.sleep(1200);
				while (true) {
//					criação do socket de servidor para receber os datagramas
					try (DatagramSocket serverSocket = new DatagramSocket(port)) {
						byte[] recBuffer = new byte[1024];
						DatagramPacket recPkt = new DatagramPacket(recBuffer, recBuffer.length);
						serverSocket.receive(recPkt);
//						O servidor receberá uma string Gson,contendo a classe mensagem
						queryOrMsgGson = new String(recPkt.getData(),
								recPkt.getOffset(),
								recPkt.getLength());
						queryOrMsg = new Gson().fromJson(queryOrMsgGson, mensagem.class);
//						Verificando se a mensagem que chegou é uma verificação de arquivos
//						de outro peer
						if (queryOrMsg.getTipo().equals("file verification")) {
							System.out.println(queryOrMsg.getMsg() + "\n");
						}
//						Verifica se a mensagem que chegou é a resposta de uma query deste peer em questão
//						a verificação é feita pelo IP que originalmente escreveu a mensagem
						else if (queryOrMsg.getTipo().equals("response") & 
									queryOrMsg.getOrgIp().equals(ip)) {
//							Verifica se a resposta já foi recebida e exibida para o peer
							if (queryOrMsgGson.equals(repeatResponse) == false) {
								repeatResponse = queryOrMsgGson;
								System.out.println(queryOrMsg.getMsg() + "\n");
								responseSaved = queryOrMsg.getMsg();
							}
						}
//						Caso a mensagem tenha o tipo "search", será realizada uma busca dentro do peer
//						e caso seja encontrado o arquivo solicitado, o peer retornará uma mensagem do tipo
//						"response" para o solicitante
						else if (queryOrMsg.getTipo().equals("search")) {
//							Verificando se a search já foi processada anteriormente
//							A verificação é feita pela mensagem inteira, através da String Gson
//							que contém a classe toda, evitando que outro peer que fez a mesma query
//							entre como "repetição"
							if (queryOrMsgGson.equals(repeatSearch)) {
								System.out.println("Requisicao do arquivo: " + queryOrMsg.getMsg()
													+ " ja processada!\n");
							}
							else {
								repeatSearch = queryOrMsgGson;
//								Procurando dentro do peer se ele contém o arquivo solicitado
								boolean found = searchInsidePeer();
//								Caso encontre, a resposta é escrita, mantendo o IP original da query
//								e contendo o nome do arquivo encontrado
									if (found == true) {
										System.out.println("Tenho o arquivo: " + queryOrMsg.getMsg());
										String response = "O arquivo: " + queryOrMsg.getMsg() 
										+ " foi encontrado na maquina: " + ip + ":" + port;
										mensagem ans = new mensagem(response, "response", queryOrMsg.getOrgIp(), 
												queryOrMsg.getOrgPort(), ip, port, queryOrMsg.getOrgIp(), 
													queryOrMsg.getOrgPort());
										envia(ans);
									}
//									Caso não seja encontrado o arquivo no peer em questão, a query é repassada
//									para o(s) peer(s) vizinho(s) que não são o peer que enviou a solicitação
									else {
										System.out.println("Nao tenho o arquivo: " + queryOrMsg.getMsg());
										if (queryOrMsg.getRemIp().equals(ip1)) {
											mensagem peer2 = new mensagem(queryOrMsg.getMsg(), "search", ip2, 
													port2, ip, port, queryOrMsg.getOrgIp(), queryOrMsg.getOrgPort());
											envia(peer2);
//										Caso a query tenha vindo pelo ip1, o peer envia para o ip2,
//										e vice-versa
										}
										else {
											mensagem peer1 = new mensagem(queryOrMsg.getMsg(), "search", ip1, 
													port1, ip, port, queryOrMsg.getOrgIp(), queryOrMsg.getOrgPort());
											envia(peer1);
										}
									}
								}
							}
						}
					}
				}
				catch (SocketException e) {
				e.printStackTrace();
				} catch (IOException e) {
				e.printStackTrace();
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (Exception e) {
				e.printStackTrace();
			}
		}	
	}
//	Este método realiza a busca, ele pede para que o peer insira o nome do arquivo,
//	em seguida sorteia um dos 2 peers para enviar a solicitação do arquivo
	public static String search(Scanner scan) throws Exception {
//		primeiramente, verifica-se se a pesquisa já foi realizada, ou se o arquivo está contido
//		no próprio peer
		pesquisar = scan.next();
		if (pesquisar.equals(repeatPesquisar) == false & searchInsidePeer() == false) {
			repeatPesquisar = pesquisar;
			Random random = new Random();
//			Sorteio do peer em que será enviada a solicitação
			int numero = random.nextInt(100);
			if (numero > 50) {
				mensagem peer1 = new mensagem(pesquisar, "search", ip1, port1, ip, port, ip, port);
				envia(peer1);
				System.out.println("\nSolicitacao enviada!\n");
			}
			else {
				mensagem peer2 = new mensagem(pesquisar, "search", ip2, port2, ip, port, ip, port);
				envia(peer2);
				System.out.println("\nSolicitacao enviada!\n");
			}
		}
//		Caso a pesquisa já tenha sido feita, ou o peer tenha o arquivo solicitado, a pesquisa retornará
//		que o peer possui o arquivo ou que a query já foi realizada!
		else {
			if (searchInsidePeer() == true) {
				responseSaved = "O arquivo: " + pesquisar + " esta dentro do proprio peer!\n";
				System.out.println(responseSaved + "\n");
			}
			else {
				System.out.println("\nPesquisa ja realizada!\n");
				System.out.println(responseSaved + "\n");
			}
		}
		return pesquisar;
	}
//	Método para enviar a query ou mensagem
	public static void envia(mensagem msg) throws Exception {
		try (DatagramSocket clientSocket = new DatagramSocket()) {
			byte[] sendData = new byte[1024];
			String msgJson = new Gson().toJson(msg);
			sendData = msgJson.getBytes();
//			Os atributos da mensagem são verificados, as informações de 
//			IP e porta do destinatário são utilizadas para encaminhar a mensagem
//			através do Socket
			DatagramPacket sendPacket = new DatagramPacket(sendData, 
					sendData.length, msg.getDestIp(), msg.getDestPort());
			clientSocket.send(sendPacket);
		}
	}
//	Método para verificar os arquivos presentes na pasta do peer
	public static String[] verificaArquivos()throws Exception { 
		//método que cria a thread de verificação de arquivos na pasta
		File file = new File(path);
		File[] arquivos = file.listFiles();
		int i = 0;
		String [] lista = new String[15];
		for (File fileTmp : arquivos) {
			lista[i] = (fileTmp.getName());
			i++;
		   } //Aqui o método lista de antemão os arquivos e deixa armazenado numa lista "lista";
		return lista;
	}
//	Método para procurar um arquivo dentro do peer
	public static boolean searchInsidePeer() throws Exception {
		// Implementa a busca do arquivo solicitado para outros peers
		boolean exists = false;
		String[] lista = new String[15];
		lista = verificaArquivos();
		for(int i = 0; i<lista.length; i++) {
			if (lista[i] != null) {
				if(lista[i].equals(queryOrMsg.getMsg())) {
					exists = true;
					}	
				}
			}
		return exists;
	}
//	Thread responsável por enviar a atualização da lista de arquivos do peer
//	para os seus peers vizinhos
	public static class ThreadVerificaArquivos extends Thread {		
		public void run() {
			try {
				ThreadVerificaArquivos.sleep(1500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			while (true) {
				try {
					String[] arquivos = verificaArquivos();
					String list = "Sou o peer de IP: " + ip + " porta: " + port + "\nLista de arquivos atualizada:\n";
//					Concatena os arquivos na string de envio
					for (int i = 0; i < arquivos.length; i++) {
						if (arquivos[i] != null) {
							list = list + arquivos[i] + "\n";
						}
					}
					mensagem peer1 = new mensagem(list, "file verification", ip1, port1, ip, port, ip, port);
					envia(peer1);
					mensagem peer2 = new mensagem(list, "file verification", ip2, port2, ip, port, ip, port);
					envia(peer2);
//					Resetando as variáveis que salvam resposta e pesquisas repetidas
					repeatPesquisar = "null";
					repeatSearch = "null";
					repeatResponse = "null";
					responseSaved = "null";
//					30s de repouso da Thread
					ThreadVerificaArquivos.sleep(30000);
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		}
	}
//	Thread que é responsável por verificar se o arquivo foi encontrado através de Time Out
	public static class ThreadTime extends Thread {
		public void run() {
			long start = System.currentTimeMillis();
			long end = start + 2 * 1000; // tempo de time out: 2s
			encontrouAlgo = false;
//			Laço de 2 segundos que fica verificando se chegou uma resposta
//			e se a mensagem que chegou possui o arquivo desejado
			while (System.currentTimeMillis() < end) {
				if ((queryOrMsg.getTipo().equals("response") & 
						queryOrMsg.getOrgIp().equals(ip)) |
							responseSaved.contains("esta dentro do proprio peer") |
								responseSaved.contains("foi encontrado na maquina:")) {
					encontrouAlgo = true;
				}
			}
			if (encontrouAlgo == false) {
				responseSaved = "O arquivo: " + pesquisar + " nao foi encontrado no sistema!\n";
				System.out.println(responseSaved + "\n");
			}
		}
	}
}
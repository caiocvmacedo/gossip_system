Comandos a serem rodados no prompt para iniciar a aplicação peer

D:
cd D:\arquivos\Documentos-HD\Scripts\Eclipse\gossip\src

Compilando a classe mensagem:
javac gossip/mensagem.java
Compilando a classe peer:
javac -cp .;lib/gson-2.9.1.jar; gossip/peer.java

Executando aplicação
java -cp .;lib/gson-2.9.1.jar; gossip/peer

Inserir IPs e ports
Inserir caminho, caminhos das pastas:
D:\arquivos\Documentos-HD\ScienceContent\Sistemas_distribuidos\pasta_de_trabalho_EP1\peer1
D:\arquivos\Documentos-HD\ScienceContent\Sistemas_distribuidos\pasta_de_trabalho_EP1\peer2
D:\arquivos\Documentos-HD\ScienceContent\Sistemas_distribuidos\pasta_de_trabalho_EP1\peer3
D:\arquivos\Documentos-HD\ScienceContent\Sistemas_distribuidos\pasta_de_trabalho_EP1\peer4

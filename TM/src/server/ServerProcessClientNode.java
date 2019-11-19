package server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import Utils.Utils;

public class ServerProcessClientNode extends Thread {

    

    private DatagramSocket serverSocket;

    private DatagramPacket receivePacket;

    ServerProcessClientNode(DatagramSocket serverSocket, DatagramPacket receivePacket) {
        this.serverSocket = serverSocket;
        this.receivePacket = receivePacket;
    }

    public void run() {
        try {
            String mensagem = new String(receivePacket.getData());
            InetAddress addressIP = receivePacket.getAddress();
            Integer port = receivePacket.getPort();
            System.out.println("PEDIDO DO: " + addressIP.getHostAddress() + ":" + port + " - DATA: " + mensagem);
            Actions action = Utils.retornarAcaoDaMensagem(mensagem);
            switch (action) {
                case SEND_VIDEO:
                    enviarVideoParaCliente(mensagem, serverSocket, addressIP, preferences.getPort());
                    break;
                
              
            }
        } catch (IOException | NullPointerException e) {
           System.out.println("ERRO");
        }
    }

   
    private void enviarArquivoParaCliente(String mensagem, DatagramSocket serverSocket, InetAddress iPAddress, Integer port) {
        int idVideo = Utils.retornarIdVideo(mensagem);
        new ServerSendFile(nodeClient, nodeClient.getPortFileTransfer(), idVideo).start();
    }
   

}

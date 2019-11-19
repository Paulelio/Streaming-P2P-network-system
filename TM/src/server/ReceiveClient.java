package server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class ReceiveClient extends Thread {


    private DatagramSocket serverSocket;


    private Boolean running = Boolean.TRUE;

    ReceiveClient(DatagramSocket serverSocket ) {
        this.serverSocket = serverSocket;
    }

    public void run() {
       System.out.print("Iniciado Thread Server Receive");
        try {
            while (running) {
                byte[] receiveData = new byte[1024];
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                serverSocket.receive(receivePacket);
                new ServerProcessClientNode(serverSocket, receivePacket).start();
            }
        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
        }
    }

    public void setRunning(Boolean running) {
        this.running = running;
    }
}

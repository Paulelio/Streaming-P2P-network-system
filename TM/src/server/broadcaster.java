package server;


import java.net.DatagramSocket;


public class broadcaster {


    private ReceiveClient ReceiveClient;

    public broadcaster( DatagramSocket serverSocket) {
        this.ReceiveClient = new ReceiveClient(serverSocket);
    }

    public void start() {
    	ReceiveClient.start();
    }

    public void stop() {
    	ReceiveClient.setRunning(Boolean.FALSE);
    }


}
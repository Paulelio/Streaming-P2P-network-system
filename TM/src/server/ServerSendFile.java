package server;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import cliente.ClientNode;

import java.io.OutputStream;


public class ServerSendFile extends Thread {


    private Integer port;

    private int idVideo;

    ServerSendFile(ClientNode cn, Integer port, int idVideo) {
        this.port = port;
        this.idVideo = idVideo;
    }

    public void run() {
        System.out.println("Iniciado Thread Server Send File");
        try {
            
            ServerSocket serverSocket = new ServerSocket(port);
            Socket socketClient = serverSocket.accept();

            
            Video video = Video.getVideoById(idVideo);
            BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);

            
            OutputStream os = socketClient.getOutputStream();

           //para melhorar
            os.write(video);
            System.out.println("Sending diveo ... ");
           
            os.flush();
            
            socketClient.close();
            serverSocket.close();
            System.out.println("File sent succesfully!");
        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
        }
    }
}

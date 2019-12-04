package Client;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.List;
import java.util.Scanner;

import org.apache.zookeeper.KeeperException;

import Server.Source;
import Zookeeper.ZKManager;

public class Client {
	
	private ZKManager zoo;

	private int id;
	private int sourceId;
	
	private String ip;
	private int port;
	
	private DatagramSocket socket;
	private List<String> sourceNodes;
	private Scanner scan;
	
	public Client() {
		initClientNode();
		
	}

	/**
	 * 
	 */
	private void initClientNode() {
		
		try {
			zoo = new ZKManager();
			scan = new Scanner(System.in);
			
			this.ip = InetAddress.getLocalHost().getHostAddress();
			this.port = 0;
			
			sourceNodes = zoo.listGroupChildren(Source.SOURCE_FOLDER_PATHNAME);
			int nSources = sourceNodes.size();
			
			System.out.print("Escolha a live que deseja ver:");
			for (int i = 1; i <= nSources; i++) {
				System.out.print(i+" ");
			}
			System.out.println("");
			boolean correct = false;
			while(!correct) {
				
				sourceId = scan.nextInt();
				if (sourceId <= nSources && sourceId > 0) {
					correct = true;
				}
				else {
					System.out.println("Introuza um número válido");
				}
			}
			byte[] data = (ip+":"+port).getBytes();
			
			zoo.joinGroup(Source.SOURCE_NODE_PATHNAME + sourceId, "client" + id, data, true);
			
		} catch (IOException | InterruptedException | KeeperException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}

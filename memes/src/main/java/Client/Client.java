package Client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.data.Stat;

import Server.Source;
import Zookeeper.ZKManager;

public class Client {
	private List<String> path;

	private ZKManager zoo;

	private int id;
	private int sourceId;

	private String ip;
	private int port;

	private DatagramSocket socket;
	private List<String> sourceNodes;
	private Scanner scan;

	private int currFrame;

	private byte[] data = new byte[256];
	
	public Client() {
		path = new ArrayList<>();
		initClientNode();
		receivePackets();
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
					System.out.println("Introuza um n�mero v�lido");
				}
			}
			
			byte[] data = (ip+":"+port).getBytes();
			String ogPath = Source.SOURCE_NODE_PATHNAME + sourceId;

			
			String joinGroupResponse = zoo.joinGroup( ogPath, "client" + id, data, true, false);
			System.out.println("memes" + joinGroupResponse);
			
			if (!joinGroupResponse.contains(":")) {
				path.add(joinGroupResponse);
			}
			
			else {
				int parents = 0;
				List<String> possibleParents = new ArrayList<>();
				Random r = new Random();
				String [] sourceChildren = joinGroupResponse.split(":")[1].split(",");
				
				updateList(sourceChildren, ogPath, possibleParents);
				
				while (parents < 3) {
					int rand = r.nextInt(possibleParents.size());
					String group = possibleParents.get(rand);
					
					String joinSourceChildrenResponse = zoo.joinGroup(group, "client" + id, data, true, false);
										
					if (joinSourceChildrenResponse.split(":")[0] != "Full") {
						path.add(joinGroupResponse);
						parents ++;
					}
					
					else {
						String[] children = joinSourceChildrenResponse.split(":")[1].split(",");

						updateList(children, group, possibleParents);
						
					}
					possibleParents.remove(rand);
				}
			}
			
			currFrame = 0;

		} catch (IOException | InterruptedException | KeeperException e) {
			
			e.printStackTrace();
		}

	}
	
	private static void updateList(String[] names, String initial, List<String> update){
		for (String string : names) {
			update.add(initial+"/"+string);
		}
	}
	
	private void receivePackets() {
		try {

			socket = new DatagramSocket();
			while(true) {
				DatagramPacket rPack = new DatagramPacket(data, data.length);
				socket.receive(rPack);

				String msg = new String(rPack.getData(), 0, rPack.getLength());
				System.out.println(msg);

//				if(zoo.listGroupChildren(path + "/client"+id).size() > 0) {
//					byte[] sendData = msg.getBytes();
//
//					DatagramPacket sPack = new DatagramPacket(sendData,sendData.length);
//				}
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}

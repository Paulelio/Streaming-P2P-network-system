package Client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.Timer;

import org.apache.commons.lang3.StringUtils;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.data.Stat;

import Server.Source;
import Server.VerifyTask;
import Zookeeper.ZKManager;

public class Client {
	private List<String> path;
	private List<String> view;
	
	private ZKManager zoo;

	private int id;
	private int sourceId;

	private String ip;
	private int port;

	private DatagramSocket socket;
	private List<String> sourceNodes;
	private Scanner scan;

	private int currFrame;
	private int[] frames = new int[5];

	private byte[] data = new byte[256];
	
	public Client() {
		path = new ArrayList<>();
		initClientNode();
		Timer t = new Timer();
		VerifyClient v = new VerifyClient(this);
		t.schedule(v, 10000);
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
			this.port = 4000;

			sourceNodes = zoo.listGroupChildren(Source.SOURCE_FOLDER_PATHNAME);
			int nSources = sourceNodes.size();

			System.out.println("Escolha a live que deseja ver: ");
			for (String s : sourceNodes) {
				System.out.print(s+" ");
			}
			
			System.out.println("");
			boolean correct = false;
			while(!correct) {
				sourceId = scan.nextInt();
				
				if (sourceId <= nSources && sourceId > 0) {
					correct = true;
				}
				
				else {
					System.out.println("Introduza um numero valido");
				}
			}
			
			byte[] data = (ip+":"+port).getBytes();
			String ogPath = "source"+getServiceNumberFromPath(sourceNodes.get(--sourceId));

			
			String joinGroupResponse = zoo.joinGroup( ogPath, "client" + id, data, true, false);
			
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
			
			socket = new DatagramSocket(4000);
			while(true) {
				
				DatagramPacket rPack = new DatagramPacket(data, data.length);
				socket.receive(rPack);
				
				String msg = new String(rPack.getData(), 0, rPack.getLength());
				System.out.println(msg);

				byte[] data = msg.getBytes();
				DatagramPacket sPack = new DatagramPacket(data, data.length);
				
				if(view != null) {
					for (String child : view) {
						String[] member = child.split("/");
						String[] info = member[member.length-1].split(":");
						InetAddress add = InetAddress.getByName(info[0]);
						sPack.setAddress(add);
						sPack.setPort(Integer.valueOf(info[1]));
						try {
							socket.send(sPack);
						}catch(IOException e) {
							continue;
						}
					}
				}
				
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	
	public List<String> getPath(){
		return path;
	}

	public void updateClientData(List<String> clientData) {
		view = new ArrayList<>(clientData);
		
	}
	
	public static int getServiceNumberFromPath(String path) {
		String numberStg = StringUtils.substringAfterLast(path,("source-"));
		return Integer.parseInt(numberStg);
	}

}

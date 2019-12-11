package Client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collections;
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

	private InetAddress ip;
	private int port;
	
	private Timer t;

	private DatagramSocket socket;
	private List<String> sourceNodes;
	private Scanner scan;

	private boolean firstFrame = true;
	private int lastFrame;
	private ArrayList<Integer> frames;

	private byte[] data = new byte[256];
	
	public Client() {
		path = new ArrayList<>();
		initClientNode();
		t = new Timer();
		VerifyClient v = new VerifyClient(this);
		t.schedule(v, 5000);
		receivePackets();
	}

	/**
	 * 
	 */
	private void initClientNode() {
		try {
			zoo = new ZKManager();
			scan = new Scanner(System.in);

			//get local address
			try(final DatagramSocket socket = new DatagramSocket()){
				  socket.connect(InetAddress.getByName("8.8.8.8"), 10002);
				  this.ip = InetAddress.getByName(socket.getLocalAddress().getHostAddress());
			}
			
			//get port
			Random rPort = new Random();
			this.port = rPort.nextInt(400) + 4000;

			System.out.println("IP - " + this.ip);
			System.out.println("Port - " + this.port);
			
			//list sources
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

			
			String joinGroupResponse = zoo.joinGroup(ogPath, "client" + this.port, data, true, false, false);
			
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
					
					String joinSourceChildrenResponse = zoo.joinGroup(group, "client" + id, data, true, false, false);
										
					if (joinSourceChildrenResponse.split(":")[0] != "Full") {
						path.add(group);
						parents ++;
					}
					
					else {
						String[] children = joinSourceChildrenResponse.split(":")[1].split(",");
						updateList(children, group, possibleParents);
						
					}
					possibleParents.remove(rand);
				}
			}
			
			lastFrame = 0;
			frames = new ArrayList<>();
			System.out.println(path.toString());

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
			socket = new DatagramSocket(this.port);
			while(true) {
				DatagramPacket rPack = new DatagramPacket(data, data.length);
				socket.receive(rPack);
				
				String msg = new String(rPack.getData(), 0, rPack.getLength());
				
				int currentFrame = Integer.valueOf(msg.split(":")[2]);
				
				if (firstFrame) {
					firstFrame = false;
					System.out.println(msg);					
					lastFrame = currentFrame;					
				}
				
				if (currentFrame == lastFrame+1) {
					System.out.println(msg);					
					lastFrame = currentFrame;
				}
				
				if (frames.size() == 5)
					frames.remove(0);
				
				frames.add(currentFrame);
				
				Collections.sort(frames);
				
				byte[] data = msg.getBytes();
				DatagramPacket sPack = new DatagramPacket(data, data.length);
				
				if(view != null) {
					System.out.println(view.toString());
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
	
	public void resetTimer() {
		t.cancel();
		t = new Timer();
		VerifyClient v = new VerifyClient(this);
		t.schedule(v, 5000);
	}
}

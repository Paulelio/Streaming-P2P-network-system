package Server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.data.Stat;

import Zookeeper.ZKManager;

public class Source {
	public static final String SOURCE_FOLDER_PATHNAME = "source_folder";
	public static final String SOURCE_NODE_PATHNAME = "source";
	
	private ZKManager zoo;
	private List<String> view;
	private DatagramSocket socket;
	
	private int id;
	private String ip;
	private int port;
	
	public Source() {
		initSourceNode();
		Timer t = new Timer();
		VerifyTask v = new VerifyTask(this);
		t.schedule(v, 10000);
		broadcastPackets();
	}

	private void initSourceNode() {
		try {
			//creates a Manager instance
			zoo = new ZKManager();
			
			//checks if the source folder exists
			Stat s = zoo.znode_exists(SOURCE_FOLDER_PATHNAME, false);
			//if not, creates one
			if(s == null) {
				zoo.createGroup(SOURCE_FOLDER_PATHNAME, false);
			}
			
			this.ip = InetAddress.getLocalHost().getHostAddress();
			this.port = 0;
			
			byte[] data = (ip+":"+port).getBytes();
			//joins source folder 
			zoo.joinGroup(SOURCE_FOLDER_PATHNAME, SOURCE_NODE_PATHNAME, data, false);
			
			//creates its own folder for network management
			this.id = zoo.listGroupChildren(SOURCE_FOLDER_PATHNAME).size();
			zoo.createGroup(SOURCE_NODE_PATHNAME + id, true);
			
		} catch (IOException | InterruptedException | KeeperException e) {
			
			e.printStackTrace();
		}
	}
	
	private void broadcastPackets()  {
		try {
			socket = new DatagramSocket();
			int frame = 0;
			
			while(true) {
				String msg = "Source Node:"+id+" frame nº:"+frame;
				 byte[] data = msg.getBytes();
				 DatagramPacket pack = new DatagramPacket(data, data.length);
				 for (String child : view) {
					String[] member = child.split("/");
					String[] info = member[member.length-1].split(":");
					InetAddress add = InetAddress.getByName(info[0]);
					pack.setAddress(add);
					pack.setPort(Integer.valueOf(info[1]));
					socket.send(pack);
				}
			}

		} catch (IOException e) {
			
			e.printStackTrace();
		}
	}

	public int getID() {
		return this.id;
	}

	public void updateClientData(List<String> clientData) {
		view = new ArrayList<>(clientData);
	}
}

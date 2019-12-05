package Client;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;

import org.apache.zookeeper.KeeperException;

import Zookeeper.ZKManager;

public class VerifyClient extends TimerTask {

	private Client client;
	private ZKManager zoo;

	public VerifyClient(Client client) {
		this.client = client;
	}

	@Override
	public void run() {
		try {
			this.zoo = new ZKManager();
			List<String> clientPath = client.getPath();

			int index = 0;
			boolean encontrado = false;
			String path = "";
			
			while(index < clientPath.size() && !encontrado) {
				if(clientPath.get(index) != null)
					path = clientPath.get(index);	
			}
			
			List<String> children = zoo.listGroupChildren(path);
			List<String> clientData =  new ArrayList<>();
			
			for (String string : children) {
				clientData.add((String) zoo.getZNodeData(path, string, null));
			}
			
			client.updateClientData(clientData);
		}catch(IOException | InterruptedException | KeeperException e){
			e.printStackTrace();
		}
	}
}

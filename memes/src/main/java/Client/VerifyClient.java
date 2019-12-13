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
			List<String> sourceNodeNames = client.getPath();
			List<String> allClientData = new ArrayList<>();
			
			for (String sourceName : sourceNodeNames) {
				List<String> children = zoo.listGroupChildren(sourceName.substring(1));
				
				List<String> clientData = new ArrayList<>();
				
				for (String child : children) {
					clientData.add((String) zoo.getZNodeData(sourceName.substring(1), child, null));
				}
				
				allClientData.addAll(clientData);
			}
			
			client.updateClientData(allClientData);
			client.resetTimer();
			
		} catch (IOException | InterruptedException | KeeperException e) {
			client.resetTimer();
		}
	}
}

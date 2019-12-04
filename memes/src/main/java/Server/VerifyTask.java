package Server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;

import org.apache.zookeeper.KeeperException;

import Zookeeper.ZKManager;

public class VerifyTask extends TimerTask {
	private Source sc;
	private ZKManager zoo;
	
	public VerifyTask(Source source) {
		this.sc = source;
		
	}

	@Override
	public void run() {
		try {
			
			this.zoo = new ZKManager();
			String sourceNodeName = sc.SOURCE_NODE_PATHNAME+sc.getID();
			List<String> children = zoo.listGroupChildren(sourceNodeName);
			List<String> clientData = new ArrayList<>();
			
			for (String child : children) {
				clientData.add((String) zoo.getZNodeData(sourceNodeName, child, null));
			}
			
			sc.updateClientData(clientData);
		} catch (IOException | InterruptedException | KeeperException e) {
			
			e.printStackTrace();
		}
		
	}
}

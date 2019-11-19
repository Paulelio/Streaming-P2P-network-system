package Utils;

import server.Actions;

public class Utils {
	 public static Actions retornarAcaoDaMensagem(String mensagem) {
	        if (mensagem.contains(" ")) {
	            String[] msg = mensagem.split(" ");
	            return Actions.valueOf(msg[0].trim().toUpperCase());
	        }
	        return Actions.valueOf(mensagem.trim().toUpperCase());
	    }

	    public static int retornarIdVideo(String mensagem) {
	        if (mensagem.contains(" ")) {
	            String[] msg = mensagem.split(" ");
	            return Integer.parseInt(msg[1].trim());
	        }
	        return -1;
	    }
}

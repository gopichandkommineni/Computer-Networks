import java.io.IOException;

import bittorrent.main.*;

public class peerProcess {

	private static boolean simulate = false;

	public static void main (String args[]) {
		String[] peers = new String[] {"1001", "1002", "1003"};
		PeerProcessExecutor peerProcessExecutor = null;

		for (String arg: args){
			if (arg.equals("-s")){
				simulate = true;
			}
		}

		if (simulate) {
			for (String peerId: peers) {
				Thread t = new Thread(new PeerProcessExecutor(peerId));
				t.start();
			}
		}
		else {
			if (args.length > 0) {
				peerProcessExecutor = new PeerProcessExecutor(args[0]);
			} else {
				//default value
				peerProcessExecutor = new PeerProcessExecutor("1001");
			}
			peerProcessExecutor.init();
		}
	}
}

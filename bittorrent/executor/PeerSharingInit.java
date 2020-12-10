package bittorrent.executor;

import bittorrent.state.BitTorrentStatus;
import bittorrent.state.PeerStatus;
import bittorrent.conn.InboundConnectionManager;
import bittorrent.conn.PeerConnectionManager;
import bittorrent.selector.OptimisticUnchokingSelector;
import bittorrent.selector.PreferredNeighborsSelector;
import bittorrent.ops.FileOps;
import bittorrent.ops.Logger;

import java.net.Socket;
import java.util.Map;
import java.util.Timer;
import java.util.concurrent.ConcurrentHashMap;

public class PeerSharingInit implements Runnable{
	private PeerStatus peerState;
	private Logger logger;

	public PeerSharingInit(String peerId) {
		BitTorrentStatus.loadPeerStatusFromConfig();
		this.peerState = BitTorrentStatus.findPeerStatus(peerId);
		this.logger = Logger.fetchLogger(peerId);
	}

	public void init() {
		FileOps.createFilesAndDirectories(this.peerState.getPeerId());
		if (peerState.isHasSharedFile()) {
			System.out.println("Shared file found with :"+ peerState.getPeerId());
			this.peerState.assignFilePieceIndexMap(FileOps.divideFile());
		}
		else {
			this.peerState.assignFilePieceIndexMap(new ConcurrentHashMap<>());
		}
		System.out.println("Peer ID :"+ peerState.getPeerId());
		BitTorrentStatus.displayConfig();
		System.out.println(peerState);

		// accept incoming connections
		Thread t = new Thread(new InboundConnectionManager(peerState));
		t.start();

		// create outgoing connections
		createOutgoingConnections();

		// Periodically select preferred neighbors for this peer
		Timer timer1 = new Timer();
		PreferredNeighborsSelector preferredNeighborsScheduler = new PreferredNeighborsSelector(peerState);
		timer1.scheduleAtFixedRate(preferredNeighborsScheduler, 200, BitTorrentStatus.findUnchokingInterval() * 1000);
		this.peerState.setTimer1(timer1);

		// Start OptimisticUnchokedPeerScheduler
		Timer timer2 = new Timer();
		OptimisticUnchokingSelector optimisticUnchokingScheduler = new OptimisticUnchokingSelector(peerState);
		timer2.scheduleAtFixedRate(optimisticUnchokingScheduler, 500, BitTorrentStatus.findOptUnchokingInterval() * 1000);
		this.peerState.setTimer2(timer2);

		try {
			t.join();
			System.out.println(this.peerState.getPeerId() + ": Exiting PeerSharingInit");
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public void run() {
		init();
	}

	public void createOutgoingConnections() {

		Map<String, PeerStatus> peers = BitTorrentStatus.findPeers();

		int currentSeqId = this.peerState.getSequenceNum();

		for (PeerStatus remotePeer : peers.values()) {

			if (currentSeqId > remotePeer.getSequenceNum()) {

				try {
					logger.establishingConnectionTo(remotePeer.getPeerId());
					Socket clientSocket = new Socket(remotePeer.getHostName(), remotePeer.getPort());
					PeerConnectionManager peerConnectionHandler = new PeerConnectionManager(clientSocket, peerState);
					peerConnectionHandler.assignRemotePeerId(remotePeer.getPeerId());
					peerState.conList().put(remotePeer.getPeerId(), peerConnectionHandler);
					Thread t = new Thread(peerConnectionHandler);
					t.start();
				} catch (Exception e) {
					e.printStackTrace();
				}

			}
		}
	}


}

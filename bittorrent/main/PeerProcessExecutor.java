package bittorrent.main;

import bittorrent.dtos.BitTorrentState;
import bittorrent.dtos.PeerState;
import bittorrent.handlers.IncomingConnectionHandler;
import bittorrent.handlers.PeerConnectionHandler;
import bittorrent.scheduler.OptimisticUnchokingScheduler;
import bittorrent.scheduler.PreferredNeighborsScheduler;
import bittorrent.utils.FileUtils;
import bittorrent.utils.Logger;

import java.net.Socket;
import java.util.Map;
import java.util.Timer;
import java.util.concurrent.ConcurrentHashMap;

public class PeerProcessExecutor implements Runnable{
	private PeerState peerState;
	private Logger logger;

	public PeerProcessExecutor(String peerId) {
		BitTorrentState.loadPeerStateFromConfig();
		this.peerState = BitTorrentState.findPeerStatus(peerId);
		this.logger = Logger.fetchLogger(peerId);
	}

	public void init() {
		FileUtils.createFilesAndDirectories(this.peerState.getPeerId());
		if (peerState.isHasSharedFile()) {
			System.out.println("Shared file found with :"+ peerState.getPeerId());
			this.peerState.assignFilePieceIndexMap(FileUtils.divideFile());
		}
		else {
			this.peerState.assignFilePieceIndexMap(new ConcurrentHashMap<>());
		}
		System.out.println("Peer ID :"+ peerState.getPeerId());
		BitTorrentState.displayConfig();
		System.out.println(peerState);

		// accept incoming connections
		Thread t = new Thread(new IncomingConnectionHandler(peerState));
		t.start();

		// create outgoing connections
		createOutgoingConnections();

		// Periodically select preferred neighbors for this peer
		Timer timer1 = new Timer();
		PreferredNeighborsScheduler preferredNeighborsScheduler = new PreferredNeighborsScheduler(peerState);
		timer1.scheduleAtFixedRate(preferredNeighborsScheduler, 200, BitTorrentState.findUnchokingInterval() * 1000);
		this.peerState.setTimer1(timer1);

		// Start OptimisticUnchokedPeerScheduler
		Timer timer2 = new Timer();
		OptimisticUnchokingScheduler optimisticUnchokingScheduler = new OptimisticUnchokingScheduler(peerState);
		timer2.scheduleAtFixedRate(optimisticUnchokingScheduler, 500, BitTorrentState.findOptUnchokingInterval() * 1000);
		this.peerState.setTimer2(timer2);

		try {
			t.join();
			System.out.println(this.peerState.getPeerId() + ": Exiting PeerProcessExecutor");
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public void run() {
		init();
	}

	public void createOutgoingConnections() {

		Map<String, PeerState> peers = BitTorrentState.findPeers();

		int currentSeqId = this.peerState.getSequenceNum();

		for (PeerState remotePeer : peers.values()) {

			if (currentSeqId > remotePeer.getSequenceNum()) {

				try {
					logger.establishingConnectionTo(remotePeer.getPeerId());
					Socket clientSocket = new Socket(remotePeer.getHostName(), remotePeer.getPort());
					PeerConnectionHandler peerConnectionHandler = new PeerConnectionHandler(clientSocket, peerState);
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

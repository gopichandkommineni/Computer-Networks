package com.bittorrent.main;

import com.bittorrent.dtos.BitTorrentState;
import com.bittorrent.dtos.PeerState;
import com.bittorrent.handlers.IncomingConnectionHandler;
import com.bittorrent.handlers.PeerConnectionHandler;
import com.bittorrent.scheduler.OptimisticUnchokingScheduler;
import com.bittorrent.scheduler.PreferredNeighborsScheduler;
import com.bittorrent.utils.FileUtils;
import com.bittorrent.utils.Logger;

import java.net.Socket;
import java.util.Map;
import java.util.Timer;
import java.util.concurrent.ConcurrentHashMap;

public class PeerProcessExecutor implements Runnable{
	private PeerState peerState;
	private Logger logger;

	public PeerProcessExecutor(String peerId) {
		BitTorrentState.setStateFromConfigFiles();
		this.peerState = BitTorrentState.getPeerState(peerId);
		this.logger = Logger.getLogger(peerId);
	}

	public void init() {
		FileUtils.makeFilesAndDirectories(this.peerState.getPeerId());
		if (peerState.isHasSharedFile()) {
			System.out.println("Shared file found with :"+ peerState.getPeerId());
			this.peerState.assignFilePieceIndexMap(FileUtils.splitFile());
		}
		else {
			this.peerState.assignFilePieceIndexMap(new ConcurrentHashMap<>());
		}
		System.out.println("Peer ID :"+ peerState.getPeerId());
		BitTorrentState.showConfiguration();
		System.out.println(peerState);

		// accept incoming connections
		Thread t = new Thread(new IncomingConnectionHandler(peerState));
		t.start();

		// create outgoing connections
		createOutgoingConnections();

		// Periodically select preferred neighbors for this peer
		Timer timer1 = new Timer();
		PreferredNeighborsScheduler preferredNeighborsScheduler = new PreferredNeighborsScheduler(peerState);
		timer1.scheduleAtFixedRate(preferredNeighborsScheduler, 200, BitTorrentState.getUnchokingInterval() * 1000);
		this.peerState.setTimer1(timer1);

		// Start OptimisticUnchokedPeerScheduler
		Timer timer2 = new Timer();
		OptimisticUnchokingScheduler optimisticUnchokingScheduler = new OptimisticUnchokingScheduler(peerState);
		timer2.scheduleAtFixedRate(optimisticUnchokingScheduler, 500, BitTorrentState.getOptimisticUnchokingInterval() * 1000);
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

		Map<String, PeerState> peers = BitTorrentState.getPeers();

		int currentSeqId = this.peerState.getSequenceNum();

		for (PeerState remotePeer : peers.values()) {

			if (currentSeqId > remotePeer.getSequenceNum()) {

				try {
					logger.logTcpConnectionTo(remotePeer.getPeerId());
					Socket clientSocket = new Socket(remotePeer.getHostName(), remotePeer.getPort());
					PeerConnectionHandler peerConnectionHandler = new PeerConnectionHandler(clientSocket, peerState);
					peerConnectionHandler.setRemotePeerId(remotePeer.getPeerId());
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

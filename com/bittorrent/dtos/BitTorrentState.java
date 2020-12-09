package com.bittorrent.dtos;

import com.bittorrent.utils.PropertiesEnum;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class BitTorrentState {

	public static int numberOfPieces;
	public static int numberOfPreferredNeighbors;
	public static int unchokingInterval;
	public static int optimisticUnchokingInterval;
	public static String fileName;
	public static long fileSize;
	public static int pieceSize;

	private static ConcurrentHashMap<String, PeerState> peers = new ConcurrentHashMap<>();

	public static PeerState findPeerStatus(String id) {
		return peers.get(id);
	}

	public static Map<String, PeerState> findPeers() {
		return peers;
	}

	public static int peerCount() {
		return peers.size();
	}

	public static int pieceCount() {
		return numberOfPieces;
	}

	public static void initPieceCount(int numberOfPieces) {
		BitTorrentState.numberOfPieces = numberOfPieces;
	}

	public static int findNumPreferPeers() {
		return numberOfPreferredNeighbors;
	}

	public static void assignNumPreferPeers(int numberOfPreferredNeighbors) {
		BitTorrentState.numberOfPreferredNeighbors = numberOfPreferredNeighbors;
	}

	public static int findUnchokingInterval() {
		return unchokingInterval;
	}

	public static void assignUnchokingInterval(int unchokingInterval) {
		BitTorrentState.unchokingInterval = unchokingInterval;
	}

	public static int findOptUnchokingInterval() {
		return optimisticUnchokingInterval;
	}

	public static void assignOptimisticUnchokingInterval(int optimisticUnchokingInterval) {
		BitTorrentState.optimisticUnchokingInterval = optimisticUnchokingInterval;
	}

	public static String findFile() {
		return fileName;
	}

	public static void setFileName(String fileName) {
		BitTorrentState.fileName = fileName;
	}

	public static long findSizeOfTheFile() {
		return fileSize;
	}

	public static void assignSizeOfTheFile(long fileSize) {
		BitTorrentState.fileSize = fileSize;
	}

	public static int findPieceLength() {
		return pieceSize;
	}

	public static void assignPieceLength(int pieceSize) {
		BitTorrentState.pieceSize = pieceSize;
	}

	public static void calAndAssignPieceCount() {
		numberOfPieces = (int)Math.ceil((double)fileSize / pieceSize);
		System.out.println("BitTorrent current state - Number of pieces: " + numberOfPieces);
	}

	public static String findPathOfLogFile() {
		return System.getProperty("user.dir") + File.separatorChar
		+ "project/log_peer_";
	}

	public static String findPeerLogExt() {
		return ".log";
	}

	public static void createPeerMap() {
		Scanner sc = null;
		int seq = 1;
		try {
			sc = new Scanner(new File(System.getProperty("user.dir") + File.separatorChar
			+ "PeerInfo.cfg"));
			while (sc.hasNextLine()) {
				String arr[] = sc.nextLine().split(" ");
				PeerState peer = new PeerState();
				peer.assignSequenceNum(seq++);
				peer.assignPeerId(arr[0]);
				peer.assignHostName(arr[1]);
				peer.assignPort(Integer.parseInt(arr[2]));
				if (arr[3].equals("1")) {
					peer.containsFile(true);
				}
				else {
					peer.containsFile(false);
				}
				peers.put(arr[0], peer);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		finally {
			sc.close();
		}
	}

	public static void displayConfig() {
		System.out.println( "PeerProperties [numberOfPreferredNeighbors=" + numberOfPreferredNeighbors);
		System.out.println(", unchokingInterval="+ unchokingInterval);
		System.out.println(", optimisticUnchokingInterval="+ optimisticUnchokingInterval);
		System.out.println(", fileName="+ fileName);
		System.out.println(", fileSize="+ fileSize);
		System.out.println(", pieceSize=" + pieceSize+ "]");
	}

	public static void loadPeerStateFromConfig() {

		Properties properties = new Properties();
		try {
			FileInputStream in = new FileInputStream(PropertiesEnum.COMMON_PROPERTIES_CONFIG_PATH.getValue());
			properties.load(in);
		}
		catch (IOException ex) {
			throw new RuntimeException("File not found : " + ex.getMessage());
		}

		fileName = properties.get("FileName").toString();
		fileSize = Long.parseLong(properties.get("FileSize").toString());
		numberOfPreferredNeighbors = 
				Integer.parseInt(properties.get("NumberOfPreferredNeighbors").toString());
		optimisticUnchokingInterval =
				Integer.parseInt(properties.get("OptimisticUnchokingInterval").toString());
		pieceSize = Integer.parseInt(properties.getProperty("PieceSize"));
		unchokingInterval =
				Integer.parseInt(properties.getProperty("UnchokingInterval"));
		calAndAssignPieceCount();

		System.out.println(System.getProperty("user.dir") + File.separatorChar);
		System.out.println(System.getProperty("user.dir") + File.separatorChar + BitTorrentState.fileName);

		createPeerMap();

	}

	public static synchronized boolean isFileDownloadedbyAll() {
		for (PeerState peerState: peers.values()) {
			if (peerState.getBitField().nextClearBit(0) != numberOfPieces) {
				System.out.println(peerState.getPeerId() + " has incomplete file, so not exiting");
				return false;
			}
		}
		return true;
	}


}
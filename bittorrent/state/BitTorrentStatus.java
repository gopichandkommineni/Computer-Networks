package bittorrent.state;

//import bittorrent.ops.PropertiesEnum;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class BitTorrentStatus {

	public static int numberOfPieces;
	public static int numberOfPreferredNeighbors;
	public static int unchokingInterval;
	public static int optimisticUnchokingInterval;
	public static String fileName;
	public static long fileSize;
	public static int pieceSize;

	private static ConcurrentHashMap<String, PeerStatus> peers = new ConcurrentHashMap<>();

	public static PeerStatus findPeerStatus(String id) {
		// returns the status of the Peer
		return peers.get(id);
	}

	public static Map<String, PeerStatus> findPeers() {
		// returns a map containing all the peers
		return peers;
	}

	public static int peerCount() {
		// returns the number of peers present for the user
		return peers.size();
	}

	public static int pieceCount() {
		// returns the total number of pieces present in the file
		return numberOfPieces;
	}

	public static void initPieceCount(int numberOfPieces) {
		// initialise the piece count
		BitTorrentStatus.numberOfPieces = numberOfPieces;
	}

	public static int findNumPreferPeers() {
		// returns the number of preferred neighbours
		return numberOfPreferredNeighbors;
	}

	public static void assignNumPreferPeers(int numberOfPreferredNeighbors) {
		// sets the number of preferred neighbours
		BitTorrentStatus.numberOfPreferredNeighbors = numberOfPreferredNeighbors;
	}

	public static int findUnchokingInterval() {
		// finds the unchocking interval for peers
		return unchokingInterval;
	}

	public static void assignUnchokingInterval(int unchokingInterval) {
		// sets the unchocking interval
		BitTorrentStatus.unchokingInterval = unchokingInterval;
	}

	public static int findOptUnchokingInterval() {
		// returns the optimistic unchocking interval
		return optimisticUnchokingInterval;
	}

	public static void assignOptimisticUnchokingInterval(int optimisticUnchokingInterval) {

		// sets the optimistic unchocking interval
		BitTorrentStatus.optimisticUnchokingInterval = optimisticUnchokingInterval;
	}

	public static String findFile() {
		//finds the file name
		return fileName;
	}

	public static void setFileName(String fileName) {
		// sets the file name
		BitTorrentStatus.fileName = fileName;
	}

	public static long findSizeOfTheFile() {
		// finds the file size
		return fileSize;
	}

	public static void assignSizeOfTheFile(long fileSize) {
		// set size of the file
		BitTorrentStatus.fileSize = fileSize;
	}

	public static int findPieceLength() {
		// return length of each piece
		return pieceSize;
	}

	public static void assignPieceLength(int pieceSize) {
		// set length of the piece
		BitTorrentStatus.pieceSize = pieceSize;
	}

	public static void calAndAssignPieceCount() {
		// calculate and set num of pieces
		numberOfPieces = (int)Math.ceil((double)fileSize / pieceSize);
		System.out.println("BitTorrent current state - Number of pieces: " + numberOfPieces);
	}

	public static String findPathOfLogFile() {
		// return path of log file
		return System.getProperty("user.dir") + File.separatorChar
		+ "project/log_peer_";
	}

	public static String findPeerLogExt() {

		// return log extension
		return ".log";
	}

	public static void createPeerMap() {
		// create map for storing peers as peer state
		Scanner sc = null;
		int seq = 1;
		try {
			sc = new Scanner(new File(System.getProperty("user.dir") + File.separatorChar
			+ "PeerInfo.cfg"));
			while (sc.hasNextLine()) {
				String arr[] = sc.nextLine().split(" ");
				PeerStatus peer = new PeerStatus();
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

		// print the properties of peers
		System.out.println( "PeerProperties [numberOfPreferredNeighbors=" + numberOfPreferredNeighbors);
		System.out.println(", unchokingInterval="+ unchokingInterval);
		System.out.println(", optimisticUnchokingInterval="+ optimisticUnchokingInterval);
		System.out.println(", fileName="+ fileName);
		System.out.println(", fileSize="+ fileSize);
		System.out.println(", pieceSize=" + pieceSize+ "]");
	}

	public static void loadPeerStatusFromConfig() {

		// load peers into map from config

		Properties properties = new Properties();
		try {
			FileInputStream in = new FileInputStream(System.getProperty("user.dir") + File.separatorChar
			+ "Common.cfg");
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
		System.out.println(System.getProperty("user.dir") + File.separatorChar + BitTorrentStatus.fileName);

		createPeerMap();

	}

	public static synchronized boolean isFileDownloadedbyAll() {

		// check if file is downloaded by all peers
		for (PeerStatus peerState: peers.values()) {
			if (peerState.getBitField().nextClearBit(0) != numberOfPieces) {
				System.out.println(peerState.getPeerId() + " has incomplete file, so not exiting");
				return false;
			}
		}
		return true;
	}


}
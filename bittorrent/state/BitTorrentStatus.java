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

		// gives the peer status
		return peers.get(id);
	}

	public static Map<String, PeerStatus> findPeers() {

		// gives a map with all the peers
		return peers;
	}

	public static int peerCount() {

		// gives the total number of peers available for the user
		return peers.size();
	}

	public static int pieceCount() {

		// gives the total number of pieces in the file
		return numberOfPieces;
	}

	public static void initPieceCount(int numberOfPieces) {

		// initialize the piece count
		BitTorrentStatus.numberOfPieces = numberOfPieces;
	}

	public static int findNumPreferPeers() {

		// gives the preferred neighbours count
		return numberOfPreferredNeighbors;
	}

	public static void assignNumPreferPeers(int numberOfPreferredNeighbors) {

		// sets the preferred neighbours count
		BitTorrentStatus.numberOfPreferredNeighbors = numberOfPreferredNeighbors;
	}

	public static int findUnchokingInterval() {

		// determine the unchocking time period for peers
		return unchokingInterval;
	}

	public static void assignUnchokingInterval(int unchokingInterval) {

		// sets the unchocking time period
		BitTorrentStatus.unchokingInterval = unchokingInterval;
	}

	public static int findOptUnchokingInterval() {

		// gives the optimistic unchocking time period
		return optimisticUnchokingInterval;
	}

	public static void assignOptimisticUnchokingInterval(int optimisticUnchokingInterval) {

		// sets the optimistic unchocking time period
		BitTorrentStatus.optimisticUnchokingInterval = optimisticUnchokingInterval;
	}

	public static String findFile() {

		// gives the file name
		return fileName;
	}

	public static void setFileName(String fileName) {

		// sets the file name
		BitTorrentStatus.fileName = fileName;
	}

	public static long findSizeOfTheFile() {

		// gives the file size
		return fileSize;
	}

	public static void assignSizeOfTheFile(long fileSize) {

		// sets the file size
		BitTorrentStatus.fileSize = fileSize;
	}

	public static int findPieceLength() {

		// gives size of every piece
		return pieceSize;
	}

	public static void assignPieceLength(int pieceSize) {

		// sets size of the piece
		BitTorrentStatus.pieceSize = pieceSize;
	}

	public static void calAndAssignPieceCount() {

		// compute and sets the number of pieces
		numberOfPieces = (int)Math.ceil((double)fileSize / pieceSize);
		System.out.println("BitTorrent current state - Number of pieces: " + numberOfPieces);
	}

	public static String findPathOfLogFile() {

		// gives log file path
		return System.getProperty("user.dir") + File.separatorChar
		+ "project/log_peer_";
	}

	public static String findPeerLogExt() {

		// gives .log extension
		return ".log";
	}

	public static void createPeerMap() {

		// puts peers as peer state in a map
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

		// prints the peer properties
		System.out.println( "PeerProperties [numberOfPreferredNeighbors=" + numberOfPreferredNeighbors);
		System.out.println(", unchokingInterval="+ unchokingInterval);
		System.out.println(", optimisticUnchokingInterval="+ optimisticUnchokingInterval);
		System.out.println(", fileName="+ fileName);
		System.out.println(", fileSize="+ fileSize);
		System.out.println(", pieceSize=" + pieceSize+ "]");
	}

	public static void loadPeerStatusFromConfig() {

		// parse config file and load peers to map

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

		// verify whether file is downloaded by all the peers
		for (PeerStatus peerState: peers.values()) {
			if (peerState.getBitField().nextClearBit(0) != numberOfPieces) {
				System.out.println(peerState.getPeerId() + " has incomplete file, so not exiting");
				return false;
			}
		}
		return true;
	}


}
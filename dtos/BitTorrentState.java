package com.bittorrent.dtos;

import com.bittorrent.utils.PropertiesEnum;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class BitTorrentState {

	public static int pieceCount;
	public static int preferredNeighbourCount;
	public static int unchokingInterval;
	public static int optimisticUnchokingInterval;
	public static String fileName;
	public static long fileSize;
	public static int pieceSize;

	private static ConcurrentHashMap<String, PeerState> peers = new ConcurrentHashMap<>();

	public static PeerState getPeerState(String id) {
		return peers.get(id);
	}

	public static Map<String, PeerState> getPeers() {
		return peers;
	}

	public static int numberOfPeers() {
		return peers.size();
	}

	public static int getpieceCount() {
		return pieceCount;
	}

	public static void setpieceCount(int pieceCount) {
		BitTorrentState.pieceCount = pieceCount;
	}

	public static int getpreferredNeighbourCount() {
		return preferredNeighbourCount;
	}

	public static void setpreferredNeighbourCount(int preferredNeighbourCount) {
		BitTorrentState.preferredNeighbourCount = preferredNeighbourCount;
	}

	public static int getUnchokingInterval() {
		return unchokingInterval;
	}

	public static void setUnchokingInterval(int unchokingInterval) {
		BitTorrentState.unchokingInterval = unchokingInterval;
	}

	public static int getOptimisticUnchokingInterval() {
		return optimisticUnchokingInterval;
	}

	public static void setOptimisticUnchokingInterval(int optimisticUnchokingInterval) {
		BitTorrentState.optimisticUnchokingInterval = optimisticUnchokingInterval;
	}

	public static String getFileName() {
		return fileName;
	}

	public static void setFileName(String fileName) {
		BitTorrentState.fileName = fileName;
	}

	public static long getFileSize() {
		return fileSize;
	}

	public static void setFileSize(long fileSize) {
		BitTorrentState.fileSize = fileSize;
	}

	public static int getPieceSize() {
		return pieceSize;
	}

	public static void setPieceSize(int pieceSize) {
		BitTorrentState.pieceSize = pieceSize;
	}

	public static void calculateAndSetpieceCount() {
		pieceCount = (int)Math.ceil((double)fileSize / pieceSize);
		System.out.println("BitTorrent current state - Number of pieces: " + pieceCount);
	}

	public static String getPeerLogFilePath() {
		return PropertiesEnum.PEER_LOG_FILE_PATH.getValue();
	}

	public static String getPeerLogFileExtension() {
		return PropertiesEnum.PEER_LOG_FILE_EXTENSION.getValue();
	}

	public static void setPeerMapFromProperties() {
		Scanner sc = null;
		int seq = 1;
		try {
			sc = new Scanner(new File(PropertiesEnum.PEER_PROPERTIES_CONFIG_PATH.getValue()));
			while (sc.hasNextLine()) {
				String arr[] = sc.nextLine().split(" ");
				PeerState peer = new PeerState();
				peer.setSequenceId(seq++);
				peer.setPeerId(arr[0]);
				peer.setHostName(arr[1]);
				peer.setPort(Integer.parseInt(arr[2]));
				if (arr[3].equals("1")) {
					peer.setHasSharedFile(true);
				}
				else {
					peer.setHasSharedFile(false);
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

	public static void showConfiguration() {
		System.out.println( "PeerProperties [preferredNeighbourCount=" + preferredNeighbourCount);
		System.out.print(", unchokingInterval=" + unchokingInterval);
		System.out.print(", optimisticUnchokingInterval="+ optimisticUnchokingInterval);
		System.out.print(", fileName="+ fileName);
		System.out.print(", fileSize="+ fileSize);
		System.out.print(", pieceSize="+ pieceSize+ "]");
	}

	public static void setStateFromConfigFiles() {

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
		preferredNeighbourCount = 
				Integer.parseInt(properties.get("NumberOfPreferredNeighbors").toString());
		optimisticUnchokingInterval =
				Integer.parseInt(properties.get("OptimisticUnchokingInterval").toString());
		pieceSize = Integer.parseInt("PieceSize");
		unchokingInterval =
				Integer.parseInt(properties.getProperty("UnchokingInterval");
		calculateAndSetpieceCount();

		System.out.println(PropertiesEnum.PROPERTIES_FILE_PATH.getValue());
		System.out.println(PropertiesEnum.PROPERTIES_FILE_PATH.getValue() + BitTorrentState.fileName);

		setPeerMapFromProperties();

	}

	public static synchronized boolean hasAllPeersDownloadedFile() {
		for (PeerState peerState: peers.values()) {
			if (peerState.getBitField().nextClearBit(0) != pieceCount) {
				System.out.println(peerState.getPeerId() + " has incomplete file, so not exiting");
				return false;
			}
		}
		return true;
	}

}

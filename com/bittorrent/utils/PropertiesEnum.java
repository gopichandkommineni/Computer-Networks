package com.bittorrent.utils;

import java.io.File;

public enum PropertiesEnum {

	NUMBER_OF_PREFERRED_NEIGHBORS("NumberOfPreferredNeighbors"),
	UNCHOKING_INTERVAL("UnchokingInterval"),
	OPTIMISTIC_UNCHOKING_INTERVAL("OptimisticUnchokingInterval"),
	FILENAME("FileName"),
	FILESIZE("FileSize"),
	PIECESIZE("PieceSize"),
	COMMON_PROPERTIES_CONFIG_PATH(System.getProperty("user.dir") + File.separatorChar
			+ "Common.cfg"),
	PROPERTIES_FILE_PATH(System.getProperty("user.dir") + File.separatorChar),
	PROPERTIES_CREATED_FILE_PATH(System.getProperty("user.dir") + File.separatorChar
			+ "project/peer_"),
	PEER_PROPERTIES_CONFIG_PATH(System.getProperty("user.dir") + File.separatorChar
			+ "PeerInfo.cfg"),
	PEER_LOG_FILE_EXTENSION(".log"),
	PEER_LOG_FILE_PATH(System.getProperty("user.dir") + File.separatorChar
			+ "project/log_peer_");
	
	private final String value;

	PropertiesEnum(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}
}
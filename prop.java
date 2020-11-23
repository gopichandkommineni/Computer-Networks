package com.bittorrent.utils;

import java.io.File;

public class prop {

    static int NUMBER_OF_PREFERRED_NEIGHBORS;
    static int UNCHOKING_INTERVAL;
    static int OPTIMISTIC_UNCHOKING_INTERVAL;
    static String FILENAME;
    static long FILESIZE;
    static int PIECESIZE;
    static String COMMON_PROPERTIES_CONFIG_PATH = System.getProperty("user.dir") + File.separatorChar + "Common.cfg";
    static String PROPERTIES_FILE_PATH = System.getProperty("user.dir") + File.separatorChar;
    static String PROPERTIES_CREATED_FILE_PATH = System.getProperty("user.dir") + File.separatorChar + "project/peer_";
    static String PEER_PROPERTIES_CONFIG_PATH = System.getProperty("user.dir") + File.separatorChar + "PeerInfo.cfg";
    static String PEER_LOG_FILE_EXTENSION = ".log";
    static String PEER_LOG_FILE_PATH = System.getProperty("user.dir") + File.separatorChar + "project/log_peer_";

    public prep(){
        
    }
}

package bittorrent.utils;

import bittorrent.dtos.BitTorrentState;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/***
 * Logger utility class designed as Singleton to
 * avoid concurrency issues
 */
public class Logger {

	private static Map<String, Logger> logMap = new HashMap<>();

	public PrintWriter outputWriter = null;

	private String pId;

	public static Logger fetchLogger(String pId) {
		synchronized (Logger.class) {
			if (logMap.get(pId) == null) {
				logMap.put(pId, new Logger(pId));
			}
		}
		return logMap.get(pId);
	}

	/**
	 * Constructor: Creates directories for logging
	 * and initializes PrintWriter
	 */
	private Logger(String pId) {
		try {
			System.out.println("Logger started for peer: " + pId);
			this.pId = pId;
			File f = createPeerLogDirectory(pId);
			outputWriterInit(f);
		}
		catch (Exception e) {
			System.out.println("Exception "+ e.getMessage());
		}
	}

	private File createPeerLogDirectory(String pId) throws Exception{

		String filePath = BitTorrentState.findPathOfLogFile() + pId
				+ BitTorrentState.findPeerLogExt();

		File f = new File(filePath);
		f.getParentFile().mkdirs();

		return f;
	}

	private void outputWriterInit(File f) throws IOException{

		f.createNewFile();
		FileOutputStream fileOutStream = new FileOutputStream(f, false);
		outputWriter = new PrintWriter(fileOutStream, true);
	}

	private String fetchTimeStamp() {

		Timestamp ts = new Timestamp(System.currentTimeMillis());
		return ts.toString();
	}

	private void outputFile(String msg) {

		synchronized (this) {
			outputWriter.println(msg);
		}
	}

	public void receivedHaveMsg(String fromPId, int pieceIndx) {

		outputFile(fetchTimeStamp()
				+ ": Peer "
				+ pId
				+ " received the 'have' message from "
				+ fromPId
				+ " for the piece "
				+ pieceIndx + ".");
	}



	public void establishingConnectionTo(String toPId) {
		outputFile(fetchTimeStamp()
				+ ": Peer "
				+ pId
				+ " makes a connection to Peer "
				+ toPId
				+ ".");
	}

	public void establishingConnectionFrom(String fromPId) {
		outputFile(fetchTimeStamp()
				+ ": Peer "
				+ pId
				+ " is connected from Peer "
				+ fromPId
				+ ".");
	}

	public void preferredNeighborsChange(Map<String, String> preferredNeighbors) {

		StringBuilder msg = new StringBuilder();
		msg.append(fetchTimeStamp());
		msg.append(": Peer ");
		msg.append(pId);
		msg.append(" has preferred neighbors [");
		String delimiter = "";

		for (String remotePId: preferredNeighbors.values()) {

			msg.append(delimiter);
			delimiter = ", ";
			msg.append(remotePId);

		}
		outputFile(msg.toString() + "].");
	}


	public void optimisticallyUnchokedNeighborChange(String unchokedNeighbor) {

		outputFile(fetchTimeStamp()
				+ ": Peer "
				+ pId
				+ " has the optimistically unchoked neighbor "
				+ unchokedNeighbor
				+ ".");
	}

	public void unchoked(String pId1) {

		outputFile(fetchTimeStamp()
				+ ": Peer "
				+ pId
				+ " is unchoked by "
				+ pId1
				+ ".");
	}

	public void choked(String id) {

		outputFile(fetchTimeStamp()
				+ ": Peer "
				+ pId
				+ " is choked by "
				+ id
				+ ".");
	}


	public void receivedInterestedMsg(String id) {

		outputFile(fetchTimeStamp()
				+ ": Peer "
				+ pId
				+ " received the 'interested' messaging from "
				+ id
				+ ".");
	}


	public void receivedNotInterestedMsg(String id) {

		outputFile(fetchTimeStamp()
				+ ": Peer "
				+ pId
				+ " received the 'not interested' messaging from "
				+ id
				+ ".");
	}


	public void pieceDownloadCompleted(String id, int pieceIndx, int totalPieces) {

		outputFile(fetchTimeStamp()
				+ ": Peer "
				+ pId
				+ " has downloaded the piece "
				+ pieceIndx
				+ " from "
				+ id
				+ "."
				+ "Now the number of pieces it has is "
				+ totalPieces);

	}

	public void fileDownloadCompleted() {

		outputFile(fetchTimeStamp()
				+ "Peer "
				+ pId
				+ " has downloaded the complete file");
	}



}
package bittorrent.state;

import bittorrent.conn.PeerConnectionManager;
import bittorrent.messaging.Message;

import java.net.ServerSocket;
import java.util.BitSet;
import java.util.Map;
import java.util.Timer;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class PeerStatus {

	private int seqID;
	private String pID;
	private String hostName;
	private int portNO;
	private boolean hasSharedFile;
	private boolean fileReceived = false;
	private BitSet bitField;
	private ConcurrentHashMap<Integer, byte[]> fileSplitMap;
	private ConcurrentHashMap<String, String> preferredNeighbours = new ConcurrentHashMap<>();
	private String optimisticUnchokedPeerId;
	private Map<String, String> interestedNeighbours = new ConcurrentHashMap<>();
	private ConcurrentHashMap<String, PeerConnectionManager> connections = new ConcurrentHashMap<>();
	private BlockingQueue<Message> queue = new LinkedBlockingQueue<>();
	private double dataRate = 0;
	private Timer timer1;
	private Timer timer2;
	private ServerSocket serverSocket;
	private AtomicBoolean downloadComplete = new AtomicBoolean(false);

	public String getCurrOptUnchPeer() {
		// return current optimistic unchocking peers
		return optimisticUnchokedPeerId;
	}

	public void setCurrOptUnchPeer(String optimisticUnchokedPeerId) {
		// set which peers are currently optimistically unchocked
		this.optimisticUnchokedPeerId = optimisticUnchokedPeerId;
	}

	public boolean downloadCheck() {
		// check if the file is downloaded or not
		return downloadComplete.get();
	}

	public void setDownloadCheck(boolean value) {
		// if the file is downloaded set it as downloaded
		this.downloadComplete.set(value);
	}

	public ConcurrentHashMap<String, PeerConnectionManager> conList() {
		// return the connection list
		return connections;
	}

	public ServerSocket findServSocket() {
		// find the server socket
		return serverSocket;
	}

	public void assignServSocket(ServerSocket serverSocket) {
		// set the server socket
		this.serverSocket = serverSocket;
	}

	public void setTimer1(Timer timer1) {
		// set timer for download
		this.timer1 = timer1;
	}

	public void setTimer2(Timer timer2) {
		// set timer for download
		this.timer2 = timer2;
	}

	public void blockAllTasks() {
		// Block the remaining Tasks
		System.out.println(getPeerId() + ": stopping scheduler tasks");
		timer1.cancel();
		timer1.purge();
		timer2.cancel();
		timer2.purge();
	}

	public void assignNewInterestedPeers(Map<String, String> interestedNeighbours) {
		// find and set new peers interested in file
		this.interestedNeighbours = interestedNeighbours;
	}

	public double findDataRate() {
		// return the download data rate
		return dataRate;
	}

	public void assignDataRate(double dataRate) {
		// set the download data rate
		this.dataRate = dataRate;
	}

	public BlockingQueue<Message> getBlockingPeers(){
		// return Queue containing peers that are blocked
		return queue;
	}

	public synchronized void insertPieceIntoMap(int index, byte[] piece) {
		// insert file piece in file map
		this.fileSplitMap.put(index, piece);
		this.bitField.set(index);
	}

	public Map<String, String> findPeersInterested() {
		// get interested peers list
		return interestedNeighbours;
	}

	public int numOfPreferNeighbours(){
		// return no of preferred neighbours
		return preferredNeighbours.size();
	}

	public void discardPeersInterested(String pID) {
		// remove peers interested 
		interestedNeighbours.remove(pID);
	}

	public void insertPeersInterested(String pID) {
		// add new peers that are interested
		this.interestedNeighbours.put(pID, pID);
	}

	public void putPreferredNeighbours(String pID) {
		// add preferred neighbours into the list
		preferredNeighbours.put(pID, pID);
	}

	public void assignBitfieldValue(BitSet bitField) {
		// assign new bitfield value
		this.bitField = bitField;
	}

	public ConcurrentHashMap<Integer, byte[]> receiveFilePieceIndexMap() {
		// get the map that stored file pieces
		return fileSplitMap;
	}

	public void assignFilePieceIndexMap(ConcurrentHashMap<Integer, byte[]> fileSplitMap) {
		// create file piece index map
		this.fileSplitMap = fileSplitMap;
	}

	public ConcurrentHashMap<String, String> findPreferPeers() {
		return preferredNeighbours;
	}

	public void assignPeferPeers(ConcurrentHashMap<String, String> preferredNeighbours) {
		// find and set preferred peers
		this.preferredNeighbours = preferredNeighbours;
	}

	public BitSet getBitField() {
		return bitField;
	}

	public String getPeerId() {
		return pID;
	}

	public String getHostName() {
		return hostName;
	}

	public int getPort() {
		return portNO;
	}

	public boolean isHasSharedFile() {
		return hasSharedFile;
	}

	public void assignPeerId(String pID) {
		this.pID = pID;
	}

	public void assignHostName(String hostName) {
		this.hostName = hostName;
	}

	public void assignPort(int portNO) {
		this.portNO = portNO;
	}

	public void containsFile(boolean hasSharedFile) {
		// check if the peer has file
		if (hasSharedFile) {
			if (this.bitField == null) {
				this.bitField = new BitSet(BitTorrentStatus.pieceCount());
			}
			this.bitField.set(0, BitTorrentStatus.pieceCount());
		}

		this.hasSharedFile = hasSharedFile;
	}

	public boolean checkFileStatus(){
		// check if the file is downloaded or not
		return fileReceived;
	}

	public void assignFileStatus(boolean fileReceived) {
		// set the file status
		this.fileReceived = fileReceived;
	}

	public int getSequenceNum() {
		// return sequence number
		return seqID;
	}

	public void assignSequenceNum(int seqID) {
		// give a seq num
		this.seqID = seqID;
	}

	public PeerStatus(String pID, String hostName, int portNO, boolean hasSharedFile) {
		this.pID = pID;
		this.hostName = hostName;
		this.portNO = portNO;
		this.hasSharedFile = hasSharedFile;
	}

	public PeerStatus(){
		this.bitField = new BitSet(BitTorrentStatus.pieceCount());
	}

	@Override
	public String toString() {
		return "PeerState{" +
				"sequenceID=" + seqID +
				", peerId='" + pID + '\'' +
				", hostName='" + hostName + '\'' +
				", portNO=" + portNO +
				", hasSharedFile=" + hasSharedFile +
				", fileReceived=" + fileReceived +
				'}';
	}

	public static void main(String args[]) {
//		BitSet bitSet = new BitSet(5);
//		BitSet bitSet1 = new BitSet(5);
//		bitSet.set(0);
//		bitSet1.set(0,4);
//		System.out.println(bitSet.g);
	}
}

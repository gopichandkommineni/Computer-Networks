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

public class 	PeerStatus {

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

		// gives the current optimistic unchocked peers
		return optimisticUnchokedPeerId;
	}

	public void setCurrOptUnchPeer(String optimisticUnchokedPeerId) {

		// sets the current optimistic unchocked peers
		this.optimisticUnchokedPeerId = optimisticUnchokedPeerId;
	}

	public boolean downloadCheck() {

		// verify whether the file is downloaded
		return downloadComplete.get();
	}

	public void setDownloadCheck(boolean value) {

		// set the file as downloaded if the download is complete
		this.downloadComplete.set(value);
	}

	public ConcurrentHashMap<String, PeerConnectionManager> conList() {

		// gives the list of connections
		return connections;
	}

	public ServerSocket findServSocket() {

		// gives the server socket
		return serverSocket;
	}

	public void assignServSocket(ServerSocket serverSocket) {

		// sets the server socket
		this.serverSocket = serverSocket;
	}

	public void setTimer1(Timer timer1) {

		// sets timer1 for downloading
		this.timer1 = timer1;
	}

	public void setTimer2(Timer timer2) {

		// sets timer2 for downloading
		this.timer2 = timer2;
	}

	public void blockAllTasks() {

		// stop the scheduler Tasks
		System.out.println(getPeerId() + ": stopping scheduler tasks");
		timer1.cancel();
		timer1.purge();
		timer2.cancel();
		timer2.purge();
	}

	public void assignNewInterestedPeers(Map<String, String> interestedNeighbours) {

		// search and set the new peers that are interested in the file
		this.interestedNeighbours = interestedNeighbours;
	}

	public double findDataRate() {

		// gives the data rate for download
		return dataRate;
	}

	public void assignDataRate(double dataRate) {

		// sets the data rate for download
		this.dataRate = dataRate;
	}

	public BlockingQueue<Message> getBlockingPeers(){

		// gives a Queue which consists of blocked peers
		return queue;
	}

	public synchronized void insertPieceIntoMap(int index, byte[] piece) {

		// put file piece to file map
		this.fileSplitMap.put(index, piece);
		this.bitField.set(index);
	}

	public Map<String, String> findPeersInterested() {

		// gives list of interested peers
		return interestedNeighbours;
	}

	public int numOfPreferNeighbours(){

		// gives preferred neighbours count
		return preferredNeighbours.size();
	}

	public void discardPeersInterested(String pID) {

		// discards the interested peers
		interestedNeighbours.remove(pID);
	}

	public void insertPeersInterested(String pID) {

		// puts new interested peers
		this.interestedNeighbours.put(pID, pID);
	}

	public void putPreferredNeighbours(String pID) {

		// puts preferred neighbours to list
		preferredNeighbours.put(pID, pID);
	}

	public void assignBitfieldValue(BitSet bitField) {

		// allocates a new bitfield value
		this.bitField = bitField;
	}

	public ConcurrentHashMap<Integer, byte[]> receiveFilePieceIndexMap() {

		// gives the map that has file pieces
		return fileSplitMap;
	}

	public void assignFilePieceIndexMap(ConcurrentHashMap<Integer, byte[]> fileSplitMap) {

		// generate file piece index map
		this.fileSplitMap = fileSplitMap;
	}

	public ConcurrentHashMap<String, String> findPreferPeers() {
		return preferredNeighbours;
	}

	public void assignPeferPeers(ConcurrentHashMap<String, String> preferredNeighbours) {

		// determines and sets preferred peers
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

		// verify whether the peer has file
		if (hasSharedFile) {
			if (this.bitField == null) {
				this.bitField = new BitSet(BitTorrentStatus.pieceCount());
			}
			this.bitField.set(0, BitTorrentStatus.pieceCount());
		}

		this.hasSharedFile = hasSharedFile;
	}

	public boolean checkFileStatus(){

		// verify whether the file is downloaded
		return fileReceived;
	}

	public void assignFileStatus(boolean fileReceived) {

		// sets the status of the file
		this.fileReceived = fileReceived;
	}

	public int getSequenceNum() {

		// gives the sequence number
		return seqID;
	}

	public void assignSequenceNum(int seqID) {

		// assigns a sequence number
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

	}
}

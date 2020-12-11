package bittorrent.ops;

import bittorrent.state.BitTorrentStatus;
import bittorrent.state.PeerStatus;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.*;

public class FileOps {

	public static ConcurrentHashMap<Integer, byte[]> divideFile() {
		File f= new File(System.getProperty("user.dir") + File.separatorChar + BitTorrentStatus.fileName);
		FileInputStream fis = null;
		DataInputStream dis = null;
		try {
			fis = new FileInputStream(f);
			dis = new DataInputStream(fis);

			int pieceNum = BitTorrentStatus.pieceCount();
			ConcurrentHashMap<Integer, byte[]> fileMap = new ConcurrentHashMap<>();

			for (int j = 0; j < pieceNum; j++) {
				int pieceLen = j != pieceNum - 1 ? BitTorrentStatus.findPieceLength()
						: (int) (BitTorrentStatus.findSizeOfTheFile() % BitTorrentStatus.findPieceLength());
				byte[] piece = new byte[pieceLen];
				dis.readFully(piece);
				fileMap.put(j, piece);
			}
			return fileMap;

		}
		catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				fis.close();
				dis.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	public static void combinePiecesAndOutputFile(PeerStatus peerState) {
		String filePath = System.getProperty("user.dir") + File.separatorChar
		+ "project/peer_" + peerState.getPeerId()
				+ File.separatorChar + BitTorrentStatus.fileName;
		System.out.println("Combining pieces and writing to file" + filePath);
		FileOutputStream outStream = null;
		try {
			outStream = new FileOutputStream(filePath);
			for (int j = 0; j < peerState.receiveFilePieceIndexMap().size(); j++) {
				try {
					outStream.write(peerState.receiveFilePieceIndexMap().get(j));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		finally {
			try {
				outStream.flush();
			}
			catch (Exception e){
				e.printStackTrace();
			}
		}
	}

	public static void createFilesAndDirectories(String pId){
		try {
			String filePath = System.getProperty("user.dir") + File.separatorChar
			+ "project/peer_" + pId
					+ File.separatorChar + BitTorrentStatus.fileName;
			File newFile = new File(filePath);

			// creates the parent directories if not present
			newFile.getParentFile().mkdirs(); // Will create parent directories if not exists
			newFile.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String args[]) {

	}
}

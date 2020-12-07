package com.bittorrent.utils;

import com.bittorrent.dtos.BitTorrentState;
import com.bittorrent.dtos.PeerState;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.*;

public class FileUtils {

	public static ConcurrentHashMap<Integer, byte[]> divideFile() {
		File f= new File(PropertiesEnum.PROPERTIES_FILE_PATH.getValue() + BitTorrentState.fileName);
		FileInputStream fileInStream = null;
		DataInputStream dataInStream = null;
		try {
			fileInStream = new FileInputStream(f);
			dataInStream = new DataInputStream(fileInStream);

			int pieceNum = BitTorrentState.pieceCount();
			ConcurrentHashMap<Integer, byte[]> fileDivideMap = new ConcurrentHashMap<>();

			for (int j = 0; j < pieceNum; j++) {
				int pieceLen = j != pieceNum - 1 ? BitTorrentState.findPieceLength()
						: (int) (BitTorrentState.findSizeOfTheFile() % BitTorrentState.findPieceLength());
				byte[] piece = new byte[pieceLen];
				dataInStream.readFully(piece);
				fileDivideMap.put(j, piece);
			}
			return fileDivideMap;

		}
		catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				fileInStream.close();
				dataInStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	public static void combinePiecesAndOutputFile(PeerState peerState) {
		String filePath = PropertiesEnum.PROPERTIES_CREATED_FILE_PATH.getValue() + peerState.getPeerId()
				+ File.separatorChar + BitTorrentState.fileName;
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
			String filePath = PropertiesEnum.PROPERTIES_CREATED_FILE_PATH.getValue() + pId
					+ File.separatorChar + BitTorrentState.fileName;
			File newFile = new File(filePath);
			newFile.getParentFile().mkdirs(); // Will create parent directories if not exists
			newFile.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String args[]) {
		// For testing only...
//		BitTorrentState.loadPeerStateFromConfig();
//		PeerState peerState = new PeerState();
//		FileUtils fileHandler = new FileUtils("1001");
//		fileHandler.createFilesAndDirectories();
//		peerState.assignFilePieceIndexMap(fileHandler.divideFile());
//		fileHandler.combinePiecesAndOutputFile(peerState.receiveFilePieceIndexMap());
	}
}

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

	public static ConcurrentHashMap<Integer, byte[]> splitFile() {
		File file = new File(PropertiesEnum.PROPERTIES_FILE_PATH.getValue() + BitTorrentState.fileName);
		FileInputStream fileInputStream = null;
		DataInputStream dataInputStream = null;
		try {
			fileInputStream = new FileInputStream(file);
			dataInputStream = new DataInputStream(fileInputStream);

			int numberOfPieces = BitTorrentState.getNumberOfPieces();
			ConcurrentHashMap<Integer, byte[]> fileSplitMap = new ConcurrentHashMap<>();

			for (int i = 0; i < numberOfPieces; i++) {
				int pieceSize = i != numberOfPieces - 1 ? BitTorrentState.getPieceSize()
						: (int) (BitTorrentState.getFileSize() % BitTorrentState.getPieceSize());
				byte[] piece = new byte[pieceSize];
				dataInputStream.readFully(piece);
				fileSplitMap.put(i, piece);
			}
			return fileSplitMap;

		}
		catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				fileInputStream.close();
				dataInputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	public static void joinPiecesAndWriteFile(PeerState peerState) {
		String fileNameWithPath = PropertiesEnum.PROPERTIES_CREATED_FILE_PATH.getValue() + peerState.getPeerId()
				+ File.separatorChar + BitTorrentState.fileName;
		System.out.println("Joining pieces and writing to file " + fileNameWithPath);
		FileOutputStream outputStream = null;
		try {
			outputStream = new FileOutputStream(fileNameWithPath);
			for (int i = 0; i < peerState.receiveFilePieceIndexMap().size(); i++) {
				try {
					outputStream.write(peerState.receiveFilePieceIndexMap().get(i));
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
				outputStream.flush();
			}
			catch (Exception ex){
				ex.printStackTrace();
			}
		}
	}

	public static void makeFilesAndDirectories(String peerId){
		try {
			String fileNameWithPath = PropertiesEnum.PROPERTIES_CREATED_FILE_PATH.getValue() + peerId
					+ File.separatorChar + BitTorrentState.fileName;
			File createdFile = new File(fileNameWithPath);
			createdFile.getParentFile().mkdirs(); // Will create parent directories if not exists
			createdFile.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String args[]) {
		// For testing only...
//		BitTorrentState.setStateFromConfigFiles();
//		PeerState peerState = new PeerState();
//		FileUtils fileHandler = new FileUtils("1001");
//		fileHandler.makeFilesAndDirectories();
//		peerState.assignFilePieceIndexMap(fileHandler.splitFile());
//		fileHandler.joinPiecesAndWriteFile(peerState.receiveFilePieceIndexMap());
	}
}

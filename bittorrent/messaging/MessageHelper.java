package bittorrent.messaging;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.BitSet;

public class MessageHelper {
    public static byte[] combineByteArray(byte[] i, byte[] j) {
        byte[] res = new byte[i.length + j.length];
        System.arraycopy(i, 0, res, 0, i.length);
        System.arraycopy(j, 0, res, i.length, j.length);
        return res;
    }

    public static byte[] combineByteArray(byte[] i, int iLength, byte[] j, int jLength) {
        byte[] res = new byte[iLength + jLength];
        System.arraycopy(i, 0, res, 0, iLength);
        System.arraycopy(j, 0, res, iLength, jLength);
        return res;
    }

    public static byte[] convertByteArrayToInteger(int i) {
        byte[] res = new byte[4];
        res[3] = (byte) (i & 0xFF);
        res[2] = (byte) ((i >> 8) & 0xFF);
        res[1] = (byte) ((i >> 16) & 0xFF);
        res[0] = (byte) ((i >> 24) & 0xFF);
        return res;
    }

    public static int convertByteArrayToInteger(byte[] j) {
        int val = 0;
        for (int i = 0; i < 4; i++) {
            int shft = (4 - 1 - i) * 8;
            val += (j[i] & 0x000000FF) << shft;
        }
        return val;
    }

    public static byte[] combineByte(byte[] i, byte j) {
        byte[] res = new byte[i.length + 1];
        System.arraycopy(i, 0, res, 0, i.length);
        res[i.length] = j;
        return res;
    }

    public static byte[] inputBytes(BufferedInputStream ip, byte[] byteArr, int length) throws IOException {
        int size = length;
        int index = 0;
        while (size != 0) {
            int dataSize = ip.available();
            int input = Math.min(size, dataSize);
            byte[] dataInput = new byte[input];
            if (input != 0) {
                ip.read(dataInput);
                byteArr = combineByteArray(byteArr, index, dataInput, input);
                index += input;
                size -= input;
            }
        }
        return byteArr;
    }

    public static BitSet convertByteArrayToBitset(byte[] bytes) {
        BitSet bits = new BitSet();
        for (int i = 0; i < bytes.length * 8; i++) {
            if ((bytes[bytes.length - i / 8 - 1] & (1 << (i % 8))) > 0) {
                bits.set(i);
            }
        }
        return bits;
    }

    public static byte[] convertToByteArray(BitSet bits) {
        byte[] bytes = new byte[bits.length() / 8 + 1];
        for (int i = 0; i < bits.length(); i++) {
            if (bits.get(i)) {
                bytes[bytes.length - i / 8 - 1] |= 1 << (i % 8);
            }
        }
        return bytes;
    }

    public static byte[] removeBytes(byte[] a) {
        byte[] payLoad = new byte[a.length - 4];
        for (int i = 4; i < a.length; i++) {
            payLoad[i - 4] = a[i];
            a[i - 4] = a[i];
        }
        return payLoad;
    }

    public static byte[] fetchPieceIndex(byte[] a){
        byte[] pieceIndx = new byte[4];
        for(int i=0;i<4;i++){
            pieceIndx[i] = a[i];
        }
        return pieceIndx;
    }
}

import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;

//import bittorrent.utils.PropertiesEnum;

public class StartRemotePeers {

    public static void main(String[] args) {

        String path = "Computer-Networks/";

        ArrayList<PeerInfo> peerList = new ArrayList<>();
        Scanner sc = null;
        int seq = 1;
        try {
            sc = new Scanner(new File(System.getProperty("user.dir") + File.separatorChar
			+ "PeerInfo.cfg"));
            while (sc.hasNextLine()) {
                String arr[] = sc.nextLine().split(" ");
                if (arr.length > 0) {
                    peerList.add(new PeerInfo(arr[0], arr[1]));
                }
            }
        } catch (IOException e) {
            System.out.println("PeerInfo.cfg missing");
        }
        finally {
            sc.close();
        }

        for (PeerInfo pInfo : peerList) {
            try {
                Thread.sleep(1000);
                
                Runtime.getRuntime().exec("ssh -i .ssh/id_rsa gopichan@" + pInfo.getHostName() + " cd " + path +
                        "; java peerProcess " + pInfo.getPeerID() + " > " + pInfo.peerID + "console.log");
                System.out.println("Start remote peer " + pInfo.getPeerID() + " at " + pInfo.getHostName());
            }
            catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static class PeerInfo {

        private String peerID;
        private String hostName;

        public PeerInfo(String peerID, String hostName) {
            super();
            this.peerID = peerID;
            this.hostName = hostName;
        }

        public String getPeerID() {
            return peerID;
        }

        public String getHostName() {
            return hostName;
        }

    }

}
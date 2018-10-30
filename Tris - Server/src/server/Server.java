package server;

import server.lobby.Lobby;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.concurrent.Semaphore;

/**
 *
 * @author pigro
 */
public class Server {

    private static String ip;
    private static int port;
    
    private static ArrayList<Lobby> lobby = new ArrayList(0);
    private static Semaphore semaforoLobby;
    
    public static void main(String[] args) {
        try {
            ip = InetAddress.getLocalHost().getHostAddress();
            port = 9000;
            semaforoLobby = new Semaphore(100000, true);
            ServerSocket listener = new ServerSocket(port);
            try{
                System.out.println("Aspetto players");
                while(true){
                    ServerPlayer sp = new ServerPlayer(listener.accept(), ip, port, lobby, semaforoLobby);
                    sp.start();
                    System.out.println("Server: Player connesso");
                }
            } finally {
                listener.close();
            }
        } catch (UnknownHostException ex) {
            System.err.println("Errore nell'ottenere ip");
            ex.printStackTrace();
        } catch (IOException ex) {
            System.err.println("Errore nel ServerSocket");
            ex.printStackTrace();
        }
    }
    
}
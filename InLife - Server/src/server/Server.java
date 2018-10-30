package server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;

/**
 *
 * @author pigro
 */
public class Server extends Thread {

    private static String ip;
    private static int port;
    
    @Override
    public void run() {
        try {
            ip = InetAddress.getLocalHost().getHostAddress();
            port = 9000;
            ServerSocket listener = new ServerSocket(port);
            try{
                System.out.println("Aspetto...");
                while(true){
                    ServerUser su = new ServerUser(listener.accept(), ip, port);
                    su.start();
                    System.out.println("Server: user connesso");
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
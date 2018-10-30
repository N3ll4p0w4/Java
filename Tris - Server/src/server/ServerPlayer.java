/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import server.lobby.Lobby;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author pigro
 */
public class ServerPlayer extends Thread {

    private Socket player;
    private String serverIp;
    private int serverPort;
    private ArrayList<Lobby> lobby;
    private Semaphore semaforo;

    public ServerPlayer(Socket player, String serverIp, int serverPort, ArrayList<Lobby> lobby, Semaphore semaforo) {
        this.player = player;
        this.serverIp = serverIp;
        this.serverPort = serverPort;
        this.lobby = lobby;
        this.semaforo = semaforo;
    }

    @Override
    public void run() {
        while (player.isConnected() && !this.isInterrupted()) {
            String messaggio = ricevi(player);
            if (messaggio.equals("Lobbies")) {
                String lobbysNames = "";
                try {
                    semaforo.acquire();
                    for (int i = 0; i < lobby.size(); i++) {
                        if(!lobby.get(i).isOnline()){
                            System.out.println("Rimossa: "+lobby.get(i).getNome());
                            lobby.remove(i);
                            i--;
                            continue;
                        }
                        lobbysNames += lobby.get(i).getNome() + ";" + lobby.get(i).getPort() + ";" + lobby.get(i).getNplayer() + ";" + lobby.get(i).getMaxPlayer() + ";" + "\n";
                    }
                    semaforo.release();
                } catch (InterruptedException ex) {
                    semaforo.release();
                }
                invia(player, lobbysNames);
            } else if (messaggio.equals("NewLobby")) {
                String lobbyName = ricevi(player);
                try {
                    semaforo.acquire();
                    int newPort = serverPort + 1;
                    int pos;
                    for (int i = 0; i < lobby.size(); i++) {
                        if(!lobby.get(i).isOnline()){
                            System.out.println("Rimossa: "+lobby.get(i).getNome());
                            lobby.remove(i);
                            i--;
                        }
                    }
                    for (pos = 0; pos < lobby.size() && newPort == lobby.get(pos).getPort(); newPort++, pos++) {}
                    Lobby newLobby = new Lobby(serverIp, newPort, lobbyName, 2);
                    if (pos >= lobby.size()) {
                        lobby.add(newLobby);
                    } else {
                        lobby.add(pos, newLobby);
                    }
                    newLobby.start();
                    semaforo.release();
                    while(!newLobby.isOnline()){
                        //AspettaCheSiaOn
                    }
                    invia(player, newLobby.getPort()+"");
                    try {
                        player.close();
                    } catch (IOException ex) {}
                    this.interrupt();
                } catch (InterruptedException ex) {
                    semaforo.release();
                }
            } else if (messaggio.equals("CloseLobby")) {
                int nLobby = Integer.parseInt(ricevi(player));
                try {
                    semaforo.acquire();
                    lobby.remove(nLobby);
                    semaforo.release();
                } catch (InterruptedException ex) {
                    semaforo.release();
                }
                invia(player, "Lobby eliminata");
            }
        }
    }

    private void invia(Socket socket, String data) {
        try {
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            out.writeUTF(data);
        } catch (SocketException ex) {
            System.err.println("Errore nel Socket del Server");
            //ex.printStackTrace();
        } catch (IOException ex) {
            System.err.println("Errore nel DataOutputStream");
            //ex.printStackTrace();
        }
    }

    private String ricevi(Socket socket) {
        try {
            DataInputStream input = new DataInputStream(socket.getInputStream());
            String s = input.readUTF();
            while (s == null) {
                s = input.readUTF();
            }
            return s;
        } catch (SocketException ex) {
            System.err.println("Errore nel Socket del Client");
            //ex.printStackTrace();
        } catch (IOException ex) {
            //System.err.println("Errore nel DataInputStream");
            System.err.println("Client has left");
            try {
                player.close();
            } catch (IOException ex1) {}
            this.interrupt();
            return "";
            //ex.printStackTrace();
        }
        return "";
    }

}

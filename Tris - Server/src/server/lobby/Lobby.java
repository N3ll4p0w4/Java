/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server.lobby;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;

/**
 *
 * @author pigro
 */
public class Lobby extends Thread {
    
    private Socket g1, g2;
    
    private String ip;
    private int port;
    private String nome;
    private boolean lobbyOn;
    
    private int nPlayer;
    private int maxPlayer;
    
    private int turno;

    public Lobby(String ip, int port, String nome, int maxPlayer) {
        this.ip = ip;
        this.port = port;
        this.nome = nome;
        nPlayer = 0;
        this.maxPlayer = maxPlayer;
        lobbyOn = false;
    }
    
    @Override
    public void run(){
        try {
            ServerSocket listener = new ServerSocket(port);
            System.out.println(nome+" online");
            lobbyOn = true;
            try{
                g1 = listener.accept();
                nPlayer++;
                invia(g1, nPlayer+"");
                System.out.println(nome+": Player1 connesso!");
                g2 = listener.accept();
                nPlayer++;
                invia(g2, nPlayer+"");
                System.out.println(nome+": Player2 connesso!");
                turno = (int)(Math.random()*maxPlayer+1);
                invia(g1, turno+"");
                invia(g2, turno+"");
                invia(g1, "StartMatch");
                invia(g2, "StartMatch");
                String mess;
                if(turno == 1) mess = ricevi(g1);
                else mess = ricevi(g2);
                while(!mess.equals("Finish")){
                    if(turno == 1){
                        invia(g2, mess);
                        turno = 2;
                        mess = ricevi(g2);
                    } else {
                        invia(g1, mess);
                        turno = 1;
                        mess = ricevi(g1);
                    }
                }
                if(turno == 1) mess = ricevi(g2);
                else mess = ricevi(g1);
                System.out.println("Fine partita");
                g1.close();
                g2.close();
                lobbyOn = false;
            } finally {
                listener.close();
            }
        } catch (UnknownHostException ex) {
            System.out.println("Errore nell'ottenere ip");
            ex.printStackTrace();
        } catch (IOException ex) {
            System.out.println("Errore nel ServerSocket");
            ex.printStackTrace();
        }
    }
    
    public String getIp(){
        return ip;
    }   
    
    public int getPort(){
        return port;
    }   
    
    public String getNome(){
        return nome;
    } 
    
    public int getNplayer(){
        return nPlayer;
    } 
    
    public int getMaxPlayer(){
        return maxPlayer;
    }
    
    public boolean isOnline(){
        return lobbyOn;
    }
    
    private void invia(Socket socket, String data){
        try {
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            out.writeUTF(data);
        }  catch(SocketException ex) {
            System.err.println("Errore nel Socket del Client");
            //ex.printStackTrace();
        }catch(IOException ex) {
            System.err.println("Errore nel DataOutputStream");
            //ex.printStackTrace();
        }
    }
    private String ricevi(Socket socket){
        try {
            DataInputStream input = new DataInputStream(socket.getInputStream());
            String s = input.readUTF();
            while(s == null)
                s = input.readUTF();
            return s;
        }  catch(SocketException ex) {
            System.err.println("Errore nel Socket del Server");
            //ex.printStackTrace();
        } catch (IOException ex) {
            System.err.println("Errore nel DataInputStream");
            //ex.printStackTrace();
        }
        return null;
    }   
    
}
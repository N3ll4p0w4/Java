/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import javax.imageio.ImageIO;
import Accounts.Account;
import Accounts.AccountManager;

/**
 *
 * @author pigro
 */
public class ServerUser extends Thread {

    private Socket user;
    private String serverIp;
    private int serverPort;
    private Account account;
    
    private ObjectOutputStream out;
    private ObjectInputStream in;

    public ServerUser(Socket player, String serverIp, int serverPort) {
        this.user = player;
        this.serverIp = serverIp;
        this.serverPort = serverPort;
        
        try {            
            out = new ObjectOutputStream(user.getOutputStream());
            in = new ObjectInputStream(user.getInputStream());
            user.setTcpNoDelay(true);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void run() {
        while (user.isConnected() && !this.isInterrupted()) {
            String messaggio = ricevi();
            //Thread t = new Thread(() -> {
                if (messaggio.equals("Login")) {
                    Login();
                }
                if (messaggio.equals("Register")) {
                    Register();
                }
                if (messaggio.equals("MyProfile")) {
                    MyProfile();
                }
                if (messaggio.equals("Account")) {
                    Account();
                }
            //});
            //t.start();
        }
    }

    private void invia(String data) {
        try {
            out.writeUTF(data);
            out.flush();
        } catch (SocketException ex) {
            System.err.println("Errore nel Socket del Server");
            //ex.printStackTrace();
        } catch (IOException ex) {
            System.err.println("Errore nel DataOutputStream");
            ex.printStackTrace();
        }
    }

    private String ricevi() {
        try {
            String s = in.readUTF();
            while (s == null) {
                s = in.readUTF();
            }
            return s;
        } catch (SocketException ex) {
            System.err.println("Errore nel Socket del Client");
            ex.printStackTrace();
        } catch (IOException ex) {
            //System.err.println("Errore nel DataInputStream");
            System.err.println("Client has left");
            try {
                user.close();
            } catch (IOException ex1) {}
            this.interrupt();
            return "";
            //ex.printStackTrace();
        }
        return "";
    }
    
    private void inviaImmagine(BufferedImage immagine) {
        byte[] buffer = null;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            ImageIO.write(immagine, AccountManager.typeImages, baos);
            baos.flush();
            buffer = baos.toByteArray();
            baos.close();
            out.writeObject(buffer);
            out.flush();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private BufferedImage riceviImmagine() {
        BufferedImage image = null;
        byte[] imageByte = null;
        try {
            imageByte = (byte[]) in.readObject();
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        }
        InputStream is = new ByteArrayInputStream(imageByte);
        try {
            image = ImageIO.read(is);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return image;
    }
    

    private void Login(){
        String username = ricevi();
        String password = ricevi();
        
        //Provo con username
        Account ac = AccountManager.getAccountByUsername(username);
        if(ac != null && ac.getPassword().equals(password)){
            account = ac;
            invia("true");
        } else if(ac != null && !ac.getPassword().equals(password)) {
            invia("false");
            invia("password;");
            ac = null;
            return;
        } else ac = null;
        
        //Provo con email
        if(ac == null){
            ac = AccountManager.getAccountByEmail(username);
            if(ac != null && ac.getPassword().equals(password)){
                account = ac;
                invia("true");
            } else if(ac != null && !ac.getPassword().equals(password)) {
                invia("false");
                invia("password;");
                ac = null;
                return;
            } else ac = null;
        } 
        //Username errato
        if(ac == null){
            invia("false");
            invia("username;");
        }
    }
    
    private void Register(){
        String name = ricevi();
        String username = ricevi();
        String email = ricevi();
        String password = ricevi();
        
        String error = "";
        if(!(AccountManager.getAccountByUsername(username) == null)){
            error += "usernameExist;";
        }
        if(!(AccountManager.getAccountByEmail(email) == null)){
            error += "emailExist;";
        }
        if(!error.isEmpty()){
            invia("false");
            invia(error);
            return;
        }
        
        invia("true");
        account = AccountManager.creaAccount(AccountManager.getNextId(), username, name, email, password);
        AccountManager.aggiungiAccount(account);
        account.save();
    }
    
    
    /**
     * My Profile
     * 
     */
    
    private void MyProfile(){
        String messaggio = ricevi();
        if(messaggio.equals("getMyAccount")){
            getMyAccount();
        }
        if(messaggio.equals("getMyImmagineProfilo")){
            getMyImmagineProfilo();
        }
        if(messaggio.equals("setMyImmagineProfilo")){
            setMyImmagineProfilo();
        }
        if(messaggio.equals("removeMyImmagineProfilo")){
            removeMyImmagineProfilo();
        }
        if(messaggio.equals("acceptRichiestaDa")){
            acceptRichiestaDa();
        }
        if(messaggio.equals("declineRichiestaDa")){
            declineRichiestaDa();
        }
        if(messaggio.equals("deleteRichiestaA")){
            deleteRichiestaA();
        }
        if(messaggio.equals("sendRichiesta")){
            sendRichiesta();
        }
    }
    
    private void getMyAccount(){
        invia(account.toString());
    }
    private void getMyImmagineProfilo(){
        inviaImmagine(account.getImmagineProfilo());
    }
    private void setMyImmagineProfilo(){
        BufferedImage bi = riceviImmagine();
        if(bi != null){
            account.setImmagineProfilo(bi);
            account.saveImmagineProfilo();
        }
    }
    private void removeMyImmagineProfilo(){
        account.removeImmagineProfilo();
    }
    private void sendRichiesta(){
        String idAccount = ricevi();
        Account a = AccountManager.getAccountById(idAccount);
        account.addRichiestaA(a);
        account.save();
        a.addRichiestaDa(account);
        a.save();
    }
    private void acceptRichiestaDa(){
        String idAccount = ricevi();
        Account a = AccountManager.getAccountById(idAccount);
        account.accettaRichiestaDa(a);
        account.save();
        a.accettaRichiestaA(account);
        a.save();
    }
    private void declineRichiestaDa(){
        String idAccount = ricevi();
        Account a = AccountManager.getAccountById(idAccount);
        account.removeRichiestaDa(a);
        account.save();
        a.removeRichiestaA(account);
        a.save();
    }
    private void deleteRichiestaA(){
        String idAccount = ricevi();
        Account a = AccountManager.getAccountById(idAccount);
        account.removeRichiestaA(a);
        account.save();
        a.removeRichiestaDa(account);
        a.save();
    }

    private void Account() {
        String messaggio = ricevi();
        if(messaggio.equals("getAccount")){
            getAccount();
        }
        if(messaggio.equals("getAccountImmagineProfilo")){
            getAccountImmagineProfilo();
        }
        
        if(messaggio.equals("getAccountMinimal")){
            getAccountMinimal();
        }
        if(messaggio.equals("getAccountImmagineProfiloMinimal")){
            getAccountMinimalImmagineProfilo();
        }
        if(messaggio.equals("getAccountsStartWith")){
            getAccountsStartWith();
        }
    }
    
    private void getAccount(){
        String idAccount = ricevi();
        Account a = AccountManager.getAccountById(idAccount);
        invia(a.toString());
    }
    
    private void getAccountImmagineProfilo(){
        String idAccount = ricevi();
        Account a = AccountManager.getAccountById(idAccount);
        inviaImmagine(a.getImmagineProfilo());
    }

    private void getAccountMinimal() {
        String idAccount = ricevi();
        Account a = AccountManager.getAccountById(idAccount);
        invia(a.toStringMinimal());
    }

    private void getAccountMinimalImmagineProfilo() {
    }

    private void getAccountsStartWith() {
        String startWith = ricevi();
        int maxRisultati = Integer.parseInt(ricevi());
        
        ArrayList<Account> accounts = AccountManager.accountsByUsername.getTStartWith(startWith, maxRisultati);
        Account byId = AccountManager.getAccountById(startWith);
        
        if(accounts.size() < maxRisultati){
            ArrayList<Account> accountsName = AccountManager.accountsByName.getTStartWith(startWith, maxRisultati-accounts.size());
            int mancanti = maxRisultati-accounts.size();
            for(int i=0; i<mancanti; i++){
                if(accountsName.size() >= i)
                    break;
                accounts.add(accountsName.get(i));
            }
        }
        
        if(accounts.size() < maxRisultati){
            ArrayList<Account> accountsEmail = AccountManager.accountsByEmail.getTStartWith(startWith, maxRisultati-accounts.size());
            int mancanti = maxRisultati-accounts.size();
            for(int i=0; i<mancanti; i++){
                if(accountsEmail.size() >= i)
                    break;
                accounts.add(accountsEmail.get(i));
            }
        }
            
        if(byId != null)
            accounts.add(0, byId);
        
        String risposta = "";
        for(int i=0; i<accounts.size(); i++){
            risposta += accounts.get(i).getId()+";";
        }
        invia(risposta);
    }
    
}

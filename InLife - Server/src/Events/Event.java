/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Events;

import Accounts.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.concurrent.Semaphore;
import javax.imageio.ImageIO;
import static Accounts.AccountManager.pathFileAccounts;
import static Accounts.AccountManager.pathProfilePhotoAccounts;
import static Accounts.AccountManager.pathProfilePhotoAccountsDefault;
import static Accounts.AccountManager.typeImages;
import servereventorganizer.StruttureDati.SuffixTree;

/**
 *
 * @author pigro
 */
public class Event {
    
    private String id;
    private String username;
    private String email;
    private String name;
    private String password;
    private BufferedImage immagineProfilo;
    
    //Amici
    private SuffixTree<Event> amici = new SuffixTree<>();
    private ArrayList<Event> amiciArray = new ArrayList<>();
    private Semaphore amiciSemaphore = new Semaphore(1000);
    
    //Richieste Da (Ricevute e da accettare o rifiutare)
    private SuffixTree<Event> richiesteDa = new SuffixTree<>();
    private ArrayList<Event> richiesteDaArray = new ArrayList<>();
    private Semaphore richiesteDaSemaphore = new Semaphore(1000);
    
    //Richieste A (Fatte e da aspettare risposta)
    private SuffixTree<Event> richiesteA = new SuffixTree<>();
    private ArrayList<Event> richiesteAArray = new ArrayList<>();
    private Semaphore richiesteASemaphore = new Semaphore(1000);

    public Event() {
    }
    
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
    
    public BufferedImage getImmagineProfilo() {
        if(immagineProfilo == null){
            try {
                File inputFile = new File(pathProfilePhotoAccounts+this.getId()+"."+typeImages);
                BufferedImage bi;
                if(inputFile.exists())
                    bi = ImageIO.read(inputFile);
                else
                    bi = ImageIO.read(new File(pathProfilePhotoAccountsDefault));
                this.setImmagineProfilo(bi);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return immagineProfilo;
    }
    
    //Immagine Profilo
    
    public void setImmagineProfilo(BufferedImage immagineProfilo) {
        this.immagineProfilo = immagineProfilo;
    }
    
    public void saveImmagineProfilo(){
        try {
            File outputfile = new File(pathProfilePhotoAccounts+this.getId()+"."+typeImages);
            BufferedImage bi = this.getImmagineProfilo();
            ImageIO.write(bi, typeImages, outputfile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void removeImmagineProfilo(){
        this.immagineProfilo = null;
        File outputfile = new File(pathProfilePhotoAccounts+this.getId()+"."+typeImages);
        outputfile.delete();
    }
    
    //Amici

    public SuffixTree<Event> getAmici() {
        return amici;
    }
    
    public ArrayList<Event> getAmiciArray() {
        return amiciArray;
    }
    
    public void addAmico(Event account) {
        try {
            amiciSemaphore.acquire();
        } catch (InterruptedException ex) {}
        amici.insert(account, account.getId());
        amiciArray.add(account);
        amiciSemaphore.release();
    }
    
    public void removeAmico(Event account) {
        try {
            amiciSemaphore.acquire();
        } catch (InterruptedException ex) {}
        amici.removeT(account.getId());
        amiciArray.remove(account);
        amiciSemaphore.release();
    }

    //Richieste Da
    
    public SuffixTree<Event> getRichiesteDa() {
        return richiesteDa;
    }
    
    public ArrayList<Event> getRichiesteDaArray() {
        return richiesteDaArray;
    }
    
    public void addRichiestaDa(Event account) {
        try {
            richiesteDaSemaphore.acquire();
        } catch (InterruptedException ex) {}
        richiesteDa.insert(account, account.getId());
        richiesteDaArray.add(account);
        richiesteDaSemaphore.release();
    }
    
    public void removeRichiestaDa(Event account) {
        try {
            richiesteDaSemaphore.acquire();
        } catch (InterruptedException ex) {}
        richiesteDa.removeT(account.getId());
        richiesteDaArray.remove(account);
        richiesteDaSemaphore.release();
    }
    
    public void accettaRichiestaDa(Event account){
        try {
            richiesteDaSemaphore.acquire();
            amiciSemaphore.acquire();
        } catch (InterruptedException ex) {}
        richiesteDa.removeT(account.getId());
        richiesteDaArray.remove(account);
        amici.insert(account, account.getId());
        amiciArray.add(account);
        richiesteDaSemaphore.release();
        amiciSemaphore.release();
    }
    
    //Richieste A
    
    public SuffixTree<Event> getRichiesteA() {
        return richiesteA;
    }
    
    public ArrayList<Event> getRichiesteAArray() {
        return richiesteAArray;
    }
    
    public void addRichiestaA(Event account) {
        try {
            richiesteASemaphore.acquire();
        } catch (InterruptedException ex) {}
        richiesteA.insert(account, account.getId());
        richiesteAArray.add(account);
        richiesteASemaphore.release();
    }
    
    public void removeRichiestaA(Event account) {
        try {
            richiesteASemaphore.acquire();
        } catch (InterruptedException ex) {}
        richiesteA.removeT(account.getId());
        richiesteAArray.remove(account);
        richiesteASemaphore.release();
    }
    
    public void accettaRichiestaA(Event account){
        try {
            richiesteASemaphore.acquire();
            amiciSemaphore.acquire();
        } catch (InterruptedException ex) {}
        richiesteA.removeT(account.getId());
        richiesteAArray.remove(account);
        amici.insert(account, account.getId());
        amiciArray.add(account);
        richiesteASemaphore.release();
        amiciSemaphore.release();
    }
    
    //Cancella account
    
    public void removeAccount(){
        EventManager.rimuoviAccount(this);
        for(int i=0; i<amiciArray.size(); i++){
            amiciArray.get(i).removeAmico(this);
            amiciArray.get(i).save();
        }
        for(int i=0; i<richiesteDaArray.size(); i++){
            richiesteDaArray.get(i).removeRichiestaA(this);
            richiesteDaArray.get(i).save();
        }
        for(int i=0; i<richiesteAArray.size(); i++){
            richiesteAArray.get(i).removeRichiestaDa(this);
            richiesteAArray.get(i).save();
        }
        removeImmagineProfilo();
        File old = new File(pathFileAccounts+this.getUsername()+".txt");
        old.delete();
    }
    
    public void save(){
        PrintWriter writer;
        try {
            writer = new PrintWriter(EventManager.pathFileAccounts+this.getId()+".txt");
            writer.println(this.toString());
            writer.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    public String toStringMinimal(){
        String s = "";
        s += this.id+"\r\n";
        s += this.username+"\r\n";
        s += this.name+"\r\n";
        
        return s;
    }
    
    @Override
    public String toString(){
        String s = "";
        s += this.id+"\r\n";
        s += this.username+"\r\n";
        s += this.email+"\r\n";
        s += this.name+"\r\n";
        s += this.password+"\r\n";
            
        //Amici
        for(int i=0; i<this.amiciArray.size(); i++)
            s += this.amiciArray.get(i).getId()+";";
        s += "\r\n";
            
        //RichiesteDa
        for(int i=0; i<this.richiesteDaArray.size(); i++)
            s += this.richiesteDaArray.get(i).getId()+";";
        s += "\r\n";
        
        //RichiesteA
        for(int i=0; i<this.richiesteAArray.size(); i++)
            s += this.richiesteAArray.get(i).getId()+";";
        s += "\r\n";
        
        return s;
    }
}

package Events;

import Accounts.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Scanner;
import servereventorganizer.StruttureDati.SuffixTree;
import utils.Utils;

/**
 *
 * @author pigro
 */
public class EventManager {
    
    public static SuffixTree<Event> accountsById = new SuffixTree<>();
    public static SuffixTree<Event> accountsByUsername = new SuffixTree<>();
    public static SuffixTree<Event> accountsByEmail = new SuffixTree<>();
    public static SuffixTree<Event> accountsByName = new SuffixTree<>();
    public static String lastId = "000000000/";
    
    public static final String typeImages = "jpg";
    public static final String pathFileAccounts = "Database/Users/";
    public static final String pathProfilePhotoAccounts = "Database/ProfilePhotos/";
    public static final String pathProfilePhotoAccountsDefault = "Database/Esempi/ImmagineProfiloDefault."+typeImages;
    
    public static ArrayList<Event> getAccounts(){
        return accountsById.getTStartWith("");
    }
    
    public static void aggiungiAccount(Event account){
        accountsById.insert(account, account.getId());
        accountsByUsername.insert(account, account.getUsername());
        accountsByEmail.insert(account, account.getEmail());
        accountsByName.insert(account, account.getName());
        if(lastId.compareTo(account.getId()) < 0)
            lastId = account.getId();
    }
    
    public static String getNextId(){
        //L'id va da 0 a 9, poi da 'A' a 'Z' e poi da 'a' a 'z'
        String li = lastId;
        for(int i=li.length()-1; i >= 0; i--){
            if(li.charAt(i) == '9'){
                li = li.substring(0, i)+"A"+li.substring(i+1);
                break;
            } else if(li.charAt(i) == 'Z'){
                li = li.substring(0, i)+"a"+li.substring(i+1);
                break;
            } else if(li.charAt(i) == 'z'){
                li = li.substring(0, i)+"0"+li.substring(i+1);
            } else {
                int c = li.charAt(i)+1;
                li = li.substring(0, i)+(char)c+li.substring(i+1);
                break;
            }
        }
        return li;
    }
    
    
    public static Event getAccountById(String id){
        Event account = accountsById.getTEquals(id);
        return account;
    }
    
    public static Event getAccountByUsername(String username){
        Event account = accountsByUsername.getTEquals(username);
        return account;
    }
    
    public static Event getAccountByEmail(String email){
        Event account = accountsByEmail.getTEquals(email);
        return account;
    }
    
    public static Event getAccountByName(String name){
        Event account = accountsByName.getTEquals(name);
        return account;
    }
    
    public static Event creaAccount(String id, String username, String name, String email, String password){
        Event account = new Event();
        account.setId(id);
        account.setUsername(username);
        account.setEmail(email);
        account.setName(name);
        account.setPassword(password);
        aggiungiAccount(account);
        return account;
    }
    
    public static Event creaAccount(String id){
        Event account = new Event();
        account.setId(id);
        accountsById.insert(account, id);
        return account;
    }
    
    public static void rimuoviAccount(Event account){
        accountsById.removeT(account.getId());
        accountsByUsername.removeT(account.getUsername());
        accountsByEmail.removeT(account.getEmail());
        accountsByName.removeT(account.getName());
    }
    
    public static void saveAccountsToFiles(){
        ArrayList<Event> accounts = getAccounts();
        for(int i=0; i<accounts.size(); i++)
            accounts.get(i).save();
    }
    
    public static void loadAccountsFromFiles(){
        File folder = new File(pathFileAccounts);
        File[] files = folder.listFiles();
        try {
            for(int i=0; i<files.length; i++){
                Event account;
                Scanner scanner = new Scanner(files[i]);
                
                String id = scanner.nextLine();
                account = getAccountById(id);
                if(account == null)
                    account = creaAccount(id);
                
                account.setUsername(scanner.nextLine());
                account.setEmail(scanner.nextLine());
                account.setName(scanner.nextLine());
                account.setPassword(scanner.nextLine());
                
                //Amici
                String amici = scanner.nextLine();
                ArrayList<String> amiciStrings = Utils.getStringsFromStringWithPuntoEVirgola(amici);
                for (int j=0; j<amiciStrings.size(); j++){
                    Event amico = getAccountById(amiciStrings.get(j));
                    if(amico == null){
                        amico = creaAccount(amiciStrings.get(j));
                    }
                    account.addAmico(amico);
                }

                //RichiesteDa
                String richiesteDa = scanner.nextLine();
                ArrayList<String> richiesteDaStrings = Utils.getStringsFromStringWithPuntoEVirgola(richiesteDa);
                for (int j=0; j<richiesteDaStrings.size(); j++){
                    Event richiestaDa = getAccountById(richiesteDaStrings.get(j));
                    if(richiestaDa == null){
                        richiestaDa = creaAccount(richiesteDaStrings.get(j));
                    }
                    account.addRichiestaDa(richiestaDa);
                }

                //RichiesteA
                String richiesteA = scanner.nextLine();
                ArrayList<String> richiesteAStrings = Utils.getStringsFromStringWithPuntoEVirgola(richiesteA);
                for (int j=0; j<richiesteAStrings.size(); j++){
                    Event richiestaA = getAccountById(richiesteAStrings.get(j));
                    if(richiestaA == null){
                        richiestaA = creaAccount(richiesteAStrings.get(j));
                    }
                    account.addRichiestaA(richiestaA);
                }
                
                aggiungiAccount(account);
                
                scanner.close();
            }
            
        } catch(Exception ex){
            ex.printStackTrace();
        }
    }
}

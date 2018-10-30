package servereventorganizer.StruttureDati;

import java.util.ArrayList;
import java.util.concurrent.Semaphore;

public class SuffixTree<T> {
    
    private T object;
    
    private char carattere = '*';
    private ArrayList<SuffixTree> suffixTrees = new ArrayList<>();
    private Semaphore suffixTreesSemaphore = new Semaphore(1000000);

    public SuffixTree(){}

    public SuffixTree(char carattere){
        this.carattere = carattere;
    }
    
    public void insert(T object, String s){
        try {
            suffixTreesSemaphore.acquire();
        } catch (InterruptedException ex) {}
        insert(object, s, 0);
        suffixTreesSemaphore.release();
    }
    private void insert(T object, String s, int at){
        if(s.length() == at){
            this.object = object;
            return;
        }

        for(int i=0; i<suffixTrees.size(); i++){
            if(s.charAt(at) == suffixTrees.get(i).getCarattere()) {
                suffixTrees.get(i).insert(object, s, at + 1);
                return;
            } else if(s.charAt(at) < suffixTrees.get(i).getCarattere()) {
                SuffixTree ramo = new SuffixTree(s.charAt(at));
                suffixTrees.add(i, ramo);
                ramo.insert(object, s, at+1);
                return;
            }
        }
        SuffixTree ramo = new SuffixTree(s.charAt(at));
        suffixTrees.add(ramo);
        ramo.insert(object, s, at+1);
        return;
    }
    
    public ArrayList<T> getTStartWith(String string){
        ArrayList<T> objects = new ArrayList<>();
        try {
            suffixTreesSemaphore.acquire();
        } catch (InterruptedException ex) {}
        getTStartWith(objects, string, 0);
        suffixTreesSemaphore.release();
        return objects;
    }
    private ArrayList<T> getTStartWith(ArrayList<T> objects, String string, int at){
        if(string.length() <= at){
            if(this.object != null)
                objects.add(this.object);
            for(int i=0; i<suffixTrees.size(); i++){
                suffixTrees.get(i).getTStartWith(objects, string, at+1);
            }
        } else {
            for(int i=0; i<suffixTrees.size(); i++){
                if(suffixTrees.get(i).carattere == string.charAt(at)){
                    suffixTrees.get(i).getTStartWith(objects, string, at+1);
                    break;
                }
            }
        }
        return objects;
    }
    
    public ArrayList<T> getTStartWith(String string, int nRisultatiMax){
        ArrayList<T> objects = new ArrayList<>();
        try {
            suffixTreesSemaphore.acquire();
        } catch (InterruptedException ex) {}
        getTStartWith(objects, string, 0, nRisultatiMax);
        suffixTreesSemaphore.release();
        return objects;
    }
    private ArrayList<T> getTStartWith(ArrayList<T> objects, String string, int at, int nRisultatiMax){
        if(objects.size() < nRisultatiMax){
            if(string.length() <= at){
                if(this.object != null)
                    objects.add(this.object);
                for(int i=0; i<suffixTrees.size(); i++){
                    suffixTrees.get(i).getTStartWith(objects, string, at+1, nRisultatiMax);
                }
            } else {
                for(int i=0; i<suffixTrees.size(); i++){
                    if(suffixTrees.get(i).carattere == string.charAt(at)){
                        suffixTrees.get(i).getTStartWith(objects, string, at+1, nRisultatiMax);
                        break;
                    }
                }
            }
        }
        return objects;
    }

    public T getTEquals(String s){
        T object;
        try {
            suffixTreesSemaphore.acquire();
        } catch (InterruptedException ex) {}
        object = getTEquals(s, 0);
        suffixTreesSemaphore.release();
        return object;
    }
    private T getTEquals(String s, int at){
        if(s.length() == at){
            return this.object;
        } else if(s.length() > at) {
            for(int i=0; i<suffixTrees.size(); i++){
                if(suffixTrees.get(i).carattere == s.charAt(at)){
                    return (T) suffixTrees.get(i).getTEquals(s, at+1);
                }
            }
        }
        return null;
    }
    
    public void removeT(String s){
        try {
            suffixTreesSemaphore.acquire();
        } catch (InterruptedException ex) {}
        removeT(s, 0);
        suffixTreesSemaphore.release();
    }
    private void removeT(String s, int at){
        if(s.length() == at){
            this.object = null;
        } else if(s.length() > at) {
            for(int i=0; i<suffixTrees.size(); i++){
                if(suffixTrees.get(i).carattere == s.charAt(at)){
                    suffixTrees.get(i).removeT(s, at+1);
                    if(suffixTrees.get(i).sizeSuffixTrees() == 0 && suffixTrees.get(i).getT() == null)
                        suffixTrees.remove(i);
                } else if(s.charAt(at) < suffixTrees.get(i).getCarattere()) {
                    return;
                }
            }
        }
        return;
    }
    
    public void setT(T object){
        this.object = object;
    }

    public T getT() {
        return object;
    }

    public void setCarattere(char carattere) {
        this.carattere = carattere;
    }

    public char getCarattere() {
        return carattere;
    }
    
    public int sizeSuffixTrees(){
        return suffixTrees.size();
    }
}

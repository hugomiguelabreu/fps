package UI;

import javafx.collections.ObservableMap;

import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

public class Teste {

    public static Observable observable;
    private static ArrayList<String> array;

    public static void main(String[] args) {

        array = new ArrayList<>();

        for (int i=0; i<100; i++ ){

            array.add("lol" + i);

            observable.notifyObservers();

        }

    }

    public static void addObserver(Observer o){

        observable.addObserver(o);

    }

    public static ArrayList<String> getArray() {
        return array;
    }

    public static Observable getObservable() {
        return observable;
    }
}

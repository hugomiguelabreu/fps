package Event;

import com.turn.ttorrent.common.Torrent;

import java.util.ArrayList;

public class ArrayListEvent<E> extends ArrayList<E>{

    private ArrayEvent arrayEvent;

    public void registerCallback(ArrayEvent arrayEvent){

        this.arrayEvent = arrayEvent;
    }

//    @Override
//    public boolean add(E e){
//        boolean b = super.add(e);
//        arrayEvent.addEvent();
//        return b;
//    }

    public boolean addTorrent(E e){
        boolean b = super.add(e);
        arrayEvent.addEventTorrent((Torrent) e);
        return b;
    }


//    // TODO vai dar bosta eventualmente
//    public boolean addReturnIndex(E e){
//        boolean b = super.add(e);
//        arrayEvent.addReturnIndex(super.size()); // so e coerente se for single thread
//        return b;
//    }


}

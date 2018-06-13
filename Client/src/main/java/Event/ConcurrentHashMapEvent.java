package Event;

import java.util.concurrent.ConcurrentHashMap;

public class ConcurrentHashMapEvent<K,V> extends ConcurrentHashMap<K,V> {

    private MapEvent mapEvent;

    public void registerCallback(MapEvent mapEvent){
        this.mapEvent = mapEvent;
    }

    @Override
    public V put(K key, V value){
        V ret = super.put(key,value);
        mapEvent.putEvent();
        return ret;
    }

    @Override
    public V remove(Object key){
        V ret = super.remove(key);
        mapEvent.removeEvent();
        return ret;
    }




}

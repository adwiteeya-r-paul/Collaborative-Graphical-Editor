import java.util.Map;
import java.util.TreeMap;
import java.util.Set;


public class Sketch {
    static TreeMap<Integer, Shape> shapeid;

    public Sketch() {
        shapeid = new TreeMap<>();
    }

    public synchronized TreeMap<Integer, Shape> addShape(int id, Shape s) {
        shapeid.put(id, s);
        return shapeid;
    }

    public static synchronized int getsize() {
        return shapeid.size();
    }

    public static synchronized TreeMap<Integer, Shape> getMap(){return shapeid;}

    public static synchronized Shape getVal(Integer key) { return shapeid.get(key);}

    public String toString(){
        String s = "";
        for (int id: shapeid.keySet()){
            s += "add " + id  + " " + shapeid.get(id).toString()+"\n";
        }

    return s;}

    public Set<Integer> descendingKeySet() {
        return shapeid.descendingKeySet();
    }

    public void remove(Integer id) {
         shapeid.remove(id);
    }
}








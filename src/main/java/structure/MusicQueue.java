package structure;
import java.util.Collection;
import java.util.Iterator;
import java.util.Queue;

/**
 * 
 * @author Andrew Carlson
 */
public class MusicQueue implements Queue<Song>{

    
    private int size;
    private boolean isEmpty;
    private Queue<Song> queue;


    public MusicQueue(){
        this.size = 0;
        this.isEmpty = true;
        this.queue = null;

    }

    /**
     * Returns the current size of the queue.
     */
    @Override
    public int size(){
        return this.size;
    }

    /**
     * Checks to see if the queue is empty.
     * 
     * @return True if queue is empty, else false.
     */
    @Override
    public boolean isEmpty() {
        return isEmpty;
    }

    /**
     * Searches the queue for a specific element.
     * 
     * @param o The desired element.
     * @return True if element is found inside the queue, else false.
     */
    @Override
    public boolean contains(Object o) {
        for(Object e: queue){
            if(e == o){
                return true;
            }
        }
        return false;
    }

    @Override
    public Iterator<Song> iterator() {
        return queue.iterator();
    }

    
    @Override
    public Object[] toArray() {
        return queue.toArray();
    }

    @Override
    public Object[] toArray(Object[] a) {
        return queue.toArray(a);
    }

    @Override
    public boolean remove(Object o) {
        return queue.remove(o);
    }

    @Override
    public boolean containsAll(Collection c) {
        return queue.contains(c);
    }

    @Override
    public boolean addAll(Collection c) {
        return queue.addAll(c);
    }

    @Override
    public boolean removeAll(Collection E) {
        return queue.removeAll(E);
    }

    @Override
    public boolean retainAll(Collection c) {
        return queue.retainAll(c);
    }

    @Override
    public void clear() {
        queue.clear();
    }

    @Override
    public boolean add(Song e) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean offer(Song e) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public Song remove() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Song poll() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Song element() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Song peek() {
        // TODO Auto-generated method stub
        return null;
    }
    
}

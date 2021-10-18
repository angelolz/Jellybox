package structure;

import java.util.LinkedList;

/**
 * 
 * @author Andrew Carlson
 */
public class MusicQueue extends LinkedList<Song>{

    
    private int size;
    private boolean isEmpty;
    private LinkedList<Song> queue;


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
    public void clear() {
        queue.clear();
    }

    @Override
    public boolean add(Song e) {
        if(queue.add(e)){
            return true;
        }
        return false;
    }

    @Override
    public Song remove() {
        return null;
    }

    @Override
    public Song poll() {
        return null;
    }

    @Override
    public Song element() {
        return null;
    }

    @Override
    public Song peek() {
        return null;
    }
}

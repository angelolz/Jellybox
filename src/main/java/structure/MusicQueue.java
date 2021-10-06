package structure;
import java.util.Collection;
import java.util.Iterator;
import java.util.Queue;

/**
 * 
 * @author Andrew Carlson
 */
public class MusicQueue<T> implements Queue{

    private int size;
    private boolean isEmpty;
    private Queue<Object> queue;

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
    public Iterator<Object> iterator() {
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
    public boolean removeAll(Collection c) {
        return queue.removeAll(c);
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
    public boolean add(Object e) {
        isEmpty = false;
        return queue.add(e);
    }

    @Override
    public boolean offer(Object e) {
        return offer(e);
    }

    @Override
    public Object remove() {
        if(size <= 1){
            isEmpty = true;
        }
        size--;
        return queue.remove();
    }

    @Override
    public Object poll() {
        return queue.poll();
    }

    @Override
    public Object element() {
        return queue.element();
    }

    @Override
    public Object peek() {
        return queue.peek();
    }
    
}

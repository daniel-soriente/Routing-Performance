package ass1;

import java.util.LinkedList;
import java.util.Queue;

public class QueueInterface<T> {

	public void add(T e) {
		queue.add(e);
	}

	public T remove() {
		return queue.remove();
	}
	
	public boolean isEmpty() {
		return queue.isEmpty();
	}
	
	public boolean contains(T e) {
		return queue.contains(e);
	}

	private Queue<T> queue = new LinkedList<T>();
}

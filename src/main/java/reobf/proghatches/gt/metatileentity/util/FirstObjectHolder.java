package reobf.proghatches.gt.metatileentity.util;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Optional;

public class FirstObjectHolder<T> implements java.util.List<T>{
	T obj;
	public Optional<T> opt(){
		
		return Optional.ofNullable(obj);
	}
	@Override
	public int size() {
		throw new RuntimeException();
		
	}

	@Override
	public boolean isEmpty() {
		throw new RuntimeException();
		
	}

	@Override
	public boolean contains(Object o) {
		throw new RuntimeException();
		
	}

	@Override
	public Iterator iterator() {
		throw new RuntimeException();
		
	}

	@Override
	public Object[] toArray() {
		throw new RuntimeException();
		
	}

	@Override
	public Object[] toArray(Object[] a) {
		throw new RuntimeException();
		
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean add(Object e) {
		if(obj!=null)return false;
		obj=(T) e;
		return true;
	}

	@Override
	public boolean remove(Object o) {
		throw new RuntimeException();
		
	}

	@Override
	public boolean containsAll(Collection c) {
		throw new RuntimeException();
		
	}

	@Override
	public boolean addAll(Collection c) {
		throw new RuntimeException();
	
	}

	@Override
	public boolean addAll(int index, Collection c) {
		throw new RuntimeException();
		
	}

	@Override
	public boolean removeAll(Collection c) {
		throw new RuntimeException();
		
	}

	@Override
	public boolean retainAll(Collection c) {
		throw new RuntimeException();
	
	}

	@Override
	public void clear() {
		throw new RuntimeException();
		
	}

	@Override
	public T get(int index) {
		throw new RuntimeException();
		
	}

	@Override
	public T set(int index, Object element) {
		throw new RuntimeException();
		
	}

	@Override
	public void add(int index, Object element) {
		throw new RuntimeException();
		
	}

	@Override
	public T remove(int index) {
		throw new RuntimeException();
		
	}

	@Override
	public int indexOf(Object o) {
		throw new RuntimeException();
		
	}

	@Override
	public int lastIndexOf(Object o) {
		throw new RuntimeException();
		
	}

	@Override
	public ListIterator listIterator() {
		throw new RuntimeException();
		
	}

	@Override
	public ListIterator listIterator(int index) {
		throw new RuntimeException();
	
	}

	@Override
	public List subList(int fromIndex, int toIndex) {
		throw new RuntimeException();
	
	}

}

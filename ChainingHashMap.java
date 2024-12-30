package hw5;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

public class ChainingHashMap<K, V> implements Map<K, V> {

  private ArrayList<Entry<K,V>>[] table;
  private int capacity;
  private int primeIndex;
  private int size;
  private double loadFactor;
  private final int[] primes = {11, 23, 47, 97, 197, 397, 797, 1597, 3203, 6421, 12853, 25717, 51437, 102877,
      205759, 411527, 823117, 1646237, 3292489, 6584983, 13169977};

  /**
   * Create an empty map.
   */
  public ChainingHashMap() {
    this.primeIndex = 0;
    this.table = new ArrayList[primes[primeIndex]];
    this.capacity = primes[0];
    this.size = 0;
    this.loadFactor = 0;
  }

  @Override
  public void insert(K k, V v) throws IllegalArgumentException {
    if (loadFactor >= 0.50) {
      rehash();
    }
    if (has(k) || k == null) {
      throw new IllegalArgumentException();
    }
    int index = getIndex(k);
    if (table[index] == null) {
      table[index] = new ArrayList<>();
    }
    table[index].add(new Entry<>(k, v));
    size++;
    loadFactor = (double) size / (double) capacity;
  }

  @Override
  public V remove(K k) throws IllegalArgumentException {
    if (k == null || get(k) == null) {
      throw new IllegalArgumentException();
    }
    List<Entry<K,V>> list = table[getIndex(k)];
    int index = 0;
    V v = null;
    for (Entry<K,V> entry : list) {
      if (entry.key.equals(k)) {
        v = entry.value;
        break;
      }
      index++;
    }
    list.remove(index);
    size--;
    loadFactor = (double) size / capacity;
    return v;
  }

  private int getIndex(K k) {
    return (k.hashCode() & 0x7FFFFFFF) % capacity;
  }

  private void rehash() {
    primeIndex++;
    if (primeIndex < primes.length) {
      // capacity is the next largest in the array
      capacity = primes[primeIndex];
    } else {
      // no more available elements in the primes[] array, so the increase the capacity, double the current.
      capacity *= 2;
    }
    ArrayList<Entry<K,V>>[] oldTable = table;
    table = new ArrayList[capacity];
    for (List<Entry<K, V>> group : oldTable) {
      if (group != null) {
        for (Entry<K, V> entry : group) {
          insert(entry.key, entry.value);
        }
      }
    }
  }

  @Override
  public void put(K k, V v) throws IllegalArgumentException {
    Entry<K,V> entry = find(k);
    if (entry == null) {
      throw new IllegalArgumentException();
    }
    entry.value = v;
  }

  @Override
  public V get(K k) throws IllegalArgumentException {
    Entry<K,V> entry = find(k);
    if (entry == null) {
      throw new IllegalArgumentException();
    }
    return entry.value;
  }

  @Override
  public boolean has(K k) {
    if (k == null) {
      return false;
    }
    Entry<K,V> entry = find(k);
    return entry != null;
  }

  // Find entry for key k, throw exception if k is null.
  private Entry<K,V> find(K k) {
    if (k == null) {
      throw new IllegalArgumentException();
    }
    List<Entry<K,V>> list = table[getIndex(k)];
    if (list != null) {
      for (Entry<K,V> entry : list) {
        if (entry.key.equals(k)) {
          return entry;
        }
      }
    }
    return null;
  }

  @Override
  public int size() {
    return size;
  }

  @Override
  public Iterator<K> iterator() {
    return new HashmapIterator();
  }

  // Entry to store a key, and a value pair.
  private static class Entry<K,V> {
    K key;
    V value;

    Entry(K k, V v) {
      this.key = k;
      this.value = v;
    }
  }

  private class HashmapIterator implements Iterator<K> {
    private int listIndex;
    private int insideListIndex;
    private Entry<K,V> next;

    HashmapIterator() {
      this.listIndex = 0;
      this.insideListIndex = 0;
      this.next = findNext();
    }

    private Entry<K,V> findNext() {
      while (listIndex < table.length) {
        if (table[listIndex] != null && insideListIndex < table[listIndex].size()) {
          Entry<K,V> entry = table[listIndex].get(insideListIndex);
          insideListIndex++;
          return entry;
        } else {
          listIndex++;
          insideListIndex = 0;
        }
      }
      return null;
    }

    @Override
    public boolean hasNext() {
      return next != null;
    }

    @Override
    public K next() {
      if (!hasNext()) {
        throw new NoSuchElementException();
      }
      Entry<K,V> entry = next;
      next = findNext();
      return entry.key;
    }
  }
}

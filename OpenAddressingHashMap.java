package hw5;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class OpenAddressingHashMap<K, V> implements Map<K, V> {

  private Entry<K,V>[] table;
  private int capacity;
  private int size;
  private final double loadFactorThreshold = 0.50;
  private int primeIndex;
  private double loadFactor;
  private final int[] primes = {11, 23, 47, 97, 197, 397, 797, 1597, 3203, 6421, 12853, 25717, 51437, 102877,
      205759, 411527, 823117, 1646237, 3292489, 6584983, 13169977};

  /**
   * Create an empty map.
   */
  public OpenAddressingHashMap() {
    this.primeIndex = 0;
    this.table = new Entry[primes[primeIndex]];
    this.capacity = primes[0];
    this.size = 0;
    this.loadFactor = 0;
  }

  @Override
  public void insert(K k, V v) throws IllegalArgumentException {
    if (k == null || has(k)) {
      throw new IllegalArgumentException();
    }
    if (loadFactor >= loadFactorThreshold) {
      rehash();
    }
    insertHelper(k, v);
    loadFactor = (double) size / (double) capacity;
  }

  private void insertHelper(K k, V v) {
    int tombstone = -1;
    int index = ((getIndex(k)) & 0x7FFFFFFF) % capacity;
    while (table[index] != null) {
      if (table[index].isTombstone && tombstone == -1) {
        tombstone = index;
      }
      index++;
      if (index >= capacity) {
        index = 0;
      }
    }
    if (tombstone != -1) {
      table[tombstone] = new Entry<>(k, v);
    } else {
      table[index] = new Entry<>(k, v);
    }
    size++;
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
    Entry<K,V>[] oldTable = table;
    table = new Entry[capacity];
    for (Entry<K, V> entry : oldTable) {
      if (entry != null && !entry.isTombstone) {
        insert(entry.key, entry.value);
      }
    }
  }

  private int getIndex(K k) {
    return ((k.hashCode() & 0x7FFFFFFF) % capacity);
  }

  private int hash(K k) {
    return 1 + (k.hashCode() & 0x7FFFFFFF) % (capacity - 2);
  }

  @Override
  public V remove(K k) throws IllegalArgumentException {
    Entry<K,V> entry = find(k);
    if (entry == null) {
      throw new IllegalArgumentException("Key cannot be null");
    }
    entry.isTombstone = true;
    size--;
    return entry.value;
  }

  @Override
  public void put(K k, V v) throws IllegalArgumentException {
    Entry<K,V> e = this.find(k);
    if (e == null || e.isTombstone) {
      throw new IllegalArgumentException();
    }
    e.value = v;
  }

  @Override
  public V get(K k) throws IllegalArgumentException {
    Entry<K,V> e = find(k);
    if (e == null) {
      throw new IllegalArgumentException();
    }
    return e.value;
  }

  @Override
  public boolean has(K k) {
    if (k == null) {
      return false;
    }
    Entry<K,V> e = find(k);
    return e != null;
  }

  // Find entry for key k, throw exception if k is null.
  private Entry<K,V> find(K k) {
    if (k == null) {
      throw new IllegalArgumentException("cannot handle null key");
    }

    int index = ((getIndex(k)) & 0x7FFFFFFF) % capacity;
    while (table[index] != null) {
      if (table[index].key.equals(k)) {
        if (!table[index].isTombstone) {
          return table[index];
        } else if (table[index].isTombstone) {
          return null;
        }
      }
      index++;
      if (index >= capacity) {
        index = 0;
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
    boolean isTombstone;

    Entry(K k, V v) {
      this.key = k;
      this.value = v;
      this.isTombstone = false;
    }
  }

  private class HashmapIterator implements Iterator<K> {
    private int index;
    private int entries;

    HashmapIterator() {
      this.index = 0;
      this.entries = 0;
    }

    @Override
    public boolean hasNext() {
      return entries < size;
    }

    @Override
    public K next() {
      if (!hasNext()) {
        throw new NoSuchElementException();
      }
      while (hasNext() && (table[index] == null || table[index].isTombstone)) {
        index++;
      }
      K k = table[index].key;
      entries++;
      index++;
      return k;
    }
  }
}

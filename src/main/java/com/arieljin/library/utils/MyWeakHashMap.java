package com.arieljin.library.utils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

public final class MyWeakHashMap<K, V> {
	private HashMap<K, WeakReference<V>> map = new HashMap<K, WeakReference<V>>();

	public WeakReference<V> put(K key, V value) {
		return map.put(key, new WeakReference<V>(value));
	}

	public List<V> values() {
		List<V> list = new ArrayList<V>();

		Set<K> removeSet = new HashSet<K>();

		for (Entry<K, WeakReference<V>> entry : map.entrySet()) {
			V v = entry.getValue().get();
			if (v != null) {
				list.add(v);
			} else {
				removeSet.add(entry.getKey());
			}
		}

		for (K key : removeSet) {
			map.remove(key);
		}

		return list;
	}

	public boolean containsKey(String key) {
		if (map.containsKey(key)) {
			V v = map.get(key).get();
			if (v == null) {
				map.remove(key);
				return false;
			}
			return true;
		}
		return false;
	}

	public Set<K> keySet() {
		return map.keySet();
	}

	public boolean isEmpty() {
		return map.isEmpty();
	}

	public void clear() {
		map.clear();
	}

	public V get(K key) {
		if (map.containsKey(key)) {
			V v = map.get(key).get();
			if (v == null) {
				map.remove(key);
			}
			return v;
		}
		return null;
	}

	public void remove(K key) {
		map.remove(key);
	}

	public int size() {
		return map.size();
	}
}
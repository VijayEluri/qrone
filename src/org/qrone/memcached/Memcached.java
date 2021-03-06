package org.qrone.memcached;

import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Set;

public interface Memcached {
	public void clearAll();
	public boolean contains(String key);
	public boolean remove(String key);
	public boolean remove(String key, long millisNoReAdd);
	public Set<String> removeAll(Collection<String> keys);
	public Set<String> removeAll(Collection<String> keys, long millisNoReAdd);
	public Object get(String key);
	public Map<String, Object> getAll(Collection<String> keys);
	public long increment(String key, long delta);
	public void put(String key, Object value);
	public void put(String key, Object value, int ttlmillis);
	public void put(String key, Object value, Date expire);
	public void put(String key, Object value, int ttlmillis, SetPolicy polocy);
	public void put(String key, Object value, Date expire, SetPolicy polocy);
	public void putAll(Map<String, Object> values);
	public void putAll(Map<String, Object> values, int ttlmillis);
	public void putAll(Map<String, Object> values, Date expire);
	public void putAll(Map<String, Object> values, int ttlmillis, SetPolicy polocy);
	public void putAll(Map<String, Object> values, Date expire, SetPolicy polocy);
	
	public enum SetPolicy{
		SET_ALWAYS,
		REPLACE_ONLY_IF_PRESENT ,
		ADD_ONLY_IF_NOT_PRESENT
	}
}

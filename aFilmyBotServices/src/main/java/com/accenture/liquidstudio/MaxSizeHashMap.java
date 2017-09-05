package com.accenture.liquidstudio;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.Map;

public class MaxSizeHashMap<K, V> extends LinkedHashMap<K, V> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4401624469091108086L;
	private final int maxSize;
	private long timeMapReset;

	public MaxSizeHashMap(int maxSize) {
		this.maxSize = maxSize;
		setMapResetTime();
//		System.out.println(timeMapReset);
	}

	/**
	 * Whenever get or put is called, 
	 */
	private void setMapResetTime() {
		LocalDateTime ldt = LocalDateTime.now().plusDays(1);
		timeMapReset = ldt.atZone(ZoneId.systemDefault()).toEpochSecond();
	}

	@Override
	public V get(Object key)
	{
//		System.out.println("Getting value from over ridden method");
		checkExpiredMap();
		V val = super.get(key);
		System.out.println("getting value from map --> "+val);
		return val;
	}
	
	@Override
	public V put(K key, V value)
	{
//		System.out.println("Putting value from over ridden method");
		checkExpiredMap();
		V val = super.put(key, value);
		System.out.println("\ninserting value to map --> "+value);
		return val;
	}

	/**
	 * Whenever get or put is called, this method is executed, it checks if the map needs to be emptied
	 * Preferably so in order to load new information
	 */
	private void checkExpiredMap() {
//		System.out.println("checking expired time");
		long currentTime = LocalDateTime.now().atZone(ZoneId.systemDefault()).toEpochSecond();
//		System.out.println(currentTime + " <--> " + timeMapReset);
		if(currentTime > timeMapReset)
			{
				System.out.println("resetting map");
				this.clear();
				setMapResetTime();
			}
	}

	@Override
	protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
		return size() > maxSize;
	}
}
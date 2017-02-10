package edu.teco.smartlambda.authentication.entities;

import javafx.util.Pair;

import java.util.List;

/**
 * Created by Matteo on 01.02.2017.
 */
public class User {
	
	private String name;
	private Key primaryKey;
	private boolean isAdmin;
	private List<Key> keyList;
	
	public Pair<Key, String> createKey() {
		return null;
	}
	
	
	
	public List<User> getVisibleUsers() {
		return null;
	}
	
	
}

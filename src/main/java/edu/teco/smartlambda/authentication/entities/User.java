package edu.teco.smartlambda.authentication.entities;

import edu.teco.smartlambda.authentication.AuthenticationService;
import edu.teco.smartlambda.authentication.InsufficientPermissionsException;
import javafx.util.Pair;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.util.Set;

import static javax.persistence.GenerationType.IDENTITY;

/**
 * Created by Matteo on 01.02.2017.
 */
@Entity
@Table(name = "User")
public class User {
	

	private int      id;
	private String   name;
	private Key      primaryKey;
	private boolean  isAdmin;
	private Set<Key> keyList;
	
	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "id", unique = true, nullable = false)
	private int getId() {
		return id;
	}
	
	private void setId(final int id) {
		this.id = id;
	}
	
	@Column(name = "name", unique = true, nullable = false)
	public String getName() {
		return name;
	}
	
	private void setName(final String name) {
		this.name = name;
	}
	
	@OneToOne(fetch = FetchType.LAZY,mappedBy = "User")
	public Key getPrimaryKey() {
		return primaryKey;
	}
	
	private void setPrimaryKey(final Key primaryKey) {
		this.primaryKey = primaryKey;
	}
	
	@Column(name = "isAdmin",nullable = false)
	public boolean isAdmin() {
		return isAdmin;
	}
	
	private void setAdmin(final boolean admin) {
		isAdmin = admin;
	}
	

	public Pair<Key, String> createKey() throws InsufficientPermissionsException {
		if (AuthenticationService.getInstance().getAuthenticatedKey().isPresent()) {
			if (AuthenticationService.getInstance().getAuthenticatedKey().get().equals(this.getPrimaryKey())) {
				//TODO create Key and return it
			}
		}
		throw new InsufficientPermissionsException();
			return null;
	}
	
	
	
	public Set<User> getVisibleUsers() {
		if (this.isAdmin) {
			//TODO return all Users
		} else {
			//TODO search in database for all own keys and all of their permissions for foreign Users. return them as a Set.
		}
		
			return null;
	}
	
	
}

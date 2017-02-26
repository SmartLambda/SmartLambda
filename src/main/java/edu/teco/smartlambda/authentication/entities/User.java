package edu.teco.smartlambda.authentication.entities;

import edu.teco.smartlambda.Application;
import edu.teco.smartlambda.authentication.AuthenticationService;
import edu.teco.smartlambda.authentication.InsufficientPermissionsException;
import edu.teco.smartlambda.authentication.NameConflictException;
import lombok.Getter;
import org.apache.commons.lang3.tuple.Pair;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.util.Set;

import static javax.persistence.GenerationType.IDENTITY;
import static org.torpedoquery.jpa.Torpedo.from;
import static org.torpedoquery.jpa.Torpedo.select;
import static org.torpedoquery.jpa.Torpedo.where;

/**
 * Created by Matteo on 01.02.2017.
 */
@Entity
@Table(name = "User")
public class User {
	@Getter
	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "id", unique = true, nullable = false)
	private int     id;
	@Getter
	@Column(name = "name", unique = true, nullable = false)
	private String  name;
	@Getter
	@OneToOne(fetch = FetchType.LAZY)
	@PrimaryKeyJoinColumn
	private Key     primaryKey;
	@Getter
	@Column(name = "isAdmin", nullable = false)
	private boolean isAdmin;
	
	public User() {
		//TODO (Git-Hub) authentication.
	}
	
	private void setId(final int id) {
		this.id = id;
	}
	
	private void setName(final String name) {
		this.name = name;
	}
	
	private void setPrimaryKey(final Key primaryKey) {
		this.primaryKey = primaryKey;
	}
	
	private void setAdmin(final boolean admin) {
		isAdmin = admin;
	}

	public Pair<Key, String> createKey(String name) throws InsufficientPermissionsException, NameConflictException {
		if (AuthenticationService.getInstance().getAuthenticatedKey().isPresent()) {
			if (AuthenticationService.getInstance().getAuthenticatedKey().get().equals(this.getPrimaryKey())) {
				//TODO create Key and return it
			}
		}
		throw new InsufficientPermissionsException();
	}
	
	@Transient
	public Set<User> getVisibleUsers() {
		if (this.isAdmin) {
			//TODO return all Users
		} else {
			//TODO search in database for all own keys and all of their permissions for foreign Users. return them as a Set.
		}
		
		return null;
	}
	
	/**
	 * Retrieve a single User by its name
	 *
	 * @param name User name
	 *
	 * @return a User object
	 */
	public static User getByName(final String name) {
		final User query = from(User.class);
		where(query.getName()).eq(name);
		return select(query).get(Application.getInstance().getSessionFactory().getCurrentSession());
	}
}

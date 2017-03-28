package edu.teco.smartlambda.identity;

import edu.teco.smartlambda.authentication.entities.User;
import lombok.Getter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import static javax.persistence.GenerationType.IDENTITY;

/**
 *
 */
@Entity
@Table(name = "GitHubCredential")
public class GitHubCredential {
	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "id", unique = true, nullable = false)
	@Getter
	private int    id;
	@Getter
	@Column(name = "accessToken", unique = true, nullable = false)
	private String accessToken;
	@Getter
	@OneToOne
	@JoinColumn(name = "user")
	private User user;
	
	/**
	 * Empty constructor, used by Hibernate
	 */
	public GitHubCredential() {
		
	}
	
	public GitHubCredential(final String accessToken, final User user) {
		this.accessToken = accessToken;
		this.user = user;
	}
}
	

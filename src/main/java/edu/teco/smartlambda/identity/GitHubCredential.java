package edu.teco.smartlambda.identity;

import lombok.Getter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import static javax.persistence.GenerationType.IDENTITY;

/**
 * Created on 07.03.17.
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
	
	/**
	 * Empty constructor, used by Hibernate
	 */
	public GitHubCredential() {
		
	}
	
	public GitHubCredential(String accessToken) {
		this.accessToken = accessToken;
	}
}
	

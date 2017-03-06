package edu.teco.smartlambda.authentication.entities;

import edu.teco.smartlambda.Application;
import edu.teco.smartlambda.authentication.AuthenticationService;
import edu.teco.smartlambda.identity.NullIdentityProvider;
import org.apache.commons.lang3.tuple.Pair;
import org.hibernate.Transaction;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class UserTest {
	
	private static AuthenticationService service;
	private static Map                   params;
	private static User                  user;

	@Before
	public void buildUp() {
		Application.getInstance().getSessionFactory().getCurrentSession().beginTransaction();
		service = AuthenticationService.getInstance();
		params     = new HashMap<String, String>();
		params.put("name", "UserTest.User");
		user = (User) new NullIdentityProvider().register(params)
				.getLeft();//TODO WTF!?
		service.authenticate(user.getPrimaryKey());
	}
	
	@After
	public void tearDown() throws Exception {
		final Transaction transaction = Application.getInstance().getSessionFactory().getCurrentSession().getTransaction();
		if (transaction.isActive()) transaction.rollback();
	}
	
	@Test
	public void createKey() throws Exception {
		
		final Pair<Key, String> keyPair = user.createKey("UserTest.createKey");
		final Key key = keyPair.getLeft();
		final String id;
		try {
			MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
			id = arrayToString(sha256.digest(keyPair.getRight().getBytes()));
			Assert.assertTrue(Key.getKeyById(id).isPresent());
		} catch (NoSuchAlgorithmException a) {
			Assert.fail();
		}
	}
	
	private String arrayToString(byte[] array) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < array.length; ++i) {
			sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100).substring(1,3));
		}
		return sb.toString();
	}
	
	@Test
	public void getVisibleUsers() throws Exception {
		final AuthenticationService authenticationService = AuthenticationService.getInstance();
		
		final Map<String, String>   params1                = new HashMap<>();
		params1.put("name", "UserTest.authenticateViaKey.User1");
		final User user1 = new NullIdentityProvider().register(params1).getLeft();
		
		final Map<String, String>   params2                = new HashMap<>();
		params2.put("name", "UserTest.authenticateViaKey.User2");
		final User user2 = new NullIdentityProvider().register(params2).getLeft();
		
		authenticationService.authenticate(user1.getPrimaryKey());
		assert authenticationService.getAuthenticatedKey().isPresent();
		
		Assert.assertFalse(user2.getVisibleUsers().contains(user1));
		
		user2.getPrimaryKey().grantPermission(user1, PermissionType.CREATE);
		
		Assert.assertFalse(user2.getVisibleUsers().contains(user2));
		Assert.assertTrue(user2.getVisibleUsers().contains(user1));
	}
}
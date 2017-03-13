package edu.teco.smartlambda.authentication.entities;

import edu.teco.smartlambda.Application;
import edu.teco.smartlambda.authentication.AuthenticationService;
import edu.teco.smartlambda.authentication.DuplicateKeyException;
import edu.teco.smartlambda.authentication.InsufficientPermissionsException;
import edu.teco.smartlambda.identity.NullIdentityProvider;
import edu.teco.smartlambda.lambda.AbstractLambda;
import edu.teco.smartlambda.lambda.Lambda;
import edu.teco.smartlambda.lambda.LambdaDecorator;
import edu.teco.smartlambda.lambda.LambdaFacade;
import edu.teco.smartlambda.runtime.RuntimeRegistry;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.hibernate.Transaction;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class UserTest {
	
	private AuthenticationService service;
	private User                  user;
	private Lambda                lambda;
	private final String          lambdaName = "UserTestLambda";
	
	@Before
	public void buildUp() throws Exception {
		Application.getInstance().getSessionFactory().getCurrentSession().beginTransaction();
		this.service = AuthenticationService.getInstance();
		final Map<String, String> params = new HashMap<>();
		params.put("name", "UserTest.User");
		this.user = new NullIdentityProvider().register(params).getLeft();
		this.service.authenticate(this.user.getPrimaryKey());
		
		final AbstractLambda abstractLambda = LambdaFacade.getInstance().getFactory().createLambda();
		this.lambda = LambdaDecorator.unwrap(abstractLambda);
		this.lambda.setOwner(this.user);
		this.lambda.setName(this.lambdaName);
		this.lambda.setRuntime(RuntimeRegistry.getInstance().getRuntimeByName("jre8"));
		this.lambda.deployBinary(IOUtils.toByteArray(KeyTest.class.getClassLoader().getResourceAsStream("lambda.jar")));
		this.lambda.save();
	}
	
	@After
	public void tearDown() throws Exception {
		final Transaction transaction = Application.getInstance().getSessionFactory().getCurrentSession().getTransaction();
		if (transaction.isActive()) transaction.rollback();
	}
	
	@Test
	public void createKey() throws Exception {
		final Pair<Key, String> keyPair = this.user.createKey("UserTest.createKey");
		final String            id;
		
		final MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
		id = this.arrayToString(sha256.digest(keyPair.getRight().getBytes()));
		Assert.assertTrue(Key.getKeyById(id).isPresent());
	}
	
	@Test (expected = DuplicateKeyException.class)
	public void createKeyDuplicateName() throws Exception {
		this.user.createKey("UserTest.createKeyDuplicateName");
		this.user.createKey("UserTest.createKeyDuplicateName");
	}
	
	private String arrayToString(final byte[] array) {
		final StringBuilder sb = new StringBuilder();
		for (final byte currentByte : array) {
			sb.append(Integer.toHexString((currentByte & 0xFF) | 0x100).substring(1, 3));
		}
		return sb.toString();
	}
	
	@Test
	public void getVisibleUsersUserPermission() throws Exception {
		final AuthenticationService authenticationService = AuthenticationService.getInstance();
		
		final Map<String, String> params1 = new HashMap<>();
		params1.put("name", "UserTest.getVisibleUsersUserPermission.User1");
		final User user1 = new NullIdentityProvider().register(params1).getLeft();
		
		final Map<String, String> params2 = new HashMap<>();
		params2.put("name", "UserTest.getVisibleUsersUserPermission.User2");
		final User user2 = new NullIdentityProvider().register(params2).getLeft();
		
		authenticationService.authenticate(user1.getPrimaryKey());
		assert authenticationService.getAuthenticatedKey().isPresent();
		
		Assert.assertFalse(user2.getVisibleUsers().contains(user1));
		
		user2.getPrimaryKey().grantPermission(user1, PermissionType.CREATE);
		
		Assert.assertFalse(user2.getVisibleUsers().contains(user2));
		Assert.assertTrue(user2.getVisibleUsers().contains(user1));
	}
	
	@Test
	public void getVisibleUsersLambdaPermission() throws Exception {
		final AuthenticationService authenticationService = AuthenticationService.getInstance();

		final Map<String, String> params2 = new HashMap<>();
		params2.put("name", "UserTest.getVisibleUsersLambdaPermission.User2");
		final User user2 = new NullIdentityProvider().register(params2).getLeft();
		
		authenticationService.authenticate(this.user.getPrimaryKey());
		assert authenticationService.getAuthenticatedKey().isPresent();
		
		Assert.assertFalse(user2.getVisibleUsers().contains(this.user));
		
		user2.getPrimaryKey().grantPermission(this.lambda, PermissionType.CREATE);
		
		Assert.assertFalse(user2.getVisibleUsers().contains(user2));
		Assert.assertTrue(user2.getVisibleUsers().contains(this.user));
	}
	
	@Test
	public void getVisibleUsersAsAdmin() throws Exception {
		final Map<String, String> params1 = new HashMap<>();
		params1.put("name", "UserTest.getVisibleUsersAdmin.User1");
		final User user1 = new NullIdentityProvider().register(params1).getLeft();
		
		final Map<String, String> params2 = new HashMap<>();
		params2.put("name", "UserTest.getVisibleUsersAdmin.User2");
		final User user2 = new NullIdentityProvider().register(params2).getLeft();
		
		Assert.assertFalse(user2.getVisibleUsers().contains(user1));
		
		user2.setAdmin(true);
		
		Assert.assertTrue(user2.getVisibleUsers().contains(user2));
		Assert.assertTrue(user2.getVisibleUsers().contains(user1));
	}
	
	@Test
	public void testUserProperties() {
		final Map<String, String> params = new HashMap<>();
		final String              name   = "UserTest.testUserProperties.User";
		params.put("name", name);
		final User user = new NullIdentityProvider().register(params).getLeft();
		Assert.assertEquals(name, user.getName());
		Assert.assertNotNull(user.getId());
		Assert.assertNotEquals(0, user.getId());
	}
	
	@Test
	public void testUserAdminProperty() {
		Assert.assertFalse(this.user.isAdmin());
		this.user.setAdmin(true);
		Assert.assertTrue(this.user.isAdmin());
	}
	
	@Test(expected = InsufficientPermissionsException.class)
	public void createKeyWithInsufficientPermission() {
		final Map<String, String> params = new HashMap<>();
		final String              name   = "UserTest.createKeyWithInsufficientPermission.User";
		params.put("name", name);
		final User user = new NullIdentityProvider().register(params).getLeft();
		assert !(user.getPrimaryKey().equals(this.service.getAuthenticatedKey().orElseThrow(AssertionError::new)));
		user.createKey("UserTest.createKeyWithInsufficientPermission.Key");
	}
	
	@Test
	public void getKeyByName() throws Exception {
		final String name = "UserTest.getKeyByName";
		final Key    key  = this.user.createKey(name).getLeft();
		
		Assert.assertSame(this.user.getKeyByName(name).orElseThrow(AssertionError::new), key);
	}
	
	@Test (expected = InsufficientPermissionsException.class)
	public void getKeyByNameAsNonPrimaryKey() throws Exception {
		final String name = "UserTest.getKeyByNameAsNonPrimaryKey";
		final Key key = this.user.createKey(name).getLeft();
		AuthenticationService.getInstance().authenticate(key);
		this.user.getKeyByName(name);
	}
	
	@Test(expected = InsufficientPermissionsException.class)
	public void getVisibleLambdasWithoutPrimaryKey() throws Exception {
		final Map<String, String> params = new HashMap<>();
		final String              name   = "UserTest.getVisibleLambdasWithoutPrimaryKey.User";
		params.put("name", name);
		final User user = new NullIdentityProvider().register(params).getLeft();
		
		this.service.authenticate(user.createKey("UserTest.getVisibleLambdasWithoutPrimaryKey.Key").getLeft());
		
		assert !this.service.getAuthenticatedKey().orElseThrow(AssertionError::new).isPrimaryKey();
		user.getLambdas();
	}
	
	@Test
	public void getVisibleLambdas() throws Exception {
		final Map<String, String> params2 = new HashMap<>();
		params2.put("name", "UserTest.getVisibleLambdas.User2");
		final User user2 = new NullIdentityProvider().register(params2).getLeft();
		AuthenticationService.getInstance().authenticate(user2.getPrimaryKey());
		Assert.assertTrue(this.user.getVisibleLambdas().isEmpty());
		
		AuthenticationService.getInstance().authenticate(this.user.getPrimaryKey());
		user2.getPrimaryKey().grantPermission(this.lambda, PermissionType.READ);
		AuthenticationService.getInstance().authenticate(user2.getPrimaryKey());
		Assert.assertTrue(this.user.getVisibleLambdas().size() == 1);
		Assert.assertTrue(this.user.getVisibleLambdas().iterator().next().getName().equals(this.lambdaName));
	}
	

}
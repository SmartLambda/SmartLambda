package edu.teco.smartlambda.authentication.entities;

import edu.teco.smartlambda.Application;
import edu.teco.smartlambda.authentication.AuthenticationService;
import edu.teco.smartlambda.authentication.InsufficientPermissionsException;
import edu.teco.smartlambda.identity.NullIdentityProvider;
import edu.teco.smartlambda.lambda.AbstractLambda;
import edu.teco.smartlambda.lambda.Lambda;
import edu.teco.smartlambda.lambda.LambdaDecorator;
import edu.teco.smartlambda.lambda.LambdaFacade;
import edu.teco.smartlambda.runtime.RuntimeRegistry;
import org.apache.commons.compress.utils.IOUtils;
import org.hibernate.Transaction;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created on 07.02.2017.
 */
public class KeyTest {
	
	private Key    key;
	private Lambda lambda;
	private User   user;
	
	@Before
	public void buildUp() throws Exception{
		final AbstractLambda abstractLambda = LambdaFacade.getInstance().getFactory().createLambda();
		
		Application.getInstance().getSessionFactory().getCurrentSession().beginTransaction();
		
		final Map<String, String> params = new HashMap<>();
		params.put("name", "KeyTest.User");
		this.user = new NullIdentityProvider().register(params).getLeft();
		
		this.lambda = LambdaDecorator.unwrap(abstractLambda);
		this.lambda.setName("TestLambda");
		this.lambda.setOwner(this.user);
		this.lambda.setRuntime(RuntimeRegistry.getInstance().getRuntimeByName("jre8"));
		try {
			this.lambda.deployBinary(IOUtils.toByteArray(KeyTest.class.getClassLoader().getResourceAsStream("lambda.jar")));
		} catch (final AssertionError a) {
			//assert file.canExecute(); in edu.teco.smartlambda.container.DockerContainerBuilder.storeFile can be ignored for this testcase.
		}
		this.lambda.save();
		//Application.getInstance().getSessionFactory().getCurrentSession().save(lambda);
		
		AuthenticationService.getInstance().authenticate(this.user.getPrimaryKey());
		
		this.key = this.user.createKey("KeyTest.buildUp").getLeft();
		
		this.key.grantPermission(this.lambda, PermissionType.DELETE);
		this.key.grantPermission(this.lambda, PermissionType.EXECUTE);
		
		this.key.grantPermission(this.user, PermissionType.DELETE);
		this.key.grantPermission(this.user, PermissionType.GRANT);
	}
	
	@After
	public void tearDown() throws Exception {
		final Transaction transaction = Application.getInstance().getSessionFactory().getCurrentSession().getTransaction();
		if (transaction.isActive()) transaction.rollback();
	}
	
	@Test
	public void grantUserPermissionToOtherKey() throws Exception {
		final Key key2 = this.user.createKey("KeyTest.grantUserPermissionToOtherKey").getLeft();
		AuthenticationService.getInstance().authenticate(this.key);
		key2.grantPermission(this.user, PermissionType.DELETE);
		Assert.assertTrue(key2.hasPermission(this.user, PermissionType.DELETE));
	}
	
	@Test
	public void grantLambdaPermissionToOtherKey() throws Exception {
		final Key key2 = this.user.createKey("KeyTest.grantLambdaPermissionToOtherKey").getLeft();
		AuthenticationService.getInstance().authenticate(this.key);
		key2.grantPermission(this.lambda, PermissionType.DELETE);
		Assert.assertTrue(key2.hasPermission(this.lambda, PermissionType.DELETE));
	}
	
	@Test (expected = InsufficientPermissionsException.class)
	public void grantUserPermissionWithoutPermission() throws Exception{
		final Key otherKey = this.user.createKey("KeyTest.grantUserPermissionWithoutPermission").getLeft();
		AuthenticationService.getInstance().authenticate(otherKey);
		this.key.grantPermission(this.user, PermissionType.CREATE);
	}
	
	@Test (expected = InsufficientPermissionsException.class)
	public void grantLambdaPermissionWithoutPermission() throws Exception{
		final Key otherKey = this.user.createKey("KeyTest.grantUserPermissionWithoutPermission").getLeft();
		AuthenticationService.getInstance().authenticate(otherKey);
		this.key.grantPermission(this.lambda, PermissionType.CREATE);
	}
	
	@Test
	public void hasPermission() throws Exception {
		Assert.assertTrue(this.key.hasPermission(this.lambda, PermissionType.DELETE));
	}
	
	@Test
	public void hasPermission1() throws Exception {
		Assert.assertTrue(this.key.hasPermission(this.user, PermissionType.GRANT));
	}
	
	@Test
	public void getPermissions() throws Exception {
		
		final Set<PermissionType> expected = new HashSet<>();
		expected.add(PermissionType.DELETE);
		expected.add(PermissionType.GRANT);
		expected.add(PermissionType.EXECUTE);
		
		final Set<PermissionType> got = new HashSet<>();
		for (final Permission perm : this.key.getPermissions()) {
			got.add(perm.getPermissionType());
		}
		Assert.assertTrue(got.containsAll(expected));
		Assert.assertTrue(expected.containsAll(got));
	}
	
	@Test
	public void grantPermissions() throws Exception {
		
		this.key.grantPermission(this.lambda, PermissionType.DELETE);
		Assert.assertTrue(this.key.hasPermission(this.lambda, PermissionType.DELETE));
	}
	
	@Test
	public void delete() throws Exception {
		final String id = this.key.getId();
		this.key.delete();
		Assert.assertFalse(Key.getKeyById(id).isPresent());
	}
	
	@Test (expected = InsufficientPermissionsException.class)
	public void deleteWithoutPermission() throws Exception {
		final Key otherKey = this.user.createKey("KeyTest.deleteWithoutPermission").getLeft();
		AuthenticationService.getInstance().authenticate(otherKey);
		this.key.delete();
	}
	
	@Test
	public void revokePermission() throws Exception {
		Assert.assertTrue(this.key.hasPermission(this.lambda, PermissionType.EXECUTE));
		this.key.revokePermission(this.lambda, PermissionType.EXECUTE);
		Assert.assertFalse(this.key.hasPermission(this.lambda, PermissionType.EXECUTE));
	}
	
	@Test (expected = InsufficientPermissionsException.class)
	public void revokePermissionWithoutPermission() throws Exception {
		final Key otherKey = this.user.createKey("KeyTest.revokePermissionWithoutPermission").getLeft();
		AuthenticationService.getInstance().authenticate(otherKey);
		this.key.revokePermission(this.lambda, PermissionType.EXECUTE);
	}
	
	//TODO Cover Lombok Getters/Setters
	
	@Test
	public void revokePermissionUser() throws Exception {
		this.key.revokePermission(this.user, PermissionType.GRANT);
		final Set<PermissionType> got = new HashSet<>();
		for (final Permission perm : this.key.getPermissions()) {
			got.add(perm.getPermissionType());
		}
		Assert.assertFalse(got.contains(PermissionType.GRANT));
	}
	
	@Test (expected = InsufficientPermissionsException.class)
	public void revokePermissionUserWithoutPermission() throws Exception {
		final Key otherKey = this.user.createKey("KeyTest.revokePermissionUserWithoutPermission").getLeft();
		AuthenticationService.getInstance().authenticate(otherKey);
		this.key.revokePermission(this.user, PermissionType.GRANT);
	}
	
	@Test
	public void getVisiblePermissions() throws Exception {
		final AuthenticationService as = AuthenticationService.getInstance();
		
		final Map<String, String> params1 = new HashMap<>();
		params1.put("name", "KeyTest.getVisiblePermissions.User1");
		final User user1 = new NullIdentityProvider().register(params1).getLeft();
		final Key  key1  = user1.getPrimaryKey();

		this.lambda.setOwner(user1);
		this.lambda.save();

		
		final Map<String, String> params2 = new HashMap<>();
		params2.put("name", "KeyTest.getVisiblePermissions.User2");
		final User user2 = new NullIdentityProvider().register(params2).getLeft();
		final Key  key2  = user2.getPrimaryKey();
		
		
		final Map<String, String> params3 = new HashMap<>();
		params3.put("name", "KeyTest.getVisiblePermissions.User3");
		final User user3 = new NullIdentityProvider().register(params3).getLeft();
		final Key  key3  = user2.getPrimaryKey();
		
		as.authenticate(key1);
		
		//1. User-Permissions of this Key for the currently authenticated User
		assert as.getAuthenticatedKey().orElseThrow(AssertionError::new).equals(key1);
		Assert.assertFalse(this.containsPermission(key2.getVisiblePermissions(), user1, PermissionType.CREATE));
		key2.grantPermission(user1, PermissionType.CREATE);
		Assert.assertTrue(this.containsPermission(key2.getVisiblePermissions(), user1, PermissionType.CREATE));
		
		//2. Lambda-Permissions of this Key for the currently authenticated Users Lambdas
		assert as.getAuthenticatedKey().orElseThrow(AssertionError::new).equals(key1);
		Assert.assertFalse(this.containsPermission(key2.getVisiblePermissions(), this.lambda, PermissionType.READ));
		key2.grantPermission(this.lambda, PermissionType.READ);
		Assert.assertTrue(this.containsPermission(key2.getVisiblePermissions(), this.lambda, PermissionType.READ));
		
		//3. User-Permissions of this Key, sharing a User with the currently authenticated Keys GRANT-Permissions
		assert as.getAuthenticatedKey().orElseThrow(AssertionError::new).equals(key1);
		Assert.assertFalse(this.containsPermission(key3.getVisiblePermissions(), user1, PermissionType.PATCH));
		key2.grantPermission(user1, PermissionType.GRANT);
		key3.grantPermission(user1, PermissionType.PATCH);
		as.authenticate(key2);
		Assert.assertTrue(this.containsPermission(key3.getVisiblePermissions(), user1, PermissionType.PATCH));
		as.authenticate(key1);
		
		//4. Lambda-Permissions of this Key, sharing a Lambda with the currently authenticated Keys GRANT-Permissions
		assert as.getAuthenticatedKey().orElseThrow(AssertionError::new).equals(key1);
		Assert.assertFalse(this.containsPermission(key3.getVisiblePermissions(), this.lambda, PermissionType.STATUS));
		key2.revokePermission(user1, PermissionType.GRANT);//revoke user-permission because otherwise lambda-permission wouldn't be set
		key2.grantPermission(this.lambda, PermissionType.GRANT);
		key3.grantPermission(this.lambda, PermissionType.STATUS);
		as.authenticate(key2);
		Assert.assertTrue(this.containsPermission(key3.getVisiblePermissions(), this.lambda, PermissionType.STATUS));
		as.authenticate(key1);
	}
	
	private boolean containsPermission(final Set<Permission> permissions, final User user, final PermissionType type) {
		for (final Permission permission : permissions) {
			if (permission != null && permission.getUser() != null && permission.getPermissionType() != null) {
				if (permission.getUser().equals(user) && permission.getPermissionType().equals(type)) {
					return true;
				}
			}
		}
		return false;
	}
	
	private boolean containsPermission(final Set<Permission> permissions, final Lambda lambda, final PermissionType type) {
		for (final Permission permission : permissions) {
			if (permission != null && permission.getLambda() != null && permission.getPermissionType() != null) {
				if (permission.getLambda().equals(lambda) && permission.getPermissionType().equals(type)) {
					return true;
				}
			}
		}
		return false;
	}
}
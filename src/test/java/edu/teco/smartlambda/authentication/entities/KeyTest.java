package edu.teco.smartlambda.authentication.entities;

import edu.teco.smartlambda.Application;
import edu.teco.smartlambda.authentication.AuthenticationService;
import edu.teco.smartlambda.identity.NullIdentityProvider;
import edu.teco.smartlambda.lambda.Lambda;
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
 * Created by Matteo on 07.02.2017.
 */
public class KeyTest {
	
	Key    key;
	Lambda lambda;
	User   user;
	
	@Before
	public void buildUp() throws Exception{
		//lambda = LambdaFacade.getInstance().getFactory().createLambda(); TODO
		Application.getInstance().getSessionFactory().getCurrentSession().beginTransaction();
		Map<String, String> params = new HashMap<>();
		params.put("name", "KeyTest.User");
		user = new NullIdentityProvider().register(params).getLeft();
		AuthenticationService.getInstance().authenticate(user.getPrimaryKey());
	
		key = user.createKey("KeyTest.buildUp").getLeft();
		
		//key.grantPermission(lambda, PermissionType.DELETE);
		//key.grantPermission(lambda, PermissionType.EXECUTE);
		
		key.grantPermission(user, PermissionType.DELETE);
		key.grantPermission(user, PermissionType.GRANT);

		/*
			TODO Interesting test case: grant permission to create on behalf of another user
			and check if it works
		 */
	}
	
	@After
	public void tearDown() throws Exception {
		Transaction transaction = Application.getInstance().getSessionFactory().getCurrentSession().getTransaction();
		if (transaction.isActive()) transaction.rollback();
	}
	
	/*@Test
	public void hasPermission() throws Exception {
		
		Assert.assertTrue(key.hasPermission(lambda, PermissionType.DELETE));
		
	}*/
	
	@Test
	public void hasPermission1() throws Exception {
		Assert.assertTrue(key.hasPermission(user, PermissionType.GRANT));
	}
	
	@Test
	public void getPermissions() throws Exception {
		
		Set<PermissionType> expected = new HashSet<>();
		/*list.add(new Permission(lambda, PermissionType.DELETE, key));
		if (!revokedFirst) {
			list.add(new Permission(lambda, PermissionType.EXECUTE, key));
			
		}*/
		expected.add(PermissionType.DELETE);
		expected.add(PermissionType.GRANT);
		Set<PermissionType> got = new HashSet<>();
		for (Permission perm : key.getPermissions()) {
			got.add(perm.getPermissionType());
		}
		Assert.assertTrue(got.containsAll(expected));
		Assert.assertTrue(expected.containsAll(got));
	}
	
	/*@Test
	public void grantPermissions() throws Exception {
		
		key.grantPermission(lambda, PermissionType.DELETE);
		Assert.assertTrue(key.hasPermission(lambda, PermissionType.DELETE));
	}*/
	
	@Test
	public void delete() throws Exception {
		//TODO
	}
	
	/*@Test
	public void revokePermission() throws Exception {
		
		key.revokePermission(lambda, PermissionType.EXECUTE);
		Assert.assertFalse(key.hasPermission(lambda, PermissionType.EXECUTE));
		revokedFirst = true;
	}*/
	
	@Test
	public void revokePermissionUser() throws Exception {
		key.revokePermission(user, PermissionType.DELETE);
		Set<PermissionType> got = new HashSet<>();
		for (Permission perm : key.getPermissions()) {
			got.add(perm.getPermissionType());
		}
		Assert.assertFalse(got.contains(PermissionType.DELETE));
	}
}
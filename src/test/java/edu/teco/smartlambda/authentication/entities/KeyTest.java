package edu.teco.smartlambda.authentication.entities;

import edu.teco.smartlambda.Application;
import edu.teco.smartlambda.authentication.InsufficientPermissionsException;
import edu.teco.smartlambda.authentication.NameConflictException;
import edu.teco.smartlambda.identity.IdentityProviderRegistry;
import edu.teco.smartlambda.identity.NullIdentityProvider;
import edu.teco.smartlambda.lambda.Lambda;
import org.hibernate.Transaction;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by Matteo on 07.02.2017.
 */
public class KeyTest {
	
	Key    key;
	Lambda lambda = new Lambda();
	static User user;
	boolean revokedFirst = false;
	boolean revokedSecond = false;
	
	@Before
	public void buildUp() {
		Application.getInstance().getSessionFactory().getCurrentSession().beginTransaction();
		Map<String, String> params = new HashMap<>();
		params.put("name", "KeyTest.User");
		user    = IdentityProviderRegistry.getInstance().getIdentityProviderByName(NullIdentityProvider.class.getName()).register(params);
		
		try {
		key = user.createKey("KeyTest.buildUp").getLeft();

		key.grantPermission(lambda, PermissionType.DELETE);
		key.grantPermission(lambda, PermissionType.EXECUTE);
		
		key.grantPermission(user, PermissionType.DELETE);
		key.grantPermission(user, PermissionType.GRANT);
		} catch (InsufficientPermissionsException i) {
			Assert.fail();
		} catch (NameConflictException n) {
			Assert.fail("NameConflictException");
		}
		/*
			Interesting test case: grant permission to create on behalf of another user
			and check if it works
		 */
	}
	
	@After
	public void tearDown() throws Exception {
		Transaction transaction = Application.getInstance().getSessionFactory().getCurrentSession().getTransaction();
		if (transaction.isActive()) transaction.rollback();
	}
	
	@Test
	public void hasPermission() throws Exception {
		
		Assert.assertTrue(key.hasPermission(lambda, PermissionType.DELETE));
		
	}
	
	@Test
	public void hasPermission1() throws Exception {
		
		Assert.assertTrue(key.hasPermission(user, PermissionType.DELETE));
	}
	
	@Test
	public void getPermissions() throws Exception {
		
		List<Permission> list = new ArrayList<>();
		list.add(new Permission(lambda, PermissionType.DELETE, key));
		if (!revokedFirst) {
			list.add(new Permission(lambda, PermissionType.EXECUTE, key));
			
		}
		list.add(new Permission(user, PermissionType.DELETE, key));
		if (!revokedSecond) {
			list.add(new Permission(user, PermissionType.GRANT, key));
		}
		int size = list.size();

		Set<Permission> permissions = key.getPermissions();

		Assert.assertTrue(size == permissions.size());
		Assert.assertEquals(size, list.size());
		
	}
	
	@Test
	public void grantPermissions() throws Exception {
		
		key.grantPermission(lambda, PermissionType.DELETE);
		Assert.assertTrue(key.hasPermission(lambda, PermissionType.DELETE));
	}
	
	@Test
	public void isPrimaryKey() throws Exception {
		
	}
	
	@Test
	public void delete() throws Exception {
				
		/*
			TODO: delete from the database and check it's not there anymore
		 */
		
	}
	
	@Test
	public void revokePermission() throws Exception {
		
		key.revokePermission(lambda, PermissionType.EXECUTE);
		Assert.assertFalse(key.hasPermission(lambda, PermissionType.EXECUTE));
		revokedFirst = true;
	}
	
	@Test
	public void revokePermission1() throws Exception {
		
		key.revokePermission(user, PermissionType.GRANT);
		Assert.assertFalse(key.hasPermission(user, PermissionType.GRANT));
		revokedSecond = true;
		
	}
}
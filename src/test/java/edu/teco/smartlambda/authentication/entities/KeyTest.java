package edu.teco.smartlambda.authentication.entities;

import edu.teco.smartlambda.authentication.InsufficientPermissionsException;
import edu.teco.smartlambda.lambda.Lambda;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by Matteo on 07.02.2017.
 */
public class KeyTest {
	
	Key    key;
	Lambda lambda = new Lambda();
	User user = new User();
	boolean revokedFirst = false;
	boolean revokedSecond = false;
	
	
	@BeforeClass
	public void buildUp() {
		try {
		key = (Key) user.createKey().getRight();

		key.grantPermission(lambda, PermissionType.DELETE);
		key.grantPermission(lambda, PermissionType.EXECUTE);
		
		key.grantPermission(user, PermissionType.DELETE);
		key.grantPermission(user, PermissionType.GRANT);
		} catch (InsufficientPermissionsException i) {
			Assert.fail();
		}
		/*
			Interesting test case: grant permission to create on behalf of another user
			and check if it works
		 */
		
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
		list.add(new Permission(lambda, PermissionType.DELETE));
		if (!revokedFirst) {
			list.add(new Permission(lambda, PermissionType.EXECUTE));
			
		}
		list.add(new Permission(user, PermissionType.DELETE));
		if (!revokedSecond) {
			list.add(new Permission(user, PermissionType.GRANT));
		}
		int size = list.size();
		Method m = key.getClass().getDeclaredMethod("getPermissions");
		m.setAccessible(true);
		Set<Permission> permissions = (Set<Permission>) m.invoke(key);
		m.setAccessible(false);
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
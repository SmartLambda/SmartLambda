package edu.teco.smartlambda.authentication.entities;

import edu.teco.smartlambda.lambda.Lambda;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

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
		
		key = new Key("abcd", new User());
		key.grantPermission(lambda, PermissionType.DELETE);
		key.grantPermission(lambda, PermissionType.EXECUTE);
		
		key.grantPermission(user, PermissionType.DELETE);
		key.grantPermission(user, PermissionType.GRANT);
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
		list.add(new Permission(PermissionType.DELETE, lambda));
		if (!revokedFirst) {
			list.add(new Permission(PermissionType.EXECUTE, lambda));
			
		}
		list.add(new Permission(PermissionType.DELETE, user));
		if (!revokedSecond) {
			list.add(new Permission(PermissionType.GRANT, user));
		}
		int size = list.size();
		List<Permission> permissions = key.getPermissions();
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
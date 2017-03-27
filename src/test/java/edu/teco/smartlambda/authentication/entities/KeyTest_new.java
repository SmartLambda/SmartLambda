package edu.teco.smartlambda.authentication.entities;

import edu.teco.smartlambda.Application;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.torpedoquery.jpa.Torpedo;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

/**
 *
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({Application.class, Torpedo.class})
public class KeyTest_new {
	
	private Key        key;
	private Permission permission;
	private Session    session;
	
	@Before
	public void setup() {
		this.key = new Key();
		this.permission = mock(Permission.class);
		
		this.session = mock(Session.class);
		mockStatic(Application.class);
		when(Application.getInstance()).thenReturn(mock(Application.class));
		when(Application.getInstance().getSessionFactory()).thenReturn(mock((SessionFactory.class)));
		when(Application.getInstance().getSessionFactory().getCurrentSession()).thenReturn(this.session);
		
		mockStatic(Torpedo.class);
		when(Torpedo.from(any())).thenReturn(this.permission);
		when(this.permission.getKey()).thenReturn(this.key);
	}
	
	@Test
	public void getPermissions() throws Exception {
		this.key.getPermissions();
	}
	
	@Test
	public void getVisiblePermissions() throws Exception {
	
	}
	
	@Test
	public void hasPermission() throws Exception {
	
	}
	
	@Test
	public void hasPermission1() throws Exception {
	
	}
	
	@Test
	public void isPrimaryKey() throws Exception {
	
	}
	
	@Test
	public void delete() throws Exception {
	}
	
	@Test
	public void grantPermission() throws Exception {
	}
	
	@Test
	public void grantPermission1() throws Exception {
	}
	
	@Test
	public void revokePermission() throws Exception {
	}
	
	@Test
	public void revokePermission1() throws Exception {
	}
	
	@Test
	public void getKeyById() throws Exception {
	}
	
	@Test
	public void getId() throws Exception {
	}
	
	@Test
	public void getName() throws Exception {
	}
	
	@Test
	public void getUser() throws Exception {
	}
	
	@Test
	public void setName() throws Exception {
	}
}
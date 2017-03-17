package edu.teco.smartlambda.container;

import edu.teco.smartlambda.utility.TestUtility;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 *
 */
public class ImageFactoryTest {
	
	@Test
	public void construct() throws Exception {
		TestUtility.coverPrivateDefaultConstructor(ImageFactory.class);
	}
	
	@Test
	public void getImageById() throws Exception {
		assertNotNull(ImageFactory.getImageById("42"));
		assertEquals("42", ImageFactory.getImageById("42").getId());
	}
}
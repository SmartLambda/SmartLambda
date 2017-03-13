package edu.teco.smartlambda.container.docker;

import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.messages.ContainerCreation;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.junit.Assert.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 *
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(DockerClientProvider.class)
public class DockerImageTest {
	
	private static final String IMAGE_ID = "31337";
	
	private static DockerImage image;
	
	private static DockerClient mockedDockerClient;
	
	@Before
	public void setUp() throws Exception {
		image = new DockerImage(IMAGE_ID);
		
		mockedDockerClient = mock(DockerClient.class);
		final ContainerCreation mockedContainerCreation = mock(ContainerCreation.class);
		
		PowerMockito.mockStatic(DockerClientProvider.class);
		when(DockerClientProvider.get()).thenReturn(mockedDockerClient);
		when(mockedDockerClient.createContainer(any())).thenReturn(mockedContainerCreation);
		when(mockedContainerCreation.id()).thenReturn(IMAGE_ID);
	}
	
	@Test
	public void start() throws Exception {
		image.start();
		verify(mockedDockerClient).startContainer(IMAGE_ID);
	}
	
	@Test
	public void getId() throws Exception {
		assertSame(IMAGE_ID, image.getId());
	}
	
	@Test
	public void delete() throws Exception {
		image.delete();
		verify(mockedDockerClient).removeImage(eq(IMAGE_ID), anyBoolean(), anyBoolean());
	}
}
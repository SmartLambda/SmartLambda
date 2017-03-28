package edu.teco.smartlambda.container.docker;

import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.LogStream;
import org.apache.commons.io.output.NullOutputStream;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.OutputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

/**
 *
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({DockerClientProvider.class, HttpHijackingWorkaround.class})
public class DockerContainerTest {
	
	private static final String ID = "31337";
	private static DockerContainer dockerContainer;
	
	private static LogStream    mockedLogStream;
	private static OutputStream mockedOutputStream;
	
	@BeforeClass
	public static void setup() throws Exception {
		dockerContainer = new DockerContainer(ID);
		
		final DockerClient mockedDockerClient = mock(DockerClient.class);
		
		//noinspection unchecked
		mockedLogStream = mock(LogStream.class);
		mockedOutputStream = mock(OutputStream.class);
		
		mockStatic(HttpHijackingWorkaround.class);
		mockStatic(DockerClientProvider.class);
		
		when(DockerClientProvider.get()).thenReturn(mockedDockerClient);
		when(mockedDockerClient.attachContainer(ID, DockerClient.AttachParameter.STREAM, DockerClient.AttachParameter.STDIN,
				DockerClient.AttachParameter.STDOUT, DockerClient.AttachParameter.STDERR)).thenReturn(mockedLogStream);
		when(HttpHijackingWorkaround.getOutputStream(any(LogStream.class), anyString())).thenReturn(mockedOutputStream);
	}
	
	@Test
	public void getStdIn() throws Exception {
		assertSame(mockedOutputStream, dockerContainer.getStdIn());
	}
	
	@Test(timeout = 1000L)
	public void attach() throws Exception {
		try (NullOutputStream outputStream = new NullOutputStream(); NullOutputStream errorStream = new NullOutputStream()) {
			dockerContainer.attach(outputStream, errorStream);
			verify(mockedLogStream).attach(outputStream, errorStream, true);
		}
	}
	
	@Test
	public void getId() throws Exception {
		assertNotNull(dockerContainer.getId());
		assertEquals(ID, dockerContainer.getId());
	}
}
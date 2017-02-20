package edu.teco.smartlambda.container;

import java.util.ArrayList;
import java.util.List;

/**
 * A builder implementation for {@link DockerContainer}s
 */
public class DockerContainerBuilder implements ContainerBuilder {
	
	private final List<String> commands = new ArrayList<>();
	
	@Override
	public DockerContainer build() {
		return null;
	}
	
	@Override
	public ContainerBuilder appendCommand(final String command) {
		this.commands.add(command);
		return this;
	}
	
	@Override
	public ContainerBuilder storeFile(final byte[] binary, final String name, final boolean executable) {
		//// FIXME: 2/20/17
		return this;
	}
	
	private String generateContainerId() {
		//// FIXME: 2/20/17
		return null;
	}
}

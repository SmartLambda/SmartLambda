package edu.teco.smartlambda.container;

/**
 *
 */
public class DockerContainer implements Container {
	public DockerContainer(final String containerId) {
		//// FIXME: 2/17/17
	}
	
	public DockerContainer() {
		// empty container
	}
	
	@Override
	public void start() {
		
	}
	
	@Override
	public void storeBinary(final byte[] content, final String name) {
		
	}
	
	@Override
	public String save() {
		return null;
	}
}

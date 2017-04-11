package edu.teco.smartlambda.runtime;

import edu.teco.smartlambda.container.ImageBuilder;
import org.apache.commons.io.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.jar.JarInputStream;
import java.util.zip.ZipEntry;

/**
 * A {@link Runtime} implementation for Java 8
 */
public class JRE8 implements Runtime {
	
	private static final String NAME        = "jre8";
	private static final String BINARY_NAME = "lambda.jar";
	
	private static final String EXECUTION_SERVICE_NAME = "executionservice.jar";
	
	@Override
	public void setupContainerImage(final ImageBuilder builder) {
		try {
			builder.storeFile(IOUtils.toByteArray(this.getClass().getClassLoader().getResourceAsStream("jre8/executionservice.jar")),
					EXECUTION_SERVICE_NAME);
		} catch (final IOException e) {
			throw new RuntimeException("Failed to read execution service JAR resource", e);
		}
		
		builder.setCommand("java -jar " + EXECUTION_SERVICE_NAME).setTemplate("openjdk:8-jre-alpine");
	}
	
	@Override
	public String getName() {
		return NAME;
	}
	
	@Override
	public String getBinaryName() {
		return BINARY_NAME;
	}
	
	@Override
	public boolean verifyBinary(final byte[] binaryData) {
		try (JarInputStream inputStream = new JarInputStream(new ByteArrayInputStream(binaryData))) {
			ZipEntry entry;
			while ((entry = inputStream.getNextEntry()) != null) {
				if (entry.getName().endsWith("lambda.json")) return true;
			}
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
		return false;
	}
}

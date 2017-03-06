package edu.teco.smartlambda.lambda;

import edu.teco.smartlambda.container.DockerContainerBuilder;
import edu.teco.smartlambda.runtime.JRE8;
import org.apache.commons.io.IOUtils;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.io.InputStream;

import static org.junit.Assume.assumeNotNull;

/**
 *
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class FullLambdaDeploymentAndExecution {
	
	@BeforeClass
	public static void setup() {
		
	}
	
	@Test
	public void _a_deployLambda() throws Exception {
		assumeNotNull(LambdaFacade.getInstance());
		assumeNotNull(LambdaFacade.getInstance().getFactory());
		
		// load test lambda resource
		final InputStream testLambdaArchiveStream =
				FullLambdaDeploymentAndExecution.class.getClassLoader().getResourceAsStream("test_lambda" + ".jar");
		assumeNotNull(testLambdaArchiveStream);
		
		final byte[] testLambdaArchiveBinary = IOUtils.toByteArray(testLambdaArchiveStream);
		
		final AbstractLambda lambda = LambdaFacade.getInstance().getFactory().createLambda();
		lambda.setRuntime(new JRE8());
		lambda.deployBinary(testLambdaArchiveBinary);
		lambda.save();
	}
	
	public void _b_executeLambda() throws Exception {
		final DockerContainerBuilder builder = new DockerContainerBuilder();
		builder.setCommand("ls");
		builder.setTemplate("openjdk:8");
		builder.build();
	}
}

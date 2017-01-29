package edu.teco.smartlambda.processor;

import com.google.auto.service.AutoService;
import edu.teco.smartlambda.execution.LambdaFunction;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * An annotation processor that generates the required cconfiguration file for an archive containing a SmartLambda function
 */
@AutoService(Processor.class)
public class LambdaFunctionProcessor extends AbstractProcessor {
	
	@Override
	public Set<String> getSupportedAnnotationTypes() {
		return Stream.of(LambdaFunction.class.getCanonicalName()).collect(Collectors.toSet());
	}
	
	@Override
	public SourceVersion getSupportedSourceVersion() {
		return SourceVersion.RELEASE_8;
	}
	
	@Override
	public boolean process(final Set<? extends TypeElement> set, final RoundEnvironment roundEnvironment) {
		return false;
	}
}

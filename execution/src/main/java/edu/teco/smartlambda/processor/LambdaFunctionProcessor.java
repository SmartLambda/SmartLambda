package edu.teco.smartlambda.processor;

import com.google.auto.service.AutoService;
import edu.teco.smartlambda.execution.LambdaFunction;
import edu.teco.smartlambda.execution.LambdaParameter;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * An annotation processor that generates the required configuration file for an archive containing a SmartLambda function
 */
@AutoService(Processor.class)
public class LambdaFunctionProcessor extends AbstractProcessor {
	
	private TypeMirror lambdaParameterType;
	
	@Override
	public Set<String> getSupportedAnnotationTypes() {
		return Stream.of(LambdaFunction.class.getCanonicalName()).collect(Collectors.toSet());
	}
	
	@Override
	public SourceVersion getSupportedSourceVersion() {
		return SourceVersion.RELEASE_8;
	}
	
	@Override
	public synchronized void init(ProcessingEnvironment processingEnv) {
		super.init(processingEnv);
		
		// get the type mirror of LambdaParameters
		lambdaParameterType = processingEnv.getElementUtils().getTypeElement(LambdaParameter.class.getCanonicalName()).asType();
	}
	
	@Override
	public boolean process(final Set<? extends TypeElement> set, final RoundEnvironment roundEnvironment) {
		for (final Element element : roundEnvironment.getElementsAnnotatedWith(LambdaFunction.class)) {
			assert element instanceof ExecutableElement; // forced through RetentionPolicy
			
			final ExecutableElement               functionElement = (ExecutableElement) element;
			final List<? extends VariableElement> parameters      = functionElement.getParameters();
			
			String  lambdaFunctionEnclosingClassName;
			String  lambdaFunctionName;
			boolean hasParameter;
			boolean hasReturnValue;
			String  lambdaParameterClassName;
			String  lambdaReturnValueClassName;
			
			// TODO get meta data
			
			if (parameters.size() > 1) {
				throw new IllegalLambdaFunctionException("Illegal amount of lambda parameters. Maximum allowed: 1");
			} else if (parameters.size() == 1) {
				if (!processingEnv.getTypeUtils().isAssignable(parameters.get(0).asType(), lambdaParameterType)) {
					throw new IllegalLambdaFunctionException("Lambda parameter does not implement " + LambdaParameter.class.getCanonicalName());
				}
				
				hasParameter = true;
				System.out.println(parameters.get(0).asType().toString());
			}
			
			// TODO write meta file
		}
		
		return true;
	}
}

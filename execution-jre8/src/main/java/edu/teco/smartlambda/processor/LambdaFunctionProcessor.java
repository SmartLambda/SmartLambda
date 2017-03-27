package edu.teco.smartlambda.processor;

import com.google.auto.service.AutoService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import edu.teco.smartlambda.execution.LambdaExecutionService;
import edu.teco.smartlambda.execution.LambdaFunction;
import edu.teco.smartlambda.execution.LambdaParameter;
import edu.teco.smartlambda.execution.LambdaReturnValue;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.io.Writer;
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
	private TypeMirror lambdaReturnType;
	
	@Override
	public Set<String> getSupportedAnnotationTypes() {
		return Stream.of(LambdaFunction.class.getCanonicalName()).collect(Collectors.toSet());
	}
	
	@Override
	public SourceVersion getSupportedSourceVersion() {
		return SourceVersion.RELEASE_8;
	}
	
	@Override
	public synchronized void init(final ProcessingEnvironment processingEnv) {
		super.init(processingEnv);
		
		// get the type mirror of LambdaParameter, LambdaReturnValue
		this.lambdaParameterType = processingEnv.getElementUtils().getTypeElement(LambdaParameter.class.getCanonicalName()).asType();
		this.lambdaReturnType = processingEnv.getElementUtils().getTypeElement(LambdaReturnValue.class.getCanonicalName()).asType();
	}
	
	@Override
	public boolean process(final Set<? extends TypeElement> set, final RoundEnvironment roundEnvironment) {
		for (final Element element : roundEnvironment.getElementsAnnotatedWith(LambdaFunction.class)) {
			assert element instanceof ExecutableElement; // forced through RetentionPolicy
			
			final ExecutableElement functionElement = (ExecutableElement) element;
			
			final String  lambdaFunctionEnclosingClassName = functionElement.getEnclosingElement().asType().toString();
			final String  lambdaFunctionName               = functionElement.getSimpleName().toString();
			final boolean hasParameter;
			final boolean hasReturnValue;
			final String  lambdaParameterClassName;
			final String  lambdaReturnValueClassName;
			
			// retrieve parameter
			final List<? extends VariableElement> parameters = functionElement.getParameters();
			
			if (parameters.size() > 1) {
				throw new IllegalLambdaFunctionException("Illegal amount of lambda parameters. Maximum allowed: 1");
			} else if (parameters.size() == 1) {
				if (!this.processingEnv.getTypeUtils().isAssignable(parameters.get(0).asType(), this.lambdaParameterType)) {
					throw new IllegalLambdaFunctionException("Lambda parameter does not implement " + LambdaParameter.class
							.getCanonicalName());
				}
				
				hasParameter = true;
				lambdaParameterClassName = parameters.get(0).asType().toString();
			} else {
				hasParameter = false;
				lambdaParameterClassName = "";
			}
			
			// retrieve return value
			final TypeMirror returnType = functionElement.getReturnType();
			if (returnType.getKind() == TypeKind.VOID) {
				hasReturnValue = false;
				lambdaReturnValueClassName = "";
			} else {
				if (!this.processingEnv.getTypeUtils().isAssignable(returnType, this.lambdaReturnType)) {
					throw new IllegalLambdaFunctionException("Lambda return value is neither void nor implements " + LambdaReturnValue
							.class.getCanonicalName());
				}
				
				hasReturnValue = true;
				lambdaReturnValueClassName = returnType.toString();
			}
			
			// create meta data model
			final LambdaMetaData metaData = new LambdaMetaData(lambdaFunctionEnclosingClassName, lambdaFunctionName, hasParameter,
			                                                   hasReturnValue, lambdaParameterClassName, lambdaReturnValueClassName);
			
			// serialize meta data
			final Gson   gson         = new GsonBuilder().create();
			final String jsonMetaData = gson.toJson(metaData);
			
			// write meta data to JAR file
			try {
				final FileObject metaFile = this.processingEnv.getFiler().createResource(StandardLocation.CLASS_OUTPUT, "",
				                                                                    LambdaExecutionService.LAMBDA_META_DATA_FILE);
				
				try (final Writer writer = metaFile.openWriter()) {
					writer.write(jsonMetaData);
				}
			} catch (final IOException e) {
				this.processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, e.getMessage());
			}
		}
		
		return true;
	}
}

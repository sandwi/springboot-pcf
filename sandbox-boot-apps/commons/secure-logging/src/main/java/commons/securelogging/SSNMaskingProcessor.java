package commons.securelogging;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.util.Set;

@SupportedAnnotationTypes("commons.securelogging.MaskSSN")
@SupportedSourceVersion(SourceVersion.RELEASE_11)
public class SSNMaskingProcessor extends AbstractProcessor {
    @Override
    public synchronized void init(ProcessingEnvironment env){ }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for ( TypeElement annotation : annotations ) {
            for ( Element element : roundEnv.getElementsAnnotatedWith(annotation) ) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "found @Log at " + element);
            }
        }
        return true;
    }
}

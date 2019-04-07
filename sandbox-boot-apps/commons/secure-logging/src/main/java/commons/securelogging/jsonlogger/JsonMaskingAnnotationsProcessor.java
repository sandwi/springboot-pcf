package commons.securelogging.jsonlogger;

import java.io.IOException;
import java.util.function.Function;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import commons.securelogging.*;

public class JsonMaskingAnnotationsProcessor extends AnnotationIntrospector {
    @Override
    public Object findSerializer(Annotated am) {

        if (am.hasAnnotation(MaskSSN.class)) {
            return new MaskingSerializer(new SSNMaskingFunction());
        } else if (am.hasAnnotation(MaskCardPAN.class)) {
            return new MaskingSerializer(new PANMaskingFunction());
        } else if (am.hasAnnotation(MaskAccountNumber.class)) {
            return new MaskingSerializer(new AccountNumberMaskingFunction());
        }

        return super.findSerializer(am);
    }

    public boolean hasIgnoreMarker(AnnotatedMember member) {
        boolean skipPassword = member.hasAnnotation(MaskData.class);
        return skipPassword || super.hasIgnoreMarker(member);
    }

    @Override
    public Version version() {
        return Version.unknownVersion();
    }

    static class MaskingSerializer extends StdSerializer<String> {

        private Function masker;

        public MaskingSerializer(Function masker) {
            super(String.class);
            this.masker = masker;
        }

        @Override
        public void serialize(String value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
            jgen.writeObject(masker.apply(value));
        }
    }
}

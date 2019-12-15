package commons.securelogging;

import java.util.function.Function;

public class SSNMaskingFunction implements Function {

    @Override
    public Object apply(Object o) {
        return o == null ? null : o.toString()
                .replaceAll("\\s+", "")
                .replaceAll("-+", "")
                .replaceAll ("^[0-9]{5}", "*****");
    }
}

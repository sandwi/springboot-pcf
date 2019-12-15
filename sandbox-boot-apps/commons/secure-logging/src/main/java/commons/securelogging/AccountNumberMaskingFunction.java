package commons.securelogging;

import java.util.function.Function;

public class AccountNumberMaskingFunction implements Function {

    @Override
    public Object apply(Object o) {
        return o == null ? null : o.toString().replaceAll ("^[0-9]+([0-9]{4}$)", "********$1");
    }
}

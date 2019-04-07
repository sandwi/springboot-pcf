package commons.securelogging.jsonlogger;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.MapFunction;

import java.util.function.Function;

public class JsonMaskingDirective {

    private String jsonPathExpression;
    private Function maskingFunction;
    private boolean removeItAll;

    public JsonMaskingDirective(String jsonPathExpression) {
        this.jsonPathExpression = jsonPathExpression;
        this.removeItAll = true;
    }

    public JsonMaskingDirective(String jsonPathExpression, Function maskingFunction) {
        this.jsonPathExpression = jsonPathExpression;
        this.maskingFunction = maskingFunction;
        this.removeItAll = false;
    }

    public String mask(String json) {
        if (removeItAll) {
            return JsonPath.parse(json).delete(jsonPathExpression).jsonString();
        } else {
            MapFunction function = new MapFunction() {
                @Override
                public Object map(Object currentValue, Configuration configuration) {
                    return maskingFunction.apply(currentValue);
                }
            };
            return JsonPath.parse(json).map(jsonPathExpression, function).jsonString();
        }
    }
}

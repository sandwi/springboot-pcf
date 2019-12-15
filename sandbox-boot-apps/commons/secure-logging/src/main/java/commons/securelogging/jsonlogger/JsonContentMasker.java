package commons.securelogging.jsonlogger;

import commons.securelogging.jsonlogger.JsonMaskingDirective;

public class JsonContentMasker {

    private JsonMaskingDirective[] directives;

    public JsonContentMasker(JsonMaskingDirective... directives) {
        this.directives = directives;
    }

    public String mask(String json) {
        for (JsonMaskingDirective info : directives) {
            json = info.mask(json);
        }
        return json;
    }
}


package commons.securelogging.jsonlogger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import commons.securelogging.AccountNumberMaskingFunction;
import commons.securelogging.SSNMaskingFunction;
import net.logstash.logback.encoder.org.apache.commons.lang.StringUtils;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class JsonLoggingWrapper {
    private Logger logger;
    private ObjectMapper mapper;

    @Value("${mask.soap.ssnAttributeNames:SSN}")
    private String soapSSNAttributeNames;

    @Value("${mask.soap.accountAttributeNames:AcctId}")
    private String soapAccountAttributeNames;

    @Value("${mask.soap.passwordAttributeNames:Password}")
    private String soapPasswordAttributeNames;

    @Value("${mask.json.accountAttributeNames:accountId}")
    private String jsonAccountAttributeNames;

    public JsonLoggingWrapper() {
        this(LoggerFactory.getLogger(JsonLoggingWrapper.class), new ObjectMapper());
    }

    public JsonLoggingWrapper(Logger customLogger) {
        this(customLogger, new ObjectMapper());
    }

    public JsonLoggingWrapper(ObjectMapper mapper) {
        this(LoggerFactory.getLogger(JsonLoggingWrapper.class), mapper);
    }

    private JsonLoggingWrapper(Logger customLogger, ObjectMapper mapper) {
        this.logger = customLogger;
        this.mapper = mapper;
        this.mapper.setAnnotationIntrospector(new JsonMaskingAnnotationsProcessor());
    }


    /**
     * Logs the given object and metadata as JSON with sensitive information redacted
     *
     * @param obj      the object we want to log
     * @param url      the URL associated with this log message (we will mostly be logging some kind of HTTP request/response)
     * @param type     the type of object (like 'request' or 'response')
     * @param masker the masker which is used to mask the sensitive information
     * @deprecated
     */
    @Deprecated
    public void logMasked(Object obj, String url, String type, JsonContentMasker masker) {
        logMasked(obj, Collections.emptyMap(), url, type, masker);
    }

    public void logMasked(Object obj, Map<String, String> headers, String url, String type, JsonContentMasker masker) {

        ObjectNode loggingNode = mapper.createObjectNode();
        loggingNode.put("url", url);
        loggingNode.put("type", type);

        try {
            String maskedJson = masker.mask(mapper.writeValueAsString(obj));
            JsonNode node = mapper.readTree(maskedJson);
            loggingNode.set("content", node);
            logger.info(loggingNode.toString());
        } catch (IOException e) {
            logger.debug(e.getMessage(), e);
        }
    }

    /**
     * Logs the given object and metadata as JSON with all information
     *
     * @param obj  the object we want to log
     * @param url  the URL associated with this log message (we will mostly be logging some kind of HTTP request/response)
     * @param type the type of object (like 'request' or 'response')
     */
    public void log(Object obj, String url, String type) {
        log(obj, url, type, Collections.emptyMap());
    }

    public void log(Object obj, String url, String type, Map<String, String> headers) {

        ObjectNode loggingNode = mapper.createObjectNode();
        loggingNode.put("url", url);
        loggingNode.put("type", type);
        loggingNode.set("content", mapper.valueToTree(obj));
        logger.info(loggingNode.toString());
    }


    /**
     * Masks the given object and then logs it
     *
     * @param data the String we want to mask and log
     * @param url  the URL associated with this log message (we will mostly be logging some kind of SOAP HTTP request/response)
     * @param type the type of object (like 'request' or 'response')
     */
    public void maskAndLog(String data, String url, String type) {
        maskAndLog(data, url, type, Collections.emptyMap());
    }

    public void maskAndLog(String data, String url, String type, Map<String, String> headers) {

        try {

            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            documentBuilderFactory.setNamespaceAware(true);
            //avoids XXE injections
            documentBuilderFactory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            documentBuilderFactory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            documentBuilderFactory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);

            DocumentBuilder builder = documentBuilderFactory.newDocumentBuilder();

            Document doc = builder.parse(new InputSource(new StringReader(data)));


            data = maskAccountNumber(doc, data);
            data = maskSSN(doc, data);
            data = maskPassword(doc, data);
            data = maskPayload(data);

            this.log(data, url, type, headers);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private String maskAccountNumber(Document doc, String obj) {
        AccountNumberMaskingFunction anMaskingFunction = new AccountNumberMaskingFunction();
        List<String> accountNumberTags = Stream.of(soapAccountAttributeNames.split(","))
                .map(String::trim)
                .collect(Collectors.toList());
        for (String accountNumberTag : accountNumberTags) {
            NodeList nodeList = doc.getDocumentElement().getElementsByTagNameNS("*", accountNumberTag);
            if (nodeList.getLength() > 0) {
                obj = mask(anMaskingFunction, doc, obj, accountNumberTag);
            }
        }
        return obj;

    }

    private String maskSSN(Document doc, String obj) {
        SSNMaskingFunction ssnMaskingFunction = new SSNMaskingFunction();
        List<String> ssnTags = Stream.of(soapSSNAttributeNames.split(","))
                .map(String::trim)
                .collect(Collectors.toList());
        for (String ssnTag : ssnTags) {
            NodeList nodeList = doc.getDocumentElement().getElementsByTagNameNS("*", ssnTag);
            if (nodeList.getLength() > 0) {
                obj = mask(ssnMaskingFunction, doc, obj, ssnTag);
            }
        }
        return obj;
    }

    private String maskPassword(Document doc, String obj) {

        List<String> passwordTags = Stream.of(soapPasswordAttributeNames.split(","))
                .map(String::trim)
                .collect(Collectors.toList());

        //TODO: refactor this so all redacts can call the same method
        String tag;
        String maskededTag;

        for (String passwordTag : passwordTags) {
            NodeList nodeList = doc.getDocumentElement().getElementsByTagNameNS("*", passwordTag);
            for (int i = 0; i < nodeList.getLength(); i++) {
                tag =
                        "<" + nodeList.item(i).getPrefix() + ":" + passwordTag + ">" +
                                nodeList.item(i).getFirstChild().getNodeValue() +
                                "</" + nodeList.item(i).getPrefix() + ":" + passwordTag + ">"
                ;
                maskededTag = "";

                obj = obj.replace(tag, maskededTag);

            }
        }
        return obj;
    }

    private String mask(Function maskingFunction, Document doc, String obj, String redact) {
        NodeList nodeList = doc.getDocumentElement().getElementsByTagNameNS("*", redact);
        String tag;
        String maskedTag;
        for (int i = 0; i < nodeList.getLength(); i++) {
            if (nodeList.item(i).getFirstChild() != null) {
                tag =
                        "<" + ((nodeList.item(i).getPrefix() == null) ? "" : nodeList.item(i).getPrefix() + ":") + redact + ">" +
                                nodeList.item(i).getFirstChild().getNodeValue()
                ;

                maskedTag =
                        "<" + ((nodeList.item(i).getPrefix() == null) ? "" : nodeList.item(i).getPrefix() + ":") + redact + ">" +
                                maskingFunction.apply(nodeList.item(i).getFirstChild().getNodeValue())
                ;

                //updates the stringResponse with the redacted tag
                obj = obj.replace(tag, maskedTag);

            }
        }
        return obj;
    }


    /*Mask the login Payload*/
    private String maskPayload(String data) {
        StringBuffer payload = new StringBuffer(data);

        List<String> tokens = new ArrayList<String>();
        tokens.add("SSN=");
        tokens.add("ANUM[0-9]+$*=");


        String patternString = "\\b(" + StringUtils.join(tokens, "|") + ")\\b";
        Pattern pattern = Pattern.compile(patternString);
        Matcher matcher = pattern.matcher(payload);

        while (matcher.find()) {
            if (matcher.group().contains("SSN")) {
                payload = maskSSN(payload, matcher.group());
            }

            if (matcher.group().contains("ANUM")) {
                payload = maskAccountNumber(payload, matcher.group());
            }
        }
        return payload.toString();
    }

    private StringBuffer maskSSN(StringBuffer payload, String firstString) {
        SSNMaskingFunction ssnRedactionFunction = new SSNMaskingFunction();
        String endString = "&amp;";

        Pattern pattern = Pattern.compile(firstString + "(.*?)" + endString);
        Matcher matcher = pattern.matcher(payload);
        while (matcher.find()) {
            int position = payload.lastIndexOf(matcher.group(1));
            payload.replace(position, position + matcher.group(1).length(), (String) ssnRedactionFunction.apply(matcher.group(1)));
        }
        return payload;
    }

    /*Redact the Account number in SSOLogin Payload*/
    private StringBuffer maskAccountNumber(StringBuffer payload, String firstString) {
        AccountNumberMaskingFunction redactAccountNumber = new AccountNumberMaskingFunction();
        String endString = "&amp;";

        Pattern pattern = Pattern.compile(firstString + "(.*?)" + endString);
        Matcher matcher = pattern.matcher(payload);
        while (matcher.find()) {
            int position = payload.lastIndexOf(matcher.group(1));
            payload.replace(position, position + matcher.group(1).length(), (String) redactAccountNumber.apply(matcher.group(1)));

        }
        return payload;
    }

    /**
     * Parse the json string (Json Request/response) to object and then logs it
     *
     * @param data the String we want to redact and log
     * @param url  the URL associated with this log message (we will mostly be logging some kind of Rest HTTP request/response)
     * @param type the type of object (like 'request' or 'response')
     */
    public void maskAndlogRest(String data, String url, String type) {
        maskAndlogRest(data, url, type, Collections.emptyMap());
    }

    public void maskAndlogRest(String data, String url, String type, Map<String, String> headers) {

        JSONParser jsonParser = new JSONParser(JSONParser.MODE_STRICTEST);
        JSONObject jsonObject = null;
        Object object;
        try {
            //Create a JSONObject or JsonArray from json string
            object = jsonParser.parse(data);
            traverseJsonObject(object);
            this.log(object, url, type, headers);
            //Traverse through each element to look for PI data

        } catch (ParseException parseException) {
            //when request/response body is empty/invalid json
            this.log(jsonObject, url, type, headers);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * Traverse through Json object and redact PI
     *
     * @param object JSON request/response
     * @return
     */
    private Object traverseJsonObject(Object object) {
        if (object instanceof JSONObject) {
            JSONObject jsonObject = (JSONObject) object;
            for (Object key : jsonObject.keySet()) {
                String keyStr = (String) key;
                Object keyvalue = jsonObject.get(keyStr);
                //verify if the value is json array
                traverseJsonArray(keyvalue);
                keyvalue = maskAccountNumber(keyvalue, keyStr);
                keyvalue = maskSSN(keyvalue, keyStr);
                jsonObject.put(keyStr, keyvalue);
                traverseJsonObject(keyvalue);
            }
        } else {//When response is an array
            traverseJsonArray(object);
        }
        return object;
    }


    /**
     * Traverse through Json Array
     *
     * @param object
     * @return
     */
    private void traverseJsonArray(Object object) {
        if (object instanceof JSONArray) {
            JSONArray jsonArray = (JSONArray) object;
            for (int i = 0; i < jsonArray.size(); i++) {
                traverseJsonObject(jsonArray.get(i));
            }
        }
    }

    private Object maskSSN(Object value, String key) {
        //TODO Fetch the list of SSN attribute field names from yml file for redaction

        //Specific scenario for customers dataservice where SSN value is stored inside TaxData.value
        if (key.equalsIgnoreCase("TaxData")) {
            maskSSNData((JSONObject) value);
        }
        return value;
    }

    /**
     * Pass TaxID object
     *
     * @param jsonObj TaxID jsonobject which contains type and value fields
     */
    private void maskSSNData(JSONObject jsonObj) {
        SSNMaskingFunction ssnRedactionFunction = new SSNMaskingFunction();
        for (Object key : jsonObj.keySet()) {
            String keyStr = (String) key;
            Object keyvalue = jsonObj.get(keyStr);
            if (keyStr.equalsIgnoreCase("value")) {
                keyvalue = ssnRedactionFunction.apply(keyvalue);
                jsonObj.put(keyStr, keyvalue);
            }
        }
    }

    /**
     * redacts the accountNumber if key is present in mask.json.accountAttributeNames
     *
     * @param value
     * @param key
     * @return Redacted accountNumber value for logging
     */
    private Object maskAccountNumber(Object value, String key) {
        AccountNumberMaskingFunction accountNumberRedactionFunction = new AccountNumberMaskingFunction();
        List<String> accountNumberTags = Stream.of(jsonAccountAttributeNames.split(","))
                .map(String::trim)
                .collect(Collectors.toList());
        for (String accountNumberTag : accountNumberTags) {
            if (key.equalsIgnoreCase(accountNumberTag)) {
                value = accountNumberRedactionFunction.apply(value);
            }
        }
        //As of now only /transactions endpoint has description field which contains the account number
        if (key.equalsIgnoreCase("description")) {
            value = "****";
        }
        return value;
    }
}

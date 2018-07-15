package api;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.HashMap;

public class JsonWrapper {
    public static HashMap<String, Object> generateJson(Object data){
        HashMap<String, Object> result = new HashMap<>();
        result.put("status", "success");
        result.put("message", null);
        result.put("data", data);
        return result;
    }

    public static HashMap<String, Object> generateError(String msg){
        HashMap<String, Object> result = new HashMap<>();
        result.put("status", "fail");
        result.put("message", msg);
        result.put("data", null);
        return result;
    }

    public static String encode(final String clearText) {
        try {
            return new String(
                    Base64.getEncoder().encode(MessageDigest.getInstance("SHA-256").digest(clearText.getBytes())));
        } catch (NoSuchAlgorithmException e) {
            return clearText;
        }
    }
}

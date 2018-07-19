package api.controller;

import org.json.JSONObject;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class w2uiDemo {

        @RequestMapping(value = "/getData")
        public w2uiRespose getData(@RequestBody String payload){
            String j = null;
            try {
                j = URLDecoder.decode(payload, "UTF-8").substring(8);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            System.out.println(j);
            JSONObject jObject = new JSONObject(  j );
            System.out.println(jObject);
            w2uiRespose res = new w2uiRespose(jObject.getInt("offset"), jObject.getInt("limit"));
            return res;
        }
}

class w2uiRespose {
    public String status;
    public int total;
    public List<Map<String, Object>> records;

    w2uiRespose(int offset, int limit)
    {
        status = "success";
        total = 10000000;
        records = new ArrayList<>();
        for (int i=0;i<limit;i++)
        {
            Map<String, Object> record = new HashMap<>();
            record.put("recid", i + offset);
            record.put("fname", i + " Name - "  + i  + " " + offset);
            records.add(record);
        }
    }
}

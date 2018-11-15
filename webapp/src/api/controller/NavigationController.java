package api.controller;

import api.Cell;
import api.JsonWrapper;
import org.springframework.web.bind.annotation.*;
import org.zkoss.json.JSONArray;
import org.zkoss.json.JSONObject;
import org.zkoss.json.parser.JSONParser;
import org.zkoss.util.Pair;
import org.zkoss.zss.model.CellRegion;
import org.zkoss.zss.model.SBook;
import org.zkoss.zss.model.SCell;
import org.zkoss.zss.model.SSheet;
import org.zkoss.zss.model.impl.Model;
import org.zkoss.zss.model.impl.RCV_Model;
import org.zkoss.zss.model.sys.BookBindings;


import java.util.*;

@RestController
public class NavigationController {
    //http://127.0.0.1:8080//api/getSortAttrs/tjhtmdfii/airbnb_small
    @RequestMapping(value = "/api/getSortAttrs/{bookId}/{sheetName}",
            method = RequestMethod.GET)
    public HashMap<String, Object> getSortAttrs(@PathVariable String bookId,
                                                @PathVariable String sheetName) {
        SBook book = BookBindings.getBookById(bookId);
        SSheet currentSheet = book.getSheetByName(sheetName);
        CellRegion tableRegion = new CellRegion(0, 0,//100000,20);
                0, currentSheet.getEndColumnIndex());
        ArrayList<SCell> result = (ArrayList<SCell>) currentSheet.getCells(tableRegion);
        JSONArray headers = new JSONArray();
        for (SCell aResult : result) {
            headers.add(aResult.getStringValue());
        }
        return JsonWrapper.generateJson(headers);
    }

    //http://127.0.0.1:8080//api/startNav/bjhv2juw1/airbnb_small/0
    @RequestMapping(value = "/api/startNav/{bookId}/{sheetName}/{attr_index}",
            method = RequestMethod.GET)
    public HashMap<String, Object> startNav(@PathVariable String bookId,
                                            @PathVariable String sheetName,
                                            @PathVariable String attr_index) {
        SBook book = BookBindings.getBookById(bookId);
        SSheet currentSheet = book.getSheetByName(sheetName);
        currentSheet.getDataModel().setIndexString("col_" + attr_index);
        currentSheet.clearCache();
        return JsonWrapper.generateJson(currentSheet.getDataModel().navS.createNavS(currentSheet));
    }

    @RequestMapping(value = "/api/getScrollPath",
            method = RequestMethod.POST)
    public HashMap<String, Object> getScrollPath(@RequestBody String value) {
        JSONParser parser = new JSONParser();
        JSONObject dict = (JSONObject) parser.parse(value);
        SBook book = BookBindings.getBookById((String) dict.get("bookId"));
        SSheet currentSheet = book.getSheetByName((String) dict.get("sheetName"));
        int row = (int) dict.get("rowNum");
        int level = (int) dict.get("level");
        return JsonWrapper.generateJson(currentSheet.getDataModel().navS.getScrollPath(row, level));
    }

    //http://127.0.0.1:8080//api/getFlatten/tjhtmdfii/airbnb_small/0,2
    @RequestMapping(value = "/api/getFlatten/{bookId}/{sheetName}/{path}",
            method = RequestMethod.GET)
    public HashMap<String, Object> getFlatten(@PathVariable String bookId,
                                              @PathVariable String sheetName,
                                              @PathVariable String path) {
        System.out.println("Retrieve flattened view.");
        if (path == null) {
            path = "";
            System.out.println("Warning: null path as parameter.");
        }
        SBook book = BookBindings.getBookById(bookId);
        SSheet currentSheet = book.getSheetByName(sheetName);
        String[] tokens = path.split(",");
        int[] indices = new int[tokens.length];
        for (int i = 0; i < tokens.length; i++) {
            indices[i] = Integer.parseInt(tokens[i]);
        }
        return JsonWrapper.generateJson(currentSheet.getDataModel().navS.getFlattenView(indices));
    }


    @RequestMapping(value = "/api/getChildren", method = RequestMethod.POST)
    public HashMap<String, Object> getChildren(@RequestBody String value) {
        System.out.println("zoom in");
        JSONParser parser = new JSONParser();
        JSONObject dict = (JSONObject) parser.parse(value);
        SBook book = BookBindings.getBookById((String) dict.get("bookId"));
        SSheet currentSheet = book.getSheetByName((String) dict.get("sheetName"));

        String pathString = (String) dict.get("path");
        String[] tokens;
        if (pathString.isEmpty()) {
            tokens = new String[0];
        } else {
            tokens = pathString.split(",");
        }
        int[] indices = new int[tokens.length];
        for (int i = 0; i < tokens.length; i++) {
            indices[i] = Integer.parseInt(tokens[i]);
        }
        return JsonWrapper.generateJson(((RCV_Model) currentSheet.getDataModel()).navS.getNavChildrenWithContext(indices));
    }

    @RequestMapping(value = "/api/redefineBoundaries", method = RequestMethod.POST)
    public HashMap<String, Object> redefineBoundaries(@RequestBody String value) {

        JSONParser parser = new JSONParser();
        JSONObject dict = (JSONObject) parser.parse(value);
        SBook book = BookBindings.getBookById((String) dict.get("bookId"));
        SSheet currentSheet = book.getSheetByName((String) dict.get("sheetName"));

        String pathString = (String) dict.get("path");
        String[] tokens;
        if (pathString.isEmpty()) {
            tokens = new String[0];
        } else {
            tokens = pathString.split(",");
        }
        int[] indices = new int[tokens.length];
        for (int i = 0; i < tokens.length; i++) {
            indices[i] = Integer.parseInt(tokens[i]);
        }
        return JsonWrapper.generateJson(((RCV_Model) currentSheet.getDataModel()).navS.getBucketsWithLeaves(indices));
    }

    @RequestMapping(value = "/api/updateBoundaries", method = RequestMethod.POST)
    public HashMap<String, Object> updateBoundaries(@RequestBody String value) {

        JSONParser parser = new JSONParser();
        JSONObject dict = (JSONObject) parser.parse(value);
        SBook book = BookBindings.getBookById((String) dict.get("bookId"));
        SSheet currentSheet = book.getSheetByName((String) dict.get("sheetName"));
        JSONArray bucket_ls = (JSONArray) dict.get("bucketArray");

        String pathString = (String) dict.get("path");
        String[] tokens;
        if (pathString.isEmpty()) {
            tokens = new String[0];
        } else {
            tokens = pathString.split(",");
        }
        int[] indices = new int[tokens.length];
        for (int i = 0; i < tokens.length; i++) {
            indices[i] = Integer.parseInt(tokens[i]);
        }

        ArrayList<String> bkt_arr = new ArrayList<String>();

        for(int i=0;i<bucket_ls.size();i++)
        {
            JSONArray ls = (JSONArray) bucket_ls.get(i);
            String start = ls.get(0).toString();
            String end = ls.get(1).toString();
            bkt_arr.add(start+"#"+end);
        }

        ((RCV_Model) currentSheet.getDataModel()).navS.updateNavBucketTree(indices,bkt_arr);
        return JsonWrapper.generateJson(null);
    }

    ///api/sortBlock/{bookId}/{sheetName}/{path}/{attr_indices}/{order}
//    http://localhost:8080/api/sortBlock/gji5fi8vh/airbnb_small/%201/9,6,7/0  (sort by price/longitude/latitude)
    @RequestMapping(value = "/api/sortBlock/{bookId}/{sheetName}/{path}/{attr_indices}/{order}",
            method = RequestMethod.GET)
    public HashMap<String, Object> sortBlock(@PathVariable String bookId,
                                             @PathVariable String sheetName,
                                             @PathVariable String path,
                                             @PathVariable String attr_indices,
                                             @PathVariable String order) {
        System.out.println("Sort block");
        System.out.println(attr_indices);
        SBook book = BookBindings.getBookById(bookId);
        SSheet currentSheet = book.getSheetByName(sheetName);

        int[] indices = getIndices(path);
        int[] attrIndices;
        String[] tokens = attr_indices.split(",");
        attrIndices = new int[tokens.length];
        for (int i = 0; i < tokens.length; i++) {
            attrIndices[i] = Integer.parseInt(tokens[i]) - 1;
        }
        int orderInt = Integer.parseInt(order);
        currentSheet.getDataModel().navigationSortBucketByAttribute(currentSheet, indices, attrIndices, orderInt);
        JSONObject retObj = new JSONObject();
        retObj.put("success", true);
        return JsonWrapper.generateJson(retObj);
    }


    @RequestMapping(value = "/api/getHierarchicalAggregateFormula",
            method = RequestMethod.POST)
    public HashMap<String, Object> getHierarchicalAggregateFormula(@RequestBody String value) {
        JSONParser parser = new JSONParser();
        JSONObject dict = (JSONObject) parser.parse(value);
        String bookId = (String) dict.get("bookId");
        String sheetName = (String) dict.get("sheetName");
        String path = (String) dict.get("path");
        JSONArray formula_ls = (JSONArray) dict.get("formula_ls");

        SBook book = BookBindings.getBookById(bookId);
        SSheet currentSheet = book.getSheetByName(sheetName);

        int[] indices;
        indices = getIndices(path);
        int[] attrIndices = new int[formula_ls.size()];
        String[] aggregates = new String[formula_ls.size()];
        List<List<String>> param_arr_ls = new ArrayList<>();
        List<Boolean> getCharts = new ArrayList<>();

        for (int i = 0; i < formula_ls.size(); i++) {
            JSONObject temp = (JSONObject) formula_ls.get(i);
            attrIndices[i] = Integer.parseInt((String) temp.get("attr_index")) - 1;
            aggregates[i] = (String) temp.get("function");
            getCharts.add((Boolean) temp.get("getChart"));
            JSONArray param_ls = (JSONArray) temp.get("param_ls");
            List<String> param_arr = new ArrayList<>();
            for (Object param_l : param_ls) param_arr.add((String) param_l);
            param_arr_ls.add(param_arr);
        }
        List<List<Object>> agg;
        try {
            Model model = currentSheet.getDataModel();
            agg = model.navS.navigationGroupAggregateValue(model, indices, attrIndices, aggregates, param_arr_ls, getCharts);
        } catch (RuntimeException e) {
            return JsonWrapper.generateError(e.getMessage());
        }
        return JsonWrapper.generateJson(agg);
    }

    @RequestMapping(value = "/api/getBrushColorList",
            method = RequestMethod.POST)
    public HashMap<String, Object> getBrushColorList(@RequestBody String value) {
        JSONParser parser = new JSONParser();
        JSONObject dict = (JSONObject) parser.parse(value);
        String bookId = (String) dict.get("bookId");
        String sheetName = (String) dict.get("sheetName");
        JSONArray first_ls = (JSONArray) dict.get("first");
        JSONArray last_ls = (JSONArray) dict.get("last");
        JSONArray cond_ls = (JSONArray) dict.get("conditions");
        JSONArray val_ls = (JSONArray) dict.get("values");
        int attrIndex = (Integer) dict.get("index");
        SBook book = BookBindings.getBookById(bookId);
        SSheet currentSheet = book.getSheetByName(sheetName);

        ArrayList<Integer> indices = new ArrayList<Integer>();
        Model model = currentSheet.getDataModel();
        //Todo: when on demand loading available
        /*for(int i=0;i<first_ls.size();i++)
        {
            int first = (Integer) first_ls.get(i);
            int last = (Integer) last_ls.get(i);
            for(int j=first;j<=last;j++)
            {
                SCell sCell = currentSheet.getCell(j, attrIndex);

                double currvalue = Double.parseDouble(String.valueOf(sCell.getValue()));
                double queryValue = Double.parseDouble((String) val_ls.get(i));

                if(model.navS.isConditionSatisfied(currvalue,(String) cond_ls.get(i),queryValue))
                    indices.add(j);
            }
        }*/

        System.out.println("Calling brush color list");
        int first = (Integer) first_ls.get(0);
        int last = (Integer) last_ls.get(0);
        for(int j=first;j<=last;j++)
        {
            SCell sCell = currentSheet.getCell(j, attrIndex);

            try {
                double currvalue = Double.parseDouble(String.valueOf(sCell.getValue()));
                double queryValue = Double.parseDouble((String) val_ls.get(0));

                if (model.navS.isConditionSatisfied(currvalue, (String) cond_ls.get(0), queryValue))
                    indices.add(j);
            }catch (Exception e)
            {
                String currvalue = String.valueOf(sCell.getValue());
                String queryValue = (String) val_ls.get(0);

                if (model.navS.isConditionSatisfiedStr(currvalue, (String) cond_ls.get(0), queryValue))
                    indices.add(j);
            }
        }


        return JsonWrapper.generateJson(indices);
    }

    private int[] getIndices(String path) {
        int[] indices;
        if (!path.equals(" ")) {
            path = path.substring(1);
            String[] tokens = path.split(",");
            indices = new int[tokens.length];
            for (int i = 0; i < tokens.length; i++) {
                indices[i] = Integer.parseInt(tokens[i]);
            }
        } else {
            indices = new int[0];
        }
        return indices;
    }
}

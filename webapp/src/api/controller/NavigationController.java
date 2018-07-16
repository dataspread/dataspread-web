package api.controller;

import api.JsonWrapper;
import org.model.AutoRollbackConnection;
import org.model.DBContext;
import org.model.DBHandler;
import org.springframework.web.bind.annotation.*;
import org.zkoss.json.JSONArray;
import org.zkoss.json.JSONObject;
import org.zkoss.json.parser.JSONParser;
import org.zkoss.zss.model.CellRegion;
import org.zkoss.zss.model.SBook;
import org.zkoss.zss.model.SCell;
import org.zkoss.zss.model.SSheet;
import org.zkoss.zss.model.impl.Bucket;
import org.zkoss.zss.model.impl.Model;
import org.zkoss.zss.model.impl.ROM_Model;
import org.zkoss.zss.model.impl.sys.TableMonitor;
import org.zkoss.zss.model.sys.BookBindings;
import org.zkoss.zss.ui.impl.JSONObj;

import java.util.*;

@RestController
public class NavigationController {
    //http://127.0.0.1:8080//api/getSortAttrs/tjhtmdfii/airbnb_small
    @RequestMapping(value = "/api/getSortAttrs/{bookId}/{sheetName}",
            method = RequestMethod.GET)
    public HashMap<String, Object> getSortAttrs(@PathVariable String bookId,
                                               @PathVariable String sheetName){
        System.out.println("getSortAttrs:"+bookId+","+sheetName);

        SBook book = BookBindings.getBookById(bookId);
        SSheet currentSheet = book.getSheetByName(sheetName);

        CellRegion tableRegion =  new CellRegion(0, 0,//100000,20);
                0,currentSheet.getEndColumnIndex());

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
                                            @PathVariable String attr_index){
        System.out.println("startNav");
        SBook book = BookBindings.getBookById(bookId);
        SSheet currentSheet = book.getSheetByName(sheetName);

        currentSheet.getDataModel().setIndexString("col_"+attr_index);
        currentSheet.clearCache();


        return JsonWrapper.generateJson(currentSheet.getDataModel().createNavS(currentSheet));

    }

    //http://127.0.0.1:8080//api/getChildren/tjhtmdfii/airbnb_small/0,2
    @RequestMapping(value = "/api/getChildren/{bookId}/{sheetName}/{path}",
            method = RequestMethod.GET)
    public HashMap<String, Object> getChildren(@PathVariable String bookId,
                                            @PathVariable String sheetName,
                                            @PathVariable String path){
        System.out.println("zoom in");
        SBook book = BookBindings.getBookById(bookId);
        SSheet currentSheet = book.getSheetByName(sheetName);

        String[] tokens = path.split(",");
        int[] indices = new int[tokens.length];
        for (int i = 0; i < tokens.length; i++) {
            indices[i] = Integer.parseInt(tokens[i]);
        }

        return JsonWrapper.generateJson(currentSheet.getDataModel().getNavChildren(indices));

    }

    ///api/getHierarchicalAggregate/{bookId}/{sheetName}/{level}/{attr_index}/{func_id}

    @RequestMapping(value = "/api/getHierarchicalAggregate/{bookId}/{sheetName}/{path}/{attr_indices}/{agg_ids}",
            method = RequestMethod.GET)
    public HashMap<String, Object> getHierarchicalAggregate(@PathVariable String bookId,
                                                            @PathVariable String sheetName,
                                                            @PathVariable String path,
                                                            @PathVariable String attr_indices,
                                                            @PathVariable String agg_ids) {
        System.out.println("hierarchical column");
        SBook book = BookBindings.getBookById(bookId);
        SSheet currentSheet = book.getSheetByName(sheetName);

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

        int[] attrIndices;
        String[] tokens = attr_indices.split(",");
        attrIndices = new int[tokens.length];
        for (int i = 0; i < tokens.length; i++) {
            attrIndices[i] = Integer.parseInt(tokens[i])-1;
        }


        String[] aggregates;
        System.out.println(agg_ids);
        tokens = agg_ids.split("\\.,\\.");
        aggregates = new String[tokens.length];
        System.arraycopy(tokens, 0, aggregates, 0, tokens.length);


        List<List<Double>> agg = currentSheet.getDataModel().navigationGroupAggregateValue(currentSheet, indices, attrIndices, aggregates);

        JSONArray agg_obj_array = new JSONArray();

        for(int attr_i=0;attr_i<attrIndices.length;attr_i++)
        {
            JSONObject obj = new JSONObject();
            obj.put("attribute",attrIndices[attr_i]+1);
            obj.put("aggregate_id",aggregates[attr_i]);

            List<Double> temp_agg = agg.get(attr_i);
            /*JSONArray agg_ls = new JSONArray();

            for(int agg_i=0;agg_i<temp_agg.size();agg_i++)
                agg_ls.add(temp_agg.get(agg_i));*/
            obj.put("list",temp_agg);

            agg_obj_array.add(obj);
        }

        return JsonWrapper.generateJson(agg_obj_array);

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
        SBook book = BookBindings.getBookById(bookId);
        SSheet currentSheet = book.getSheetByName(sheetName);

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
    public HashMap<String, Object> getHierarchicalAggregateFormula(@RequestBody String value){
        JSONParser parser = new JSONParser();
        JSONObject ret = new JSONObject();

        JSONObject dict = (JSONObject)parser.parse(value);
        String bookId = (String)dict.get("bookId");
        String sheetName = (String)dict.get("sheetName");
        String path = (String)dict.get("path");
        JSONArray formula_ls = (JSONArray) dict.get("formula_ls");

        SBook book = BookBindings.getBookById(bookId);
        SSheet currentSheet = book.getSheetByName(sheetName);

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

        int[] attrIndices = new int[formula_ls.size()];
        String[] aggregates = new String[formula_ls.size()];
        ArrayList<ArrayList<String>> param_arr_ls = new ArrayList<ArrayList<String>>();

        for(int i=0;i<formula_ls.size();i++)
        {
            JSONObject temp = (JSONObject) formula_ls.get(i);
            attrIndices[i] = Integer.parseInt((String) temp.get("attr_index"));
            aggregates[i] = (String) temp.get("function");

            JSONArray param_ls = (JSONArray) dict.get("param_ls");

            ArrayList<String> param_arr = new ArrayList<String>();

            for(int j=0;i<param_ls.size();j++)
                param_arr.add((String) param_ls.get(j));

            param_arr_ls.add(param_arr);
        }

        List<List<Double>> agg = currentSheet.getDataModel().navigationGroupAggregateValue(currentSheet, indices, attrIndices, aggregates);

        //List<List<Double>> agg = currentSheet.getDataModel().navigationGroupAggregateValue(currentSheet, indices, attrIndices, aggregates, param_arr_ls);

        JSONArray agg_obj_array = new JSONArray();

        for(int attr_i=0;attr_i<attrIndices.length;attr_i++)
        {
            JSONObject obj = new JSONObject();
            obj.put("attribute",attrIndices[attr_i]+1);
            obj.put("aggregate_id",aggregates[attr_i]);

            List<Double> temp_agg = agg.get(attr_i);
        /*JSONArray agg_ls = new JSONArray();

        for(int agg_i=0;agg_i<temp_agg.size();agg_i++)
            agg_ls.add(temp_agg.get(agg_i));*/
            obj.put("list",temp_agg);

            agg_obj_array.add(obj);
        }

        return JsonWrapper.generateJson(agg_obj_array);


    }
    
}

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 *    contributor license agreements.  See the NOTICE file distributed with
 *    this work for additional information regarding copyright ownership.
 *    The ASF licenses this file to You under the Apache License, Version 2.0
 *    (the "License"); you may not use this file except in compliance with
 *    the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package org.zkoss.poi.hssf.dev;

import org.zkoss.poi.hssf.usermodel.HSSFPatriarch;
import org.zkoss.poi.hssf.usermodel.HSSFSheet;
import org.zkoss.poi.hssf.usermodel.HSSFWorkbook;

import java.io.FileInputStream;
import java.io.FileOutputStream;

/**
 *  Utility to test that POI produces readable output
 *  after re-saving xls files.
 *
 *  Usage: ReSave [-dg] input.xls
 *    -dg    initialize drawings, causes to re-build escher aggregates in all sheets
 */
public class ReSave {
    public static void main(String[] args) throws Exception {
        boolean initDrawing = false;
        for(String arg : args) {
            if(arg.equals("-dg")) initDrawing = true;
            else {
                System.out.print("reading " + arg + "...");
                FileInputStream is = new FileInputStream(arg);
                HSSFWorkbook wb = new HSSFWorkbook(is);
                is.close();
                System.out.println("done");

                for(int i = 0; i < wb.getNumberOfSheets(); i++){
                    HSSFSheet sheet = wb.getSheetAt(i);
                    if(initDrawing) {
                        HSSFPatriarch dg = sheet.getDrawingPatriarch();
                    }
                }

                String outputFile = arg.replace(".xls", "-saved.xls");
                System.out.print("saving to " + outputFile + "...");
                FileOutputStream out = new FileOutputStream(outputFile);
                wb.write(out);
                out.close();
                System.out.println("done");
            }
        }
    }
}

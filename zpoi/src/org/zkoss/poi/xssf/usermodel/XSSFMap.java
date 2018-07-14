/* ====================================================================
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */

package org.zkoss.poi.xssf.usermodel;

import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTMap;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTSchema;
import org.w3c.dom.Node;
import org.zkoss.poi.POIXMLDocumentPart;
import org.zkoss.poi.util.Internal;
import org.zkoss.poi.xssf.model.MapInfo;
import org.zkoss.poi.xssf.model.SingleXmlCells;
import org.zkoss.poi.xssf.usermodel.helpers.XSSFSingleXmlCell;

import java.util.List;
import java.util.Vector;

/**
 * This class implements the Map element (Open Office XML Part 4:
 * chapter 3.16.2)
 * <p/>
 * This element contains all of the properties related to the XML map,
 * and the behaviors expected during data refresh operations.
 *
 * @author Roberto Manicardi
 */


public class XSSFMap {

    private CTMap ctMap;

    private MapInfo mapInfo;


    public XSSFMap(CTMap ctMap, MapInfo mapInfo) {
        this.ctMap = ctMap;
        this.mapInfo = mapInfo;
    }


    @Internal
    public CTMap getCtMap() {
        return ctMap;
    }


    @Internal
    public CTSchema getCTSchema() {
        String schemaId = ctMap.getSchemaID();
        return mapInfo.getCTSchemaById(schemaId);
    }

    public Node getSchema() {
        Node xmlSchema = null;

        CTSchema schema = getCTSchema();
        xmlSchema = schema.getDomNode().getFirstChild();

        return xmlSchema;
    }

    /**
     * @return the list of Single Xml Cells that provide a map rule to this mapping.
     */
    public List<XSSFSingleXmlCell> getRelatedSingleXMLCell() {
        List<XSSFSingleXmlCell> relatedSimpleXmlCells = new Vector<XSSFSingleXmlCell>();

        int sheetNumber = mapInfo.getWorkbook().getNumberOfSheets();
        for (int i = 0; i < sheetNumber; i++) {
            XSSFSheet sheet = mapInfo.getWorkbook().getSheetAt(i);
            for (POIXMLDocumentPart p : sheet.getRelations()) {
                if (p instanceof SingleXmlCells) {
                    SingleXmlCells singleXMLCells = (SingleXmlCells) p;
                    for (XSSFSingleXmlCell cell : singleXMLCells.getAllSimpleXmlCell()) {
                        if (cell.getMapId() == ctMap.getID()) {
                            relatedSimpleXmlCells.add(cell);
                        }
                    }
                }
            }
        }
        return relatedSimpleXmlCells;
    }

    /**
     * @return the list of all Tables that provide a map rule to this mapping
     */
    public List<XSSFTable> getRelatedTables() {

        List<XSSFTable> tables = new Vector<XSSFTable>();
        int sheetNumber = mapInfo.getWorkbook().getNumberOfSheets();

        for (int i = 0; i < sheetNumber; i++) {
            XSSFSheet sheet = mapInfo.getWorkbook().getSheetAt(i);
            for (POIXMLDocumentPart p : sheet.getRelations()) {
                if (p.getPackageRelationship().getRelationshipType().equals(XSSFRelation.TABLE.getRelation())) {
                    XSSFTable table = (XSSFTable) p;
                    if (table.mapsTo(ctMap.getID())) {
                        tables.add(table);
                    }
                }
            }
        }

        return tables;
    }
}

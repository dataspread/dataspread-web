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
package org.zkoss.poi.xssf.eventusermodel;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.zkoss.poi.POIXMLException;
import org.zkoss.poi.openxml4j.exceptions.InvalidFormatException;
import org.zkoss.poi.openxml4j.exceptions.OpenXML4JException;
import org.zkoss.poi.openxml4j.opc.OPCPackage;
import org.zkoss.poi.openxml4j.opc.PackagePart;
import org.zkoss.poi.openxml4j.opc.PackagePartName;
import org.zkoss.poi.openxml4j.opc.PackageRelationship;
import org.zkoss.poi.openxml4j.opc.PackageRelationshipCollection;
import org.zkoss.poi.openxml4j.opc.PackageRelationshipTypes;
import org.zkoss.poi.openxml4j.opc.PackagingURIHelper;
import org.zkoss.poi.xssf.model.CommentsTable;
import org.zkoss.poi.xssf.model.SharedStringsTable;
import org.zkoss.poi.xssf.model.StylesTable;
import org.zkoss.poi.xssf.model.ThemesTable;
import org.zkoss.poi.xssf.usermodel.XSSFRelation;
import org.apache.xmlbeans.XmlException;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTSheet;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTWorkbook;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.WorkbookDocument;

/**
 * This class makes it easy to get at individual parts
 *  of an OOXML .xlsx file, suitable for low memory sax
 *  parsing or similar.
 * It makes up the core part of the EventUserModel support
 *  for XSSF.
 */
public class XSSFReader {
    private OPCPackage pkg;
    private PackagePart workbookPart;

    /**
     * Creates a new XSSFReader, for the given package
     */
    public XSSFReader(OPCPackage pkg) throws IOException, OpenXML4JException {
        this.pkg = pkg;

        PackageRelationship coreDocRelationship = this.pkg.getRelationshipsByType(
                PackageRelationshipTypes.CORE_DOCUMENT).getRelationship(0);

        // Get the part that holds the workbook
        workbookPart = this.pkg.getPart(coreDocRelationship);
    }


    /**
     * Opens up the Shared Strings Table, parses it, and
     *  returns a handy object for working with
     *  shared strings.
     */
    public SharedStringsTable getSharedStringsTable() throws IOException, InvalidFormatException {
        ArrayList<PackagePart> parts = pkg.getPartsByContentType( XSSFRelation.SHARED_STRINGS.getContentType());
        return parts.size() == 0 ? null : new SharedStringsTable(parts.get(0), null);
    }

    /**
     * Opens up the Styles Table, parses it, and
     *  returns a handy object for working with cell styles
     */
    public StylesTable getStylesTable() throws IOException, InvalidFormatException {
        ArrayList<PackagePart> parts = pkg.getPartsByContentType( XSSFRelation.STYLES.getContentType());
        if(parts.size() == 0) return null;
        
        // Create the Styles Table, and associate the Themes if present
        StylesTable styles = new StylesTable(parts.get(0), null);
        parts = pkg.getPartsByContentType( XSSFRelation.THEME.getContentType());
        if(parts.size() != 0) {
           styles.setTheme(new ThemesTable(parts.get(0), null));
        }
        return styles;
    }

    /**
     * Returns an InputStream to read the contents of the
     *  shared strings table.
     */
    public InputStream getSharedStringsData() throws IOException, InvalidFormatException {
        return XSSFRelation.SHARED_STRINGS.getContents(workbookPart);
    }

    /**
     * Returns an InputStream to read the contents of the
     *  styles table.
     */
    public InputStream getStylesData() throws IOException, InvalidFormatException {
        return XSSFRelation.STYLES.getContents(workbookPart);
    }

    /**
     * Returns an InputStream to read the contents of the
     *  themes table.
     */
    public InputStream getThemesData() throws IOException, InvalidFormatException {
        return XSSFRelation.THEME.getContents(workbookPart);
    }

    /**
     * Returns an InputStream to read the contents of the
     *  main Workbook, which contains key overall data for
     *  the file, including sheet definitions.
     */
    public InputStream getWorkbookData() throws IOException, InvalidFormatException {
        return workbookPart.getInputStream();
    }

    /**
     * Returns an InputStream to read the contents of the
     *  specified Sheet.
     * @param relId The relationId of the sheet, from a r:id on the workbook
     */
    public InputStream getSheet(String relId) throws IOException, InvalidFormatException {
        PackageRelationship rel = workbookPart.getRelationship(relId);
        if(rel == null) {
            throw new IllegalArgumentException("No Sheet found with r:id " + relId);
        }

        PackagePartName relName = PackagingURIHelper.createPartName(rel.getTargetURI());
        PackagePart sheet = pkg.getPart(relName);
        if(sheet == null) {
            throw new IllegalArgumentException("No data found for Sheet with r:id " + relId);
        }
        return sheet.getInputStream();
    }
    
    /**
     * Returns an Iterator which will let you get at all the
     *  different Sheets in turn.
     * Each sheet's InputStream is only opened when fetched
     *  from the Iterator. It's up to you to close the
     *  InputStreams when done with each one.
     */
    public Iterator<InputStream> getSheetsData() throws IOException, InvalidFormatException {
        return new SheetIterator(workbookPart);
    }

    /**
     * Iterator over sheet data.
     */
    public static class SheetIterator implements Iterator<InputStream> {

        /**
         *  Maps relId and the corresponding PackagePart
         */
        private Map<String, PackagePart> sheetMap;

        /**
         * Current CTSheet bean
         */
        private CTSheet ctSheet;
        
        /**
         * Iterator over CTSheet objects, returns sheets in <tt>logical</tt> order.
         * We can't rely on the Ooxml4J's relationship iterator because it returns objects in physical order,
         * i.e. as they are stored in the underlying package
         */
        private Iterator<CTSheet> sheetIterator;

        /**
         * Construct a new SheetIterator
         *
         * @param wb package part holding workbook.xml
         */
        private SheetIterator(PackagePart wb) throws IOException {

            /**
             * The order of sheets is defined by the order of CTSheet elements in workbook.xml
             */
            try {
                //step 1. Map sheet's relationship Id and the corresponding PackagePart
                sheetMap = new HashMap<String, PackagePart>();
                for(PackageRelationship rel : wb.getRelationships()){
                    if(rel.getRelationshipType().equals(XSSFRelation.WORKSHEET.getRelation()) ||
                       rel.getRelationshipType().equals(XSSFRelation.CHARTSHEET.getRelation())){
                        PackagePartName relName = PackagingURIHelper.createPartName(rel.getTargetURI());
                        sheetMap.put(rel.getId(), wb.getPackage().getPart(relName));
                    }
                }
                //step 2. Read array of CTSheet elements, wrap it in a ArayList and construct an iterator
                //Note, using XMLBeans might be expensive, consider refactoring to use SAX or a plain regexp search
                CTWorkbook wbBean = WorkbookDocument.Factory.parse(wb.getInputStream()).getWorkbook();
                sheetIterator = wbBean.getSheets().getSheetList().iterator(); 
            } catch (InvalidFormatException e){
                throw new POIXMLException(e);
            } catch (XmlException e){
                throw new POIXMLException(e);
            }
        }

        /**
         * Returns <tt>true</tt> if the iteration has more elements.
         *
         * @return <tt>true</tt> if the iterator has more elements.
         */
        public boolean hasNext() {
            return sheetIterator.hasNext();
        }

        /**
         * Returns input stream of the next sheet in the iteration
         *
         * @return input stream of the next sheet in the iteration
         */
        public InputStream next() {
            ctSheet = sheetIterator.next();

            String sheetId = ctSheet.getId();
            try {
                PackagePart sheetPkg = sheetMap.get(sheetId);
                return sheetPkg.getInputStream();
            } catch(IOException e) {
                throw new POIXMLException(e);
            }
        }

        /**
         * Returns name of the current sheet
         *
         * @return name of the current sheet
         */
        public String getSheetName() {
            return ctSheet.getName();
        }
        
        /**
         * Returns the comments associated with this sheet,
         *  or null if there aren't any
         */
        public CommentsTable getSheetComments() {
           PackagePart sheetPkg = getSheetPart();
           
           // Do we have a comments relationship? (Only ever one if so)
           try {
              PackageRelationshipCollection commentsList = 
                   sheetPkg.getRelationshipsByType(XSSFRelation.SHEET_COMMENTS.getRelation());
              if(commentsList.size() > 0) {
                 PackageRelationship comments = commentsList.getRelationship(0);
                 PackagePartName commentsName = PackagingURIHelper.createPartName(comments.getTargetURI());
                 PackagePart commentsPart = sheetPkg.getPackage().getPart(commentsName);
                 return new CommentsTable(commentsPart, comments);
              }
           } catch (InvalidFormatException e) {
              return null;
           } catch (IOException e) {
              return null;
           }
           return null;
        }
        
        public PackagePart getSheetPart() {
           String sheetId = ctSheet.getId();
           return sheetMap.get(sheetId);
        }

        /**
         * We're read only, so remove isn't supported
         */
        public void remove() {
            throw new IllegalStateException("Not supported");
        }
    }

    /**
     * Returns an ExternalLinkPart with the specified relationId.
     * @param relId the relationId of the external reference, from a r:id on the workbook
     */
    public PackagePart getExternalLink(String relId) throws IOException, InvalidFormatException {
        PackageRelationship rel = workbookPart.getRelationship(relId);
        if(rel == null) {
            throw new IllegalArgumentException("No ExternalLink found with r:id " + relId);
        }
        PackagePartName relName = PackagingURIHelper.createPartName(rel.getTargetURI());
        System.out.println("relName:"+relName);
        PackagePart externalLink = pkg.getPart(relName);
        if(externalLink == null) {
            throw new IllegalArgumentException("No data found for ExternalLink with r:id " + relId);
        }
        return externalLink;
    }
}

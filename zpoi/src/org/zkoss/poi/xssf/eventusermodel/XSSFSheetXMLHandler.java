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

import org.zkoss.poi.ss.usermodel.BuiltinFormats;
import org.zkoss.poi.ss.usermodel.DataFormatter;
import org.zkoss.poi.ss.usermodel.ZssContext;
import org.zkoss.poi.xssf.model.StylesTable;
import org.zkoss.poi.xssf.usermodel.XSSFCellStyle;
import org.zkoss.poi.xssf.usermodel.XSSFRichTextString;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * This class handles the processing of a sheet#.xml 
 *  sheet part of a XSSF .xlsx file, and generates
 *  row and cell events for it.
 */
public class XSSFSheetXMLHandler extends DefaultHandler {
   /**
    * These are the different kinds of cells we support.
    * We keep track of the current one between
    *  the start and end.
    */
   enum xssfDataType {
       BOOLEAN,
       ERROR,
       FORMULA,
       INLINE_STRING,
       SST_STRING,
       NUMBER,
   }
   
   /**
    * Table with the styles used for formatting
    */
   private StylesTable stylesTable;

   private ReadOnlySharedStringsTable sharedStringsTable;

   /**
    * Where our text is going
    */
   private final SheetContentsHandler output;

   // Set when V start element is seen
   private boolean vIsOpen;
   // Set when F start element is seen
   private boolean fIsOpen;
   // Set when an Inline String "is" is seen
   private boolean isIsOpen;
   // Set when a header/footer element is seen
   private boolean hfIsOpen;

   // Set when cell start element is seen;
   // used when cell close element is seen.
   private xssfDataType nextDataType;

   // Used to format numeric cell values.
   private short formatIndex;
   private String formatString;
   private final DataFormatter formatter;
   private String cellRef;
   private boolean formulasNotResults;

   // Gathers characters as they are seen.
   private StringBuffer value = new StringBuffer();
   private StringBuffer formula = new StringBuffer();
   private StringBuffer headerFooter = new StringBuffer();

   /**
    * Accepts objects needed while parsing.
    *
    * @param styles  Table of styles
    * @param strings Table of shared strings
    */
   public XSSFSheetXMLHandler(
           StylesTable styles,
           ReadOnlySharedStringsTable strings,
           SheetContentsHandler sheetContentsHandler,
           DataFormatter dataFormatter,
           boolean formulasNotResults) {
       this.stylesTable = styles;
       this.sharedStringsTable = strings;
       this.output = sheetContentsHandler;
       this.formulasNotResults = formulasNotResults;
       this.nextDataType = xssfDataType.NUMBER;
       this.formatter = dataFormatter;
   }
   /**
    * Accepts objects needed while parsing.
    *
    * @param styles  Table of styles
    * @param strings Table of shared strings
    */
   public XSSFSheetXMLHandler(
           StylesTable styles,
           ReadOnlySharedStringsTable strings,
           SheetContentsHandler sheetContentsHandler,
           boolean formulasNotResults) {
       this(styles, strings, sheetContentsHandler, new DataFormatter(ZssContext.getCurrent().getLocale(), false), formulasNotResults); //20111227, henrichen@zkoss.org: ZSS-68
   }

   private boolean isTextTag(String name) {
      if("v".equals(name)) {
         // Easy, normal v text tag
         return true;
      }
      if("inlineStr".equals(name)) {
         // Easy inline string
         return true;
      }
      if("t".equals(name) && isIsOpen) {
         // Inline string <is><t>...</t></is> pair
         return true;
      }
      // It isn't a text tag
      return false;
   }
   
   public void startElement(String uri, String localName, String name,
                            Attributes attributes) throws SAXException {

       if (isTextTag(name)) {
           vIsOpen = true;
           // Clear contents cache
           value.setLength(0);
       } else if ("is".equals(name)) {
          // Inline string outer tag
          isIsOpen = true;
       } else if ("f".equals(name)) {
          // Clear contents cache
          formula.setLength(0);
          
          // Mark us as being a formula if not already
          if(nextDataType == xssfDataType.NUMBER) {
             nextDataType = xssfDataType.FORMULA;
          }
          
          // Decide where to get the formula string from
          String type = attributes.getValue("t");
          if(type != null && type.equals("shared")) {
             // Is it the one that defines the shared, or uses it?
             String ref = attributes.getValue("ref");
             String si = attributes.getValue("si");
             
             if(ref != null) {
                // This one defines it
                // TODO Save it somewhere
                fIsOpen = true;
             } else {
                // This one uses a shared formula
                // TODO Retrieve the shared formula and tweak it to 
                //  match the current cell
                if(formulasNotResults) {
                   System.err.println("Warning - shared formulas not yet supported!");
                } else {
                   // It's a shared formula, so we can't get at the formula string yet
                   // However, they don't care about the formula string, so that's ok!
                }
             }
          } else {
             fIsOpen = true;
          }
       }
       else if("oddHeader".equals(name) || "evenHeader".equals(name) ||
             "firstHeader".equals(name) || "firstFooter".equals(name) ||
             "oddFooter".equals(name) || "evenFooter".equals(name)) {
          hfIsOpen = true;
          // Clear contents cache
          headerFooter.setLength(0);
       }
       else if("row".equals(name)) {
           int rowNum = Integer.parseInt(attributes.getValue("r")) - 1;
           output.startRow(rowNum);
       }
       // c => cell
       else if ("c".equals(name)) {
           // Set up defaults.
           this.nextDataType = xssfDataType.NUMBER;
           this.formatIndex = -1;
           this.formatString = null;
           cellRef = attributes.getValue("r");
           String cellType = attributes.getValue("t");
           String cellStyleStr = attributes.getValue("s");
           if ("b".equals(cellType))
               nextDataType = xssfDataType.BOOLEAN;
           else if ("e".equals(cellType))
               nextDataType = xssfDataType.ERROR;
           else if ("inlineStr".equals(cellType))
               nextDataType = xssfDataType.INLINE_STRING;
           else if ("s".equals(cellType))
               nextDataType = xssfDataType.SST_STRING;
           else if ("str".equals(cellType))
               nextDataType = xssfDataType.FORMULA;
           else if (cellStyleStr != null) {
              // Number, but almost certainly with a special style or format
               int styleIndex = Integer.parseInt(cellStyleStr);
               XSSFCellStyle style = stylesTable.getStyleAt(styleIndex);
               this.formatIndex = style.getDataFormat();
               this.formatString = style.getDataFormatString();
               if (this.formatString == null)
                   this.formatString = BuiltinFormats.getBuiltinFormat(this.formatIndex);
           }
       }
   }

   public void endElement(String uri, String localName, String name)
           throws SAXException {
       String thisStr = null;

       // v => contents of a cell
       if (isTextTag(name)) {
           vIsOpen = false;
           
           // Process the value contents as required, now we have it all
           switch (nextDataType) {
               case BOOLEAN:
                   char first = value.charAt(0);
                   thisStr = first == '0' ? "FALSE" : "TRUE";
                   break;

               case ERROR:
                   thisStr = "ERROR:" + value.toString();
                   break;

               case FORMULA:
                   if(formulasNotResults) {
                      thisStr = formula.toString();
                   } else {
                      String fv = value.toString();
                      
                      if (this.formatString != null) {
                         try {
                            // Try to use the value as a formattable number
                            double d = Double.parseDouble(fv);
                            thisStr = formatter.formatRawCellContents(d, this.formatIndex, this.formatString);
                         } catch(NumberFormatException e) {
                            // Formula is a String result not a Numeric one
                            thisStr = fv;
                         }
                      } else {
                         // No formating applied, just do raw value in all cases
                         thisStr = fv;
                      }
                   }
                   break;

               case INLINE_STRING:
                   // TODO: Can these ever have formatting on them?
                   XSSFRichTextString rtsi = new XSSFRichTextString(value.toString());
                   thisStr = rtsi.toString();
                   break;

               case SST_STRING:
                   String sstIndex = value.toString();
                   try {
                       int idx = Integer.parseInt(sstIndex);
                       XSSFRichTextString rtss = new XSSFRichTextString(sharedStringsTable.getEntryAt(idx));
                       thisStr = rtss.toString();
                   }
                   catch (NumberFormatException ex) {
                       System.err.println("Failed to parse SST index '" + sstIndex + "': " + ex.toString());
                   }
                   break;

               case NUMBER:
                   String n = value.toString();
                   if (this.formatString != null)
                       thisStr = formatter.formatRawCellContents(Double.parseDouble(n), this.formatIndex, this.formatString);
                   else
                       thisStr = n;
                   break;

               default:
                   thisStr = "(TODO: Unexpected type: " + nextDataType + ")";
                   break;
           }
           
           // Output
           output.cell(cellRef, thisStr);
       } else if ("f".equals(name)) {
          fIsOpen = false;
       } else if ("is".equals(name)) {
          isIsOpen = false;
       } else if ("row".equals(name)) {
          output.endRow();
       }
       else if("oddHeader".equals(name) || "evenHeader".equals(name) ||
             "firstHeader".equals(name)) {
          hfIsOpen = false;
          output.headerFooter(headerFooter.toString(), true, name);
       }
       else if("oddFooter".equals(name) || "evenFooter".equals(name) ||
             "firstFooter".equals(name)) {
          hfIsOpen = false;
          output.headerFooter(headerFooter.toString(), false, name);
       }
   }

   /**
    * Captures characters only if a suitable element is open.
    * Originally was just "v"; extended for inlineStr also.
    */
   public void characters(char[] ch, int start, int length)
           throws SAXException {
       if (vIsOpen) {
           value.append(ch, start, length);
       }
       if (fIsOpen) {
          formula.append(ch, start, length);
       }
       if (hfIsOpen) {
          headerFooter.append(ch, start, length);
       }
   }

   /**
    * You need to implement this to handle the results
    *  of the sheet parsing.
    */
   public interface SheetContentsHandler {
      /** A row with the (zero based) row number has started */
      public void startRow(int rowNum);
      /** A row with the (zero based) row number has ended */
      public void endRow();
      /** A cell, with the given formatted value, was encountered */
      public void cell(String cellReference, String formattedValue);
      /** A header or footer has been encountered */
      public void headerFooter(String text, boolean isHeader, String tagName);
   }
}

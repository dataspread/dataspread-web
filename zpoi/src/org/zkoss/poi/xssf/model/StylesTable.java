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

package org.zkoss.poi.xssf.model;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;
import java.util.Map.Entry;

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTBorder;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTBorders;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTCellAlignment;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTCellProtection;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTCellStyleXfs;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTCellXfs;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTDxf;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTDxfs;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTCellStyles;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTCellStyle;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTFill;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTFills;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTFont;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTFonts;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTNumFmt;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTNumFmts;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTStylesheet;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTXf;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.STHorizontalAlignment;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.STPatternType;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.STVerticalAlignment;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.StyleSheetDocument;
import org.zkoss.poi.POIXMLDocumentPart;
import org.zkoss.poi.openxml4j.opc.PackagePart;
import org.zkoss.poi.openxml4j.opc.PackageRelationship;
import org.zkoss.poi.ss.usermodel.BuiltinFormats;
import org.zkoss.poi.ss.usermodel.FontFamily;
import org.zkoss.poi.ss.usermodel.FontScheme;
import org.zkoss.poi.xssf.usermodel.XSSFCellStyle;
import org.zkoss.poi.xssf.usermodel.XSSFColor;
import org.zkoss.poi.xssf.usermodel.XSSFFont;
import org.zkoss.poi.xssf.usermodel.XSSFNamedStyle;
import org.zkoss.poi.xssf.usermodel.extensions.XSSFCellBorder;
import org.zkoss.poi.xssf.usermodel.extensions.XSSFCellFill;


/**
 * Table of styles shared across all sheets in a workbook.
 *
 * @author ugo
 */
public class StylesTable extends POIXMLDocumentPart {
	private Map<Integer, String> numberFormats = new LinkedHashMap<Integer,String>();
	private List<XSSFFont> fonts = new ArrayList<XSSFFont>();
	private List<XSSFCellFill> fills = new ArrayList<XSSFCellFill>();
	private List<XSSFCellBorder> borders = new ArrayList<XSSFCellBorder>();
	private List<CTXf> styleXfs = new ArrayList<CTXf>();
	private List<CTXf> xfs = new ArrayList<CTXf>();
	
	//ZSS-854
	// name -> CTCellStyle
	private Map<String, CTCellStyle> cellStyles = new HashMap<String, CTCellStyle>();
	
	private List<CTDxf> dxfs = new ArrayList<CTDxf>();

	/**
	 * The first style id available for use as a custom style
	 */
	public static final int FIRST_CUSTOM_STYLE_ID = BuiltinFormats.FIRST_USER_DEFINED_FORMAT_INDEX + 1;

	private StyleSheetDocument doc;
	private ThemesTable theme;

	/**
	 * Create a new, empty StylesTable
	 */
	public StylesTable() {
		super();
		doc = StyleSheetDocument.Factory.newInstance();
		doc.addNewStyleSheet();
		// Initialization required in order to make the document readable by MSExcel
		initialize();
	}

	public StylesTable(PackagePart part, PackageRelationship rel) throws IOException {
		super(part, rel);
		readFrom(part.getInputStream());
	}

	public ThemesTable getTheme() {
        return theme;
    }

    public void setTheme(ThemesTable theme) {
        this.theme = theme;
        
        // Pass the themes table along to things which need to 
        //  know about it, but have already been created by now
        for(XSSFFont font : fonts) {
           font.setThemesTable(theme);
        }
        for(XSSFCellBorder border : borders) {
           border.setThemesTable(theme);
        }
    }

	/**
	 * Read this shared styles table from an XML file.
	 *
	 * @param is The input stream containing the XML document.
	 * @throws IOException if an error occurs while reading.
	 */
	protected void readFrom(InputStream is) throws IOException {
		try {
			doc = StyleSheetDocument.Factory.parse(is);

            CTStylesheet styleSheet = doc.getStyleSheet();
            
            init(styleSheet); //20110114, henrichen@zkoss.org: setArray(xyz[]) does not guarantee getArray() equals to xyz[]
		} catch (XmlException e) {
			throw new IOException(e.getLocalizedMessage());
		}
	}
    
    //20110114, henrichen@zkoss: setArray(xyz[]) does not guarantee getArray() equals to xyz[]
    @SuppressWarnings("deprecation") //YK: getXYZArray() array accessors are deprecated in xmlbeans with JDK 1.5 support
    private void init(CTStylesheet styleSheet) {
    	numberFormats = new LinkedHashMap<Integer,String>();
    	fonts = new ArrayList<XSSFFont>();
    	fills = new ArrayList<XSSFCellFill>();
    	borders = new ArrayList<XSSFCellBorder>();
    	styleXfs = new ArrayList<CTXf>();
    	xfs = new ArrayList<CTXf>();
    	dxfs = new ArrayList<CTDxf>();
    	cellStyles = new HashMap<String, CTCellStyle>();
    	
        // Grab all the different bits we care about
		CTNumFmts ctfmts = styleSheet.getNumFmts();
        if( ctfmts != null){
            for (CTNumFmt nfmt : ctfmts.getNumFmtArray()) {
                numberFormats.put((int)nfmt.getNumFmtId(), nfmt.getFormatCode());
            }
        }

        CTFonts ctfonts = styleSheet.getFonts();
        if(ctfonts != null){
			int idx = 0;
			for (CTFont font : ctfonts.getFontArray()) {
			   // Create the font and save it. Themes Table supplied later
				XSSFFont f = new XSSFFont(font, idx);
				fonts.add(f);
				idx++;
			}
		}

        CTFills ctfills = styleSheet.getFills();
        if(ctfills != null){
            for (CTFill fill : ctfills.getFillArray()) {
                fills.add(new XSSFCellFill(fill));
            }
        }

        CTBorders ctborders = styleSheet.getBorders();
        if(ctborders != null) {
            for (CTBorder border : ctborders.getBorderArray()) {
                borders.add(new XSSFCellBorder(border, theme));
            }
        }

        CTCellXfs cellXfs = styleSheet.getCellXfs();
        if(cellXfs != null) xfs.addAll(Arrays.asList(cellXfs.getXfArray()));
        
        //ZSS-854
		CTCellStyles ctCellStyles = styleSheet.getCellStyles();
		if (ctCellStyles != null) {
			for (CTCellStyle style : ctCellStyles.getCellStyleArray()) {
				cellStyles.put(style.getName(), style);
			}
		}

        CTCellStyleXfs cellStyleXfs = styleSheet.getCellStyleXfs();
        if(cellStyleXfs != null) styleXfs.addAll(Arrays.asList(cellStyleXfs.getXfArray()));

        CTDxfs styleDxfs = styleSheet.getDxfs();
		if(styleDxfs != null) dxfs.addAll(Arrays.asList(styleDxfs.getDxfArray()));
		
	}

	// ===========================================================
	//  Start of style related getters and setters
	// ===========================================================

	public String getNumberFormatAt(int idx) {
		return numberFormats.get(idx);
	}

	public int putNumberFormat(String fmt) {
		if (numberFormats.containsValue(fmt)) {
			// Find the key, and return that
			for(Integer key : numberFormats.keySet() ) {
				if(numberFormats.get(key).equals(fmt)) {
					return key;
				}
			}
			throw new IllegalStateException("Found the format, but couldn't figure out where - should never happen!");
		}

		// Find a spare key, and add that
		int newKey = FIRST_CUSTOM_STYLE_ID;
		while(numberFormats.containsKey(newKey)) {
			newKey++;
		}
		numberFormats.put(newKey, fmt);
		return newKey;
	}

	public XSSFFont getFontAt(int idx) {
		return fonts.get(idx);
	}

	/**
	 * Records the given font in the font table.
	 * Will re-use an existing font index if this
	 *  font matches another, EXCEPT if forced
	 *  registration is requested.
	 * This allows people to create several fonts
	 *  then customise them later.
	 * Note - End Users probably want to call
	 *  {@link XSSFFont#registerTo(StylesTable)}
	 */
	public int putFont(XSSFFont font, boolean forceRegistration) {
		int idx = -1;
		if(!forceRegistration) {
			idx = fonts.indexOf(font);
		}

		if (idx != -1) {
			return idx;
		}
		
		idx = fonts.size();
		fonts.add(font);
		return idx;
	}
	public int putFont(XSSFFont font) {
		return putFont(font, false);
	}

	public XSSFCellStyle getStyleAt(int idx) {
		int styleXfId = 0;

		// 0 is the empty default
		if(xfs.get(idx).getXfId() > 0) {
			styleXfId = (int) xfs.get(idx).getXfId();
		}

		return new XSSFCellStyle(idx, styleXfId, this, theme);
	}
	public int putStyle(XSSFCellStyle style) {
		CTXf mainXF = style.getCoreXf();

		if(! xfs.contains(mainXF)) {
			xfs.add(mainXF);
		}
		return xfs.indexOf(mainXF);
	}
	
	public XSSFCellBorder getBorderAt(int idx) {
		return borders.get(idx);
	}

	public int putBorder(XSSFCellBorder border) {
		int idx = borders.indexOf(border);
		if (idx != -1) {
			return idx;
		}
		borders.add(border);
		border.setThemesTable(theme);
		return borders.size() - 1;
	}

	public XSSFCellFill getFillAt(int idx) {
		return fills.get(idx);
	}

	public List<XSSFCellBorder> getBorders(){
		return borders;
	}

	public List<XSSFCellFill> getFills(){
		return fills;
	}

	public List<XSSFFont> getFonts(){
		return fonts;
	}

	public Map<Integer, String> getNumberFormats(){
		return numberFormats;
	}

	public int putFill(XSSFCellFill fill) {
		int idx = fills.indexOf(fill);
		if (idx != -1) {
			return idx;
		}
		fills.add(fill);
		return fills.size() - 1;
	}

	public CTXf getCellXfAt(int idx) {
		return xfs.get(idx);
	}
	public int putCellXf(CTXf cellXf) {
		xfs.add(cellXf);
		return xfs.size();
	}
   public void replaceCellXfAt(int idx, CTXf cellXf) {
      xfs.set(idx, cellXf);
   }

	public CTXf getCellStyleXfAt(int idx) {
		return styleXfs.get(idx);
	}
	public void replaceCellStyleXfAt(int idx, CTXf cellStyleXf) {
	   styleXfs.set(idx, cellStyleXf);
	}
	
	/**
	 * get the size of cell styles
	 */
	public int getNumCellStyles(){
        // Each cell style has a unique xfs entry
        // Several might share the same styleXfs entry
        return xfs.size();
	}

	/**
	 * For unit testing only
	 */
	public int _getNumberFormatSize() {
		return numberFormats.size();
	}

	/**
	 * For unit testing only
	 */
	public int _getXfsSize() {
		return xfs.size();
	}
	/**
	 * For unit testing only
	 */
	public int _getStyleXfsSize() {
		return styleXfs.size();
	}
	/**
	 * For unit testing only!
	 */
	public CTStylesheet getCTStylesheet() {
		return doc.getStyleSheet();
	}
    public int _getDXfsSize() {
        return dxfs.size();
    }


	/**
	 * Write this table out as XML.
	 *
	 * @param out The stream to write to.
	 * @throws IOException if an error occurs while writing.
	 */
	public void writeTo(OutputStream out) throws IOException {
		XmlOptions options = new XmlOptions(DEFAULT_XML_OPTIONS);

		// Work on the current one
		// Need to do this, as we don't handle
		//  all the possible entries yet
        CTStylesheet styleSheet = doc.getStyleSheet();

		// Formats
		CTNumFmts formats = CTNumFmts.Factory.newInstance();
		formats.setCount(numberFormats.size());
		for (Entry<Integer, String> fmt : numberFormats.entrySet()) {
			CTNumFmt ctFmt = formats.addNewNumFmt();
			ctFmt.setNumFmtId(fmt.getKey());
			ctFmt.setFormatCode(fmt.getValue());
		}
		styleSheet.setNumFmts(formats);

		int idx;
		// Fonts
		CTFonts ctFonts = CTFonts.Factory.newInstance();
		ctFonts.setCount(fonts.size());
		CTFont[] ctfnt = new CTFont[fonts.size()];
		idx = 0;
		for(XSSFFont f : fonts) ctfnt[idx++] = f.getCTFont();
		ctFonts.setFontArray(ctfnt);
		styleSheet.setFonts(ctFonts);

		// Fills
		CTFills ctFills = CTFills.Factory.newInstance();
		ctFills.setCount(fills.size());
		CTFill[] ctf = new CTFill[fills.size()];
		idx = 0;
		for(XSSFCellFill f : fills) ctf[idx++] = f.getCTFill();
		ctFills.setFillArray(ctf);
		styleSheet.setFills(ctFills);

		// Borders
		CTBorders ctBorders = CTBorders.Factory.newInstance();
		ctBorders.setCount(borders.size());
		CTBorder[] ctb = new CTBorder[borders.size()];
		idx = 0;
		for(XSSFCellBorder b : borders) ctb[idx++] = b.getCTBorder();
		ctBorders.setBorderArray(ctb);
		styleSheet.setBorders(ctBorders);

		// Xfs
		if(xfs.size() > 0) {
			CTCellXfs ctXfs = CTCellXfs.Factory.newInstance();
			ctXfs.setCount(xfs.size());
			ctXfs.setXfArray(
					xfs.toArray(new CTXf[xfs.size()])
			);
			styleSheet.setCellXfs(ctXfs);
		}

		// Style xfs
		if(styleXfs.size() > 0) {
			CTCellStyleXfs ctSXfs = CTCellStyleXfs.Factory.newInstance();
			ctSXfs.setCount(styleXfs.size());
			ctSXfs.setXfArray(
					styleXfs.toArray(new CTXf[styleXfs.size()])
			);
			styleSheet.setCellStyleXfs(ctSXfs);
		}

		//ZSS-854
		// Style cellStyles
		if (cellStyles.size() > 0) {
			CTCellStyles ctCellStyles = CTCellStyles.Factory.newInstance();
			ctCellStyles.setCount(cellStyles.size());
			ctCellStyles.setCellStyleArray(cellStyles.values().toArray(new CTCellStyle[cellStyles.size()])
			);
			styleSheet.setCellStyles(ctCellStyles);
		}
		
		// Style dxfs
		if(dxfs.size() > 0) {
			CTDxfs ctDxfs = CTDxfs.Factory.newInstance();
			ctDxfs.setCount(dxfs.size());
			ctDxfs.setDxfArray(dxfs.toArray(new CTDxf[dxfs.size()])
			);
			styleSheet.setDxfs(ctDxfs);
		}

		// Save
		doc.save(out, options);
	
		//20110114, henrichen@zkoss.org: setArray(xyz[]) does not guarantee getArray() equals to xyz[]
		init(styleSheet);
	}

	@Override
	protected void commit() throws IOException {
		PackagePart part = getPackagePart();
		OutputStream out = part.getOutputStream();
		writeTo(out);
		out.close();
	}

	private void initialize() {
		//CTFont ctFont = createDefaultFont();
		XSSFFont xssfFont = createDefaultFont();
		fonts.add(xssfFont);

		CTFill[] ctFill = createDefaultFills();
		fills.add(new XSSFCellFill(ctFill[0]));
		fills.add(new XSSFCellFill(ctFill[1]));

		CTBorder ctBorder = createDefaultBorder();
		borders.add(new XSSFCellBorder(ctBorder));

		CTXf styleXf = createDefaultXf();
		styleXfs.add(styleXf);
		CTXf xf = createDefaultXf();
		xf.setXfId(0);
		xfs.add(xf);
	}

	private static CTXf createDefaultXf() {
		CTXf ctXf = CTXf.Factory.newInstance();
		ctXf.setNumFmtId(0);
		ctXf.setFontId(0);
		ctXf.setFillId(0);
		ctXf.setBorderId(0);
		return ctXf;
	}
	private static CTBorder createDefaultBorder() {
		CTBorder ctBorder = CTBorder.Factory.newInstance();
		ctBorder.addNewBottom();
		ctBorder.addNewTop();
		ctBorder.addNewLeft();
		ctBorder.addNewRight();
		ctBorder.addNewDiagonal();
		return ctBorder;
	}


	private static CTFill[] createDefaultFills() {
		CTFill[] ctFill = new CTFill[]{CTFill.Factory.newInstance(),CTFill.Factory.newInstance()};
		ctFill[0].addNewPatternFill().setPatternType(STPatternType.NONE);
		ctFill[1].addNewPatternFill().setPatternType(STPatternType.GRAY_125);
		return ctFill;
	}

	private static XSSFFont createDefaultFont() {
		CTFont ctFont = CTFont.Factory.newInstance();
		XSSFFont xssfFont=new XSSFFont(ctFont, 0);
		xssfFont.setFontHeightInPoints(XSSFFont.DEFAULT_FONT_SIZE);
		xssfFont.setColor(XSSFFont.DEFAULT_FONT_COLOR);//setTheme
		xssfFont.setFontName(XSSFFont.DEFAULT_FONT_NAME);
		xssfFont.setFamily(FontFamily.SWISS);
		xssfFont.setScheme(FontScheme.MINOR);
		return xssfFont;
	}

	public CTDxf getDxfAt(int idx) {
		return dxfs.get(idx);
	}

	public int putDxf(CTDxf dxf) {
		this.dxfs.add(dxf);
		return this.dxfs.size();
	}

	public XSSFCellStyle createCellStyle() {
		CTXf xf = CTXf.Factory.newInstance();
		xf.setNumFmtId(0);
		xf.setFontId(0);
		xf.setFillId(0);
		xf.setBorderId(0);
		xf.setXfId(0);
		int xfSize = styleXfs.size();
		int indexXf = putCellXf(xf);
		return new XSSFCellStyle(indexXf - 1, xfSize - 1, this, theme);
	}

	/**
	 * Finds a font that matches the one with the supplied attributes
	 */
	public XSSFFont findFont(short boldWeight, short color, short fontHeight, String name, boolean italic, boolean strikeout, short typeOffset, byte underline) {
		for (XSSFFont font : fonts) {
			if (	(font.getBoldweight() == boldWeight)
					&& font.getColor() == color
					&& font.getFontHeight() == fontHeight
					&& font.getFontName().equals(name)
					&& font.getItalic() == italic
					&& font.getStrikeout() == strikeout
					&& font.getTypeOffset() == typeOffset
					&& font.getUnderline() == underline)
			{
				return font;
			}
		}
		return null;
	}

	//ZSS-728
	/**
	 * Find a font per the given font.
	 */
	public XSSFFont findFont(short boldWeight, XSSFColor color, short fontHeight, String name, boolean italic, boolean strikeout, short typeOffset, byte underline) {
		for (XSSFFont font : fonts) {
			XSSFColor color0 = font.getXSSFColor();
			if (	(font.getBoldweight() == boldWeight)
					&& (color0 == color || (color != null && color.equals(color0)))
					&& font.getFontHeight() == fontHeight
					&& font.getFontName().equals(name)
					&& font.getItalic() == italic
					&& font.getStrikeout() == strikeout
					&& font.getTypeOffset() == typeOffset
					&& font.getUnderline() == underline)
			{
				return font;
			}
		}
		return null;
	}

	//ZSS-787
	//20141017, henrichen: give way for setting the default cell style
	public void setDefaultCellStyle(XSSFCellStyle cellStyle) {
		CTXf styleXf = prepareStyleXf(cellStyle);
		replaceCellStyleXfAt(0, styleXf);
		CTXf cellXf = prepareCellXf(cellStyle);
		replaceCellXfAt(0, cellXf);
	}

	private CTXf prepareCellXf(XSSFCellStyle cellStyle) {
		CTXf cellXf1 = cellStyle.getCellXf();
		CTXf cellXf0 = (CTXf) cellXf1.copy();
		if (cellXf0.isSetApplyAlignment()) {
			cellXf0.unsetApplyAlignment();
		}
		if (cellXf0.isSetApplyBorder()) {
			cellXf0.unsetApplyBorder();
		}
		if (cellXf0.isSetApplyFill()) {
			cellXf0.unsetApplyFill();
		}
		if (cellXf0.isSetApplyFont()) {
			cellXf0.unsetApplyFont();
		}
		if (cellXf0.isSetApplyNumberFormat()) {
			cellXf0.unsetApplyNumberFormat();
		}
		if (cellXf0.isSetApplyProtection()) {
			cellXf0.unsetApplyProtection();
		}
		if (cellXf0.isSetProtection()) {
			final CTCellProtection prot = cellXf0.getProtection();
			//default !hidden && locked
			if (prot.isSetHidden() && !prot.getHidden()) {
				prot.unsetHidden();
			}
			if (prot.isSetLocked() && prot.getLocked()) {
				prot.unsetLocked();
			}
			if (!prot.isSetLocked() && !prot.isSetHidden()) {
				cellXf0.unsetProtection();
			}
		}
		if (cellXf0.isSetAlignment()) {
			CTCellAlignment align = cellXf0.getAlignment();
			if (align.isSetWrapText() && !align.getWrapText()) { //wrapText == false
				align.unsetWrapText();
			}
			if (align.isSetHorizontal() && STHorizontalAlignment.GENERAL.equals(align.getHorizontal())) { // horizontal == "general"
				align.unsetHorizontal();
			}
			if (align.isSetVertical() && STVerticalAlignment.BOTTOM.equals(align.getVertical())) { // vertical == "bottom"
				align.unsetVertical();
			}
			if (align.isNil()) {
				cellXf0.unsetAlignment();
			}
		}
		cellXf0.setBorderId(0);
		cellXf0.setFillId(0);
		cellXf0.setFontId(0);
		cellXf0.setNumFmtId(0);

		return cellXf0;
	}
	
	private CTXf prepareStyleXf(XSSFCellStyle cellStyle) { 
		CTXf cellXf0 = prepareCellXf(cellStyle);
		CTXf styleXf = (CTXf) cellXf0.copy();//CTXf.Factory.newInstance();
//		styleXf.setBorderId(0);
//		styleXf.setFillId(0);
//		styleXf.setFontId(0);
//		styleXf.setNumFmtId(0);
		if (cellXf0.isSetAlignment()) {
			styleXf.setAlignment(cellXf0.getAlignment());
		}
		if (cellXf0.isSetProtection()) {
			styleXf.setProtection(cellXf0.getProtection());
		}
		if (styleXf.isSetXfId()) {
			styleXf.unsetXfId();
		}

		return styleXf;
	}
	
	//ZSS-854
	public int putCellStyle(CTCellStyle style) {
		cellStyles.put(style.getName(), style);
		return cellStyles.size() - 1;
	}
	//ZSS-854
	public int putCellStyleXf(CTXf xf) {
		styleXfs.add(xf);
		return styleXfs.size() - 1;
	}
	
	//ZSS-854
	public CTCellStyle getCellStyleByName(String name) {
		return cellStyles.get(name);
	}

	//ZSS-854
	public Collection<CTXf> getCellStyleXfs() {
		return styleXfs;
	}

	//ZSS-854
	public Collection<CTCellStyle> getCellStyles() {
		return cellStyles.values();
	}

	//ZSS-854
	public CTCellStyle getCellStyle(String name) {
		return cellStyles.get(name);
	}
	
	//ZSS-854
	public XSSFNamedStyle getStyleByName(String name) {
		int styleXfId = 0;

		CTCellStyle style = cellStyles.get(name);
		if (style != null) {
			styleXfId = (int) style.getXfId();
		}

		return new XSSFNamedStyle(name, style.getCustomBuiltin(), (int) style.getBuiltinId(), styleXfId, this, theme);  
	}
	
	//ZSS-854
	public Collection<CTDxf> getCellStyleDxfs() {
		return dxfs;
	}
	
	//ZSS-854
	public XSSFCellStyle createDefaultCellStyle(XSSFCellBorder border, XSSFCellFill fill, XSSFFont font, int numFmtId) {
		int fontId = putFont(font);
		int fillId = putFill(fill);
		int borderId = putBorder(border);
		
		CTXf xf = CTXf.Factory.newInstance();
		xf.setNumFmtId(numFmtId);
		xf.setFontId(fontId);
		xf.setFillId(fillId);
		xf.setBorderId(borderId);
		return new XSSFCellStyle(xf, this, theme);
	}

	//ZSS-854
	public List<CTXf> getCellXfs() {
		return xfs;
	}
}

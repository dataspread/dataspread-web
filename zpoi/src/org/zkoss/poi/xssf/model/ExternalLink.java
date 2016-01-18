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

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.ExternalLinkDocument;
import org.zkoss.poi.POIXMLDocumentPart;
import org.zkoss.poi.openxml4j.opc.PackagePart;
import org.zkoss.poi.openxml4j.opc.PackageRelationship;
import org.zkoss.poi.ss.usermodel.BuiltinFormats;


/**
 * Table of styles shared across all sheets in a workbook.
 *
 * @author Henri Chen (henrichen at zkoss dot org) - [book1.xlsx]Sheet1:Sheet3!xxx external reference
 */
public class ExternalLink extends POIXMLDocumentPart {
	private String _bookName;
	private String _linkIndex;

	/**
	 * The first style id available for use as a custom style
	 */
	public static final int FIRST_CUSTOM_STYLE_ID = BuiltinFormats.FIRST_USER_DEFINED_FORMAT_INDEX + 1;

	private ExternalLinkDocument doc;

	/**
	 * Create a new, empty StylesTable
	 */
	public ExternalLink() {
		super();
		doc = ExternalLinkDocument.Factory.newInstance();
		doc.addNewExternalLink();
		// Initialization required in order to make the document readable by MSExcel
		initialize();
	}

	public ExternalLink(PackagePart part, PackageRelationship rel) throws IOException {
		super(part, rel);
		readFrom(part.getInputStream());
	}

	/**
	 * Read this shared external links from an XML file.
	 *
	 * @param is The input stream containing the XML document.
	 * @throws IOException if an error occurs while reading.
	 */
	protected void readFrom(InputStream is) throws IOException {
		try {
			doc = ExternalLinkDocument.Factory.parse(is);
			final String rid =  doc.getExternalLink().getExternalBook().getId();
			PackageRelationship extrel = getPackagePart().getRelationship(rid);
			_bookName = extrel.getTargetURI().toString();
			final String srcPath = extrel.getSourceURI().toString();
			final int j = srcPath.lastIndexOf("externalLink");
			final int k = srcPath.lastIndexOf(".xml");
			_linkIndex = srcPath.substring(j+12, k);
			//TODO externalbook, sheetname, sheetdataset ...
		} catch (XmlException e) {
			throw new IOException(e.getLocalizedMessage());
		}
	}

	// ===========================================================
	//  Start of external link related getters and setters
	// ===========================================================

	public String getBookName() {
		return _bookName;
	}
	
	public void setBookName(String bookname) {
		_bookName = bookname;
	}
	
	public String getLinkIndex() {
		return _linkIndex;
	}
	
	public void setLinkIndex(String index) {
		_linkIndex = index;
	}

	/**
	 * Write this external link out as XML.
	 *
	 * @param out The stream to write to.
	 * @throws IOException if an error occurs while writing.
	 */
	public void writeTo(OutputStream out) throws IOException {
		XmlOptions options = new XmlOptions(DEFAULT_XML_OPTIONS);

		//TODO not supported yet. externalLink?.xml and _rel/externalLink?.xml.rel
		//operate on doc

		// Save
		doc.save(out, options);
	}

	@Override
	protected void commit() throws IOException {
		PackagePart part = getPackagePart();
		OutputStream out = part.getOutputStream();
		writeTo(out);
		out.close();
	}

	private void initialize() {
		//TODO, shall prepare some default value for ExternalLink document?
	}
}

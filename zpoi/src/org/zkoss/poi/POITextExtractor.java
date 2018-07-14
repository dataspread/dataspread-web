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
package org.zkoss.poi;

/**
 * Common Parent for Text Extractors
 *  of POI Documents. 
 * You will typically find the implementation of
 *  a given format's text extractor under
 *  org.zkoss.poi.[format].extractor .
 * @see org.zkoss.poi.hssf.extractor.ExcelExtractor
 * @see org.zkoss.poi.hslf.extractor.PowerPointExtractor
 * @see org.zkoss.poi.hdgf.extractor.VisioTextExtractor
 * @see org.zkoss.poi.hwpf.extractor.WordExtractor
 */
public abstract class POITextExtractor {
	/** The POIDocument that's open */
	protected POIDocument document;

	/**
	 * Creates a new text extractor for the given document
	 */
	public POITextExtractor(POIDocument document) {
		this.document = document;
	}
	/**
	 * Creates a new text extractor, using the same
	 *  document as another text extractor. Normally
	 *  only used by properties extractors.
	 */
	protected POITextExtractor(POITextExtractor otherExtractor) {
		this.document = otherExtractor.document;
	}
	
	/**
	 * Retrieves all the text from the document.
	 * How cells, paragraphs etc are separated in the text
	 *  is implementation specific - see the javadocs for
	 *  a specific project for details.
	 * @return All the text from the document
	 */
	public abstract String getText();
	
	/**
	 * Returns another text extractor, which is able to
	 *  output the textual content of the document
	 *  metadata / properties, such as author and title.
	 */
	public abstract POITextExtractor getMetadataTextExtractor();
}

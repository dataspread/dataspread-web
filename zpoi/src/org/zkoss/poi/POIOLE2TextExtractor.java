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

import org.zkoss.poi.hpsf.DocumentSummaryInformation;
import org.zkoss.poi.hpsf.SummaryInformation;
import org.zkoss.poi.hpsf.extractor.HPSFPropertiesExtractor;
import org.zkoss.poi.poifs.filesystem.DirectoryEntry;
import org.zkoss.poi.poifs.filesystem.POIFSFileSystem;

/**
 * Common Parent for OLE2 based Text Extractors
 *  of POI Documents, such as .doc, .xls
 * You will typically find the implementation of
 *  a given format's text extractor under
 *  org.zkoss.poi.[format].extractor .
 * @see org.zkoss.poi.hssf.extractor.ExcelExtractor
 * @see org.zkoss.poi.hslf.extractor.PowerPointExtractor
 * @see org.zkoss.poi.hdgf.extractor.VisioTextExtractor
 * @see org.zkoss.poi.hwpf.extractor.WordExtractor
 */
public abstract class POIOLE2TextExtractor extends POITextExtractor {
	/**
	 * Creates a new text extractor for the given document
	 */
	public POIOLE2TextExtractor(POIDocument document) {
		super(document);
	}

	/**
	 * Returns the document information metadata for the document
	 */
	public DocumentSummaryInformation getDocSummaryInformation() {
		return document.getDocumentSummaryInformation();
	}
	/**
	 * Returns the summary information metadata for the document
	 */
	public SummaryInformation getSummaryInformation() {
		return document.getSummaryInformation();
	}

	/**
	 * Returns an HPSF powered text extractor for the
	 *  document properties metadata, such as title and author.
	 */
	public POITextExtractor getMetadataTextExtractor() {
		return new HPSFPropertiesExtractor(this);
	}

    public DirectoryEntry getRoot()
    {
        return document.directory;
    }

    /**
     * Return the underlying POIFS FileSystem of this document.
     *
     * @deprecated Use {@link #getRoot()} instead
     */
    @Deprecated
    public POIFSFileSystem getFileSystem()
    {
        return document.directory.getFileSystem();
    }
}

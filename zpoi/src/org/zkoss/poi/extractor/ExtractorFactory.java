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
package org.zkoss.poi.extractor;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.xmlbeans.XmlException;
import org.zkoss.poi.POIOLE2TextExtractor;
import org.zkoss.poi.POITextExtractor;
import org.zkoss.poi.POIXMLDocument;
import org.zkoss.poi.POIXMLTextExtractor;
import org.zkoss.poi.hdgf.extractor.VisioTextExtractor;
import org.zkoss.poi.hpbf.extractor.PublisherTextExtractor;
import org.zkoss.poi.hslf.extractor.PowerPointExtractor;
import org.zkoss.poi.hsmf.MAPIMessage;
import org.zkoss.poi.hsmf.datatypes.AttachmentChunks;
import org.zkoss.poi.hsmf.extractor.OutlookTextExtactor;
import org.zkoss.poi.hssf.extractor.EventBasedExcelExtractor;
import org.zkoss.poi.hssf.extractor.ExcelExtractor;
import org.zkoss.poi.hwpf.OldWordFileFormatException;
import org.zkoss.poi.hwpf.extractor.Word6Extractor;
import org.zkoss.poi.hwpf.extractor.WordExtractor;
import org.zkoss.poi.openxml4j.exceptions.InvalidFormatException;
import org.zkoss.poi.openxml4j.exceptions.OpenXML4JException;
import org.zkoss.poi.openxml4j.opc.OPCPackage;
import org.zkoss.poi.openxml4j.opc.PackagePart;
import org.zkoss.poi.openxml4j.opc.PackageRelationshipCollection;
import org.zkoss.poi.poifs.filesystem.DirectoryEntry;
import org.zkoss.poi.poifs.filesystem.DirectoryNode;
import org.zkoss.poi.poifs.filesystem.Entry;
import org.zkoss.poi.poifs.filesystem.POIFSFileSystem;
import org.zkoss.poi.xslf.extractor.XSLFPowerPointExtractor;
import org.zkoss.poi.xslf.usermodel.XSLFRelation;
import org.zkoss.poi.xssf.extractor.XSSFEventBasedExcelExtractor;
import org.zkoss.poi.xssf.extractor.XSSFExcelExtractor;
import org.zkoss.poi.xssf.usermodel.XSSFRelation;
import org.zkoss.poi.xwpf.extractor.XWPFWordExtractor;
import org.zkoss.poi.xwpf.usermodel.XWPFRelation;

/**
 * Figures out the correct POITextExtractor for your supplied
 *  document, and returns it.
 */
public class ExtractorFactory {
	public static final String CORE_DOCUMENT_REL =
		"http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument";


	/** Should this thread prefer event based over usermodel based extractors? */
	private static final ThreadLocal<Boolean> threadPreferEventExtractors = new ThreadLocal<Boolean>() {
      protected Boolean initialValue() { return Boolean.FALSE; }
	};
	/** Should all threads prefer event based over usermodel based extractors? */
	private static Boolean allPreferEventExtractors;

   /**
    * Should this thread prefer event based over usermodel based extractors?
    * (usermodel extractors tend to be more accurate, but use more memory)
    * Default is false.
    */
	public static boolean getThreadPrefersEventExtractors() {
	   return threadPreferEventExtractors.get();
	}
   /**
    * Should all threads prefer event based over usermodel based extractors?
    * (usermodel extractors tend to be more accurate, but use more memory)
    * Default is to use the thread level setting, which defaults to false.
    */
	public static Boolean getAllThreadsPreferEventExtractors() {
	   return allPreferEventExtractors;
	}

   /**
    * Should this thread prefer event based over usermodel based extractors?
    * Will only be used if the All Threads setting is null.
    */
   public static void setThreadPrefersEventExtractors(boolean preferEventExtractors) {
      threadPreferEventExtractors.set(preferEventExtractors);
   }
   /**
    * Should all threads prefer event based over usermodel based extractors?
    * If set, will take preference over the Thread level setting.
    */
   public static void setAllThreadsPreferEventExtractors(Boolean preferEventExtractors) {
      allPreferEventExtractors = preferEventExtractors;
   }


   /**
    * Should this thread use event based extractors is available?
    * Checks the all-threads one first, then thread specific.
    */
   protected static boolean getPreferEventExtractor() {
      if(allPreferEventExtractors != null) {
         return allPreferEventExtractors;
      }
      return threadPreferEventExtractors.get();
   }


	public static POITextExtractor createExtractor(File f) throws IOException, InvalidFormatException, OpenXML4JException, XmlException {
		InputStream inp = null;
        try {
            inp = new PushbackInputStream(
                new FileInputStream(f), 8);

            if(POIFSFileSystem.hasPOIFSHeader(inp)) {
                return createExtractor(new POIFSFileSystem(inp));
            }
            if(POIXMLDocument.hasOOXMLHeader(inp)) {
                return createExtractor(OPCPackage.open(f.toString()));
            }
            throw new IllegalArgumentException("Your File was neither an OLE2 file, nor an OOXML file");
        } finally {
            if(inp != null) inp.close();
        }
    }

	public static POITextExtractor createExtractor(InputStream inp) throws IOException, InvalidFormatException, OpenXML4JException, XmlException {
		// Figure out the kind of stream
		// If clearly doesn't do mark/reset, wrap up
		if(! inp.markSupported()) {
			inp = new PushbackInputStream(inp, 8);
		}

		if(POIFSFileSystem.hasPOIFSHeader(inp)) {
			return createExtractor(new POIFSFileSystem(inp));
		}
		if(POIXMLDocument.hasOOXMLHeader(inp)) {
			return createExtractor(OPCPackage.open(inp));
		}
		throw new IllegalArgumentException("Your InputStream was neither an OLE2 stream, nor an OOXML stream");
	}

	public static POIXMLTextExtractor createExtractor(OPCPackage pkg) throws IOException, OpenXML4JException, XmlException {
       PackageRelationshipCollection core =
            pkg.getRelationshipsByType(CORE_DOCUMENT_REL);
       if(core.size() != 1) {
          throw new IllegalArgumentException("Invalid OOXML Package received - expected 1 core document, found " + core.size());
       }

       PackagePart corePart = pkg.getPart(core.getRelationship(0));

       // Is it XSSF?
       for(XSSFRelation rel : XSSFExcelExtractor.SUPPORTED_TYPES) {
          if(corePart.getContentType().equals(rel.getContentType())) {
             if(getPreferEventExtractor()) {
                return new XSSFEventBasedExcelExtractor(pkg);
             } else {
                return new XSSFExcelExtractor(pkg);
             }
          }
       }

       // Is it XWPF?
       for(XWPFRelation rel : XWPFWordExtractor.SUPPORTED_TYPES) {
          if(corePart.getContentType().equals(rel.getContentType())) {
             return new XWPFWordExtractor(pkg);
          }
       }

       // Is it XSLF?
       for(XSLFRelation rel : XSLFPowerPointExtractor.SUPPORTED_TYPES) {
          if(corePart.getContentType().equals(rel.getContentType())) {
             return new XSLFPowerPointExtractor(pkg);
          }
       }

       throw new IllegalArgumentException("No supported documents found in the OOXML package (found "+corePart.getContentType()+")");
	}

	public static POIOLE2TextExtractor createExtractor(POIFSFileSystem fs) throws IOException, InvalidFormatException, OpenXML4JException, XmlException {
	   // Only ever an OLE2 one from the root of the FS
		return (POIOLE2TextExtractor)createExtractor(fs.getRoot());
	}

    /**
     * @deprecated Use {@link #createExtractor(DirectoryNode)} instead
     */
    @Deprecated
    @SuppressWarnings("unused")
    public static POITextExtractor createExtractor(DirectoryNode poifsDir, POIFSFileSystem fs)
            throws IOException, InvalidFormatException, OpenXML4JException, XmlException
    {
        return createExtractor(poifsDir);
    }

    public static POITextExtractor createExtractor(DirectoryNode poifsDir) throws IOException,
            InvalidFormatException, OpenXML4JException, XmlException
    {
        // Look for certain entries in the stream, to figure it
        // out from
        if (poifsDir.hasEntry("Workbook")) {
            if (getPreferEventExtractor()) {
                return new EventBasedExcelExtractor(poifsDir);
            }
            return new ExcelExtractor(poifsDir);
        }

        if (poifsDir.hasEntry("WordDocument")) {
            // Old or new style word document?
            try {
                return new WordExtractor(poifsDir);
            } catch (OldWordFileFormatException e) {
                return new Word6Extractor(poifsDir);
            }
        }

        if (poifsDir.hasEntry("PowerPoint Document")) {
            return new PowerPointExtractor(poifsDir);
        }

        if (poifsDir.hasEntry("VisioDocument")) {
            return new VisioTextExtractor(poifsDir);
        }

        if (poifsDir.hasEntry("Quill")) {
            return new PublisherTextExtractor(poifsDir);
        }

        if (poifsDir.hasEntry("__substg1.0_1000001E") || poifsDir.hasEntry("__substg1.0_1000001F")
                || poifsDir.hasEntry("__substg1.0_0047001E")
                || poifsDir.hasEntry("__substg1.0_0047001F")
                || poifsDir.hasEntry("__substg1.0_0037001E")
                || poifsDir.hasEntry("__substg1.0_0037001F"))
        {
            return new OutlookTextExtactor(poifsDir);
        }

        for (Iterator<Entry> entries = poifsDir.getEntries(); entries.hasNext();) {
            Entry entry = entries.next();

            if (entry.getName().equals("Package")) {
                OPCPackage pkg = OPCPackage.open(poifsDir.createDocumentInputStream("Package"));
                return createExtractor(pkg);
            }
        }
        throw new IllegalArgumentException("No supported documents found in the OLE2 stream");
    }

	/**
	 * Returns an array of text extractors, one for each of
	 *  the embeded documents in the file (if there are any).
	 * If there are no embeded documents, you'll get back an
	 *  empty array. Otherwise, you'll get one open
	 *  {@link POITextExtractor} for each embeded file.
	 */
	public static POITextExtractor[] getEmbededDocsTextExtractors(POIOLE2TextExtractor ext) throws IOException, InvalidFormatException, OpenXML4JException, XmlException {
	   // All the embded directories we spotted
		ArrayList<Entry> dirs = new ArrayList<Entry>();
		// For anything else not directly held in as a POIFS directory
		ArrayList<InputStream> nonPOIFS = new ArrayList<InputStream>();

      // Find all the embeded directories
		DirectoryEntry root = ext.getRoot();
		if(root == null) {
			throw new IllegalStateException("The extractor didn't know which POIFS it came from!");
		}

		if(ext instanceof ExcelExtractor) {
			// These are in MBD... under the root
			Iterator<Entry> it = root.getEntries();
			while(it.hasNext()) {
				Entry entry = it.next();
				if(entry.getName().startsWith("MBD")) {
					dirs.add(entry);
				}
			}
		} else if(ext instanceof WordExtractor) {
			// These are in ObjectPool -> _... under the root
			try {
				DirectoryEntry op = (DirectoryEntry)
				        root.getEntry("ObjectPool");
				Iterator<Entry> it = op.getEntries();
				while(it.hasNext()) {
					Entry entry = it.next();
					if(entry.getName().startsWith("_")) {
						dirs.add(entry);
					}
				}
			} catch(FileNotFoundException e) {}
		} else if(ext instanceof PowerPointExtractor) {
			// Tricky, not stored directly in poifs
			// TODO
		} else if(ext instanceof OutlookTextExtactor) {
		   // Stored in the Attachment blocks
		   MAPIMessage msg = ((OutlookTextExtactor)ext).getMAPIMessage();
		   for(AttachmentChunks attachment : msg.getAttachmentFiles()) {
		      if(attachment.attachData != null) {
   		         byte[] data = attachment.attachData.getValue();
   		         nonPOIFS.add( new ByteArrayInputStream(data) );
		      } else if(attachment.attachmentDirectory != null) {
		          dirs.add(attachment.attachmentDirectory.getDirectory());
		      }
		   }
		}

		// Create the extractors
		if(
		      (dirs == null || dirs.size() == 0) &&
		      (nonPOIFS == null || nonPOIFS.size() == 0)
		){
			return new POITextExtractor[0];
		}

		ArrayList<POITextExtractor> e = new ArrayList<POITextExtractor>();
		for(int i=0; i<dirs.size(); i++) {
			e.add( createExtractor(
					(DirectoryNode)dirs.get(i)
			) );
		}
		for(int i=0; i<nonPOIFS.size(); i++) {
		   try {
		      e.add( createExtractor(nonPOIFS.get(i)) );
         } catch(IllegalArgumentException ie) {
            // Ignore, just means it didn't contain
            //  a format we support as yet
		   } catch(XmlException xe) {
		      throw new IOException(xe.getMessage());
		   } catch(OpenXML4JException oe) {
		      throw new IOException(oe.getMessage());
		   }
		}
		return e.toArray(new POITextExtractor[e.size()]);
	}

	/**
	 * Returns an array of text extractors, one for each of
	 *  the embeded documents in the file (if there are any).
	 * If there are no embeded documents, you'll get back an
	 *  empty array. Otherwise, you'll get one open
	 *  {@link POITextExtractor} for each embeded file.
	 */
	public static POITextExtractor[] getEmbededDocsTextExtractors(POIXMLTextExtractor ext) {
		throw new IllegalStateException("Not yet supported");
	}
}

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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import org.zkoss.poi.hpsf.DocumentSummaryInformation;
import org.zkoss.poi.hpsf.MutablePropertySet;
import org.zkoss.poi.hpsf.PropertySet;
import org.zkoss.poi.hpsf.PropertySetFactory;
import org.zkoss.poi.hpsf.SummaryInformation;
import org.zkoss.poi.poifs.filesystem.DirectoryEntry;
import org.zkoss.poi.poifs.filesystem.DirectoryNode;
import org.zkoss.poi.poifs.filesystem.DocumentInputStream;
import org.zkoss.poi.poifs.filesystem.Entry;
import org.zkoss.poi.poifs.filesystem.EntryUtils;
import org.zkoss.poi.poifs.filesystem.NPOIFSFileSystem;
import org.zkoss.poi.poifs.filesystem.POIFSFileSystem;
import org.zkoss.poi.util.Internal;
import org.zkoss.poi.util.POILogFactory;
import org.zkoss.poi.util.POILogger;

/**
 * This holds the common functionality for all POI
 *  Document classes.
 * Currently, this relates to Document Information Properties 
 * 
 * @author Nick Burch
 */
public abstract class POIDocument {
	/** Holds metadata on our document */
	private SummaryInformation sInf;
	/** Holds further metadata on our document */
	private DocumentSummaryInformation dsInf;
	/**	The directory that our document lives in */
	protected DirectoryNode directory;
	
	/** For our own logging use */
	private final static POILogger logger = POILogFactory.getLogger(POIDocument.class);

    /* Have the property streams been read yet? (Only done on-demand) */
    private boolean initialized = false;
    

    protected POIDocument(DirectoryNode dir) {
    	this.directory = dir;
    }
    /**
     * @deprecated use {@link POIDocument#POIDocument(DirectoryNode)} instead 
     */
    @Deprecated
    protected POIDocument(DirectoryNode dir, POIFSFileSystem fs) {
       this.directory = dir;
    }
    protected POIDocument(POIFSFileSystem fs) {
       this(fs.getRoot());
    }
    protected POIDocument(NPOIFSFileSystem fs) {
       this(fs.getRoot());
    }

	/**
	 * Fetch the Document Summary Information of the document
	 */
	public DocumentSummaryInformation getDocumentSummaryInformation() {
        if(!initialized) readProperties();
        return dsInf;
    }

	/** 
	 * Fetch the Summary Information of the document
	 */
	public SummaryInformation getSummaryInformation() {
        if(!initialized) readProperties();
        return sInf;
    }
	
	/**
	 * Will create whichever of SummaryInformation
	 *  and DocumentSummaryInformation (HPSF) properties
	 *  are not already part of your document.
	 * This is normally useful when creating a new
	 *  document from scratch.
	 * If the information properties are already there,
	 *  then nothing will happen.
	 */
	public void createInformationProperties() {
        if(!initialized) readProperties();
		if(sInf == null) {
			sInf = PropertySetFactory.newSummaryInformation();
		}
		if(dsInf == null) {
			dsInf = PropertySetFactory.newDocumentSummaryInformation();
		}
	}

	/**
	 * Find, and create objects for, the standard
	 *  Documment Information Properties (HPSF).
	 * If a given property set is missing or corrupt,
	 *  it will remain null;
	 */
	protected void readProperties() {
		PropertySet ps;
		
		// DocumentSummaryInformation
		ps = getPropertySet(DocumentSummaryInformation.DEFAULT_STREAM_NAME);
		if(ps != null && ps instanceof DocumentSummaryInformation) {
			dsInf = (DocumentSummaryInformation)ps;
		} else if(ps != null) {
			logger.log(POILogger.WARN, "DocumentSummaryInformation property set came back with wrong class - ", ps.getClass());
		}

		// SummaryInformation
		ps = getPropertySet(SummaryInformation.DEFAULT_STREAM_NAME);
		if(ps instanceof SummaryInformation) {
			sInf = (SummaryInformation)ps;
		} else if(ps != null) {
			logger.log(POILogger.WARN, "SummaryInformation property set came back with wrong class - ", ps.getClass());
		}

		// Mark the fact that we've now loaded up the properties
        initialized = true;
	}

	/** 
	 * For a given named property entry, either return it or null if
	 *  if it wasn't found
	 */
	protected PropertySet getPropertySet(String setName) {
	   //directory can be null when creating new documents
	   if(directory == null || !directory.hasEntry(setName)) return null;

	   DocumentInputStream dis;
	   try {
	      // Find the entry, and get an input stream for it
	      dis = directory.createDocumentInputStream( directory.getEntry(setName) );
	   } catch(IOException ie) {
	      // Oh well, doesn't exist
	      logger.log(POILogger.WARN, "Error getting property set with name " + setName + "\n" + ie);
	      return null;
	   }

	   try {
	      // Create the Property Set
	      PropertySet set = PropertySetFactory.create(dis);
	      return set;
	   } catch(IOException ie) {
	      // Must be corrupt or something like that
	      logger.log(POILogger.WARN, "Error creating property set with name " + setName + "\n" + ie);
	   } catch(org.zkoss.poi.hpsf.HPSFException he) {
	      // Oh well, doesn't exist
	      logger.log(POILogger.WARN, "Error creating property set with name " + setName + "\n" + he);
	   }
	   return null;
	}
	
	/**
	 * Writes out the standard Documment Information Properties (HPSF)
	 * @param outFS the POIFSFileSystem to write the properties into
	 */
	protected void writeProperties(POIFSFileSystem outFS) throws IOException {
		writeProperties(outFS, null);
	}
	/**
	 * Writes out the standard Documment Information Properties (HPSF)
	 * @param outFS the POIFSFileSystem to write the properties into
	 * @param writtenEntries a list of POIFS entries to add the property names too
	 */
	protected void writeProperties(POIFSFileSystem outFS, List<String> writtenEntries) throws IOException {
        SummaryInformation si = getSummaryInformation();
        if(si != null) {
			writePropertySet(SummaryInformation.DEFAULT_STREAM_NAME, si, outFS);
			if(writtenEntries != null) {
				writtenEntries.add(SummaryInformation.DEFAULT_STREAM_NAME);
			}
		}
        DocumentSummaryInformation dsi = getDocumentSummaryInformation();
        if(dsi != null) {
			writePropertySet(DocumentSummaryInformation.DEFAULT_STREAM_NAME, dsi, outFS);
			if(writtenEntries != null) {
				writtenEntries.add(DocumentSummaryInformation.DEFAULT_STREAM_NAME);
			}
		}
	}
	
	/**
	 * Writes out a given ProperySet
	 * @param name the (POIFS Level) name of the property to write
	 * @param set the PropertySet to write out 
	 * @param outFS the POIFSFileSystem to write the property into
	 */
	protected void writePropertySet(String name, PropertySet set, POIFSFileSystem outFS) throws IOException {
		try {
			MutablePropertySet mSet = new MutablePropertySet(set);
			ByteArrayOutputStream bOut = new ByteArrayOutputStream();

			mSet.write(bOut);
			byte[] data = bOut.toByteArray();
			ByteArrayInputStream bIn = new ByteArrayInputStream(data);
			outFS.createDocument(bIn,name);

			logger.log(POILogger.INFO, "Wrote property set " + name + " of size " + data.length);
		} catch(org.zkoss.poi.hpsf.WritingNotSupportedException wnse) {
			logger.log( POILogger.ERROR, "Couldn't write property set with name " + name + " as not supported by HPSF yet");
		}
	}
	
	/**
	 * Writes the document out to the specified output stream
	 */
	public abstract void write(OutputStream out) throws IOException;

	/**
	 * Copies nodes from one POIFS to the other minus the excepts
	 * @param source is the source POIFS to copy from
	 * @param target is the target POIFS to copy to
	 * @param excepts is a list of Strings specifying what nodes NOT to copy
	 */
	@Deprecated
    protected void copyNodes( POIFSFileSystem source, POIFSFileSystem target,
            List<String> excepts ) throws IOException
    {
        EntryUtils.copyNodes( source, target, excepts );
    }

   /**
    * Copies nodes from one POIFS to the other minus the excepts
    * @param sourceRoot is the source POIFS to copy from
    * @param targetRoot is the target POIFS to copy to
    * @param excepts is a list of Strings specifying what nodes NOT to copy
    */
    @Deprecated
    protected void copyNodes( DirectoryNode sourceRoot,
            DirectoryNode targetRoot, List<String> excepts ) throws IOException
    {
        EntryUtils.copyNodes( sourceRoot, targetRoot, excepts );
    }

	/**
	 * Copies an Entry into a target POIFS directory, recursively
	 */
    @Internal
    @Deprecated
    protected void copyNodeRecursively( Entry entry, DirectoryEntry target )
            throws IOException
    {
        EntryUtils.copyNodeRecursively( entry, target );
    }
}

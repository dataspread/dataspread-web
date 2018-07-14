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

package org.zkoss.poi.hslf.record;

import org.zkoss.poi.util.LittleEndian;
import org.zkoss.poi.util.POILogger;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Hashtable;

/**
 * General holder for PersistPtrFullBlock and PersistPtrIncrementalBlock
 *  records. We need to handle them specially, since we have to go around
 *  updating UserEditAtoms if they shuffle about on disk
 * These hold references to where slides "live". If the position of a slide
 *  moves, then we have update all of these. If we come up with a new version
 *  of a slide, then we have to add one of these to the end of the chain
 *  (via CurrentUserAtom and UserEditAtom) pointing to the new slide location
 *
 * @author Nick Burch
 */

public final class PersistPtrHolder extends PositionDependentRecordAtom
{
	private byte[] _header;
	private byte[] _ptrData; // Will need to update this once we allow updates to _slideLocations
	private long _type;

	/**
	 * Holds the lookup for slides to their position on disk.
	 * You always need to check the most recent PersistPtrHolder
	 *  that knows about a given slide to find the right location
	 */
	private Hashtable<Integer,Integer> _slideLocations;
	/**
	 * Holds the lookup from slide id to where their offset is
	 *  held inside _ptrData. Used when writing out, and updating
	 *  the positions of the slides
	 */
	private Hashtable<Integer,Integer> _slideOffsetDataLocation;

	/**
	 * Get the list of slides that this PersistPtrHolder knows about.
	 * (They will be the keys in the hashtable for looking up the positions
	 *  of these slides)
	 */
	public int[] getKnownSlideIDs() {
		int[] ids = new int[_slideLocations.size()];
		Enumeration<Integer> e = _slideLocations.keys();
		for(int i=0; i<ids.length; i++) {
			Integer id = e.nextElement();
			ids[i] = id.intValue();
		}
		return ids;
	}

	/**
	 * Get the lookup from slide numbers to byte offsets, for the slides
	 *  known about by this PersistPtrHolder.
	 */
	public Hashtable<Integer,Integer> getSlideLocationsLookup() {
		return _slideLocations;
	}
	/**
	 * Get the lookup from slide numbers to their offsets inside
	 *  _ptrData, used when adding or moving slides.
	 */
	public Hashtable<Integer,Integer> getSlideOffsetDataLocationsLookup() {
		return _slideOffsetDataLocation;
	}

	/**
	 * Adds a new slide, notes or similar, to be looked up by this.
	 * For now, won't look for the most optimal on disk representation.
	 */
	public void addSlideLookup(int slideID, int posOnDisk) {
		// PtrData grows by 8 bytes:
		//  4 bytes for the new info block
		//  4 bytes for the slide offset
		byte[] newPtrData = new byte[_ptrData.length + 8];
		System.arraycopy(_ptrData,0,newPtrData,0,_ptrData.length);

		// Add to the slide location lookup hash
		_slideLocations.put(Integer.valueOf(slideID), Integer.valueOf(posOnDisk));
		// Add to the ptrData offset lookup hash
		_slideOffsetDataLocation.put(Integer.valueOf(slideID),
				Integer.valueOf(_ptrData.length + 4));

		// Build the info block
		// First 20 bits = offset number = slide ID
		// Remaining 12 bits = offset count = 1
		int infoBlock = slideID;
		infoBlock += (1 << 20);

		// Write out the data for this
		LittleEndian.putInt(newPtrData,newPtrData.length-8,infoBlock);
		LittleEndian.putInt(newPtrData,newPtrData.length-4,posOnDisk);

		// Save the new ptr data
		_ptrData = newPtrData;

		// Update the atom header
		LittleEndian.putInt(_header,4,newPtrData.length);
	}

	/**
	 * Create a new holder for a PersistPtr record
	 */
	protected PersistPtrHolder(byte[] source, int start, int len) {
		// Sanity Checking - including whole header, so treat
		//  length as based of 0, not 8 (including header size based)
		if(len < 8) { len = 8; }

		// Treat as an atom, grab and hold everything
		_header = new byte[8];
		System.arraycopy(source,start,_header,0,8);
		_type = LittleEndian.getUShort(_header,2);

		// Try to make sense of the data part:
		// Data part is made up of a number of these sets:
		//   32 bit info value
		//		12 bits count of # of entries
		//      base number for these entries
		//   count * 32 bit offsets
		// Repeat as many times as you have data
		_slideLocations = new Hashtable<Integer,Integer>();
		_slideOffsetDataLocation = new Hashtable<Integer,Integer>();
		_ptrData = new byte[len-8];
		System.arraycopy(source,start+8,_ptrData,0,_ptrData.length);

		int pos = 0;
		while(pos < _ptrData.length) {
			// Grab the info field
			long info = LittleEndian.getUInt(_ptrData,pos);

			// First 20 bits = offset number
			// Remaining 12 bits = offset count
			int offset_count = (int)(info >> 20);
			int offset_no = (int)(info - (offset_count << 20));
//System.out.println("Info is " + info + ", count is " + offset_count + ", number is " + offset_no);

			// Wind on by the 4 byte info header
			pos += 4;

			// Grab the offsets for each of the sheets
			for(int i=0; i<offset_count; i++) {
				int sheet_no = offset_no + i;
				long sheet_offset = LittleEndian.getUInt(_ptrData,pos);
				_slideLocations.put(Integer.valueOf(sheet_no), Integer.valueOf((int)sheet_offset));
				_slideOffsetDataLocation.put(Integer.valueOf(sheet_no), Integer.valueOf(pos));

				// Wind on by 4 bytes per sheet found
				pos += 4;
			}
		}
	}

	/**
	 * Return the value we were given at creation, be it 6001 or 6002
	 */
	public long getRecordType() { return _type; }

	/**
	 * At write-out time, update the references to the sheets to their
	 *  new positions
	 */
	public void updateOtherRecordReferences(Hashtable<Integer,Integer> oldToNewReferencesLookup) {
		int[] slideIDs = getKnownSlideIDs();

		// Loop over all the slides we know about
		// Find where they used to live, and where they now live
		// Then, update the right bit of _ptrData with their new location
		for(int i=0; i<slideIDs.length; i++) {
			Integer id = Integer.valueOf(slideIDs[i]);
			Integer oldPos = (Integer)_slideLocations.get(id);
			Integer newPos = (Integer)oldToNewReferencesLookup.get(oldPos);

			if(newPos == null) {
				logger.log(POILogger.WARN, "Couldn't find the new location of the \"slide\" with id " + id + " that used to be at " + oldPos);
				logger.log(POILogger.WARN, "Not updating the position of it, you probably won't be able to find it any more (if you ever could!)");
				newPos = oldPos;
			}

			// Write out the new location
			Integer dataOffset = (Integer)_slideOffsetDataLocation.get(id);
			LittleEndian.putInt(_ptrData,dataOffset.intValue(),newPos.intValue());

			// Update our hashtable
			_slideLocations.remove(id);
			_slideLocations.put(id,newPos);
		}
	}

	/**
	 * Write the contents of the record back, so it can be written
	 *  to disk
	 */
	public void writeOut(OutputStream out) throws IOException {
		out.write(_header);
		out.write(_ptrData);
	}
}

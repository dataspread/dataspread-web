/* HeaderPositionHelper.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Jan 9, 2008 12:35:40 PM     2008, Created by Dennis.Chen
}}IS_NOTE

Copyright (C) 2007 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
	This program is distributed under GPL Version 2.0 in the hope that
	it will be useful, but WITHOUT ANY WARRANTY.
}}IS_RIGHT
*/
package org.zkoss.zss.ui.impl;

import org.model.AutoRollbackConnection;
import org.model.BlockStore;
import org.model.DBContext;
import org.model.DBHandler;

import java.io.Serializable;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * 
 * A utility class for calculating position of header.
 * Each non-default size of a row or column header is stored as a {@link HeaderPositionInfo} in this helper.  
 * @author Dennis.Chen
 * 
 */
public class HeaderPositionHelper {

	int _defaultSize;
	private List<HeaderPositionInfo> _infos;
	//int[][] _customizedSize; //[0]: column/row index, [1]: width/height, [2]: column/row id

	private BlockStore blockStore;

	public HeaderPositionHelper(int defaultSize, List<HeaderPositionInfo> infos, String sheetName) {
		this._defaultSize = defaultSize;
		this._infos = infos;
		sheetName = sheetName + "_position_helper";
		try (AutoRollbackConnection connection = DBHandler.instance.getConnection()){
			DBContext dbContext = new DBContext(connection);
			blockStore = new BlockStore(dbContext, sheetName);

			for (Integer i:blockStore.keySet(dbContext)){
				HeaderPositionInfo info = blockStore.getObject(dbContext,i, HeaderPositionInfo.class);
				if (info == null){
					System.out.println("Error:HeaderPositionHelper");
				}
				setInfoValues(info.getIndex(), info.getSize(), info.getId(), info.getHidden(), info.isCustom());
			}

			for (HeaderPositionInfo info:_infos){
				blockStore.putObject(info.getIndex(), info);
			}

			blockStore.flushDirtyBlocks(dbContext);
			connection.commit();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

	}
	
	public int getDefaultSize() {
		return _defaultSize;
	}

	public List<HeaderPositionInfo> getInfos() {
		return new ArrayList<HeaderPositionInfo>(_infos);
	}

	public boolean isHidden(int cellIndex) {
		final int j = Collections.binarySearch(_infos, Integer.valueOf(cellIndex), new HeaderPositionInfoComparator());
		return j < 0 ? false : _infos.get(j).getHidden();
	}
	
	//ZSS-1000
	//@since 3.8.1
	public int getPrevNonHidden(int cellIndex) {
		cellIndex = cellIndex - 1;
		if (cellIndex < 0) { //out of bound
			return -1;
		}
		int j = Collections.binarySearch(_infos, Integer.valueOf(cellIndex), new HeaderPositionInfoComparator());
		if (j < 0 || !_infos.get(j).getHidden()) {
			return cellIndex;
		}
		cellIndex = cellIndex - 1;
		for (;--j >= 0;) {
			HeaderPositionInfo info = _infos.get(j);
			if (info.getIndex() < cellIndex) {
				return cellIndex;
			} else if (info.getIndex() == cellIndex) {
				if (!info.getHidden()) {
					return cellIndex;
				} else {
					cellIndex = cellIndex - 1;
				}
			}
		}
		return cellIndex;
	}
	
	//ZSS-1000
	//@since 3.8.1
	public int getNextNonHidden(int cellIndex) {
		cellIndex = cellIndex + 1;
		
		int j = Collections.binarySearch(_infos, Integer.valueOf(cellIndex), new HeaderPositionInfoComparator());
		if (j < 0 || !_infos.get(j).getHidden()) {
			return cellIndex;
		}
		cellIndex = cellIndex + 1;
		for (int len = _infos.size(); ++j < len;) {
			HeaderPositionInfo info = _infos.get(j);
			if (info.getIndex() > cellIndex) {
				return cellIndex;
			} else if (info.getIndex() == cellIndex) {
				if (!info.getHidden()) {
					return cellIndex;
				} else {
					cellIndex = cellIndex + 1;
				}
			}
		}
		return cellIndex;
	}
	
	public int getSize(int cellIndex) {
		final int j = Collections.binarySearch(_infos, Integer.valueOf(cellIndex), new HeaderPositionInfoComparator());
		return j < 0 ? _defaultSize : _infos.get(j).getSize();
	}

	//given target cell index, return list index. 
	private int getListIndex(int cellIndex) {
		final int j = Collections.binarySearch(_infos, Integer.valueOf(cellIndex), new HeaderPositionInfoComparator());
		return j < 0 ? -j - 1 : j;
	}
	
	public void shiftMeta(int cellIndex, int offset) {

		;

		try (AutoRollbackConnection connection = DBHandler.instance.getConnection()){
			DBContext dbContext = new DBContext(connection);
			String createTable = "CREATE TABLE  IF NOT  EXISTS  db_events (" +
					"action INTEGER NOT NULL," +
					"data TEXT);";
			final int index = getListIndex(cellIndex);
			for (int j = _infos.size() - 1; j >= index; --j) {
				final HeaderPositionInfo info = _infos.get(j);
				info.setIndex(info.getIndex() + offset);
				blockStore.putObject(info.getIndex(), info);
			}

			blockStore.flushDirtyBlocks(dbContext);
			connection.commit();
		}


	}

	public void unshiftMeta(int cellIndex, int offset) {

		try (AutoRollbackConnection connection = DBHandler.instance.getConnection()) {
            DBContext dbContext = new DBContext(connection);

            final int bindex = getListIndex(cellIndex);
            final int eindex = getListIndex(cellIndex + offset);
            for (int j = eindex - 1; j >= bindex; --j) {
                blockStore.freeBlock(_infos.get(j).getIndex());
                _infos.remove(j);
            }

            for (int j = _infos.size() - 1; j >= bindex; --j) {
                final HeaderPositionInfo info = _infos.get(j);
                info.setIndex(info.getIndex() - offset);
                blockStore.putObject(info.getIndex(), info);
            }

            blockStore.flushDirtyBlocks(dbContext);
            connection.commit();
        }
	}

	public HeaderPositionInfo getInfo(int cellIndex) {
		final int j = Collections.binarySearch(_infos, Integer.valueOf(cellIndex), new HeaderPositionInfoComparator());
		return j < 0 ? null : _infos.get(j);
	}

	//set new info values at the specified cellIndex; if not exist, create a new one and add into this Helper.
	public void setInfoValues(int cellIndex, int size, int id, boolean hidden, boolean isCustom) {
		final int j = Collections.binarySearch(_infos, Integer.valueOf(cellIndex), new HeaderPositionInfoComparator());
		final int index = j < 0 ? (-j - 1) : j;
		if (j < 0) {
			_infos.add(index, new HeaderPositionInfo(cellIndex, size, id, hidden, isCustom));
		} else {
			final HeaderPositionInfo info = _infos.get(index);
			info.setSize(size);
			info.setId(id);
			info.setHidden(hidden);
		}

		try (AutoRollbackConnection connection = DBHandler.instance.getConnection()) {
            DBContext dbContext = new DBContext(connection);

            blockStore.putObject(Integer.valueOf(cellIndex), _infos.get(index));

            blockStore.flushDirtyBlocks(dbContext);
            connection.commit();
        }

	}

	public void removeInfo(int cellIndex) {
		try (AutoRollbackConnection connection = DBHandler.instance.getConnection()) {
            DBContext dbContext = new DBContext(connection);

            final int j = Collections.binarySearch(_infos, Integer.valueOf(cellIndex), new HeaderPositionInfoComparator());
            final int index = j < 0 ? (-j - 1) : j;
            if (j >= 0) {
                blockStore.freeBlock(cellIndex);
                _infos.remove(index);
            }
            blockStore.flushDirtyBlocks(dbContext);
            connection.commit();
        }
	}
	
	//given size in pixels, return the related cellIndex
	public int getCellIndex(int px) {
		if (px < 0) {
			return 0;
		}
		int begPx = 0;
		int begIndex = 0;
		for(HeaderPositionInfo info : _infos) {
			final int cellIndex = info.getIndex();
			final int endPx = begPx + (cellIndex - begIndex) * _defaultSize; //big jump 
			if (endPx > px) { //jump beyond the target pixel
				final int step = px - begPx;
				return begIndex + (step / _defaultSize);
			}
			//still behind, forward custom size of this info
			begPx = endPx + (info.getHidden() ? 0 : info.getSize());
			if (begPx > px) { //exactly locate at this info
				return info.getIndex();
			}
			begIndex = info.getIndex() + 1; //step over to new begin index
		}
		
		//never reach px
		final int step = px - begPx;
		return begIndex + (step / _defaultSize);
	}

	//given cellIndex, return the associated start pixel
	public int getStartPixel(int cellIndex) {
		if (cellIndex < 0) {
			return 0;
		}
		int px = 0;
		int begIndex = 0;
		for(HeaderPositionInfo info : _infos) {
			final int infoIndex = info.getIndex();
			if (cellIndex > infoIndex) { //not reach the target
				px += (infoIndex - begIndex) * _defaultSize;
				px += info.getHidden() ? 0 : info.getSize();
				begIndex = infoIndex + 1; //next begin index
			} else if (cellIndex == infoIndex) { //extactly locate at the info
				px += (infoIndex - begIndex) * _defaultSize;
				return px;
			} else { //cellIndex < infoIndex; jump over the target
				break;
			}
		}
		
		//jumper over the target or never reach the target index
		px += (cellIndex - begIndex) * _defaultSize;
		return px;
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("[");
		for (HeaderPositionInfo info : _infos) {
			sb.append("[");
			sb.append(info.getIndex()).append(", ");
			sb.append(info.getSize()).append(", ");
			sb.append(info.getId()).append(", ");
			sb.append(info.getHidden()).append(", ");
			sb.append("],");

		}
		sb.append("]");
		return sb.toString();
	}

/*	static public void testSetCustomizedSize() {
		int[][] customeizedSize = new int[0][];
		int defaultSize = 40;
		HeaderPositionHelper helper = new HeaderPositionHelper(defaultSize,
				customeizedSize);
		int id = 0;
		helper.setCustomizedSize(3, 30, id++);
		helper.setCustomizedSize(9, 20, id++);
		helper.setCustomizedSize(10, 60, id++);
		helper.setCustomizedSize(5, 60, id++);
		helper.setCustomizedSize(3, 60, id++);
		helper.setCustomizedSize(9, 30, id++);
		helper.setCustomizedSize(12, 60, id++);
		helper.setCustomizedSize(1, 60, id++);
		helper.setCustomizedSize(1, 20, id++);
		helper.setCustomizedSize(12, 90, id++);

		helper.setCustomizedSize(3, 40, id++);
		helper.setCustomizedSize(9, 40, id++);
		helper.setCustomizedSize(12, 40, id++);
		helper.setCustomizedSize(10, 40, id++);
		helper.setCustomizedSize(5, 40, id++);
		helper.setCustomizedSize(1, 40, id++);

		helper.setCustomizedSize(3, 30, id++);
		helper.setCustomizedSize(9, 20, id++);
		helper.setCustomizedSize(10, 60, id++);
		helper.setCustomizedSize(5, 60, id++);

		System.out.println("===>");

	}

	static public void testinsert() {
		int[][] customeizedSize = new int[0][];
		int defaultSize = 40;
		int id = 0;
		HeaderPositionHelper helper = new HeaderPositionHelper(defaultSize,
				customeizedSize);
		helper.insert(0, 5, 205, id++);
		helper.insert(0, 4, 204, id++);
		helper.insert(1, 6, 206, id++);
		helper.insert(2, 7, 207, id++);
		helper.insert(3, 8, 208, id++);
	}

	static public void testremove() {
		int[][] customeizedSize = new int[][] { { 4, 30 }, { 9, 20 },
				{ 10, 60 }, { 13, 3 }, { 14, 5 }, { 18, 8 }, { 23, 8 },
				{ 25, 8 } };
		int defaultSize = 40;
		HeaderPositionHelper helper = new HeaderPositionHelper(defaultSize,
				customeizedSize);
		helper.remove(customeizedSize.length);
		helper.remove(0);
		helper.remove(1);
		helper.remove(3);
		helper.remove(4);
		System.out.println("===>");
	}

	static public void testGetCellIndex() {
		int[][] customeizedSize = new int[][] { { 3, 30 }, { 9, 20 },
				{ 10, 60 } };
		int defaultSize = 40;
		HeaderPositionHelper helper = new HeaderPositionHelper(defaultSize,
				customeizedSize);
		System.out.println(">>>>" + helper.getCellIndex(39));// 0
		System.out.println(">>>>" + helper.getCellIndex(40));// 1
		System.out.println(">>>>" + helper.getCellIndex(79));// 1
		System.out.println(">>>>" + helper.getCellIndex(80));// 2
		System.out.println(">>>>" + helper.getCellIndex(119));// 2
		System.out.println(">>>>" + helper.getCellIndex(120));// 3
		System.out.println(">>>>" + helper.getCellIndex(159));// 3
		System.out.println(">>>>" + helper.getCellIndex(160));// 4
		System.out.println(">>>>" + helper.getCellIndex(235));// 6
		System.out.println(">>>>" + helper.getCellIndex(369));// 9
		System.out.println(">>>>" + helper.getCellIndex(370));// 10
		System.out.println(">>>>" + helper.getCellIndex(429));// 10
		System.out.println(">>>>" + helper.getCellIndex(430));// 11
		System.out.println(">>>>" + helper.getCellIndex(480));// 12
	}
*/
	
	
	private static class HeaderPositionInfoComparator implements Comparator, Serializable {
		@Override
		public int compare(Object o1, Object o2) {
			final int i1 = o1 instanceof HeaderPositionInfo ? ((HeaderPositionInfo)o1).getIndex() : ((Integer)o1).intValue();
			final int i2 = o2 instanceof HeaderPositionInfo ? ((HeaderPositionInfo)o2).getIndex() : ((Integer)o2).intValue();
			return i1 - i2;
		} 
	}
}


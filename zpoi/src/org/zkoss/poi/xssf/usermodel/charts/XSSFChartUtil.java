/*
 *  ====================================================================
 *    Licensed to the Apache Software Foundation (ASF) under one or more
 *    contributor license agreements.  See the NOTICE file distributed with
 *    this work for additional information regarding copyright ownership.
 *    The ASF licenses this file to You under the Apache License, Version 2.0
 *    (the "License"); you may not use this file except in compliance with
 *    the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 * ====================================================================
 */

package org.zkoss.poi.xssf.usermodel.charts;

import org.zkoss.poi.ss.usermodel.charts.ChartDirection;
import org.zkoss.poi.ss.usermodel.charts.ChartDataSource;
import org.zkoss.poi.ss.usermodel.charts.ChartGrouping;
import org.zkoss.poi.ss.usermodel.charts.ChartTextSource;
import org.zkoss.poi.ss.usermodel.charts.DataSources.ArrayDataSource;
import org.openxmlformats.schemas.drawingml.x2006.chart.*;

/**
 * Package private class with utility methods.
 *
 * @author Roman Kashitsyn
 */
class XSSFChartUtil {

    private XSSFChartUtil() {}

    /**
     * Builds CTAxDataSource object content from POI ChartDataSource.
     * @param ctAxDataSource OOXML data source to build
     * @param dataSource POI data source to use
     */
    public static void buildAxDataSource(CTAxDataSource ctAxDataSource, ChartDataSource<?> dataSource) {
        if (dataSource.isNumeric()) {
            if (dataSource.isReference()) {
                buildNumRef(ctAxDataSource.addNewNumRef(), dataSource);
            } else {
                buildNumLit(ctAxDataSource.addNewNumLit(), dataSource);
            }
        } else {
            if (dataSource.isReference()) {
                buildStrRef(ctAxDataSource.addNewStrRef(), dataSource);
            } else {
                buildStrLit(ctAxDataSource.addNewStrLit(), dataSource);
            }
        }
    }

    public static ChartDataSource<? extends Number> buildDefaultNumDataSource(ChartDataSource<? extends Number> dataSource) {
    	if (dataSource.isNumeric()) {
    		return dataSource;
    	} else {
    		int len = dataSource.getPointCount();
    		Integer[] ax = new Integer[len];
    		for (int j = 0; j < len; ++j) {
    			ax[j] = new Integer(j+1);
    		}
    		return new ArrayDataSource<Integer>(ax);
    	}
    }
    
    /**
     * Builds CTNumDataSource object content from POI ChartDataSource
     * @param ctNumDataSource OOXML data source to build
     * @param dataSource POI data source to use
     */
    public static void buildNumDataSource(CTNumDataSource ctNumDataSource,
                                          ChartDataSource<? extends Number> dataSource) {
        if (dataSource.isReference()) {
            buildNumRef(ctNumDataSource.addNewNumRef(), dataSource);
        } else {
            buildNumLit(ctNumDataSource.addNewNumLit(), dataSource);
        }
    }

    private static void buildNumRef(CTNumRef ctNumRef, ChartDataSource<?> dataSource) {
        ctNumRef.setF(dataSource.getFormulaString());
        CTNumData cache = ctNumRef.addNewNumCache();
        fillNumCache(cache, dataSource);
    }

    private static void buildNumLit(CTNumData ctNumData, ChartDataSource<?> dataSource) {
        fillNumCache(ctNumData, dataSource);
    }

    private static void buildStrRef(CTStrRef ctStrRef, ChartDataSource<?> dataSource) {
        ctStrRef.setF(dataSource.getFormulaString());
        CTStrData cache = ctStrRef.addNewStrCache();
        fillStringCache(cache, dataSource);
    }

    private static void buildStrLit(CTStrData ctStrData, ChartDataSource<?> dataSource) {
        fillStringCache(ctStrData, dataSource);
    }

    private static void fillStringCache(CTStrData cache, ChartDataSource<?> dataSource) {
        int numOfPoints = dataSource.getPointCount();
        cache.addNewPtCount().setVal(numOfPoints);
        for (int i = 0; i < numOfPoints; ++i) {
            Object value = dataSource.getPointAt(i);
            if (value != null) {
                CTStrVal ctStrVal = cache.addNewPt();
                ctStrVal.setIdx(i);
                ctStrVal.setV(value.toString());
            }
        }
    }

    private static void fillNumCache(CTNumData cache, ChartDataSource<?> dataSource) {
        int numOfPoints = dataSource.getPointCount();
        cache.addNewPtCount().setVal(numOfPoints);
        for (int i = 0; i < numOfPoints; ++i) {
            Number value = (Number) dataSource.getPointAt(i);
            if (value != null) {
                CTNumVal ctNumVal = cache.addNewPt();
                ctNumVal.setIdx(i);
                ctNumVal.setV(value.toString());
            }
        }
    }
    
    
    //20111006, henrichen@zkoss.org: handle serie title text
    /**
     * Builds CTSerTx object content from POI ChartDataSource.
     * @param ctSerTx OOXML serie text source to build
     * @param textSource POI text source to use
     */
    public static void buildSerTx(CTSerTx ctSerTx, ChartTextSource textSource) {
        if (textSource.isReference()) {
        	buildStrRef(ctSerTx.addNewStrRef(), textSource);
        } else {
            ctSerTx.setV(textSource.getTextString());
        }
    }
    //20111006, henrichen@zkoss.org: handle serie title text
    private static void buildStrRef(CTStrRef ctStrRef, ChartTextSource textSource) {
        ctStrRef.setF(textSource.getFormulaString());
        CTStrData cache = ctStrRef.addNewStrCache();
        fillStringCache(cache, textSource);
    }
    //20111006, henrichen@zkoss.org: handle serie title text
    private static void fillStringCache(CTStrData cache, ChartTextSource textSource) {
        cache.addNewPtCount().setVal(1);
        CTStrVal ctStrVal = cache.addNewPt();
        ctStrVal.setIdx(0);
        ctStrVal.setV(textSource.getTextString());
    }
    
    //20111013, henrichen@zkoss.org: handle bar chart grouping
	/*packdage*/ static STBarGrouping.Enum fromChartGroupingForBar(ChartGrouping grouping) {
		if(grouping==null){
			return null;
		}
		switch (grouping) {
			case STANDARD: return STBarGrouping.STANDARD;
			case STACKED: return STBarGrouping.STACKED;
			case CLUSTERED:
				return STBarGrouping.CLUSTERED;
			case PERCENT_STACKED:
				return STBarGrouping.PERCENT_STACKED;
			default:
				throw new IllegalArgumentException();
		}
	}

    //20111013, henrichen@zkoss.org: handle bar chart grouping
	/*packdage*/ static ChartGrouping toChartGroupingForBar(CTBarGrouping ctBarGrouping) {
		if(ctBarGrouping==null){
			return null;
		}
		switch (ctBarGrouping.getVal().intValue()) {
			case STBarGrouping.INT_STANDARD: return ChartGrouping.STANDARD;
			case STBarGrouping.INT_STACKED: return ChartGrouping.STACKED;
			case STBarGrouping.INT_PERCENT_STACKED: return ChartGrouping.PERCENT_STACKED;
			case STBarGrouping.INT_CLUSTERED: return ChartGrouping.CLUSTERED;
			default:
				throw new IllegalArgumentException();
		}
	}

    //20111013, henrichen@zkoss.org: handle chart grouping
	/*packdage*/ static STGrouping.Enum fromChartGrouping(ChartGrouping grouping) {
		if(grouping==null){
			return null;
		}
		switch (grouping) {
			case STANDARD: return STGrouping.STANDARD;
			case STACKED: return STGrouping.STACKED;
			case PERCENT_STACKED: return STGrouping.PERCENT_STACKED;
			default:
				throw new IllegalArgumentException();
		}
	}

    //20111013, henrichen@zkoss.org: handle chart grouping
	/*packdage*/ static ChartGrouping toChartGrouping(CTGrouping ctGrouping) {
		if(ctGrouping==null){
			return null;
		}
		switch (ctGrouping.getVal().intValue()) {
			case STGrouping.INT_PERCENT_STACKED: return ChartGrouping.PERCENT_STACKED; //ZSS-828
			case STGrouping.INT_STANDARD: return ChartGrouping.STANDARD;
			case STGrouping.INT_STACKED: return ChartGrouping.STACKED;
			default:
				throw new IllegalArgumentException();
		}
	}

    //20111013, henrichen@zkoss.org: handle bar chart grouping
	/*packdage*/ static STBarDir.Enum fromBarDirection(ChartDirection dir) {
		if(dir==null){
			return null;
		}
		switch (dir) {
			case HORIZONTAL: return STBarDir.BAR;
			case VERTICAL: return STBarDir.COL;
			default:
				throw new IllegalArgumentException();
		}
	}

    //20111013, henrichen@zkoss.org: handle bar chart grouping
	/*packdage*/ static ChartDirection toBarDirection(CTBarDir barDir) {
		if(barDir==null){
			return null;
		}
		switch (barDir.getVal().intValue()) {
			case STBarDir.INT_BAR: return ChartDirection.HORIZONTAL;
			case STBarDir.INT_COL: return ChartDirection.VERTICAL;
			default:
				throw new IllegalArgumentException();
		}
	}
}

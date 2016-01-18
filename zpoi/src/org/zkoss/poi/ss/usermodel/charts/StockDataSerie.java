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

package org.zkoss.poi.ss.usermodel.charts;

import org.zkoss.poi.util.Beta;

/**
 * Represents a xy data serie.
 *
 * @author henrichen@zkoss.org
 */
@Beta
public interface StockDataSerie {
    /**
     * @return data source used for stock labels
     */
    ChartDataSource<?> getCategories();

    /**
     * @return data source used for volume of the stock
     */
    ChartDataSource<? extends Number> getVolumes();
    /**
     * @return data source used for open price of the stock
     */
    ChartDataSource<? extends Number> getOpens();

    /**
     * @return data source used for highest price of the stock
     */
    ChartDataSource<? extends Number> getHighs();

    /**
     * @return data source used for lowest price of the stock
     */
    ChartDataSource<? extends Number> getLows();
    
    /**
     * @return data source used for highest price of the stock
     */
    ChartDataSource<? extends Number> getCloses();
}

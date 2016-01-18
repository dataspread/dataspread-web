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

/**
 * @author henrichen@zkoss.org
 *
 */
public abstract class AbstractXYDataSerie implements XYDataSerie {
    protected int id;
    protected int order;
    protected ChartTextSource title;
    protected ChartDataSource<? extends Number> xs;
    protected ChartDataSource<? extends Number> ys;

    protected AbstractXYDataSerie(int id, int order,
		ChartTextSource title,
        ChartDataSource<? extends Number> xs,
        ChartDataSource<? extends Number> ys) {
        super();
        this.id = id;
        this.order = order;
        this.title = title;
        this.xs = xs;
        this.ys = ys;
    }

    /**
     * Returns data source used for serie title.
     * @return data source used for serie title.
     */
    @Override
	public ChartTextSource getTitle() {
		return title;
	}
	
    /**
     * Returns data source used for category values.
     * @return data source used for category values
     */
    @Override
    public ChartDataSource<? extends Number> getXs() {
        return xs;
    }

    /**
     * Returns data source used for values.
     * @return data source used for values
     */
    @Override
    public ChartDataSource<? extends Number> getYs() {
        return ys;
    }
}

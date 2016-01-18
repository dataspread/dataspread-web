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

package org.zkoss.poi.xssf.usermodel;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTArea3DChart;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTAreaChart;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTBar3DChart;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTBarChart;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTBoolean;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTBubbleChart;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTCatAx;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTChart;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTChartSpace;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTDoughnutChart;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTLine3DChart;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTLineChart;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTOfPieChart;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTOverlap;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTPageMargins;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTPie3DChart;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTPieChart;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTPlotArea;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTPrintSettings;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTRadarChart;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTScatterChart;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTStockChart;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTSurface3DChart;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTSurfaceChart;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTTitle;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTValAx;
import org.openxmlformats.schemas.drawingml.x2006.chart.ChartSpaceDocument;
import org.openxmlformats.schemas.drawingml.x2006.chart.STBarDir;
import org.openxmlformats.schemas.drawingml.x2006.chart.STBarGrouping;
import org.openxmlformats.schemas.officeDocument.x2006.relationships.STRelationshipId;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.zkoss.poi.POIXMLDocumentPart;
import org.zkoss.poi.openxml4j.opc.PackagePart;
import org.zkoss.poi.openxml4j.opc.PackageRelationship;
import org.zkoss.poi.openxml4j.opc.internal.MemoryPackagePart;
import org.zkoss.poi.ss.usermodel.Chart;
import org.zkoss.poi.ss.usermodel.ClientAnchor;
import org.zkoss.poi.ss.usermodel.charts.AxisPosition;
import org.zkoss.poi.ss.usermodel.charts.CategoryDataSerie;
import org.zkoss.poi.ss.usermodel.charts.ChartAxis;
import org.zkoss.poi.ss.usermodel.charts.ChartAxisFactory;
import org.zkoss.poi.ss.usermodel.charts.ChartData;
import org.zkoss.poi.ss.usermodel.charts.ChartGrouping;
import org.zkoss.poi.ss.usermodel.charts.ChartTextSource;
import org.zkoss.poi.ss.usermodel.charts.ChartType;
import org.zkoss.poi.util.Internal;
import org.zkoss.poi.xssf.usermodel.charts.XSSFBar3DChartData;
import org.zkoss.poi.xssf.usermodel.charts.XSSFBarChartData;
import org.zkoss.poi.xssf.usermodel.charts.XSSFCategoryAxis;
import org.zkoss.poi.xssf.usermodel.charts.XSSFChartAxis;
import org.zkoss.poi.xssf.usermodel.charts.XSSFChartDataFactory;
import org.zkoss.poi.xssf.usermodel.charts.XSSFChartLegend;
import org.zkoss.poi.xssf.usermodel.charts.XSSFColumn3DChartData;
import org.zkoss.poi.xssf.usermodel.charts.XSSFColumnChartData;
import org.zkoss.poi.xssf.usermodel.charts.XSSFDoughnutChartData;
import org.zkoss.poi.xssf.usermodel.charts.XSSFLine3DChartData;
import org.zkoss.poi.xssf.usermodel.charts.XSSFLineChartData;
import org.zkoss.poi.xssf.usermodel.charts.XSSFManualLayout;
import org.zkoss.poi.xssf.usermodel.charts.XSSFPie3DChartData;
import org.zkoss.poi.xssf.usermodel.charts.XSSFPieChartData;
import org.zkoss.poi.xssf.usermodel.charts.XSSFValueAxis;
import org.zkoss.poi.xssf.usermodel.charts.XSSFView3D;

/**
 * Represents a SpreadsheetML Chart
 * @author Nick Burch
 * @author Roman Kashitsyn
 */
public final class XSSFChart extends POIXMLDocumentPart implements Chart, ChartAxisFactory {

	/**
	 * Parent graphic frame.
	 */
	private XSSFGraphicFrame frame;

	/**
	 * Root element of the SpreadsheetML Chart part
	 */
	private CTChartSpace chartSpace;
	/**
	 * The Chart within that
	 */
	private CTChart chart;

	List<XSSFChartAxis> axis;

	/**
	 * Create a new SpreadsheetML chart
	 */
	protected XSSFChart() {
		super();
		axis = new ArrayList<XSSFChartAxis>();
		createChart();
	}

	/**
	 * Construct a SpreadsheetML chart from a package part.
	 *
	 * @param part the package part holding the chart data,
	 * the content type must be <code>application/vnd.openxmlformats-officedocument.drawingml.chart+xml</code>
	 * @param rel  the package relationship holding this chart,
	 * the relationship type must be http://schemas.openxmlformats.org/officeDocument/2006/relationships/chart
	 */
	protected XSSFChart(PackagePart part, PackageRelationship rel) throws IOException, XmlException {
		super(part, rel);

		chartSpace = ChartSpaceDocument.Factory.parse(part.getInputStream()).getChartSpace(); 
		chart = chartSpace.getChart();
		axis = new ArrayList<XSSFChartAxis>();
	}

	/**
	 * Construct a new CTChartSpace bean.
	 * By default, it's just an empty placeholder for chart objects.
	 *
	 * @return a new CTChartSpace bean
	 */
	private void createChart() {
		chartSpace = CTChartSpace.Factory.newInstance();
		chart = chartSpace.addNewChart();
		CTPlotArea plotArea = chart.addNewPlotArea();

		plotArea.addNewLayout();
		chart.addNewPlotVisOnly().setVal(true);

		CTPrintSettings printSettings = chartSpace.addNewPrintSettings();
		printSettings.addNewHeaderFooter();

		CTPageMargins pageMargins = printSettings.addNewPageMargins();
		pageMargins.setB(0.75);
		pageMargins.setL(0.70);
		pageMargins.setR(0.70);
		pageMargins.setT(0.75);
		pageMargins.setHeader(0.30);
		pageMargins.setFooter(0.30);
		printSettings.addNewPageSetup();
	}

	/**
	 * Return the underlying CTChartSpace bean, the root element of the SpreadsheetML Chart part.
	 *
	 * @return the underlying CTChartSpace bean
	 */
	@Internal
	public CTChartSpace getCTChartSpace(){
		return chartSpace;
	}

	/**
	 * Return the underlying CTChart bean, within the Chart Space
	 *
	 * @return the underlying CTChart bean
	 */
	@Internal
	public CTChart getCTChart(){
		return chart;
	}

	@Override
	protected void commit() throws IOException {
		XmlOptions xmlOptions = new XmlOptions(DEFAULT_XML_OPTIONS);

		/*
		   Saved chart space must have the following namespaces set:
		   <c:chartSpace
		      xmlns:c="http://schemas.openxmlformats.org/drawingml/2006/chart"
		      xmlns:a="http://schemas.openxmlformats.org/drawingml/2006/main"
		      xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships">
		 */
		xmlOptions.setSaveSyntheticDocumentElement(new QName(CTChartSpace.type.getName().getNamespaceURI(), "chartSpace", "c"));
		Map<String, String> map = new HashMap<String, String>();
		map.put(XSSFDrawing.NAMESPACE_A, "a");
		map.put(XSSFDrawing.NAMESPACE_C, "c");
		map.put(STRelationshipId.type.getName().getNamespaceURI(), "r");
		xmlOptions.setSaveSuggestedPrefixes(map);

		PackagePart part = getPackagePart();
		clearMemoryPackagePart(part); // 20130809, paowang@potix.com: (ZSS-358) clear package part before saving
		OutputStream out = part.getOutputStream();
		chartSpace.save(out, xmlOptions);
		out.close();
	}

	/**
	 * Returns the parent graphic frame.
	 * @return the graphic frame this chart belongs to
	 */
	public XSSFGraphicFrame getGraphicFrame() {
		return frame;
	}

	/**
	 * Sets the parent graphic frame.
	 */
	protected void setGraphicFrame(XSSFGraphicFrame frame) {
		this.frame = frame;
	}

	public XSSFChartDataFactory getChartDataFactory() {
		return XSSFChartDataFactory.getInstance();
	}

	public XSSFChart getChartAxisFactory() {
		return this;
	}

	public void plot(ChartData data, ChartAxis... axis) {
		data.fillChart(this, axis);
	}

	public XSSFValueAxis createValueAxis(AxisPosition pos) {
		long id = axis.size() + 1;
		XSSFValueAxis valueAxis = new XSSFValueAxis(this, id, pos);
		if (axis.size() == 1) {
			ChartAxis ax = axis.get(0);
			ax.crossAxis(valueAxis);
			valueAxis.crossAxis(ax);
		}
		axis.add(valueAxis);
		return valueAxis;
	}

	public XSSFCategoryAxis createCategoryAxis(AxisPosition pos) {
		long id = axis.size() + 1;
		XSSFCategoryAxis valueAxis = new XSSFCategoryAxis(this, id, pos);
		if (axis.size() == 1) {
			ChartAxis ax = axis.get(0);
			ax.crossAxis(valueAxis);
			valueAxis.crossAxis(ax);
		}
		axis.add(valueAxis);
		return valueAxis;
	}

	public List<? extends XSSFChartAxis> getAxis() {
		if (axis.isEmpty() && hasAxis()) {
			parseAxis();
		}
		return axis;
	}

	public XSSFManualLayout getManualLayout() {
		return new XSSFManualLayout(this);
	}

	/**
	 * @return true if only visible cells will be present on the chart,
	 *         false otherwise
	 */
	@Override
	public boolean isPlotOnlyVisibleCells() {
		return chart.getPlotVisOnly().getVal();
	}

	/**
	 * @param plotVisOnly a flag specifying if only visible cells should be
	 *        present on the chart
	 */
	@Override
	public void setPlotOnlyVisibleCells(boolean plotVisOnly) {
		chart.getPlotVisOnly().setVal(plotVisOnly);
	}

	/**
	 * Returns the title, or null if none is set
	 */
	public XSSFRichTextString getTitle() {
		if(! chart.isSetTitle()) {
			return null;
		}

		// TODO Do properly
		CTTitle title = chart.getTitle();

		StringBuffer text = new StringBuffer();
		XmlObject[] t = title
			.selectPath("declare namespace a='"+XSSFDrawing.NAMESPACE_A+"' .//a:t");
		for (int m = 0; m < t.length; m++) {
			NodeList kids = t[m].getDomNode().getChildNodes();
			for (int n = 0; n < kids.getLength(); n++) {
				if (kids.item(n) instanceof Text) {
					text.append(kids.item(n).getNodeValue());
				}
			}
		}

		return new XSSFRichTextString(text.toString());
	}
	
	// 20130705, paowang@potix.com: for testing legend existed or not
	public boolean hasLegend() {
		return chart.isSetLegend();  
	}

	public XSSFChartLegend getOrCreateLegend() {
		return new XSSFChartLegend(this);
	}

	public void deleteLegend() {
		if (chart.isSetLegend()) {
			chart.unsetLegend();
		}
	}

	private boolean hasAxis() {
		CTPlotArea ctPlotArea = chart.getPlotArea();
		int totalAxisCount =
			ctPlotArea.sizeOfValAxArray()  +
			ctPlotArea.sizeOfCatAxArray()  +
			ctPlotArea.sizeOfDateAxArray() +
			ctPlotArea.sizeOfSerAxArray();
		return totalAxisCount > 0;
	}

	private void parseAxis() {
		// TODO: add other axis types
		parseValueAxis();
		parseCategoryAxis();
	}

	private void parseValueAxis() {
		for (CTValAx valAx : chart.getPlotArea().getValAxList()) {
			axis.add(new XSSFValueAxis(this, valAx));
		}
	}
	
	private void parseCategoryAxis() {
		for (CTCatAx catAx : chart.getPlotArea().getCatAxList()) {
			axis.add(new XSSFCategoryAxis(this, catAx));
		}
	}

	//20111005, henrichen@zkoss.org: chart title
    public String getChartTitle() {
    	final XSSFRichTextString rstr = getTitle();
    	return rstr == null ? null : rstr.toString();
    }
	
	//20111005, henrichen@zkoss.org: 3D view
	public XSSFView3D getOrCreateView3D() {
		return new XSSFView3D(this);
	}
	
	//20111005, henrichen@zkoss.org: 3D view
	public void deleteView3D() {
		if (chart.isSetView3D()) {
			chart.unsetView3D();
		}
	}

	//20111005, henrichen@zkoss.org: 3D view
    public boolean isSetView3D() {
    	return chart.isSetView3D();
    }

    //20111006, henrichen@zkoss.org: autoTitleDeleted property
    public boolean isAutoTitleDeleted() {
    	final CTBoolean b = chart.getAutoTitleDeleted();
    	return b != null ? b.getVal() : false; 
    }
    
    //20111007, henrichen@zkoss.org: rename sheet
    public void renameSheet(String oldname, String newname) {
    	switch(getChartType()) {
	    	case Pie:
	    	{
	    		XSSFPieChartData data  = new XSSFPieChartData(this);
	    		renameSheet(data.getSeries(), oldname, newname);
	    		break;
	    	}
	    	case Pie3D:
	    	{
	    		XSSFPie3DChartData data  = new XSSFPie3DChartData(this);
	    		renameSheet(data.getSeries(), oldname, newname);
	    		break;
	    	}
	    	case Doughnut:
	    	{
	    		XSSFDoughnutChartData data  = new XSSFDoughnutChartData(this);
	    		renameSheet(data.getSeries(), oldname, newname);
	    		break;
	    	}
	    	case Bar3D:
	    	{
	    		XSSFBar3DChartData data  = new XSSFBar3DChartData(this);
	    		renameSheet(data.getSeries(), oldname, newname);
	    		break;
	    	}
	    	case Column3D:
	    	{
	    		XSSFBar3DChartData data  = new XSSFColumn3DChartData(this);
	    		renameSheet(data.getSeries(), oldname, newname);
	    		break;
	    	}
	    	case Bar:
	    	{
	    		XSSFBarChartData data  = new XSSFBarChartData(this);
	    		renameSheet(data.getSeries(), oldname, newname);
	    		break;
	    	}
	    	case Column:
	    	{
	    		XSSFBarChartData data  = new XSSFColumnChartData(this);
	    		renameSheet(data.getSeries(), oldname, newname);
	    		break;
	    	}
	    	case Line3D:
	    	{
	    		XSSFLine3DChartData data  = new XSSFLine3DChartData(this);
	    		renameSheet(data.getSeries(), oldname, newname);
	    		break;
	    	}
	    	case Line:
	    	{
	    		XSSFLineChartData data  = new XSSFLineChartData(this);
	    		renameSheet(data.getSeries(), oldname, newname);
	    		break;
	    	}
	    	//TODO other chart types
	    	case Area:
	    	case Area3D:
	    	case Bubble:
	    	case OfPie:
	    	case Radar:
	    	case Scatter:
	    	case Stock:
	    	case Surface:
	    	case Surface3D:
    	}
    }
    
    //20111007, henrichen@zkoss.org: rename CategoryDataSerie
    private void renameSheet(List<? extends CategoryDataSerie> series, String oldname, String newname) {
		for (CategoryDataSerie serie : series) {
			ChartTextSource title = serie.getTitle();
			if (title != null) {
				title.renameSheet(oldname, newname);
			}
			serie.getCategories().renameSheet(oldname, newname);
			serie.getValues().renameSheet(oldname, newname);
		}
    }
    
    //20111007, henrichen@zkoss.org: get chart type
    public ChartType getChartType() {
    	final CTPlotArea plotArea = chart.getPlotArea();
    	//Area3D
    	final CTArea3DChart[] area3ds = plotArea.getArea3DChartArray();
    	if (area3ds != null && area3ds.length > 0) {
    		return ChartType.Area3D;
    	}
    	
    	//Area
    	final CTAreaChart[] areas = plotArea.getAreaChartArray();
    	if (areas != null && areas.length > 0) {
    		return ChartType.Area;
    	}

    	//Bar3D or Column3D
    	final CTBar3DChart[] bar3ds = plotArea.getBar3DChartArray();
    	if (bar3ds != null && bar3ds.length > 0) {
    		switch(bar3ds[0].getBarDir().getVal().intValue()) {
			case STBarDir.INT_BAR: return ChartType.Bar3D;
			default:
			case STBarDir.INT_COL: return ChartType.Column3D;
    		}
    	}
    	
    	//Bar or Column
    	final CTBarChart[] bars = plotArea.getBarChartArray();
    	if (bars != null && bars.length > 0) {
    		switch(bars[0].getBarDir().getVal().intValue()) {
			case STBarDir.INT_BAR: return ChartType.Bar;
			default:
			case STBarDir.INT_COL: return ChartType.Column;
    		}
    	}
    	
    	//Bubble
    	final CTBubbleChart[] bubbles = plotArea.getBubbleChartArray();
    	if (bubbles != null && bubbles.length > 0) {
    		return ChartType.Bubble;
    	}

    	//Doughnut
    	final CTDoughnutChart[] donuts = plotArea.getDoughnutChartArray();
    	if (donuts != null && donuts.length > 0) {
    		return ChartType.Doughnut;
    	}
    	
    	//Line3D
    	final CTLine3DChart[] line3ds = plotArea.getLine3DChartArray();
    	if (line3ds != null && line3ds.length > 0) {
    		return ChartType.Line3D;
    	}
    	
    	//Line
    	final CTLineChart[] lines = plotArea.getLineChartArray();
    	if (lines != null && lines.length > 0) {
    		return ChartType.Line;
    	}
    	
    	//OfPie
    	final CTOfPieChart[] ofpies = plotArea.getOfPieChartArray();
    	if (ofpies != null && ofpies.length > 0) {
    		return ChartType.OfPie;
    	}
    	
    	//Pie3D
    	final CTPie3DChart[] pie3ds = plotArea.getPie3DChartArray();
    	if (pie3ds != null && pie3ds.length > 0) {
    		return ChartType.Pie3D;
    	}
    	
    	//Pie
    	final CTPieChart[] pies = plotArea.getPieChartArray();
    	if (pies != null && pies.length > 0) {
    		return ChartType.Pie;
    	}

    	//Radar
    	final CTRadarChart[] radars = plotArea.getRadarChartArray();
    	if (radars != null && radars.length > 0) {
    		return ChartType.Radar;
    	}

    	//Scatter
    	final CTScatterChart[] scatters = plotArea.getScatterChartArray();
    	if (scatters != null && scatters.length > 0) {
    		return ChartType.Scatter;
    	}
    	
    	//Stock
    	final CTStockChart[] stocks = plotArea.getStockChartArray();
    	if (stocks != null && stocks.length > 0) {
    		return ChartType.Stock;
    	}

    	//Surface3D
    	final CTSurface3DChart[] surface3ds = plotArea.getSurface3DChartArray();
    	if (surface3ds != null && surface3ds.length > 0) {
    		return ChartType.Surface3D;
    	}
    	
    	//Surface
    	final CTSurfaceChart[] surfaces = plotArea.getSurfaceChartArray();
    	if (surfaces != null && surfaces.length > 0) {
    		return ChartType.Surface;
    	}
    	
    	return null;
    }

    //20111111, henrichen@zkoss.org: chart poistion anchor
    private ClientAnchor _anchor;
    
	@Override
	public ClientAnchor getPreferredSize() {
		return _anchor;
	}

    //20111110, henrichen@zkoss.org: update anchor
    @Override
	public void setClientAnchor(ClientAnchor newanchor) {
    	if (_anchor == null) {
    		_anchor = newanchor;
    	} else {
	    	_anchor.setCol1(newanchor.getCol1());
	    	_anchor.setCol2(newanchor.getCol2());
	    	_anchor.setDx1(newanchor.getDx1());
	    	_anchor.setDx2(newanchor.getDx2());
	    	_anchor.setDy1(newanchor.getDy1());
	    	_anchor.setDy2(newanchor.getDy2());
	    	_anchor.setRow1(newanchor.getRow1());
	    	_anchor.setRow2(newanchor.getRow2());
    	}
    }

	//20111111, henrichen@zkoss.org
	@Override
	public String getChartId() {
		return this.getPackageRelationship().getId();
	}

    //ZSS-830
    // -100 ~ +100; default 0
    public int getBarOverlap() {
    	final CTPlotArea plotArea = chart.getPlotArea();
    	//Bar or Column
    	final CTBarChart[] bars = plotArea.getBarChartArray();
    	if (bars != null && bars.length > 0) {
    		CTBarChart barChart = bars[0];
    		if (barChart.isSetOverlap()) {
    			return barChart.getOverlap().getVal();
    		} else {
    			switch(barChart.getGrouping().getVal().intValue()) {
    			case STBarGrouping.INT_CLUSTERED: return 0;
    			case STBarGrouping.INT_STANDARD: return 0;
    			case STBarGrouping.INT_STACKED: return 100;
    			case STBarGrouping.INT_PERCENT_STACKED: return 100;
    			}
    		}
    	}
    	return 0;
    }
    
    //ZSS-830
    public void setBarOverlap(int overlap) {
    	final CTPlotArea plotArea = chart.getPlotArea();
    	//Bar or Column
    	final CTBarChart[] bars = plotArea.getBarChartArray();
    	if (bars != null && bars.length > 0) {
    		CTBarChart barChart = bars[0];
    		CTOverlap ov = !barChart.isSetOverlap() ?
    			barChart.addNewOverlap() : barChart.getOverlap();
    		ov.setVal((byte)overlap);
    	}
    }
}

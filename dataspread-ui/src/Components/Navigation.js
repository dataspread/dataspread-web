import React, {Component} from 'react'
import {HotTable} from '@handsontable/react';
import 'handsontable/dist/handsontable.full.css';
import './Navigation.css';
import * as d3 from "d3";


export default class Navigation extends Component {

    constructor(props) {
        super(props);
        //console.log(this);
        this.state = {
            alltext: true,
            currLevel: 0,
            cumulativeData: [],
            currData: [],
            viewData: [["Ford", "Volvo", "Toyota", "Honda"],
                ["2016", 10, 11, 12, 13],
                ["2017", 20, 11, 14, 13],
                ["2018", 30, 15, 12, 13]],
            wrapperWidth: window.innerWidth,
            wrapperHeight: window.innerHeight,
            colHeader: ["City"],
            aggregateData: {},
            mergeCellInfo: [],
            selectedChild: [0],
            selectedBars: [],
            childHash: new Map(),
            levelList: [],
            hieraOpen: false,
            sortChild_ls: [],
            prevPath: '',
            nextPath: '',

        }

        this.hotTableComponent = React.createRef();

        this.startNav = this.startNav.bind(this);
        this.navCellRenderer = this.navCellRenderer.bind(this);
        this.beforeOnCellMouseDownHandler = this.beforeOnCellMouseDownHandler.bind(this);
        this.afterSelectionHandler = this.afterSelectionHandler.bind(this);
        this.computeCellChart = this.computeCellChart.bind(this);
        this.zoomIn = this.zoomIn.bind(this);
        this.computePath = this.computePath.bind(this);
        this.colHeaderRenderer = this.colHeaderRenderer.bind(this);
        this.cellRenderer = this.cellRenderer.bind(this);
        this.colWidthsComputer = this.colWidthsComputer.bind(this);
        this.zoomOutHist = this.zoomOutHist.bind(this);
        this.submitHierForm = this.submitHierForm.bind(this);
        this.chartRenderer = this.chartRenderer.bind(this);
        this.jumpToHistorialView = this.jumpToHistorialView.bind(this);
        this.brushNlink = this.brushNlink.bind(this);
        this.updateBarChartFocus = this.updateBarChartFocus.bind(this);
        this.updateNavCellFocus = this.updateNavCellFocus.bind(this);
        this.jumpToFocus = this.jumpToFocus.bind(this);

    }

    startNav(data) {
        console.log(this)
        //console.log(data);
        let length = data.data.length;
        let viewData = new Array(data.data.length);
        for (let i = 0; i < length; i++) {
            viewData[i] = [""];
            viewData[i][0] = data.data[i].name;
        }
        let childHash = new Map();
        for (let i = 0; i < length; i++) {
            childHash.set(i, data.data[i].children);
        }

        this.setState({
            alltext: true,
            currLevel: 0,
            viewData: viewData,
            wrapperWidth: window.innerWidth,
            wrapperHeight: window.innerHeight,
            colHeader: ["City"],
            aggregateData: {},
            mergeCellInfo: [],
            selectedChild: [0],
            selectedBars: [],
            childHash: childHash,
            levelList: [],
            hieraOpen: false,
            sortChild_ls: [],
            prevPath: '',
            nextPath: '',
            currData: data.data,
            cumulativeData: [data.data]
        });
        //console.log(this.state.cumulativeData[0][0]);


        var currentState = this.state;
        let self = this;
        this.hotTableComponent.current.hotInstance.updateSettings({
            data: currentState.viewData,
            minCols: 1,
            readOnly: true,
            rowHeights: (currentState.wrapperHeight * 0.93 / currentState.currData.length > 90)
                ? currentState.wrapperHeight * 0.93 / currentState.currData.length
                : 90,
            width: currentState.wrapperWidth * 0.19,
            height: currentState.wrapperHeight * 0.92,
            rowHeaderWidth: 0,
            rowHeaders: true,
            colWidths: self.colWidthsComputer,
            colHeaders: self.colHeaderRenderer,
            stretchH: 'all',
            contextMenu: false,
            outsideClickDeselects: false,
            className: "wrap",
            //manualColumnResize: true,
            mergeCells: currentState.mergeCellInfo,
            beforeOnCellMouseDown: self.beforeOnCellMouseDownHandler,
            cells: self.cellRenderer,
            afterSelection: self.afterSelectionHandler,
        })
        this.hotTableComponent.current.hotInstance.view.wt.update('onCellDblClick', function (e, cell) {
            if (cell.row >= 0) {
                if (currentState.currLevel == 0) {
                    if (cell.col == 0 && currentState.cumulativeData[currentState.currLevel][cell.row].clickable) {
                        //        var child = cell.row/spanList[currLevel];
                        let child = cell.row;
                        //nav.deselectCell();
                        //zoomming = true;
                        self.zoomIn(child);
                    }
                } else {
                    if (cell.col == 1 && currentState.cumulativeData[currentState.currLevel][cell.row].clickable) {
                        //  var child = cell.row/spanList[currLevel];
                        var child = cell.row;
                        //nav.deselectCell();
                        //zoomming = true;
                        self.zoomIn(child);
                    } else if (cell.col == 0) {
                        //zoomouting = true;
                        //zoomOutHist(nav);
                    }
                }
            }
        });
    }

    beforeOnCellMouseDownHandler(e, coords, element) {
        // $("#formulaBar").val("");
        let currentState = this.state;
        let self = this;
        let topLevel = (currentState.currLevel == 0 && coords.col != 0)
        let otherLevel = (currentState.currLevel > 0 && coords.col != 1)
        // if (topLevel && coords.row >= 0) {
        //     $("#formulaBar").val("=" + navRawFormula[coords.row][coords.col - 1]);
        // }
        // else if (currLevel > 0 && coords.row >= 0 && coords.col >= 2) {
        //     $("#formulaBar").val("=" + navRawFormula[coords.row][coords.col - 2]);
        // }
        console.log("beforeoncellmousdown")
        console.log(e);
        console.log(coords);
        //|| zoomming
        if (topLevel || otherLevel ||
            e.realTarget.className == "colHeader" ||
            e.realTarget.className == "relative" || e.realTarget.className.baseVal == "bar") {

            e.stopImmediatePropagation();
        }
        if (e.realTarget.classList['3'] == "zoomInPlus") {
            e.stopImmediatePropagation();
            self.zoomIn(coords.row);
        }

        if (e.realTarget.classList['3'] == "zoomOutM") {
            e.stopImmediatePropagation();
            self.zoomOutHist();
            return;
        }
        if (e.realTarget.id == "colClose") {
            self.removeHierarchiCol(coords.col)
        }
        if (e.realTarget.classList['0'] == "slider") {
            let level = coords.col - 1;
            if (currentState.currLevel > 0)
                level = coords.col - 2;
            currentState.aggregateData.formula_ls[level].getChart =
                !currentState.aggregateData.formula_ls[level].getChart;
            console.log(currentState.aggregateData)
            self.submitHierForm(currentState.aggregateData.formula_ls);
        }
    }

    colWidthsComputer(col) {
        let currState = this.state;
        if (currState.currLevel == 0) {
            if (col == 0) {
                return currState.wrapperWidth * 0.18;
            } else {
                return currState.wrapperWidth * 0.15;
            }
        } else {
            if (col == 0) {
                return currState.wrapperWidth * 0.04;
            } else if (col == 1 && currState.alltext) {
                return currState.wrapperWidth * 0.08;
            } else {
                return currState.wrapperWidth * 0.15;
            }
        }

    }

    cellRenderer(row, column, prop) {
        let currState = this.state;
        let cellMeta = {}
        if (currState.currLevel == 0) {
            if (column == 0) {
                cellMeta.renderer = this.navCellRenderer;
            } else {
                cellMeta.renderer = this.chartRenderer;
            }
        }
        else {
            if (column <= 1) {
                cellMeta.renderer = this.navCellRenderer;
            } else {
                cellMeta.renderer = this.chartRenderer;
            }
        }
        return cellMeta;
    }

    colHeaderRenderer(col) {
        let currState = this.state;
        if (col < currState.colHeader.length) {
            if (currState.currLevel == 0) {
                switch (col) {
                    case 0:
                        return currState.colHeader[0];
                    default:
                        let check =
                            currState.aggregateData.formula_ls[col - 1].getChart ? "checked" : "";
                    return currState.colHeader[col] + "<span id='colClose' >x</span>" +
                        "<label class=\"switch\">" +
                        "  <input type=\"checkbox\"" + check + ">" +
                        "  <span class=\"slider round\"></span>" +
                        "</label>";
                }
            } else {
                switch (col) {
                    case 0:
                        return currState.colHeader[0];
                    case 1:
                        return currState.colHeader[1];
                    default:

                        let check = currState.aggregateData.formula_ls[col - 2].getChart ? "checked" : "";
                    return currState.colHeader[col] + "<span id='colClose'>x</span>" +
                        "<label class=\"switch\">" +
                        "  <input type=\"checkbox\"" + check + ">" +
                        "  <span class=\"slider round\"></span>" +
                        "</label>";
                }
            }
        }
    }

    afterSelectionHandler(r, c, r2, c2, preventScrolling,
                          selectionLayerLevel) {
        // setting if prevent scrolling after selection
        console.log(r, c, r2, c2)
        if (this.state.cumulativeData[this.state.currLevel][r] != undefined) {
            let selectedChild = [];
            selectedChild.push(r);
            let selectedBars = [];
            let barObj = {};
            barObj.cell = r;
            barObj.bars = [0];
            selectedBars.push(barObj);
            this.setState({
                selectedChild: selectedChild,
                selectedBars: selectedBars
            });

            let lowerRange = this.state.cumulativeData[this.state.currLevel][r].rowRange[0];
            console.log('afterselection');
            console.log(lowerRange)
            this.props.scrollTo(lowerRange);
            // let upperRange = cumulativeData[currLevel][r].rowRange[1];
            // updateData(cumulativeData[currLevel][r].rowRange[0], 0,
            //     cumulativeData[currLevel][r].rowRange[1], 15, true);
            // updataHighlight();
            // nav.render();
        }
    }

    navCellRenderer(instance, td, row, col, prop, value, cellProperties) {
        ////console.log(this);
        let tempString = "<div><span>" + value + "</span>";
        const currentState = this.state;
        if (row >= currentState.cumulativeData[currentState.currLevel].length) return;
        if (currentState.currLevel == 0) {

            if (currentState.selectedChild.includes(row)) {
                td.style.background = '#D3D3D3';
                td.style.color = '#4e81d3';
            } else {
                td.style.background = '#F5F5DC';
            }
            let targetCell = currentState.cumulativeData[currentState.currLevel][row];
            if (targetCell.clickable) {
                tempString += " (Rows: " + targetCell.value + ")";
                tempString += "<i class=\"fa fa-angle-right fa-2x zoomInPlus\" style=\"color: #51cf66;\" id='zm" + row + "' aria-hidden=\"true\"></i>";

                if (currentState.childHash.has(row)) {
                    let chartString = "navchartdiv" + row + col;
                    tempString += "<div id=" + chartString + " ></div>";
                    td.innerHTML = tempString + "</div>";
                    this.computeCellChart(chartString, row);
                    return;
                }
            } else {
                tempString += "<p>Total Rows: " + targetCell.value + "<br> Start Row No: " + (targetCell.rowRange[0] + 1) + "<br> End Row No: " + (targetCell.rowRange[1] + 1) + "</p>";
                td.innerHTML = tempString + "</div>";
                return;
            }

        } else {


            if (col == 1 && currentState.selectedChild.includes(row)) {
                td.style.background = '#D3D3D3';
                td.style.color = '#4e81d3';
            }
            else {
                td.style.background = '#F5F5DC';
            }
            if (col == 0) {
                tempString = "<div><i class=\"fa fa-angle-left fa-3x zoomOutM\" style=\"color: #339af0;\" id='zm" + row + "' aria-hidden=\"true\"></i>";
                let chartString = "parentCol" + row + col;
                tempString += "<div id=" + chartString + " ></div>";
                td.innerHTML = tempString + "</div>";
                let holder = d3.select("#" + chartString)
                    .append("svg")
                    .attr("width", currentState.wrapperWidth * 0.02)
                    .attr("height", currentState.wrapperHeight * 0.7);
                let yoffset = currentState.wrapperHeight * 0.4;
                // draw the text
                holder.append("text")
                    .style("fill", "black")
                    .style("font-size", "20px")
                    .attr("dy", ".35em")
                    .attr("text-anchor", "middle")
                    .attr("transform", "translate(8," + yoffset + ") rotate(270)")
                    .text(value);
                return;
            } else {
                let targetCell = currentState.cumulativeData[currentState.currLevel][row];

                if (targetCell.clickable) {
                    tempString += " (Rows: " + targetCell.value + ")";
                    tempString += "<i class=\"fa fa-angle-right fa-2x zoomInPlus\" style=\"color: #51cf66;\" id='zm" + row + "' aria-hidden=\"true\"></i>";

                    if (currentState.childHash.has(row)) {
                        let chartString = "navchartdiv" + row + col;
                        tempString += "<div id=" + chartString + " ></div>";
                        td.innerHTML = tempString + "</div>";
                        this.computeCellChart(chartString, row);
                        return;
                    }
                } else {
                    tempString += "<p>Total Rows: " + targetCell.value + "<br> Start Row No: " + (targetCell.rowRange[0] + 1) + "<br> End Row No: " + (targetCell.rowRange[1] + 1) + "</p>";
                    td.innerHTML = tempString + "</div>";
                }
            }

        }


    }

    computeCellChart(chartString, row) {
        let nav = this;
        let self = this.state;
        let scrollTo = this.props.scrollTo;
        let result = self.childHash.get(row);
        let number = result.length;
        let maxLen = 0;
        let hash = new Map();
        let chartData = [];

        for (let i = 0; i < number; i++) {
            if (result[i].name.length > maxLen) {
                maxLen = result[i].name.length;
            }
            let value;
            if (result[i].name.length > 12) {
                value = result[i].name.substring(0, 13) + "...";
                hash.set(value, {name: result[i].name, range: result[i].rowRange[0]})
            } else {
                value = result[i].name;
                hash.set(value, {name: result[i].name, range: result[i].rowRange[0]});
            }
            chartData.push({name: value, count: result[i].value});
        }

        let maxleft = 75;

        let margin = {top: 0, right: 40, bottom: 5, left: maxleft};
        var fullWidth = self.currLevel == 0 ? self.wrapperWidth * 0.18 : self.wrapperWidth * 0.15;
        var fullHeight = (self.wrapperHeight * 0.95 / self.cumulativeData[self.currLevel].length > 90)
            ? self.wrapperHeight * 0.95 / self.cumulativeData[self.currLevel].length - 10 : 80;
        if (number > 6) {
            fullHeight += (number - 6) * 5;
        }
        var width = fullWidth - margin.right - margin.left;
        var height = fullHeight - margin.top - margin.bottom;
        var svg = d3.select("#" + chartString)
            .append("svg")
            .attr("width", width + margin.left + margin.right)
            .attr("height", height + margin.top + margin.bottom)
            .append("g")
            .attr("transform", "translate(" + margin.left + "," + margin.top + ")");

        var x = d3.scaleLinear()
            .range([0, width])
            .domain([0, d3.max(chartData, function (d) {
                return d.count;
            })]);

        var y = d3.scaleBand()
            .rangeRound([0, height])
            .padding(0.1)
            .domain(chartData.map(function (d) {
                return d.name;
            }));

        // //make y axis to show bar names
        var yAxis = d3.axisLeft(y)
            .tickSize(0);

        var tooltip =
            d3.select('#' + chartString).append("div").attr("class", "toolTip");
        var gy = svg.append("g")
            .attr("class", "y axis")
            .call(yAxis)
            .selectAll(".tick text")
            .data(chartData)
            .on("mouseover",
                function (d) {
                    //           //console.log(d)
                    tooltip.style("left", d3.event.pageX - 20 + "px")
                        .style("top", d3.event.pageY - 30 + "px")
                        .style("display", "inline-block")
                        .style("font", "10px")
                        .html(hash.get(d.name).name);
                })
            .on("mouseout", function (d) {
                tooltip.style("display", "none");
            });

        var bars = svg.selectAll(".bar")
            .data(chartData)
            .enter()
            .append("g")

        //append rects
        bars.append("rect")
            .attr("class", "bar")
            .attr("y", function (d) {
                return y(d.name);
            })
            .attr("height", y.bandwidth())
            .attr("x", 0)
            .attr('fill', function (d, i) {
                ////console.log("selectedBars");
                ////console.log(selectedBars);
                ////console.log(row,i);
                for (let ind = 0; ind < self.selectedBars.length; ind++) {
                    if (self.selectedBars[ind].cell == row) {
                        if (self.selectedBars[ind].bars.includes(i))
                            return "#32CC99";//'#ff4500';
                        else
                            return "#70DCB8";//'#ffA500';
                    }
                }
                return "#70DCB8";//'#ffA500';
            })
            .attr("width", function (d) {
                return x(d.count);
            })
            .style("stroke-width", 1)
            .on("mouseover",
                function (d) {
                    //           //console.log(d)
                    tooltip.style("left", d3.event.pageX - 20 + "px")
                        .style("top", d3.event.pageY - 30 + "px")
                        .style("display", "inline-block")
                        .style("font", "10px")
                        .html(hash.get(d.name).name);
                })
            .on("mouseout", function (d) {
                tooltip.style("display", "none");
            })
            .on("click", function (d, i) {

                // work around for highlight nav chart
                // let selectedChild = [];
                // selectedChild.push(row);
                // let selectedBars = [];
                // let barObj = {};
                // barObj.cell = row;
                // barObj.bars = [i];
                // selectedBars.push(barObj);
                // nav.setState({
                //     selectedChild: selectedChild,
                //     selectedBars: selectedBars
                // });
                let lowerRange = hash.get(d.name).range;


                scrollTo(lowerRange);
                //updataHighlight();
            });
        //   .on("dblclick",function(d){ alert("node was double clicked"); });

        //add a value label to the right of each bar
        bars.append("text")
            .attr("class", "label")
            //y position of the label is halfway down the bar
            .attr("y", function (d) {
                return y(d.name) + y.bandwidth() / 2 + 4;
            })
            .style("font-size", "10px")
            //x position is 3 pixels to the right of the bar
            .attr("x", function (d) {
                return x(d.count) + 3;
            })
            .text(function (d) {
                return d.count;
            });

    }

    zoomIn(child) {
        let currState = this.state;
        let selectFirstChild = false;
        //console.log("In zoom in:" + child);
        //console.log(currState.selectedChild);
        let childHash = new Map();
        if (!currState.selectedChild.includes(child) || currState.selectedChild.length == 0)
            selectFirstChild = true;

        let colHeader = currState.colHeader;
        if (currState.currLevel == 0) {
            colHeader.splice(1, 0, "")
        }
        let levelList = currState.levelList;
        levelList.push(child);
        this.setState({
            selectedChild: [],
            selectedBars: [],
            sortChild_ls: [],
            levelList: levelList,
            colHeader: colHeader,
        });
        let childlist = this.computePath(); // get the list of children

        let queryData = {};
        //console.log(this)
        queryData.bookId = this.props.bookId;
        queryData.sheetName = this.state.sheetName;
        queryData.path = childlist;
        fetch(this.state.urlPrefix + '/api/' + 'getChildren', {
            method: "POST",
            body: JSON.stringify(queryData),
            headers: {
                'Content-Type': 'text/plain'
            }
        })
            .then(response => response.json())
            .then(data => {
                //console.log(data);
                if (data.status == "success") {
                    var result = data.data;
                    //console.log(result);

                    //console.log(this)
                    let currData = result.buckets;
                    let cumulativeData = currState.cumulativeData;
                    let currLevel = currState.currLevel + 1;
                    let alltext = true;
                    for (let i = 0; i < currData.length; i++) {
                        if (currData[i].clickable) alltext = false;
                        childHash.set(i, currData[i].children);
                    }
                    let prevPath = result.prev.path;
                    let nextPath = result.later.path;
                    // let breadcrum_ls = result.breadCrumb;
                    let mergeCellInfo = [];
                    mergeCellInfo.push(
                        {row: 0, col: 0, rowspan: currData.length, colspan: 1});
                    let viewData = [];
                    //console.log(currState);

                    for (let i = 0; i < currData.length; i++) {
                        if (i == 0) {
                            //console.log(cumulativeData);

                            viewData.push([cumulativeData[parseInt(currLevel - 1)][child].name]);
                        } else {
                            viewData.push([""]);
                        }
                    }

                    //console.log(viewData);
                    cumulativeData.splice(currLevel);
                    cumulativeData.push(currData);

                    for (let i = 0; i < currData.length; i++) {
                        viewData[i][1] = cumulativeData[currLevel][i].name;
                    }
                    this.setState({
                        currData: result.buckets,
                        childHash: childHash,
                        viewData: viewData,
                        cumulativeData: cumulativeData,
                        currLevel: currLevel,
                        mergeCellInfo: mergeCellInfo,
                        prevPath: prevPath,
                        nextPath: nextPath,
                    });
                    currState = this.state;
                    this.hotTableComponent.current.hotInstance.deselectCell();
                    console.log("638")
                    console.log(viewData)
                    this.hotTableComponent.current.hotInstance.updateSettings({
                        data: viewData,
                        rowHeights: (currState.wrapperHeight * 0.95 / currState.currData.length > 90)
                            ? currState.wrapperHeight * 0.95 / currState.currData.length
                            : 90,
                        mergeCells: mergeCellInfo,
                    })
                    if (currState.hieraOpen) {
                        this.submitHierForm(currState.aggregateData.formula_ls);
                    }

                    //
                    //         cumulativeDataSize += currData.length;
                    //
                    //
                    //             zoomming = false;
                    //             //nav.selectCell(0, 1)
                    //         }
                    this.props.updateBreadcrumb(result.breadCrumb, childlist); // calculate breadcrumb
                    this.updateNavCellFocus(currData[0].rowRange[0], currData[0].rowRange[0] + 20);
                    if (selectFirstChild)
                        this.hotTableComponent.current.hotInstance.selectCell(0, 1);
                }
            })
    }

    zoomOutHist() {
        let currState = this.state;
        let childHash = new Map();
        let colHeader = currState.colHeader;
        //nav.deselectCell();
        if (currState.currLevel == 0) {
            colHeader.splice(1, 0, "")
        }
        let targetChild = currState.levelList[currState.levelList.length - 1];
        let levelList = currState.levelList;
        levelList.pop();
        this.setState({
            selectedChild: [],
            selectedBars: [],
            sortChild_ls: [],
            levelList: levelList,
            colHeader: colHeader,
            alltext: false,
        });
        let childlist = this.computePath(); // get the list of children
        // api call to /levelList + '.' + child to get currData
        let queryData = {};

        queryData.bookId = this.props.bookId;
        queryData.sheetName = this.state.sheetName;
        queryData.path = childlist;

        fetch(this.state.urlPrefix + '/api/' + 'getChildren', {
            method: "POST",
            body: JSON.stringify(queryData),
            headers: {
                'Content-Type': 'text/plain'
            }
        })
            .then(response => response.json())
            .then(data => {
                if (data.status == "success") {
                    let result = data.data;
                    ////console.log(result);
                    let breadcrum_ls = result.breadCrumb;
                    // clickable = result.clickable;
                    let currLevel = breadcrum_ls.length;
                    let currData = result.buckets;
                    for (let i = 0; i < currData.length; i++) {
                        childHash.set(i, currData[i].children);
                    }
                    let prevPath = result.prev.path;
                    let nextPath = result.later.path;
                    let numChild = currData.length;
                    let viewData = new Array(numChild);
                    let cumulativeData = currState.cumulativeData;
                    cumulativeData[currLevel] = currData;
                    let mergeCellInfo = [];
                    if (currLevel > 0) {
                        mergeCellInfo.push({row: 0, col: 0, rowspan: numChild, colspan: 1});

                        let parentName = breadcrum_ls[breadcrum_ls.length - 1];

                        for (let i = 0; i < numChild; i++) {
                            if (i == 0) {
                                viewData[i] = [parentName];
                            } else {
                                viewData[i] = [""];
                            }
                            viewData[i][1] = currData[i].name;
                        }
                    } else {
                        colHeader.splice(1, 1);
                        for (let i = 0; i < numChild; i++) {
                            viewData[i] = [currData[i].name];
                        }
                    }
                    this.setState({
                        viewData: viewData,
                        currLevel: currLevel,
                        currData: currData,
                        cumulativeData: cumulativeData,
                        childHash: childHash,
                        colHeader: colHeader,
                        mergeCellInfo: mergeCellInfo,
                        prevPath: prevPath,
                        nextPath: nextPath,
                    });
                    currState = this.state;
                    this.hotTableComponent.current.hotInstance.updateSettings({
                        data: viewData,
                        rowHeights: (currState.wrapperHeight * 0.95 / currState.currData.length > 90)
                            ? currState.wrapperHeight * 0.95 / numChild
                            : 90,
                        mergeCells: mergeCellInfo,
                    })
                    if (currState.hieraOpen) {
                        this.submitHierForm(currState.aggregateData.formula_ls);
                    }

                    this.props.updateBreadcrumb(result.breadCrumb, childlist); // calculate breadcrumb
                    this.updateNavCellFocus(this.firstRow, this.lastRow);
                }
            })
    }

    jumpToHistorialView(childlist) {
        console.log("In Jump to Historical view");
        let currState = this.state;
        let childHash = new Map();
        let clickable = true;
        //nav.deselectCell();

        let tmp_ls = childlist.split(",");
        let levelList = []
        for (let i = 0; i < tmp_ls.length; i++) {
            if (tmp_ls[i].length != 0)
                levelList[i] = parseInt(tmp_ls[i]);
        }
        let targetChild = levelList[levelList.length - 1];

        this.setState({
            selectedChild: [],
            selectedBars: [],
            sortChild_ls: [],
            levelList: levelList,
        });

        // api call to /levelList + '.' + child to get currData
        let queryData = {};

        queryData.bookId = this.props.bookId;
        queryData.sheetName = this.state.sheetName;
        queryData.path = childlist;
        fetch(this.state.urlPrefix + '/api/' + 'getChildren', {
            method: "POST",
            body: JSON.stringify(queryData),
            headers: {
                'Content-Type': 'text/plain'
            }
        }).then(response => response.json())
            .then(data => {
                if (data.status == "success") {
                    var result = data.data;
                    let breadcrumb_ls = result.breadCrumb;
                    // clickable = result.clickable;
                    let currLevel = breadcrumb_ls.length;
                    let currData = result.buckets;
                    for (let i = 0; i < currData.length; i++) {
                        childHash.set(i, currData[i].children);
                    }
                    let prevPath = result.prev.path;
                    let nextPath = result.later.path;
                    let numChild = currData.length;
                    let viewData = new Array(numChild);
                    let cumulativeData = currState.cumulativeData;
                    cumulativeData[currLevel] = currData;
                    let mergeCellInfo = [];
                    let colHeader = currState.colHeader;
                    if (breadcrumb_ls.length != 0) {
                        mergeCellInfo.push({row: 0, col: 0, rowspan: numChild, colspan: 1});

                        let parentName = breadcrumb_ls[breadcrumb_ls.length - 1];

                        for (let i = 0; i < numChild; i++) {
                            if (i == 0) {
                                viewData[i] = [parentName];
                            } else {
                                viewData[i] = [""];
                            }
                            viewData[i][1] = currData[i].name;
                        }
                    } else {
                        colHeader.splice(1, 1);
                        for (let i = 0; i < numChild; i++) {
                            viewData[i] = [currData[i].name];
                        }
                    }

                    let columWidth = [];
                    if (breadcrumb_ls.length >= 1) {
                        columWidth = [40, 160];
                    } else {
                        columWidth = 200;
                    }

                    this.setState({
                        currData: result.buckets,
                        childHash: childHash,
                        viewData: viewData,
                        cumulativeData: cumulativeData,
                        currLevel: currLevel,
                        mergeCellInfo: mergeCellInfo,
                        colHeader: colHeader,
                        prevPath: prevPath,
                        nextPath: nextPath,
                    });
                    currState = this.state;
                    this.hotTableComponent.current.hotInstance.deselectCell();

                    this.hotTableComponent.current.hotInstance.updateSettings({
                        data: viewData,
                        rowHeights: (currState.wrapperHeight * 0.95 / currState.currData.length > 90)
                            ? currState.wrapperHeight * 0.95 / currState.currData.length
                            : 90,
                        mergeCells: mergeCellInfo,
                    });
                    if (currState.hieraOpen) {
                        this.submitHierForm(currState.aggregateData.formula_ls);
                    }
                    this.props.updateBreadcrumb(result.breadCrumb, childlist); // calculate breadcrumb
                    //     if (currLevel == 0) {
                    //         updateNavCellFocus(currentFirstRow, currentLastRow);
                    //     } else
                    //         nav.selectCell(0, 1);
                    // }
                }
            });
    }

    submitHierForm(formula_ls) {

        let aggregateData = {};

        aggregateData.bookId = this.props.bookId;
        aggregateData.sheetName = this.state.sheetName;
        aggregateData.formula_ls = formula_ls;
        let childlist = this.computePath();
        aggregateData.path = " " + childlist;
        fetch(this.state.urlPrefix + '/api/' + 'getHierarchicalAggregateFormula', {
            method: "POST",
            body: JSON.stringify(aggregateData),
            headers: {
                'Content-Type': 'text/plain'
            }
        })
            .then(response => response.json())
            .then(data => {

                //console.log(data.data);
                let navAggRawData = data.data;
                let currState = this.state;
                let currLevel = currState.currLevel;
                let colHeader = currState.colHeader;
                let viewData = currState.viewData;
                let options = currState.options;
                let cumulativeData = currState.cumulativeData;
                let wrapperWidth = currState.wrapperWidth;
                let wrapperHeight = currState.wrapperHeight;
                if (currLevel == 0) {
                    colHeader.splice(
                        1,
                        colHeader.length - 1,
                    );
                    for (let i = 0; i < viewData.length; i++) {
                        viewData[i].splice(
                            1,
                            viewData[i].length - 1,
                        )
                    }
                } else {
                    colHeader.splice(
                        2,
                        colHeader.length - 2,
                    );
                    for (let i = 0; i < viewData.length; i++) {
                        viewData[i].splice(
                            2,
                            viewData[i].length - 2,
                        )
                    }
                }
                let hierarchicalColAttr = [];
                for (let i = 0; i < data.data.length; i++) {
                    let hierCol = aggregateData.formula_ls[i];
                    hierarchicalColAttr.push(parseInt(hierCol.attr_index - 1));
                    colHeader.push(options[hierCol.attr_index - 1] + " " +
                        hierCol.function + " " + hierCol.param_ls);
                }

                let targetCol = (currLevel == 0) ? 1 : 2;
                let navRawFormula = [];
                for (let i = 0; i < cumulativeData[currLevel].length; i++) {
                    let formulaRow = [];
                    for (let j = 0; j < navAggRawData.length; j++) {
                        formulaRow.push(navAggRawData[j][i].formula);
                        let text = navAggRawData[j][i].value;
                        if (isNaN(text)) {
                            viewData[i][targetCol + j] = text;
                        } else {
                            viewData[i][targetCol + j] = text.toFixed(2);
                        }
                    }
                    navRawFormula.push(formulaRow);
                }
                let columWidth = [];
                if (currLevel >= 1) {
                    columWidth = [
                        ,
                        ,
                    ];
                } else {
                    columWidth = [
                        ,
                    ];
                }

                for (let j = 0; j < navAggRawData.length; j++) {
                    columWidth.push(wrapperWidth * 0.15);
                }

                let numChild = cumulativeData[currLevel].length;
                let percentage = currState.alltext ? 0.12 : 0.18;
                // console.log(wrapperWidth)
                // console.log(percentage)
                // console.log(navAggRawData.length)
                let newWidth = wrapperWidth * (percentage + navAggRawData.length * 0.15);
                // console.log(newWidth);
                this.setState({
                    hieraOpen: true,
                    aggregateData: aggregateData,
                    navAggRawData: navAggRawData,
                    colHeader: colHeader,
                    viewData: viewData,
                    // cumulativeData: cumulativeData,
                    navRawFormula: navRawFormula,

                });
                this.hotTableComponent.current.hotInstance.updateSettings({
                    mergeCells: currState.mergeCellInfo,
                    width: newWidth,
                    manualColumnResize: columWidth,
                    minCols: 1,
                    data: viewData,
                    rowHeights: (wrapperHeight * 0.95 / numChild > 90)
                        ? wrapperHeight * 0.95 / numChild
                        : 90,
                });

                // for highlight
                let brushNLinkRows = [];
                if (navAggRawData.length == 1 && this.isPointFormula(navAggRawData[0][0].formula)) {
                    console.log("Brush color list satisfied");
                    console.log(navAggRawData)
                    let data = navAggRawData[0];
                    let queryObj = {}
                    let cond = [];
                    let value = [];
                    let firstR = [];
                    let lastR = [];

                    // for (let i = 0; i < cumulativeData[currLevel].length; i++) {
                        let formula = data[0].formula;
                        if (formula.includes("COUNTIF") || formula.includes("SUMIF")) {
                            let ls = formula.split(",")[1].split(")")[0];
                            let str = ls.substring(1, 3);
                            if (str.includes(">=") || str.includes("<=") || str.includes("<>")) {
                                cond.push(ls.substring(1, 3));
                                value.push(ls.substring(3, ls.length - 1));
                            }
                            else if (str.includes(">") || str.includes("<") || str.includes("=")) {
                                cond.push(ls.substring(1, 2));
                                value.push(ls.substring(2, ls.length - 1));
                            }
                            else {
                                cond.push("=");
                                value.push(ls.substring(1, ls.length - 1));
                            }
                        }
                        else if (formula.includes("MIN") || formula.includes("MAX") || formula.includes("MEDIAN") || formula.includes("MODE") || formula.includes("RANK") || formula.includes("SMALL") || formula.includes("LARGE")) {
                            cond.push("");
                            value.push(data[0].value.toString());
                        }

                        //TODO: when ondemand loading of data available
                        /*let first = cumulativeData[currLevel][selectedChild[i]].rowRange[0];
                        let last = cumulativeData[currLevel][selectedChild[i]].rowRange[1];

                        if (first < currentFirstRow)
                            firstR.push(currentFirstRow)
                        else
                            firstR.push(first);
                        if (last > currentLastRow)
                            lastR.push(currentLastRow);
                        else
                            lastR.push(last);*/
                        // if (this.lowerRange == 0)
                        //     firstR.push(lowerRange + 1);
                        // else
                        firstR.push(cumulativeData[currLevel][0].rowRange[0]);
                        lastR.push(cumulativeData[currLevel][cumulativeData[currLevel].length-1].rowRange[1]);
                    // }

                    queryObj.bookId = this.props.bookId;
                    queryObj.sheetName = this.state.sheetName;
                    queryObj.index = parseInt(formula_ls[0].attr_index) - 1;
                    queryObj.first = firstR;
                    queryObj.last = lastR;
                    queryObj.conditions = cond;
                    queryObj.values = value;

                    fetch(this.state.urlPrefix + '/api/' + 'getBrushColorList', {
                        method: "POST",
                        body: JSON.stringify(queryObj),
                        headers: {
                            'Content-Type': 'text/plain'
                        }
                    })
                        .then(response => response.json())
                        .then(data => {
                            if (data.status == "success") {
                                console.log(data.data);//#d4eafc

                                brushNLinkRows = data.data;

                            }
                            this.props.updateHighlight(hierarchicalColAttr, brushNLinkRows);

                        });

                }

                else //higlight hierarchical col
                    this.props.updateHighlight(hierarchicalColAttr, brushNLinkRows);
            })
    }

    isPointFormula(formula) {
        let str = formula.split("(")[0];
        var pointFunc = ["MIN", "MAX", "MEDIAN", "MODE", "RANK", "SMALL", "LARGE", "COUNTIF", "SUMIF"];
        console.log(str)
        if (pointFunc.includes(str))
            return true;
        return false;
    }

    chartRenderer(instance, td, row, col, prop, value, cellProperties) {
        let currLevel = this.state.currLevel;
        let navAggRawData = this.state.navAggRawData;
        let wrapperWidth = this.state.wrapperWidth;
        let wrapperHeight = this.state.wrapperHeight;
        let cumulativeData = this.state.cumulativeData;
        let nav = this.hotTableComponent.current.hotInstance;
        let childHash = this.state.childHash;
        //console.log(childHash)
        let colOffset = (currLevel == 0) ? 1 : 2;
        ////console.log(currLevel);
        //console.log(row);
        //console.log(col);
        //console.log(navAggRawData);
        if (navAggRawData == null || navAggRawData[col - colOffset] == null) return;
        if (navAggRawData[col - colOffset][row].chartType == 0) {
            let tempString = "chartdiv" + row + col;
            td.innerHTML = "<div id=" + tempString + " ></div>";

            let chartData = navAggRawData[col - colOffset][row]['chartData'];
            let distribution = [];

            distribution.push({min: chartData[0], max: chartData[1]});

            let min = navAggRawData[col - colOffset][0]['value'];
            let max = navAggRawData[col - colOffset][0]['value'];
            for (let i = 0; i < navAggRawData[col - colOffset].length; i++) {
                if (navAggRawData[col - colOffset][i]['value'] < min) {
                    min = navAggRawData[col - colOffset][i]['value'];
                } else if (navAggRawData[col - colOffset][i]['value'] > max) {
                    max = navAggRawData[col - colOffset][i]['value'];
                }
            }

            let margin = {top: 20, right: 40, bottom: 18, left: 35};
            // here, we want the full chart to be 700x200, so we determine
            // the width and height by subtracting the margins from those values
            let fullWidth = wrapperWidth * 0.14;
            let fullHeight = nav.getRowHeight(row);

            // the width and height values will be used in the ranges of our scales
            let width = fullWidth - margin.right - margin.left;
            let height = fullHeight;
            let svg = d3.select('#' + tempString)
                .append('svg')
                .attr('width', fullWidth)
                .attr('height', fullHeight)
                // this g is where the bar chart will be drawn
                .append('g')
                // translate it to leave room for the left and top margins
                .attr('transform', 'translate(' + margin.left + ',0)');

            svg.append("rect")
                .attr("x", width + margin.right / 4)
                .attr("y", 0)
                .attr("width", margin.right)
                .attr("height", fullHeight)
                .attr("fill", d3.interpolateGreens(
                    ((value - min) / (max - min)) * 0.85 + 0.15));

            svg.append("text")
                .attr("x", (width / 2))
                .attr("y", (margin.top / 2))
                .attr("text-anchor", "middle")
                .style("font-size", "10px")
                .style("font-weight", "bold")
                .text(value);

            // draw the rectangle
            //'#0099ff'
            let fraction = 3; // what fraction of container is the valuebar
            let valueBarHeight = height / fraction;
            let valueBar = svg.append("rect")
                .attr("x", 0 - margin.left / 2)
                .attr("y", height / 2 - valueBarHeight / 2)
                .attr("width", width)
                .attr("height", valueBarHeight)
                .attr("fill", '#0099ff');
            // add value rectangle
            let xScale = d3.scaleLinear()
                .domain([distribution[0].min, distribution[0].max])
                .range([0 - margin.left / 2, width - margin.left / 2])
                .nice();

            var highlightBar = svg.append("rect")
                .attr("x", xScale(value))
                .attr("y", height / 2 - valueBarHeight)
                .attr("width", 2)
                .attr("height", 2 * valueBarHeight)
                .attr("fill", '#000000');
            // add min, max, value text
            svg.append("text")
                .attr("x", xScale(distribution[0].min) - 5)
                .attr("y", function () {
                    return height / 2 + valueBarHeight / 2 + 8;
                })
                .attr("text-anchor", "middle")
                .style("font-size", "10px")
                .style("font-weight", "bold")
                .text(distribution[0].min);
            svg.append("text")
                .attr("x", xScale(distribution[0].max) + 5)
                .attr("y", function () {
                    return height / 2 + valueBarHeight / 2 + 8;
                })
                .attr("text-anchor", "middle")
                .style("font-size", "10px")
                .style("font-weight", "bold")
                .text(distribution[0].max);
            svg.append("text")
                .attr("x", xScale(value))
                .attr("y", function () {
                    return height / 2 + valueBarHeight + 10;
                })
                .attr("text-anchor", "middle")
                .style("font-size", "10px")
                .style("font-weight", "bold")
                .text(value);
        } else if (navAggRawData[col - colOffset][row].chartType == 1) {
            //console.log("in chart rendered");
            let tempString = "chartdiv" + row + col;
            td.innerHTML = "<div id=" + tempString + " ></div>";

            let chartData = navAggRawData[col - colOffset][row]['chartData'];
            let distribution = [];

            for (let i = 0; i < chartData.counts.length; i++) {
                let boundstr = chartData.bins[i] + " - " + chartData.bins[i + 1];
                distribution.push({boundary: boundstr, count: chartData.counts[i]});
            }
            let min = navAggRawData[col - colOffset][0]['value'];
            let max = navAggRawData[col - colOffset][0]['value'];
            for (let i = 0; i < navAggRawData[col - colOffset].length; i++) {
                if (navAggRawData[col - colOffset][i]['value'] < min) {
                    min = navAggRawData[col - colOffset][i]['value'];
                } else if (navAggRawData[col - colOffset][i]['value'] > max) {
                    max = navAggRawData[col - colOffset][i]['value'];
                }
            }

            let special = navAggRawData[col - colOffset][row]['valueIndex'];

            var margin = {top: 20, right: 25, bottom: 18, left: 35};
            // here, we want the full chart to be 700x200, so we determine
            // the width and height by subtracting the margins from those values
            var fullWidth = wrapperWidth * 0.14;
            var fullHeight = nav.getRowHeight(row);

            // the width and height values will be used in the ranges of our scales
            var width = fullWidth - margin.right - margin.left;
            var height = fullHeight - margin.top - margin.bottom;
            var svg = d3.select('#' + tempString)
                .append('svg')
                .attr('width', fullWidth)
                .attr('height', fullHeight)
                // this g is where the bar chart will be drawn
                .append('g')
                // translate it to leave room for the left and top margins
                .attr('transform',
                    'translate(' + margin.left + ',' + margin.top + ')');

            svg.append("rect")
                .attr("x", width + margin.right / 4)
                .attr("y", 0 - margin.top)
                .attr("width", margin.right)
                .attr("height", fullHeight)
                .attr("fill", d3.interpolateGreens(
                    ((value - min) / (max - min)) * 0.85 + 0.15));

            svg.append("text")
                .attr("x", (width / 2))
                .attr("y", 0 - (margin.top / 2))
                .attr("text-anchor", "middle")
                .style("font-size", "10px")
                .style("font-weight", "bold")
                .text(value);

            let xScale =
                d3.scaleLinear()
                    .domain([
                        chartData.bins[0], chartData.bins[chartData.bins.length - 1]
                    ])
                    .range([0, width]);

            // y value determined by temp
            var maxValue = d3.max(distribution, function (d) {
                return d.count;
            });
            var yScale =
                d3.scaleLinear().domain([0, maxValue]).range([height, 0]).nice();

            var xAxis = d3.axisBottom(xScale)
            //.ticks(6,'s');
                .tickValues(chartData.bins);

            var yAxis = d3.axisLeft(yScale);
            yAxis.ticks(5);

            var barHolder = svg.append('g').classed('bar-holder', true);

            var tooltip =
                d3.select('#' + tempString).append("div").attr("class", "toolTip");

            // draw the bars
            var bars =
                barHolder.selectAll('rect.bar')
                    .data(distribution)
                    .enter()
                    .append('rect')
                    .classed('bar', true)
                    .attr('x',
                        function (d, i) {
                            // the x value is determined using the
                            // month of the datum
                            return 1 + width / (chartData.counts.length) * i;
                        })
                    .attr('width', width / (chartData.counts.length))
                    .attr('y', function (d) {
                        return yScale(d.count);
                    })
                    .attr('fill',
                        function (d, i) {
                            if (i == special) {
                                return '#ffA500';
                            } else {
                                return '#0099ff';
                            }
                        })
                    .attr('height',
                        function (d) {
                            // the bar's height should align it with the base of the
                            // chart (y=0)
                            return height - yScale(d.count);
                        })
                    .on("mouseover",
                        function (d) {
                            tooltip.style("left", d3.event.pageX - 20 + "px")
                                .style("top", d3.event.pageY - 30 + "px")
                                .style("display", "inline-block")
                                .html((d.count));
                        })
                    .on("mouseout", function (d) {
                        tooltip.style("display", "none");
                    });

            // draw the axes
            svg.append('g')
                .classed('x axis', true)
                .attr('transform', 'translate(0,' + height + ')')
                .call(xAxis);

            var yAxisEle = svg.append('g').classed('y axis', true).call(yAxis);

            // add a label to the yAxis
            svg.append('text')
                .attr('transform', 'rotate(-90)')
                .attr("y", 0 - margin.left)
                .attr("x", 0 - (height / 2))
                .style('text-anchor', 'middle')
                .style('fill', 'black')
                .attr('dy', '1em')
                .style('font-size', 10)
                .text('Count');

        } else if (navAggRawData[col - colOffset][row].chartType == 2) {
            //console.log("in nav chart renderer");
            let tempString = "chartdiv" + row + col;
            td.innerHTML = "<div id=" + tempString + " ></div>";

            let chartData = navAggRawData[col - colOffset][row]['chartData'];
            let distribution = [];

            for (let i = 0; i < chartData.counts.length; i++) {
                let boundstr = chartData.bins[i] + " - " + chartData.bins[i + 1];
                distribution.push({boundary: boundstr, count: chartData.counts[i]});
            }
            let min = navAggRawData[col - colOffset][0]['value'];
            let max = navAggRawData[col - colOffset][0]['value'];
            for (let i = 0; i < navAggRawData[col - colOffset].length; i++) {
                if (navAggRawData[col - colOffset][i]['value'] < min) {
                    min = navAggRawData[col - colOffset][i]['value'];
                } else if (navAggRawData[col - colOffset][i]['value'] > max) {
                    max = navAggRawData[col - colOffset][i]['value'];
                }
            }

            let avg = chartData.AVERAGE;
            let stdev = chartData.STDEV;

            let showSquare = 1;
            if (navAggRawData[col - colOffset][row]['formula'].includes("STDEV"))
                showSquare = 0;

            var margin = {top: 20, right: 25, bottom: 18, left: 35};
            // here, we want the full chart to be 700x200, so we determine
            // the width and height by subtracting the margins from those values
            var fullWidth = wrapperWidth * 0.14;
            var fullHeight = nav.getRowHeight(row);

            // the width and height values will be used in the ranges of our scales
            var width = fullWidth - margin.right - margin.left;
            var height = fullHeight - margin.top - margin.bottom;
            var svg = d3.select('#' + tempString)
                .append('svg')
                .attr('width', fullWidth)
                .attr('height', fullHeight)
                // this g is where the bar chart will be drawn
                .append('g')
                // translate it to leave room for the left and top margins
                .attr('transform',
                    'translate(' + margin.left + ',' + margin.top + ')');

            svg.append("rect")
                .attr("x", width + margin.right / 4)
                .attr("y", 0 - margin.top)
                .attr("width", margin.right)
                .attr("height", fullHeight)
                .attr("fill", d3.interpolateGreens(
                    ((value - min) / (max - min)) * 0.85 + 0.15));

            svg.append("text")
                .attr("x", (width / 2))
                .attr("y", 0 - (margin.top / 2))
                .attr("text-anchor", "middle")
                .style("font-size", "10px")
                .style("font-weight", "bold")
                .text(function () {
                    if (showSquare == 1)
                        return "\u03c3" +
                            "^2: " + value;
                    else
                        return "\u03c3" +
                            ": " + value;
                });

            let xScale =
                d3.scaleLinear()
                    .domain([
                        chartData.bins[0], chartData.bins[chartData.bins.length - 1]
                    ])
                    .range([0, width]);

            // y value determined by temp
            var maxValue = d3.max(distribution, function (d) {
                return d.count;
            });
            var yScale =
                d3.scaleLinear().domain([0, maxValue]).range([height, 0]).nice();

            var xAxis = d3.axisBottom(xScale)
            //.ticks(6,'s');
                .tickValues(chartData.bins);

            var yAxis = d3.axisLeft(yScale);
            yAxis.ticks(5);

            var barHolder = svg.append('g').classed('bar-holder', true);

            var tooltip =
                d3.select('#' + tempString).append("div").attr("class", "toolTip");

            // draw the bars
            var bars =
                barHolder.selectAll('rect.bar')
                    .data(distribution)
                    .enter()
                    .append('rect')
                    .classed('bar', true)
                    .attr('x',
                        function (d, i) {
                            // the x value is determined using the
                            // month of the datum
                            return 1 + width / (chartData.counts.length) * i;
                        })
                    .attr('width', width / (chartData.counts.length))
                    .attr('y', function (d) {
                        return yScale(d.count);
                    })
                    .attr('fill', '#0099ff')
                    .attr('height',
                        function (d) {
                            // the bar's height should align it with the base of the
                            // chart (y=0)
                            return height - yScale(d.count);
                        })
                    .on("mouseover",
                        function (d) {
                            tooltip.style("left", d3.event.pageX - 20 + "px")
                                .style("top", d3.event.pageY - 30 + "px")
                                .style("display", "inline-block")
                                .html((d.count));
                        })
                    .on("mouseout", function (d) {
                        tooltip.style("display", "none");
                    });

            // add the average line
            svg.append("line")
                .attr("x1", xScale(avg)) //<<== change your code here
                .attr("y1", 0)
                .attr("x2", xScale(avg)) //<<== and here
                .attr("y2", height)
                .style("stroke", '#000000')
                .style("stroke-width", 2);

            // add \mu text
            svg.append("text")
                .attr("x", xScale(avg) - 5)
                .attr("y", margin.top / 4)
                .attr("text-anchor", "middle")
                .style("font-size", "10px")
                .style("font-weight", "bold")
                .text("\u03bc");

            // add the stdev line
            svg.append("line")
                .attr("x1", xScale(avg)) //<<== change your code here
                .attr("y1", margin.top / 2)
                .attr("x2", xScale(avg + stdev)) //<<== and here
                .attr("y2", margin.top / 2)
                .style("stroke", '#000000')
                .style("stroke-width", 2);

            // add \sigma text
            svg.append("text")
                .attr("x", (xScale(avg) + xScale(avg + stdev)) / 2)
                .attr("y", margin.top / 4)
                .attr("text-anchor", "middle")
                .style("font-size", "10px")
                .style("font-weight", "bold")
                .text("\u03c3");
            // add the stdev rectangle
            svg.append("rect")
                .attr("x", xScale(avg)) //<<== change your code here
                .attr("y", margin.top / 2)
                .attr("width", xScale(avg + stdev) - xScale(avg)) //<<== and here
                .attr("height", height - margin.top / 2)
                .attr('fill', '#0099ff')
                .style("stroke", "red")
                .style("stroke-dasharray", ("3, 3"))
                .style("fill-opacity", 0.2);

            // draw the axes
            svg.append('g')
                .classed('x axis', true)
                .attr('transform', 'translate(0,' + height + ')')
                .call(xAxis);

            var yAxisEle = svg.append('g').classed('y axis', true).call(yAxis);

            // add a label to the yAxis
            svg.append('text')
                .attr('transform', 'rotate(-90)')
                .attr("y", 0 - margin.left)
                .attr("x", 0 - (height / 2))
                .style('text-anchor', 'middle')
                .style('fill', 'black')
                .attr('dy', '1em')
                .style('font-size', 10)
                .text('Count');

        } else if (navAggRawData[col - colOffset][row].chartType == 3) {
            let tempString = "chartdiv" + row + col;
            td.innerHTML = "<div id=" + tempString + " ></div>";

            let chartData = navAggRawData[col - colOffset][row]['chartData'];
            let distribution = [];

            for (let i = 0; i < chartData.counts.length; i++) {
                let boundstr = chartData.bins[i] + " - " + chartData.bins[i + 1];
                distribution.push({boundary: boundstr, count: chartData.counts[i]});
            }
            let min = navAggRawData[col - colOffset][0]['value'];
            let max = navAggRawData[col - colOffset][0]['value'];
            for (let i = 0; i < navAggRawData[col - colOffset].length; i++) {
                if (navAggRawData[col - colOffset][i]['value'] < min) {
                    min = navAggRawData[col - colOffset][i]['value'];
                } else if (navAggRawData[col - colOffset][i]['value'] > max) {
                    max = navAggRawData[col - colOffset][i]['value'];
                }
            }

            var margin = {top: 20, right: 25, bottom: 18, left: 35};
            // here, we want the full chart to be 700x200, so we determine
            // the width and height by subtracting the margins from those values
            var fullWidth = wrapperWidth * 0.14;
            var fullHeight = nav.getRowHeight(row);

            // the width and height values will be used in the ranges of our scales
            var width = fullWidth - margin.right - margin.left;
            var height = fullHeight - margin.top - margin.bottom;
            var svg = d3.select('#' + tempString)
                .append('svg')
                .attr('width', fullWidth)
                .attr('height', fullHeight)
                // this g is where the bar chart will be drawn
                .append('g')
                // translate it to leave room for the left and top margins
                .attr('transform',
                    'translate(' + margin.left + ',' + margin.top + ')');

            svg.append("rect")
                .attr("x", width + margin.right / 4)
                .attr("y", 0 - margin.top)
                .attr("width", margin.right)
                .attr("height", fullHeight)
                .attr("fill", d3.interpolateGreens(
                    ((value - min) / (max - min)) * 0.85 + 0.15));

            svg.append("text")
                .attr("x", (width / 2))
                .attr("y", 0 - (margin.top / 2))
                .attr("text-anchor", "middle")
                .style("font-size", "10px")
                .style("font-weight", "bold")
                .text(value);

            let xScale =
                d3.scaleLinear()
                    .domain([
                        chartData.bins[0], chartData.bins[chartData.bins.length - 1]
                    ])
                    .range([0, width]);

            // y value determined by temp
            var maxValue = d3.max(distribution, function (d) {
                return d.count;
            });
            var yScale =
                d3.scaleLinear().domain([0, maxValue]).range([height, 0]).nice();

            var xAxis = d3.axisBottom(xScale)
            //.ticks(6,'s');
                .tickValues(chartData.bins);

            var yAxis = d3.axisLeft(yScale);
            yAxis.ticks(5);

            var barHolder = svg.append('g').classed('bar-holder', true);

            var tooltip =
                d3.select('#' + tempString).append("div").attr("class", "toolTip");

            // draw the bars
            var bars =
                barHolder.selectAll('rect.bar')
                    .data(distribution)
                    .enter()
                    .append('rect')
                    .classed('bar', true)
                    .attr('x',
                        function (d, i) {
                            // the x value is determined using the
                            // month of the datum
                            return 1 + width / (chartData.counts.length) * i;
                        })
                    .attr('width', width / (chartData.counts.length))
                    .attr('y', function (d) {
                        return yScale(d.count);
                    })
                    .attr('fill', '#0099ff')
                    .attr('height',
                        function (d) {
                            // the bar's height should align it with the base of the
                            // chart (y=0)
                            return height - yScale(d.count);
                        })
                    .on("mouseover",
                        function (d) {
                            tooltip.style("left", d3.event.pageX - 20 + "px")
                                .style("top", d3.event.pageY - 30 + "px")
                                .style("display", "inline-block")
                                .html((d.count));
                        })
                    .on("mouseout", function (d) {
                        tooltip.style("display", "none");
                    });

            // draw the axes
            svg.append('g')
                .classed('x axis', true)
                .attr('transform', 'translate(0,' + height + ')')
                .call(xAxis);

            var yAxisEle = svg.append('g').classed('y axis', true).call(yAxis);

            // add a label to the yAxis
            svg.append('text')
                .attr('transform', 'rotate(-90)')
                .attr("y", 0 - margin.left)
                .attr("x", 0 - (height / 2))
                .style('text-anchor', 'middle')
                .style('fill', 'black')
                .attr('dy', '1em')
                .style('font-size', 10)
                .text('Count');

        } else if (navAggRawData[col - colOffset][row].chartType == 4) {

            let tempString = "chartdiv" + row + col;
            td.innerHTML = "<div id=" + tempString + " ></div>";

            let data = navAggRawData[col - colOffset][row];
            let chartData = data['chartData'];
            let distribution = [];

            for (let i = 0; i < chartData.counts.length; i++) {
                let boundstr = chartData.bins[i] + " - " + chartData.bins[i + 1];
                distribution.push({boundary: boundstr, count: chartData.counts[i]});
            }
            let min = navAggRawData[col - colOffset][0]['value'];
            let max = navAggRawData[col - colOffset][0]['value'];
            for (let i = 0; i < navAggRawData[col - colOffset].length; i++) {
                if (navAggRawData[col - colOffset][i]['value'] < min) {
                    min = navAggRawData[col - colOffset][i]['value'];
                } else if (navAggRawData[col - colOffset][i]['value'] > max) {
                    max = navAggRawData[col - colOffset][i]['value'];
                }
            }

            var margin = {top: 20, right: 25, bottom: 18, left: 35};
            // here, we want the full chart to be 700x200, so we determine
            // the width and height by subtracting the margins from those values
            var fullWidth = wrapperWidth * 0.14;
            var fullHeight = nav.getRowHeight(row);

            // the width and height values will be used in the ranges of our scales
            var width = fullWidth - margin.right - margin.left;
            var height = fullHeight - margin.top - margin.bottom;
            var svg = d3.select('#' + tempString)
                .append('svg')
                .attr('width', fullWidth)
                .attr('height', fullHeight)
                // this g is where the bar chart will be drawn
                .append('g')
                // translate it to leave room for the left and top margins
                .attr('transform',
                    'translate(' + margin.left + ',' + margin.top + ')');

            svg.append("rect")
                .attr("x", width + margin.right / 4)
                .attr("y", 0 - margin.top)
                .attr("width", margin.right)
                .attr("height", fullHeight)
                .attr("fill", d3.interpolateGreens(
                    ((value - min) / (max - min)) * 0.85 + 0.15));

            svg.append("text")
                .attr("x", (width / 2))
                .attr("y", 0 - (margin.top / 2))
                .attr("text-anchor", "middle")
                .style("font-size", "10px")
                .style("font-weight", "bold")
                .text(function () {
                    if (navAggRawData[col - colOffset][row].formula.includes("COUNTIF")) {
                        let percent = (value * 100.0) / cumulativeData[currLevel][row].value;
                        return value + " (" + percent.toFixed(2) + "%)";
                    }

                    return value;
                });

            let xScale =
                d3.scaleLinear()
                    .domain([
                        chartData.bins[0], chartData.bins[chartData.bins.length - 1]
                    ])
                    .range([0, width]);

            // y value determined by temp
            var maxValue = d3.max(distribution, function (d) {
                return d.count;
            });
            var yScale =
                d3.scaleLinear().domain([0, maxValue]).range([height, 0]).nice();

            var xAxis = d3.axisBottom(xScale)
            //.ticks(6,'s');
                .tickValues(chartData.bins);

            var yAxis = d3.axisLeft(yScale);
            yAxis.ticks(5);

            var barHolder = svg.append('g').classed('bar-holder', true);

            var tooltip =
                d3.select('#' + tempString).append("div").attr("class", "toolTip");

            // draw the bars
            var bars =
                barHolder.selectAll('rect.bar')
                    .data(distribution)
                    .enter()
                    .append('rect')
                    .classed('bar', true)
                    .attr('x',
                        function (d, i) {
                            // the x value is determined using the
                            // month of the datum
                            return 1 + width / (chartData.counts.length) * i;
                        })
                    .attr('width', width / (chartData.counts.length))
                    .attr('y', function (d) {
                        return yScale(d.count);
                    })
                    .attr('fill', '#0099ff')
                    .attr('height',
                        function (d) {
                            // the bar's height should align it with the base of the
                            // chart (y=0)
                            return height - yScale(d.count);
                        })
                    .on("mouseover",
                        function (d) {
                            tooltip.style("left", d3.event.pageX - 20 + "px")
                                .style("top", d3.event.pageY - 30 + "px")
                                .style("display", "inline-block")
                                .html((d.count));
                        })
                    .on("mouseout", function (d) {
                        tooltip.style("display", "none");
                    });

            // draw the axes
            svg.append('g')
                .classed('x axis', true)
                .attr('transform', 'translate(0,' + height + ')')
                .call(xAxis);

            var yAxisEle = svg.append('g').classed('y axis', true).call(yAxis);

            // add a label to the yAxis
            svg.append('text')
                .attr('transform', 'rotate(-90)')
                .attr("y", 0 - margin.left)
                .attr("x", 0 - (height / 2))
                .style('text-anchor', 'middle')
                .style('fill', 'black')
                .attr('dy', '1em')
                .style('font-size', 10)
                .text('Count');

            let pivotValue = data.pivotValue;
            svg.append("line")
                .attr("x1", xScale(pivotValue)) //<<== change your code here
                .attr("y1", 0 - margin.top / 4)
                .attr("x2", xScale(pivotValue)) //<<== and here
                .attr("y2", height + margin.top / 4)
                .style("stroke", '#000000')
                .style("stroke-dasharray", ("3, 3"))
                .style("stroke-width", 2);

            let dir = data.expandDirection;
            if (dir != 0) {
                // add the rectangle
                svg.append("rect")
                    .attr("x",
                        function () {
                            if (dir == 1)
                                return 0;
                            return xScale(pivotValue);
                        }) //<<== change your code here
                    .attr("y", yScale(maxValue))
                    .attr("width",
                        function () {
                            if (dir == 1)
                                return xScale(pivotValue) - xScale(chartData.bins[0]);
                            return xScale(chartData.bins[chartData.bins.length - 1]) -
                                xScale(pivotValue);
                        }) //<<== and here
                    .attr("height", height - yScale(maxValue))
                    .attr('fill', '#ffffff')
                    .style("stroke", "black")
                    //.style("stroke-dasharray", ("3, 3"))
                    .style("fill-opacity", 0.7);
            }

        } else {
            // Handsontable.renderers.TextRenderer.apply(this, arguments);
            // td.className = "htCenter htMiddle";

            let tempString = "chartdiv" + row + col;
            td.innerHTML = "<div id=" + tempString + " ></div>";
            let data = navAggRawData[col - colOffset][row];
            let min = navAggRawData[col - colOffset][0]['value'];
            let max = navAggRawData[col - colOffset][0]['value'];
            for (let i = 0; i < navAggRawData[col - colOffset].length; i++) {
                if (navAggRawData[col - colOffset][i]['value'] < min) {
                    min = navAggRawData[col - colOffset][i]['value'];
                } else if (navAggRawData[col - colOffset][i]['value'] > max) {
                    max = navAggRawData[col - colOffset][i]['value'];
                }
            }
            var margin = {top: 20, right: 30, bottom: 0, left: -20};
            var fullHeight = (wrapperHeight * 0.95 / cumulativeData[currLevel].length > 90)
                ? wrapperHeight * 0.95 / cumulativeData[currLevel].length - 10 : 80;
            if (childHash.has(row)) {
                let result = childHash.get(row);
                let number = result.length;
                fullHeight += 10;
                if (number > 6) {
                    fullHeight += (number - 6) * 5;
                }
            }
            var fullWidth = wrapperWidth * 0.14;
            ////console.log("row: " + row + " " + fullHeight+" "+ wrapperHeight+" "+ fullWidth+" "+wrapperWidth);

            // the width and height values will be used in the ranges of our scales
            var width = fullWidth - margin.right - margin.left;
            var height = fullHeight - margin.top - margin.bottom;
            var svg = d3.select('#' + tempString)
                .append('svg')
                .attr('width', fullWidth)
                .attr('height', fullHeight)
                // this g is where the bar chart will be drawn
                .append('g')
                // translate it to leave room for the left and top margins
                .attr('transform',
                    'translate(' + margin.left + ',' + margin.top + ')');
            /*svg.append("rect")
                .attr("x", width)
                .attr("y", 0 - margin.top)
                .attr("width", margin.right)
                .attr("height", fullHeight + 10)
                .attr("fill", d3.interpolateGreens(
                    ((value - min) / (max - min)) * 0.85 + 0.15));*/
            svg.append("rect")
                .attr("x", 0)
                .attr("y", 0 - margin.top)
                .attr("width", fullWidth * (value) / max)
                .attr("height", fullHeight)
                .attr("fill", '#B2EEB4');

            svg.append("text")
                .attr("x", (width / 2))
                .attr("y", height / 2 + margin.top / 2)
                .attr("text-anchor", "middle")
                .style("font-size", "20px")
                .style("font-weight", "bold")
                .text(function () {
                    if (navAggRawData[col - colOffset][row].formula.includes("COUNTIF")) {
                        let percent = (value * 100.0) / cumulativeData[currLevel][row].value;
                        return value + " (" + percent.toFixed(2) + "%)";
                    }
                    return value;
                });

        }

        td.style.background = '#FAF2ED';
        return td;
    }

    brushNlink(firstRow, lastRow) {
        //console.log("brush and link");

        let path = this.computePath();

        // console.log("path: "+path);


        let currentFocus = this.state.cumulativeData[this.state.currLevel];
        if (currentFocus == undefined)
            return;

        //console.log(currentFocus);
        let lastElement = currentFocus[currentFocus.length - 1];
        let firstElement = currentFocus[0];
        // console.log(lastElement)
        let endRow = lastElement.rowRange[1];
        let startRow = firstElement.rowRange[0];

        //console.log("endRow: " + endRow + ", firstRow: " + firstRow);
        //console.log("startRow: " + startRow + ", lastRow: " + lastRow);
        this.firstRow = firstRow;
        this.lastRow = lastRow;
        if (startRow > lastRow) {
            this.jumpToFocus(this.state.prevPath);
            // this.updateNavCellFocus(firstRow, lastRow);
        }
        else if (endRow < firstRow) {
            this.jumpToFocus(this.state.nextPath);
            // this.updateNavCellFocus(firstRow, lastRow);
        }
        else {
            this.updateNavCellFocus(firstRow, lastRow);
        }

        //updateSScolor(firstRow,lastRow);

    }

    updateBarChartFocus(firstRow, lastRow) {
        //console.log(childHash);
        let newSelectedBars = [];
        for (let selI = 0; selI < this.state.cumulativeData[this.state.currLevel].length; selI++) {
            if (this.state.childHash.get(selI) === undefined)
                continue;
            let newSelectedBar = [];
            //console.log(childHash.get(selI));
            for (let selJ = 0; selJ < this.state.childHash.get(selI).length; selJ++) {
                let lower = this.state.childHash.get(selI)[selJ].rowRange[0];
                let upper = this.state.childHash.get(selI)[selJ].rowRange[1];

                //console.log("lowerRange,upperRange", lower, upper);
                if (firstRow > upper)
                    continue;
                if (lastRow < lower)
                    break;
                newSelectedBar.push(selJ);
            }

            if (newSelectedBar.length > 0) {
                let barObj = {};
                barObj.cell = selI;
                barObj.bars = newSelectedBar;
                newSelectedBars.push(barObj);
            }
        }

        //console.log("newselectedBars");
        //console.log(newSelectedBars);
        if (newSelectedBars.length === 1 && this.state.selectedBars.length === 1 && newSelectedBars[0].cell === this.state.selectedBars[0].cell && newSelectedBars[0].bars.length === this.state.selectedBars[0].bars.length) {
            for (let selI = 0; selI < newSelectedBars[0].bars.length; selI++) {
                if (newSelectedBars[0].bars[selI] != this.state.selectedBars[0].bars[selI]) {
                    this.setState({
                        selectedBars: newSelectedBars,
                    });
                    return;
                }
            }
            return;
        }

        this.setState({
            selectedBars: newSelectedBars,
        });
        return;
    }

    updateNavCellFocus(firstRow, lastRow) {

        //console.log("firstRow,lastRow:", firstRow, lastRow);

        let currentFirstRow = firstRow;
        let currentLastRow = lastRow;

        let newSelectedChild = [];
        for (let selI = 0; selI < this.state.cumulativeData[this.state.currLevel].length; selI++) {
            let lower = this.state.cumulativeData[this.state.currLevel][selI].rowRange[0];
            let upper = this.state.cumulativeData[this.state.currLevel][selI].rowRange[1];

            //console.log("lowerRange,upperRange",lower,upper);
            if (firstRow > upper)
                continue;
            if (lastRow < lower)
                break;
            newSelectedChild.push(selI);
        }

        if (newSelectedChild.length == 1) {
          //  console.log("line1954 newselectedchild")
          //  console.log(newSelectedChild)
            if (this.state.selectedChild.length === 0 || this.state.selectedChild.length > 1 || this.state.selectedChild[0] !== newSelectedChild[0]) {
                this.hotTableComponent.current.hotInstance.deselectCell();
                this.setState({
                    currentFirstRow: currentFirstRow,
                    currentLastRow: currentLastRow,
                    selectedChild: newSelectedChild,
                })
                this.updateBarChartFocus(firstRow, lastRow);
            }
            else {
                this.updateBarChartFocus(firstRow, lastRow);
            }
        }
        else if (newSelectedChild.length > 1) {
        //    console.log("line1970 newselectedchild")
        //    console.log(newSelectedChild)
            this.hotTableComponent.current.hotInstance.deselectCell();
            this.setState({
                selectedChild: newSelectedChild,
            })
            this.updateBarChartFocus(firstRow, lastRow);
        }
    }

    jumpToFocus(path) {

        let childHash = new Map();
      //  console.log("nextPath:" + path);
        let path_str = "";
        let levelList = [];
        for (let i = 0; i < path.length; i++) {
            if (path.length != 0) {
                levelList[i] = parseInt(path[i]);
                if (i == 0)
                    path_str += path[i];
                else if (i < path.length - 1)
                    path_str += "," + path[i];
            }
        }
        let selectedChild = [];
        let selectedBars = [];
        // let sortChild_ls = [];

        let queryData = {};


        queryData.bookId = this.props.bookId;
        queryData.sheetName = this.state.sheetName;
        queryData.path = path_str;

      //  console.log("queryData:");
     //   console.log(queryData);
        fetch(this.state.urlPrefix + '/api/' + 'getChildren', {
            method: "POST",
            body: JSON.stringify(queryData),
            headers: {
                'Content-Type': 'text/plain'
            }
        }).then(response => response.json())
            .then(data => {
                if (data.status == "success") {
                    var result = data.data;
                    console.log(result);
                    let currData = result.buckets;

                    let prevPath = result.prev.path;
                    let nextPath = result.later.path;
                    let breadcrumb_ls = result.breadCrumb;
                    let currLevel = breadcrumb_ls.length;
                    let numChild = currData.length;
                    let viewData = new Array(numChild);
                    let colHeader = this.state.colHeader;
                    let cumulativeData = this.state.cumulativeData;
                    if (currLevel == 0) {
                        colHeader.splice(1, 0, "")
                    }
                  //  console.log(result);
                  //  console.log("currLevel: " + currLevel);
                    let mergeCellInfo = [];
                    if (currData.length != 0 && breadcrumb_ls.length != 0) {
                        for (let i = 0; i < currData.length; i++) {
                            childHash.set(i, currData[i].children);
                        }
                        mergeCellInfo.push({row: 0, col: 0, rowspan: currData.length, colspan: 1});
                        for (let i = 0; i < currData.length; i++) {
                            if (i == 0) {
                                viewData[i] = [breadcrumb_ls[breadcrumb_ls.length - 1]];
                            } else {
                                viewData[i] = [""];
                            }
                        }

                        cumulativeData.pop();
                        cumulativeData.push(currData);

                        for (let i = 0; i < currData.length; i++) {
                            //double layer
                            viewData[i][1] = cumulativeData[currLevel][i].name;

                        }

                        // cumulativeDataSize += currData.length;
                    }
                    else if (currData.length != 0 && breadcrumb_ls.length == 0) {
                        for (let i = 0; i < currData.length; i++) {
                            childHash.set(i, currData[i].children);
                        }
                        //colHeader.splice(1, 1);
                        cumulativeData = [];
                        cumulativeData.push(currData);
                        for (let i = 0; i < numChild; i++) {
                            viewData[i] = [currData[i].name];
                        }
                        // cumulativeDataSize += currData.length;
                    }
                    else {
                        path.splice(-1, 1);
                        this.jumpToFocus(path);
                        return;
                    }

                    // let columWidth = [];
                    // if (currLevel >= 1) {
                    //     //columWidth = [50];
                    // } else {
                    //     columWidth = 200;
                    // }
                    this.setState({
                        currData: result.buckets,
                        childHash: childHash,
                        viewData: viewData,
                        cumulativeData: cumulativeData,
                        currLevel: currLevel,
                        mergeCellInfo: mergeCellInfo,
                        colHeader: colHeader,
                        prevPath: prevPath,
                        nextPath: nextPath,
                        levelList: levelList,
                    });

                    let currState = this.state;
                    this.hotTableComponent.current.hotInstance.deselectCell();

                    this.hotTableComponent.current.hotInstance.updateSettings({
                        data: viewData,
                        rowHeights: (currState.wrapperHeight * 0.95 / currState.currData.length > 90)
                            ? currState.wrapperHeight * 0.95 / currState.currData.length
                            : 90,
                        mergeCells: mergeCellInfo,
                    });

                    if (currState.hieraOpen) {
                        this.submitHierForm(currState.aggregateData.formula_ls);
                    }
                    this.props.updateBreadcrumb(result.breadCrumb, path_str); // calculate breadcrumb
                    this.updateNavCellFocus(this.firstRow, this.lastRow);
                }
            })
    }


    computePath() {
        let childlist = "";
        let currState = this.state;
        for (let i = 0; i < currState.levelList.length - 1; i++) {
            childlist += currState.levelList[i] + ",";
        }
        if (currState.levelList.length > 0) {
            childlist += currState.levelList[currState.levelList.length - 1];
        }
        return childlist;
    }


    render() {
        if (this.state.navOpen) {
            return (
                <div id="hot-app">
                    <HotTable ref={this.hotTableComponent}/>

                </div>
            );
        } else {
            return null;
        }
    }
}

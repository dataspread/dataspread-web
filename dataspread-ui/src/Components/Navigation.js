import React, {Component} from 'react'
import {HotTable} from '@handsontable/react';
import 'handsontable/dist/handsontable.full.css';
import './Navigation.css';
import * as d3 from "d3";

export default class Navigation extends Component {

    constructor(props) {
        super(props);
        console.log(this);
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
            childHash: new Map(),
        }

        this.hotTableComponent = React.createRef();
        this.navCellRenderer = this.navCellRenderer.bind(this);
        this.afterSelectionHandleer = this.afterSelectionHandleer.bind(this);

    }

    componentDidMount() {
        fetch('http://localhost:9999' + '/api/startNav/djjrorgnt/Sheet1/1')
            .then(response => response.json())
            .then(data => {
                console.log(data);
                this.setState({
                    currData: data.data,
                    cumulativeData: [...this.state.cumulativeData, data.data]
                });
                console.log(this.state.cumulativeData);

                let length = data.data.length;
                let viewData = new Array(data.data.length);
                for (let i = 0; i <length; i++) {
                    viewData[i] = [""];
                    viewData[i][0] = this.state.cumulativeData[0][i].name;
                }
                let childHash = new Map();
                for (let i = 0; i < length; i++) {
                    childHash.set(i, data.data[i].children);
                }
                console.log(viewData);
                this.setState({
                    viewData: viewData,
                    childHash: childHash,
                })
                var currentState = this.state;
                let self = this;
                this.hotTableComponent.current.hotInstance.updateSettings({
                    data: currentState.viewData,
                    minCols: 1,
                    readOnly: true,
                    rowHeights: (currentState.wrapperHeight * 0.90 / currentState.currData.length > 90)
                        ? currentState.wrapperHeight * 0.90 / currentState.currData.length
                        : 90,
                    width: currentState.wrapperWidth * 0.19,
                    height: currentState.wrapperHeight * 0.90,
                    rowHeaderWidth: 0,
                    rowHeaders: true,
                    colWidths: function (col) {
                        if (currentState.currLevel == 0) {
                            if (col == 0) {
                                return currentState.wrapperWidth * 0.18;
                            } else {
                                return currentState.wrapperWidth * 0.15;
                            }
                        } else {
                            if (col == 0) {
                                return currentState.wrapperWidth * 0.04;
                            } else if (col == 1 && currentState.alltext) {
                                return currentState.wrapperWidth * 0.08;
                            } else {
                                return currentState.wrapperWidth * 0.15;
                            }
                        }

                    },
                    colHeaders: function (col) {
                        if (col < currentState.colHeader.length) {
                            if (currentState.currLevel == 0) {
                                switch (col) {
                                    case 0:
                                        return currentState.colHeader[0];
                                    default:
                                        let check =
                                            currentState.aggregateData.formula_ls[col - 1].getChart ? "checked" : "";
                                        return currentState.colHeader[col] + "<span id='colClose' >x</span>" +
                                            "<label class=\"switch\">" +
                                            "  <input type=\"checkbox\"" + check + ">" +
                                            "  <span class=\"slider round\"></span>" +
                                            "</label>";
                                }
                            } else {
                                switch (col) {
                                    case 0:
                                        return currentState.colHeader[0];
                                    case 1:
                                        return currentState.colHeader[1];
                                    default:
                                        let check =
                                            currentState.aggregateData.formula_ls[col - 2].getChart ? "checked" : "";
                                        return currentState.colHeader[col] + "<span id='colClose'>x</span>" +
                                            "<label class=\"switch\">" +
                                            "  <input type=\"checkbox\"" + check + ">" +
                                            "  <span class=\"slider round\"></span>" +
                                            "</label>";
                                }
                            }
                        }
                    },
                    stretchH: 'all',
                    contextMenu: false,
                    outsideClickDeselects: false,
                    className: "wrap",
                    search: true,
                    sortIndicator: true,
                    manualColumnResize: true,
                    mergeCells: currentState.mergeCellInfo,
                    beforeOnCellMouseDown: function (e, coords, element) {
                        // $("#formulaBar").val("");

                        let topLevel = (currentState.currLevel == 0 && coords.col != 0)
                        let otherLevel = (currentState.currLevel > 0 && coords.col != 1)
                        // if (topLevel && coords.row >= 0) {
                        //     $("#formulaBar").val("=" + navRawFormula[coords.row][coords.col - 1]);
                        // }
                        // else if (currLevel > 0 && coords.row >= 0 && coords.col >= 2) {
                        //     $("#formulaBar").val("=" + navRawFormula[coords.row][coords.col - 2]);
                        // }
                        console.log(e);
                        //|| zoomming
                        if (topLevel || otherLevel  ||
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
                            self.getAggregateValue();
                        }
                    },
                    cells: function (row, column, prop) {
                        let cellMeta = {}
                        if (currentState.currLevel == 0) {
                            if (column == 0) {
                                cellMeta.renderer = self.navCellRenderer;
                            } else {
                                cellMeta.renderer = self.chartRenderer;
                            }
                        }
                        else {
                            if (column <= 1) {
                                cellMeta.renderer = self.navCellRenderer;
                            } else {
                                cellMeta.renderer = self.chartRenderer;
                            }
                        }
                        return cellMeta;
                    },
                    afterSelection: self.afterSelectionHandleer,
                })
                // })
            })

    }
    afterSelectionHandleer (r, c, r2, c2, preventScrolling,
              selectionLayerLevel) {
        // setting if prevent scrolling after selection
        console.log(this);
        console.log(this.props.grid)
        console.log(r)
        console.log(c)
        console.log(r2)
        console.log(c2)
        console.log(selectionLayerLevel)
        if (this.state.cumulativeData[this.state.currLevel][r] != undefined) {
            let selectedChild = [];
            selectedChild.push(r);
            this.setState({selectedChild:selectedChild});
            // selectedBars = [];
            // let barObj = {};
            // barObj.cell = r;
            // barObj.bars = [0];
            // selectedBars.push(barObj);

            let lowerRange = this.state.cumulativeData[this.state.currLevel][r].rowRange[0];
            this.props.grid.grid.scrollToCell ({ columnIndex: 0, rowIndex: lowerRange + 27});
            // let upperRange = cumulativeData[currLevel][r].rowRange[1];
            // updateData(cumulativeData[currLevel][r].rowRange[0], 0,
            //     cumulativeData[currLevel][r].rowRange[1], 15, true);
            // updataHighlight();
            // nav.render();
        }
    }
    navCellRenderer(instance, td, row, col, prop, value, cellProperties) {
        console.log(this);
        let tempString = "<div><span>" + value + "</span>";
        let currentState = this.state;

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
                    //computeCellChart(chartString, row);
                    return;
                }
            } else {
                tempString += "<p>Total Rows: " + targetCell.value + "<br> Start Row No: " + (targetCell.rowRange[0]+1) + "<br> End Row No: " + (targetCell.rowRange[1]+1) + "</p>";
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
                        //computeCellChart(chartString, row);
                        return;
                    }
                } else {
                    tempString += "<p>Total Rows: " + targetCell.value + "<br> Start Row No: " + (targetCell.rowRange[0]+1) + "<br> End Row No: " + (targetCell.rowRange[1]+1) + "</p>";
                    td.innerHTML = tempString + "</div>";
                }
            }

        }


    }

    render() {
        return (
            <div id="hot-app">
                <HotTable ref={this.hotTableComponent}/>
                {/*settings={this.hotSettings}*/}
            </div>
        );
    }
}

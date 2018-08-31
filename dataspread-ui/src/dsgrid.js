import React, {Component} from 'react';
import { Input, Button } from 'semantic-ui-react'
import ReactResumableJs from 'react-resumable-js'
import {AutoSizer, Grid, ScrollSync} from './react-virtualized'

import Cell  from './cell';
import 'react-datasheet/lib/react-datasheet.css';
import LRUCache from "lru-cache";

export default class DSGrid extends Component {
    toColumnName(num) {
        for (var ret = '', a = 1, b = 26; (num -= a) >= 0; a = b, b *= 26) {
            ret = String.fromCharCode(parseInt((num % b) / a) + 65) + ret;
        }
        return ret;
    }

    constructor(props) {
        super(props);
        this.state = {
            rows: 100000000,
            bookName: '',
            sheetName: '',
            columns: 500,
            version: 0
        }
        this.rowHeight = 32;
        this.columnWidth = 150;
        this.dataCache = new LRUCache(1000);

        this.urlPrefix="http://localhost:8080"; // Only for testing.

        this.fetchSize = 100;
        this._onSectionRendered = this._onSectionRendered.bind(this);
        this._cellRenderer = this._cellRenderer.bind(this);
        this._loadMoreRows = this._loadMoreRows.bind(this);
        this._handleEvent = this._handleEvent.bind(this);
        this._updateCell = this._updateCell.bind(this);
    }

    componentDidMount() {

    }

    render() {
        return (
            <div>
                <Input
                    placeholder='Book Name...'
                    name="bookName"
                    onChange={this._handleEvent}/>
                <Button
                    name="bookLoadButton"
                    onClick={this._handleEvent}>
                    Load
                </Button>

                <ReactResumableJs
                    uploaderID="importBook"
                    service="/api/importFile"
                    onFileSuccess={(file, message) => {
                        console.log(file, message);
                    }}
                    onFileAdded={(file, resumable) => {
                        resumable.upload();
                    }}
                />


                <div style={{display: 'flex'}}>
                    <div style={{flex: 'auto', height: '90vh'}}>
                        <AutoSizer>
                            {({height, width}) => (
                                <ScrollSync>
                                    {({clientHeight, clientWidth, onScroll, scrollHeight, scrollLeft, scrollTop, scrollWidth}) => (
                                        <div className='GridRow'>
                                            <div className='LeftSideGridContainer'
                                                 style={{
                                                     position: 'absolute',
                                                     left: 0,
                                                     top: this.rowHeight,
                                                 }}>
                                                <Grid
                                                    height={height}
                                                    width={this.columnWidth}
                                                    style={{
                                                        overflow: 'hidden'
                                                    }}
                                                    scrollTop={scrollTop}
                                                    cellRenderer={this._rowHeaderCellRenderer}
                                                    columnWidth={this.columnWidth}
                                                    columnCount={1}
                                                    rowCount={this.state.rows}
                                                    rowHeight={this.rowHeight}
                                                />
                                            </div>

                                            <div className='LeftSideGridContainer'
                                                 style={{
                                                     position: 'absolute',
                                                     left: this.columnWidth,
                                                     top: 0,
                                                     height:this.rowHeight
                                                 }}>
                                                <Grid
                                                    height={height}
                                                    width={width - this.columnWidth}
                                                    style={{
                                                        overflow: 'hidden'
                                                    }}
                                                    scrollLeft={scrollLeft}
                                                    cellRenderer={this._columnHeaderCellRenderer}
                                                    columnWidth={this.columnWidth}
                                                    columnCount={this.state.columns}
                                                    rowCount={1}
                                                    rowHeight={this.rowHeight}
                                                />
                                            </div>


                                            <div className='RightColumn'
                                                 style={{
                                                     position: 'absolute',
                                                     left: this.columnWidth,
                                                     top: this.rowHeight
                                                 }}>
                                                <Grid
                                                    height={height}
                                                    width={width - this.columnWidth}
                                                    cellRenderer={this._cellRenderer}
                                                    columnCount={this.state.columns}
                                                    columnWidth={this.columnWidth}
                                                    rowCount={this.state.rows}
                                                    rowHeight={this.rowHeight}
                                                    onScroll={onScroll}
                                                    //onSectionRendered={this._onSectionRendered}
                                                    ref={(ref) => this.grid = ref}
                                                />
                                            </div>
                                        </div>
                                    )}
                                </ScrollSync>
                            )}
                        </AutoSizer>
                    </div>
                </div>

            </div>
        )

    }

    _handleEvent(event) {
        const target = event.target;
        const name = target.name;
        if (name == "bookName") {
            this.bookName = target.value;
            console.log(this.bookName);
        }
        else if (name == "bookLoadButton") {
            fetch(this.urlPrefix + "/api/getSheets/" + this.bookName)
                .then(res => res.json())
                .then(
                    (result) => {
                        console.log("Rows " + result['data']['sheets'][0]['numRow']);
                        console.log("numCol " + result['data']['sheets'][0]['numCol']);
                        this.dataCache.reset();
                        this.setState({
                            bookName: this.bookName,
                            sheetName: '',
                            rows: result['data']['sheets'][0]['numRow'],
                            columns: result['data']['sheets'][0]['numCol']
                        });
                    }
                )
                .catch((error) => {
                    console.error(error);
                });


        }
    }


    _onSectionRendered({
                           columnOverscanStartIndex,
                           columnOverscanStopIndex,
                           columnStartIndex,
                           columnStopIndex,
                           rowOverscanStartIndex,
                           rowOverscanStopIndex,
                           rowStartIndex,
                           rowStopIndex
                       }) {
        console.log("_onSectionRendered " +
            rowStartIndex + " "
            + rowStopIndex + " - " + rowOverscanStartIndex + " " + rowOverscanStopIndex);

    }


    _loadMoreRows({startIndex, stopIndex}) {
        console.log('loadMoreRows' + startIndex + " " + stopIndex);
        fetch(this.urlPrefix + "/api/getCellsV2/" + this.bookName
            + "/Sheet1/" + (startIndex) + "/" + (stopIndex))
            .then(res => res.json())
            .then(
                (result) => {
                    for (let i = startIndex, j = 0; i <= stopIndex; i++, j++) {
                        this.dataCache.set(i, result['values'][j]);
                    }
                    this.grid.forceUpdate();
                }
            ),
            (error) => {
            };
    }

    _rowHeaderCellRenderer({
                               key,         // Unique key within array of cells
                               rowIndex,    // Vertical (row) index of cell
                               style
                           }) {
        return (
            <div
                key={key}
                style={style}
                className='rowHeaderCell'>
                {rowIndex}
            </div>
        )
    }

    _columnHeaderCellRenderer = ({
                               columnIndex, // Horizontal (column) index of cell
                               key,         // Unique key within array of cells
                               style
                           }) => {
        return (
            <div
                key={key}
                style={style}
                className='rowHeaderCell'>
                {this.toColumnName(columnIndex + 1)}
            </div>
        )
    }

    _updateCell (
        {   rowIndex,
            columnIndex,
            value
        }
    ) {
        this.dataCache
            .get(Math.trunc((rowIndex) / this.fetchSize))
            [(rowIndex) % this.fetchSize][columnIndex] = value;
        this.grid.forceUpdate();
    }

    _cellRenderer ({
                         columnIndex, // Horizontal (column) index of cell
                         isScrolling, // The Grid is currently being scrolled
                         isVisible,   // This cell is visible within the grid (eg it is not an overscanned cell)
                         key,         // Unique key within array of cells
                         parent,      // Reference to the parent Grid (instance)
                         rowIndex,    // Vertical (row) index of cell
                         style
                     })  {
        let content;
        let cellClass = 'cell'

        let fromCache = this.dataCache.get(Math.trunc((rowIndex) / this.fetchSize));

        if (typeof fromCache == "object") {
            content = this.dataCache
                .get(Math.trunc((rowIndex) / this.fetchSize))
                [(rowIndex) % this.fetchSize][columnIndex];
        }
        else if (isScrolling) {
            cellClass = 'isScrollingPlaceholder'
            content = 'Loading ...';
        }
        else {
            cellClass = 'isScrollingPlaceholder'
            content = 'Loading ...';
            if (typeof fromCache == "undefined" && typeof this.bookName != "undefined") {
                var fetchRowIndex = rowIndex;
                //for (var i = 0; i < 5; i++) {  // TODO: Multithreaded broken -- need to fix the backend
                this.dataCache.set(Math.trunc((fetchRowIndex) / this.fetchSize), 0);
                // Load data - only if not scrolling.
                let startIndex = Math.trunc((fetchRowIndex) / this.fetchSize) * this.fetchSize;
                let stopIndex = startIndex + this.fetchSize - 1;
                console.log("Fetching  " + startIndex + " " + stopIndex);
                fetch(this.urlPrefix + "/api/getCellsV2/" + this.bookName
                    + "/Sheet1/" + startIndex + "/" + stopIndex)
                    .then(res => res.json())
                    .then(
                        (result) => {
                            this.dataCache.set(Math.trunc((fetchRowIndex) / this.fetchSize), result['values']);
                            this.grid.forceUpdate();
                        }
                    )
                    .catch((error) => {
                        console.error(error);
                    });
                //     fetchRowIndex = fetchRowIndex + this.fetchSize;
                // }

            }
        }


        return (
                <Cell
                    key={key}
                    style={style}
                    className={cellClass}
                    value={content}
                    rowIndex={rowIndex}
                    columnIndex={columnIndex}
                    onUpdate={this._updateCell}
                />
        )
    }

}
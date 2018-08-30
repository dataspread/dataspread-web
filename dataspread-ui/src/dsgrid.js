import React, {Component} from 'react';
import { Input, Button } from 'semantic-ui-react'
import ReactResumableJs from 'react-resumable-js'
import {AutoSizer, Grid, ScrollSync} from './react-virtualized'

import Cell  from './cell';
import 'react-datasheet/lib/react-datasheet.css';


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
            columns: 500,
            version: 0
        }
        var LRU = require("lru-cache");


        this.dataCache = new LRU(1000);


        this.fetchSize = 100;
        this._onSectionRendered = this._onSectionRendered.bind(this);
        this._loadMoreRows = this._loadMoreRows.bind(this);
        this._handleEvent = this._handleEvent.bind(this);
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
                    <div style={{flex: 'auto', height: '80vh'}}>
                        <AutoSizer>
                            {({height, width}) => (
                                <ScrollSync>
                                    {({clientHeight, clientWidth, onScroll, scrollHeight, scrollLeft, scrollTop, scrollWidth}) => (
                                        <div className='GridRow'>
                                            <div className='LeftSideGridContainer'
                                                 style={{
                                                     position: 'absolute',
                                                     left: 0,
                                                     top: 30,
                                                     height:1000,
                                                     width:1000
                                                 }}>
                                                <Grid
                                                    height={height}
                                                    width={150}
                                                    style={{
                                                        overflow: 'hidden'
                                                    }}
                                                    scrollTop={scrollTop}
                                                    cellRenderer={this._rowHeaderCellRenderer}
                                                    columnWidth={150}
                                                    columnCount={1}
                                                    rowCount={this.state.rows + 1}
                                                    rowHeight={30}
                                                />
                                            </div>

                                            <div className='LeftSideGridContainer'
                                                 style={{
                                                     position: 'absolute',
                                                     left: 150,
                                                     top: 0,
                                                     height:30
                                                 }}>
                                                <Grid
                                                    height={height}
                                                    width={width - 200}
                                                    style={{
                                                        overflow: 'hidden'
                                                    }}
                                                    scrollLeft={scrollLeft}
                                                    cellRenderer={this._columnHeaderCellRenderer}
                                                    columnWidth={150}
                                                    columnCount={this.state.columns}
                                                    rowCount={1}
                                                    rowHeight={30}
                                                />
                                            </div>


                                            <div className='RightColumn'
                                                 style={{
                                                     position: 'absolute',
                                                     left: 150,
                                                     top: 30
                                                 }}>
                                                <Grid
                                                    height={height}
                                                    width={width - 200}
                                                    cellRenderer={this._cellRenderer}
                                                    fixedColumnCount={1}
                                                    fixedRowCount={1}
                                                    columnCount={this.state.columns}
                                                    columnWidth={150}
                                                    rowCount={this.state.rows}
                                                    rowHeight={30}
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
            fetch("/api/getSheets/" + this.bookName)
                .then(res => res.json())
                .then(
                    (result) => {
                        console.log("Rows " + result['data']['sheets'][0]['numRow']);
                        console.log("numCol " + result['data']['sheets'][0]['numCol']);
                        this.dataCache.clear();
                        this.setState({
                            bookName: this.bookName,
                            rows: result['data']['sheets'][0]['numRow'],
                            columns: result['data']['sheets'][0]['numCol']
                        });
                    }
                ),
                (error) => {
                };


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
        fetch("/api/getCellsV2/" + this.bookName
            + "/Sheet1/" + (startIndex) + "/" + (stopIndex))
            .then(res => res.json())
            .then(
                (result) => {
                    for (let i = startIndex, j = 0; i <= stopIndex; i++, j++) {
                        this.dataCache.set(i, result['values'][j]);
                    }
                    this.grid.forceUpdateGrids();
                }
            ),
            (error) => {
            };
    }

    _rowHeaderCellRenderer = ({
                               key,         // Unique key within array of cells
                               rowIndex,    // Vertical (row) index of cell
                               style
                           }) => {
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

    _cellRenderer = ({
                         columnIndex, // Horizontal (column) index of cell
                         isScrolling, // The Grid is currently being scrolled
                         isVisible,   // This cell is visible within the grid (eg it is not an overscanned cell)
                         key,         // Unique key within array of cells
                         parent,      // Reference to the parent Grid (instance)
                         rowIndex,    // Vertical (row) index of cell
                         style
                     }) => {
        let content;
        let cellClass = 'cell'


        let fromCache = this.dataCache.get(Math.trunc((rowIndex - 1) / this.fetchSize));
        //console.log("fromCache " + fromCache);
        if (typeof fromCache == "object") {
            content = this.dataCache
                .get(Math.trunc((rowIndex - 1) / this.fetchSize))
                [(rowIndex - 1) % this.fetchSize][columnIndex];
        }
        else if (isScrolling) {
            cellClass = 'isScrollingPlaceholder'
            content = 'Loading ...';
        }
        else {
            if (typeof fromCache == "undefined" && typeof this.bookName != "undefined") {
                var fetchRowIndex = rowIndex - 1;
                //for (var i = 0; i < 5; i++) {  // TODO: Multithreaded broken -- need to fix the backend
                this.dataCache.set(Math.trunc((fetchRowIndex) / this.fetchSize), 0);
                // Load data - only if not scrolling.
                let startIndex = Math.trunc((fetchRowIndex) / this.fetchSize) * this.fetchSize;
                let stopIndex = startIndex + this.fetchSize - 1;
                console.log("Fetching  " + startIndex + " " + stopIndex);
                fetch("/api/getCellsV2/" + this.bookName
                    + "/Sheet1/" + startIndex + "/" + stopIndex)
                    .then(res => res.json())
                    .then(
                        (result) => {
                            this.dataCache.set(Math.trunc((fetchRowIndex) / this.fetchSize), result['values']);
                            this.grid.forceUpdate();
                        }
                    ),
                    (error) => {
                    };
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
                />
        )
    }
}
import React, {Component} from 'react';
import {Button, Input} from 'semantic-ui-react'
import ReactResumableJs from 'react-resumable-js'
import {AutoSizer, defaultCellRangeRenderer, Grid, ScrollSync} from './react-virtualized'

import Cell from './cell';
import 'react-datasheet/lib/react-datasheet.css';
import LRUCache from "lru-cache";
import Stomp from 'stompjs';

export default class DSGrid extends Component {
    toColumnName(num) {
        let ret, a, b;
        for (ret = '', a = 1, b = 26; (num -= a) >= 0; a = b, b *= 26) {
            ret = String.fromCharCode(Math.trunc((num % b) / a) + 65) + ret;
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
        this._disposeFromLRU = this._disposeFromLRU.bind(this);
        this.dataCache = new LRUCache({
            max: 100,
            dispose: this._disposeFromLRU,
            noDisposeOnSet: true
        });

        //this.urlPrefix="http://localhost:8080"; // Only for testing.
        this.urlPrefix = ""; // Only for testing.
        this.fetchSize = 50;

        this._onSectionRendered = this._onSectionRendered.bind(this);
        this._cellRenderer = this._cellRenderer.bind(this);
        this._handleEvent = this._handleEvent.bind(this);
        this._updateCell = this._updateCell.bind(this);
        this.processUpdates = this.processUpdates.bind(this);
        this._cellRangeRenderer = this._cellRangeRenderer.bind(this);

        this.stompClient = Stomp.client("ws://" + window.location.host + "/ds-push/websocket")
        //this.stompClient = Stomp.client("ws://localhost:8080/ds-push/websocket")
        this.stompClient.connect();
        this.stompSubscription = null;

        this.stompClient.debug = () => {
        };
    }

    _disposeFromLRU(key)
    {
        if (this.stompSubscription !=null) {
            this.stompClient.send('/push/status', {}, JSON.stringify({
                message: 'disposeFromLRU',
                blockNumber: key
            }));
        }
    }


    componentDidMount() {

    }

    componentWillUnmount() {
        console.log('Closing');
        this.stompClient.disconnect();
    }

    processUpdates(message)
    {
        let jsonMessage =  JSON.parse(message.body);
        if (jsonMessage['message'] === 'getCellsResponse')
        {
            this.dataCache.set(parseInt(jsonMessage['blockNumber'], 10),
                jsonMessage['data']);
            this.grid.forceUpdate();
        }
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
                    filetypes={["csv"]}
                    fileAccept="text/csv"
                    maxFileSize={1000000000}
                    service="/api/importFile"
                    disableDragAndDrop={true}
                    showFileList={false}
                    onFileSuccess={(file, message) => {
                        console.log(file, message);
                    }}
                    onFileAdded={(file, resumable) => {
                        resumable.upload();
                    }}
                    maxFiles={1}
                    onStartUpload={() => {
                        console.log("Start upload");
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
                                                    cellRangeRenderer={this._cellRangeRenderer}
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
        if (name === "bookName") {
            this.bookName = target.value;
            console.log(this.bookName);
        }
        else if (name === "bookLoadButton") {
            fetch(this.urlPrefix + "/api/getSheets/" + this.bookName)
                .then(res => res.json())
                .then(
                    (result) => {
                        this.dataCache.reset();
                        this.setState({
                            bookName: this.bookName,
                            sheetName: 'Sheet1',
                            rows: result['data']['sheets'][0]['numRow'],
                            columns: result['data']['sheets'][0]['numCol']
                        });
                        this.grid.scrollToCell ({ columnIndex: 0, rowIndex: 0 });
                        if (this.stompSubscription!=null)
                            this.stompSubscription.unsubscribe();
                        this.stompSubscription = this.stompClient
                            .subscribe('/user/push/updates',
                                this.processUpdates, {bookName: this.state.bookName,
                                        sheetName: this.state.sheetName,
                                        fetchSize: this.fetchSize});
                        this.grid.forceUpdate();
                        console.log("book loaded rows:" + result['data']['sheets'][0]['numRow']);
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
                {rowIndex + 1}
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
        let fromCache = this.dataCache.get(Math.trunc((rowIndex) / this.fetchSize));

        if (typeof fromCache === "object") {
            if (value[0] === '=') {
                this.dataCache
                    .get(Math.trunc((rowIndex) / this.fetchSize))[(rowIndex) % this.fetchSize][columnIndex] = ['...', value];
            }
            else {
                this.dataCache
                    .get(Math.trunc((rowIndex) / this.fetchSize))[(rowIndex) % this.fetchSize][columnIndex] = [value];
            }

            this.grid.forceUpdate();
        }
        this.stompClient.send('/push/status', {}, JSON.stringify({
            message: 'updateCell',
            row: rowIndex,
            column: columnIndex,
            value: value
        }));


        //TODO: send update to backend.
    }


    _cellRangeRenderer (props) {

        if (this.stompSubscription !=null) {
            if (!props.isScrolling) {
                if (this.rowStartIndex!==props.rowStartIndex || this.rowStopIndex!==props.rowStopIndex) {
                    this.rowStartIndex=props.rowStartIndex;
                    this.rowStopIndex=props.rowStopIndex;
                    this.stompClient.send('/push/status', {}, JSON.stringify({
                        message: 'changeViewPort',
                        rowStartIndex: this.rowStartIndex,
                        rowStopIndex: this.rowStopIndex
                    }));
                }
            }
        }
        const children = defaultCellRangeRenderer(props);
        return children;
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
        let cellContent;
        let cellClass = 'cell'

        let fromCache = this.dataCache.get(Math.trunc((rowIndex) / this.fetchSize));

        if (typeof fromCache === "object") {
            cellContent = this.dataCache
                .get(Math.trunc((rowIndex) / this.fetchSize))[(rowIndex) % this.fetchSize][columnIndex];
        }
        else {
            cellClass = 'isScrollingPlaceholder'
            cellContent = ['Loading ...'];
        }


        return (
                <Cell
                    key={key}
                    style={style}
                    className={cellClass}
                    value={cellContent[0]}
                    formula={cellContent[1]}
                    rowIndex={rowIndex}
                    columnIndex={columnIndex}
                    onUpdate={this._updateCell}
                />
        )
    }

}
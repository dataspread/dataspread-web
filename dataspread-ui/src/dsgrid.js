import React, {Component} from 'react';
import {Dimmer, Loader} from 'semantic-ui-react'
import {ArrowKeyStepper, AutoSizer, defaultCellRangeRenderer, Grid, ScrollSync} from './react-virtualized'
import Draggable from "react-draggable";


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
            sheetName: '',
            columns: 500,
            version: 0,
            focusCellRow: -1,
            focusCellColumn: -1,
            selectOppositeCellRow: -1,
            selectOppositeCellColumn: -1,
            isProcessing: false,
            initialLoadDone:false,
            columnWidths: Array(500).fill(150)
        };
        this.mouseDown = false;
        this.shiftOn = false;
        this.subscribed = false;
        this.rowHeight = 32;
        this.columnWidth = 150;
        this._disposeFromLRU = this._disposeFromLRU.bind(this);
        this.dataCache = new LRUCache({
            max: 100,
            dispose: this._disposeFromLRU,
            noDisposeOnSet: true
        });

        this.fetchSize = 50;

        this._onSectionRendered = this._onSectionRendered.bind(this);
        this._cellRenderer = this._cellRenderer.bind(this);
        this._updateCell = this._updateCell.bind(this);
        this._mouseOverCell = this._mouseOverCell.bind(this);
        this._mouseDownCell = this._mouseDownCell.bind(this);
        this._mouseUpCell = this._mouseUpCell.bind(this);
        this._processUpdates = this._processUpdates.bind(this);
        this._cellRangeRenderer = this._cellRangeRenderer.bind(this);
        this._handleKeyDown = this._handleKeyDown.bind(this);
        this._handleKeyUp = this._handleKeyUp.bind(this);

        this._changeColumnWidth = this._changeColumnWidth.bind(this);
        this._columnWidthHelper = this._columnWidthHelper.bind(this);

        // this.urlPrefix = ""; // Only for testing.
        // this.stompClient = Stomp.client("ws://" + window.location.host + "/ds-push/websocket");

        if (typeof process.env.REACT_APP_BASE_HOST === 'undefined') {
            this.urlPrefix = "";
            this.stompClient = Stomp.client("ws://" + window.location.host + "/ds-push/websocket");
        }
        else
        {
            this.urlPrefix = "http://" + process.env.REACT_APP_BASE_HOST;
            this.stompClient = Stomp.client("ws://" + process.env.REACT_APP_BASE_HOST + "/ds-push/websocket");
        }

        //this.urlPrefix = process.env.REACT_APP_BASE_URL ; // Only for testing.
        //this.stompClient = Stomp.client("ws://kite.cs.illinois.edu:8080/ds-push/websocket");
        //this.stompClient = Stomp.client("ws://localhost:8080/ds-push/websocket");
        console.log("urlPrefix:" +  this.urlPrefix);

        this.stompClient.connect({}, null, null, () => {
            alert("Lost connection to server. Please reload.")
        });
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
        //onsole.log("Grid mounted passed file id " + this.props.fileId);
        //this.setState({bookId: this.props.fileId})
    }

    componentWillUnmount() {
        console.log('Closing');
        this.stompClient.disconnect();
    }

    _processUpdates(message)
    {
        let jsonMessage =  JSON.parse(message.body);
        if (jsonMessage['message'] === 'getCellsResponse')
        {
            this.dataCache.set(parseInt(jsonMessage['blockNumber'], 10),
                jsonMessage['data']);
            this.setState({initialLoadDone: true});
            this.grid.forceUpdate();
        }
        else if (jsonMessage['message'] === 'asyncStatus') {
            let cell = jsonMessage['data']
            let fromCache = this.dataCache.get(Math.trunc(cell[0] / this.fetchSize));
            if (typeof fromCache === "object") {
                fromCache[cell[0] % this.fetchSize][cell[1]][2] = cell[2];
                this.grid.forceUpdate();
            }
        }
        else if (jsonMessage['message'] === 'pushCells') {
            for (let i in jsonMessage['data']) {
                let cell = jsonMessage['data'][i];
                let fromCache = this.dataCache.get(Math.trunc(cell[0] / this.fetchSize));
                if (typeof fromCache === "object") {
                    if (cell[3] == null)
                        fromCache[cell[0] % this.fetchSize][cell[1]] = [cell[2]];
                    else
                        fromCache[cell[0] % this.fetchSize][cell[1]] = [cell[2], cell[3]];
                }
            }
            this.grid.forceUpdate();
        }
        else if (jsonMessage['message'] === 'processingDone') {
            this.setState({isProcessing: false});
        }
        else if (jsonMessage['message'] === 'subscribed') {
            this.subscribed = true;
            this.rowStartIndex = -1; // Force reload.
            this.grid.forceUpdate();

        }
    }

    render() {
        return (
            <div onKeyDown={this._handleKeyDown} onKeyUp={this._handleKeyUp}>
                <div style={{display: 'flex'}}>
                    <div style={{flex: 'auto', height: '90vh'}}>
                        <Dimmer active={this.state.isProcessing || !this.state.initialLoadDone}>
                            <Loader>Processing</Loader>
                        </Dimmer>
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
                                                    columnWidth={this._columnWidthHelper}
                                                    columnCount={this.state.columns}
                                                    rowCount={1}
                                                    rowHeight={this.rowHeight}
                                                    ref={(ref) => this.headerGrid = ref}
                                                />
                                            </div>


                                            <div className='RightColumn'
                                                 style={{
                                                     position: 'absolute',
                                                     left: this.columnWidth,
                                                     top: this.rowHeight
                                                 }}>

                                                <ArrowKeyStepper
                                                    rowCount={this.state.rows}
                                                    columnCount={this.state.columns}>
                                                    {({onSectionRendered, scrollToColumn, scrollToRow}) => (
                                                        <div>
                                                            <Grid
                                                                height={height}
                                                                width={width - this.columnWidth}
                                                                cellRenderer={this._cellRenderer}
                                                                columnCount={this.state.columns}
                                                                columnWidth={this._columnWidthHelper}
                                                                rowCount={this.state.rows}
                                                                rowHeight={this.rowHeight}
                                                                onScroll={onScroll}
                                                                cellRangeRenderer={this._cellRangeRenderer}
                                                                scrollToRow={scrollToRow}
                                                                scrollToColumn={scrollToColumn}
                                                                onSectionRendered={onSectionRendered}
                                                                ref={(ref) => this.grid = ref}
                                                                focusCellColumn={this.state.focusCellColumn}
                                                                focusCellRow={this.state.focusCellRow}
                                                                selectOppositeCellColumn={this.state.selectOppositeCellColumn}
                                                                selectOppositeCellRow={this.state.selectOppositeCellRow}
                                                            />
                                                        </div>)}
                                                </ArrowKeyStepper>
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

    loadBook()
    {
        this.subscribed = false;
        fetch(this.urlPrefix + "/api/getSheets/" +  this.props.bookId)
            .then(res => res.json())
            .then(
                (result) => {
                    console.log(result);
                    this.dataCache.reset();
                    this.setState({
                        sheetName: 'Sheet1',
                        rows: result['data']['sheets'][0]['numRow']+100,
                        columns: result['data']['sheets'][0]['numCol']
                    });

                    this.grid.scrollToCell ({ columnIndex: 0, rowIndex: 0 });
                    if (this.stompSubscription!=null)
                        this.stompSubscription.unsubscribe();
                    this.stompSubscription = this.stompClient
                        .subscribe('/user/push/updates',
                            this._processUpdates, {bookId:  this.props.bookId,
                                sheetName: this.state.sheetName,
                                fetchSize: this.fetchSize});
                    console.log("book loaded rows:" + result['data']['sheets'][0]['numRow']);
                }
            )
            .catch((error) => {
                console.error(error);
            });
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

    _columnHeaderCellRenderer ({
                               columnIndex, // Horizontal (column) index of cell
                               key,         // Unique key within array of cells
                               style
                           }) {
        return (
            <div
                key={key}
                style={style}
                className='rowHeaderCell'>
                {this.toColumnName(columnIndex + 1)}
                <Draggable axis="x"
                           defaultClassName="DragHandle"
                           defaultClassNameDragging="DragHandleActive"
                           onDrag={(event,{deltaX}) => this._changeColumnWidth({key,deltaX})}
                           position={{x:0}}
                           zIndex={999}>
                    <a className="drag-icon">|</a>
                </Draggable>
            </div>
        )
    }

    _updateCell (
        {   rowIndex,
            columnIndex,
            value
        }
    ) {
        this.setState({isProcessing: true});


        let fromCache = this.dataCache.get(Math.trunc((rowIndex) / this.fetchSize));

        if (typeof fromCache === "object") {
            if (value[0] === '=') {
                fromCache[(rowIndex) % this.fetchSize][columnIndex] = ['...', value.substring(1)];
            }
            else {
                fromCache[(rowIndex) % this.fetchSize][columnIndex] = [value];
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

    _setFocusCell(rowIndex, columnIndex) {
        if (this.inclusiveBetween(rowIndex, 0, this.state.rows-1) &&
            this.inclusiveBetween(columnIndex, 0, this.state.columns-1)) {
            this.setState({
                focusCellRow: rowIndex,
                focusCellColumn: columnIndex,
                selectOppositeCellRow: rowIndex,
                selectOppositeCellColumn: columnIndex
            });
        }
    }

    _setSelectOppositeCell(rowIndex, columnIndex, setFocusCell) {
        if (this.inclusiveBetween(rowIndex, 0, this.state.rows-1) &&
            this.inclusiveBetween(columnIndex, 0, this.state.columns-1)) {
            this.setState({
                selectOppositeCellRow: rowIndex,
                selectOppositeCellColumn: columnIndex
            });
        }
    }

    _keyMoveCell(rowIndexOffset, columnIndexOffset) {
        if (this.shiftOn) {
            this._setSelectOppositeCell(
                this.state.selectOppositeCellRow+rowIndexOffset,
                this.state.selectOppositeCellColumn+columnIndexOffset
            );
        } else {
            this._setFocusCell(
                this.state.focusCellRow+rowIndexOffset,
                this.state.focusCellColumn+columnIndexOffset
            );
        }
    }

    _mouseOverCell (
        {   rowIndex,
            columnIndex
        }
    ) {
        if (this.mouseDown) {
            this._setSelectOppositeCell(rowIndex, columnIndex);
        }
    }

    _mouseDownCell (
        {   rowIndex,
            columnIndex
        }
    ) {
        if (!this.shiftOn) {
            this._setFocusCell(rowIndex, columnIndex);
        } else {
            this._setSelectOppositeCell(rowIndex, columnIndex);
        }
        this.mouseDown = true;
        //TODO: send selection to backend.
    }

    _mouseUpCell (
        {   rowIndex,
            columnIndex
        }
    ) {
        this.mouseDown = false;
        //TODO: send selection to backend.
    }


    _cellRangeRenderer (props) {
        if (this.subscribed) {
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

    inclusiveBetween (num, num1, num2) {
        return (num1 <= num && num <= num2) || (num2 <= num && num <= num1);
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
            if (rowIndex === this.state.focusCellRow && columnIndex === this.state.focusCellColumn) {
                cellClass = 'cellFocus';
            } else if (this.inclusiveBetween(rowIndex, this.state.focusCellRow, this.state.selectOppositeCellRow)
            && this.inclusiveBetween(columnIndex, this.state.focusCellColumn, this.state.selectOppositeCellColumn)) {
                cellClass = 'cellSelected';
            }
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
                    formula={cellContent[1] == null ? null : "=" + cellContent[1]}
                    rowIndex={rowIndex}
                    columnIndex={columnIndex}
                    isProcessing={cellContent[0] === '...'}
                    pctProgress={cellContent[2]}
                    onCellMouseOver={this._mouseOverCell}
                    onCellMouseDown={this._mouseDownCell}
                    onCellMouseUp={this._mouseUpCell}
                    onUpdate={this._updateCell}
                />
        )
    }

    _handleKeyDown(e) {
        if (e.key === 'Shift') {
            this.shiftOn = true;
        } else if (e.key === 'ArrowLeft') {
            this._keyMoveCell(0, -1);
        } else if (e.key === 'ArrowRight') {
            this._keyMoveCell(0, 1);
        } else if (e.key === 'ArrowUp') {
            this._keyMoveCell(-1, 0);
        } else if (e.key === 'ArrowDown') {
            this._keyMoveCell(1, 0);
        }
    }

    _handleKeyUp(e) {
        if (e.key === 'Shift') {
            this.shiftOn = false;
        }
    }

    _columnWidthHelper(params){
        return this.state.columnWidths[params.index];
    }

    _changeColumnWidth({key, deltaX}){
        const index = parseInt(key.split('-')[1], 10);
        const newColumnWidths = this.state.columnWidths.slice();
        if (deltaX+newColumnWidths[index] > 30) {
            newColumnWidths[index] += deltaX;
        }
        this.setState({
            columnWidths: newColumnWidths
        });

        this.headerGrid.recomputeGridSize({columnIndex: index});
        this.grid.recomputeGridSize({columnIndex: index});
    }

}
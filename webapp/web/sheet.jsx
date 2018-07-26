// Obtain the root
const rootElement = document.getElementById('root')

class DSGrid extends React.Component {

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
            columns: 500,
            version: 0
        }

        this.dataCache = new LRUMap(10000);
        this._infiniteLoaderChildFunction = this._infiniteLoaderChildFunction.bind(this);
        this._onSectionRendered = this._onSectionRendered.bind(this);
        this._isRowLoaded = this._isRowLoaded.bind(this);
        this._loadMoreRows = this._loadMoreRows.bind(this);
    }

    render() {
        const {infiniteLoaderProps} = this.props;
        return (
            <div>
                <ReactVirtualized.InfiniteLoader
                    isRowLoaded={this._isRowLoaded}
                    loadMoreRows={this._loadMoreRows}
                    rowCount={this.state.rows}
                    {...infiniteLoaderProps}>
                    {this._infiniteLoaderChildFunction}
                </ReactVirtualized.InfiniteLoader>
            </div>
        )

    }


    _infiniteLoaderChildFunction({onRowsRendered, registerChild}) {
        this._onRowsRendered = onRowsRendered;
        const {gridProps} = this.props;
        return (
            <div style={{display: 'flex'}}>
                <div style={{flex: 'auto', height: '90vh'}}>
                    <ReactVirtualized.AutoSizer>
                        {({height, width}) => (
                            <ReactVirtualized.MultiGrid
                                {...gridProps}
                                height={height}
                                width={width}
                                cellRenderer={this._cellRenderer}
                                fixedColumnCount={1}
                                fixedRowCount={1}
                                columnCount={this.state.columns + 1}
                                columnWidth={150}
                                rowCount={this.state.rows + 1}
                                rowHeight={30}
                                onSectionRendered={this._onSectionRendered}
                                ref={registerChild}
                            />
                        )}
                    </ReactVirtualized.AutoSizer>
                </div>
            </div>
        )
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
        console.log(
            rowStartIndex + " "
            + rowStopIndex + " - " + rowOverscanStartIndex + " " + rowOverscanStopIndex);

        this._onRowsRendered({
            startIndex: rowStartIndex,
            stopIndex: rowStopIndex
        });
    }


    _isRowLoaded({index}) {
        //   console.log('_isRowLoaded ' + index + this.dataCache.has(index));
        const {loadedRowsMap} = this.state;
        //return !!loadedRowsMap[index]; // STATUS_LOADING or STATUS_LOADED
        return this.dataCache.has(index);
    }

    _loadMoreRows({startIndex, stopIndex}) {
        console.log('loadMoreRows' + startIndex + " " + stopIndex);

        // Load Data.
        fetch("http://localhost:8080/getRows/" + startIndex + "/" + stopIndex)
            .then(res => res.json())
            .then(
                (result) => {
                    for (let i = startIndex, j = 0; i <= stopIndex; i++, j++) {
                        this.dataCache.set(i, result[j]);
                    }
                    console.log("Size " + this.dataCache.size);
                    //this.grid.forceUpdateGrids();
                    this.setState((prevState, props) => ({
                        version: prevState.version + 1
                    }));
                }
            ),
            (error) => {
            };

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

        content = this.dataCache.get(rowIndex);
        if (content == undefined)
            content = 'Loading...';

        var cellClass = 'cell'
        if (rowIndex == 0) {
            content = this.toColumnName(columnIndex);
            cellClass = 'columnHeaderCell';
        }

        if (columnIndex == 0) {
            content = rowIndex;
            cellClass = 'rowHeaderCell';
        }

        if (columnIndex == 0 && rowIndex == 0) {
            content = '';
        }


        return (
            <div
                key={key}
                style={style}
                className={cellClass}>
                {content}
            </div>
        )
    }
}


// Render DataSpread Grid
ReactDOM.render(
    <DSGrid/>,
    rootElement
);

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
        }

        this.dataCache = new LRUMap(10000);
        this._onSectionRendered = this._onSectionRendered.bind(this);
        this._cellRenderer = this._cellRenderer.bind(this);
    }

    render() {
        return (
            <div style={{display: 'flex'}}>
                <div style={{flex: 'auto', height: '95vh'}}>
                    <ReactVirtualized.AutoSizer>
                        {({height, width}) => (
                            <ReactVirtualized.MultiGrid
                                ref={(grid) => {
                                    this.grid = grid
                                }}
                                height={height}
                                width={width}
                                cellRenderer={this._cellRenderer}
                                onSectionRendered={this._onSectionRendered}
                                fixedColumnCount={1}
                                fixedRowCount={1}
                                columnCount={this.state.columns + 1}
                                columnWidth={150}
                                rowCount={this.state.rows + 1}
                                rowHeight={30}
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
        console.log('_onSectionRendered ' +
            rowStartIndex + " "
            + rowStopIndex + " - " + rowOverscanStartIndex + " " + rowOverscanStopIndex);

        // Check if visible range is loaded, if not load overScanRange.
        let loaded = true;
        // Added 1 to rowStopIndex to get around last row bug
        for (let i = rowStartIndex; i <= rowStopIndex + 1; i++) {
            if (!(this.dataCache.has(i))) {
                loaded = false;
                break;
            }
        }

        if (!loaded) {
            console.log("Data Not loaded " + rowStartIndex + " "
                + rowStopIndex)
            // Load Data.
            fetch("http://localhost:8080/getRows/" + rowOverscanStartIndex + "/" + rowOverscanStopIndex)
                .then(res => res.json())
                .then(
                    (result) => {
                        for (let i = rowOverscanStartIndex, j = 0; i <= rowOverscanStopIndex; i++, j++) {
                            this.dataCache.set(i, result[j]);
                        }
                        console.log("Size " + this.dataCache.size);
                        this.grid.forceUpdateGrids();
                    }
                ),
                (error) => {
                };
        }
    }


    _cellRenderer({
                      columnIndex, // Horizontal (column) index of cell
                      isScrolling, // The Grid is currently being scrolled
                      isVisible,   // This cell is visible within the grid (eg it is not an overscanned cell)
                      key,         // Unique key within array of cells
                      parent,      // Reference to the parent Grid (instance)
                      rowIndex,    // Vertical (row) index of cell
                      style
                  }) {
        //console.log("Cell Render " + rowIndex);
        //const {loadedRowsMap} = this.state;
        // console.log(rowIndex);
        let content;

        content = this.dataCache.get(rowIndex);
        if (content == undefined)
            content = 'Loading...';


        //style = pStyle
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

// Obtain the root
const rootElement = document.getElementById('root')
const STATE_LOADING = 0;

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
            bookName: '',
            columns: 500,
            version: 0
        }

        this.dataCache = new LRUMap(100);
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
                <semanticUIReact.Input
                    placeholder='Book Name...'
                    name="bookName"
                    onChange={this._handleEvent}/>
                <semanticUIReact.Button
                    name="bookLoadButton"
                    onClick={this._handleEvent}>
                    Load
                </semanticUIReact.Button>

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
                        <ReactVirtualized.AutoSizer>
                            {({height, width}) => (
                                <ReactVirtualized.MultiGrid
                                    height={height}
                                    width={width}
                                    cellRenderer={this._cellRenderer}
                                    fixedColumnCount={1}
                                    fixedRowCount={1}
                                    columnCount={this.state.columns + 1}
                                    columnWidth={150}
                                    rowCount={this.state.rows + 1}
                                    rowHeight={30}
                                    //onSectionRendered={this._onSectionRendered}
                                    ref={(ref) => this.grid = ref}
                                />
                            )}
                        </ReactVirtualized.AutoSizer>
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

        if (columnIndex == 0 && rowIndex == 0) {
            content = '';
        }
        else if (rowIndex == 0) {
            content = this.toColumnName(columnIndex);
            cellClass = 'columnHeaderCell';
        }
        else if (columnIndex == 0) {
            content = rowIndex;
            cellClass = 'rowHeaderCell';
        }
        else {
            let fromCache = this.dataCache.get(Math.trunc((rowIndex - 1) / this.fetchSize));
            //console.log("fromCache " + fromCache);
            if (typeof fromCache == "object") {
                content = this.dataCache
                    .get(Math.trunc((rowIndex - 1) / this.fetchSize))
                    [(rowIndex - 1) % this.fetchSize][columnIndex - 1];
            }
            else if (isScrolling) {
                content = '';
            }
            else {
                if (typeof fromCache == "undefined" && typeof this.bookName != "undefined") {
                    this.dataCache.set(Math.trunc((rowIndex - 1) / this.fetchSize), STATE_LOADING);
                    // Load data - only if not scrolling.
                    let startIndex = Math.trunc((rowIndex - 1) / this.fetchSize) * this.fetchSize;
                    let stopIndex = startIndex + this.fetchSize;
                    console.log("Fetching  " + startIndex + " " + stopIndex);
                    fetch("/api/getCellsV2/" + this.bookName
                        + "/Sheet1/" + startIndex + "/" + stopIndex)
                        .then(res => res.json())
                        .then(
                            (result) => {
                                this.dataCache.set(Math.trunc((rowIndex - 1) / this.fetchSize), result['values']);
                                this.grid.forceUpdateGrids();
                            }
                        ),
                        (error) => {
                        };

                }
            }
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

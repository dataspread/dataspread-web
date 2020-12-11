import React, {Component} from 'react';
import './App.css';
import DSGrid from './dsgrid';
import Toolbar from './Components/Menu/toolbar'
import Stylebar from './Components/Stylebar'
import StartupBox from './Components/StatupBox'

import Navigation from "./Components/Navigation";
import ExplorationForm from "./Components/ExplorationForm";
import BinCustomizationForm from "./Components/BinCustomizationForm";
import HistoryBar from "./Components/HistoryBar"

class App extends Component {

    constructor(props) {
        super(props);

        this.state = {
            bookId: "",
            filename: "",
            hasFileOpened: false,
            username: "",

        }
        this.onSelectFile = this.onSelectFile.bind(this)
        this.onNavFormOpen = this.onNavFormOpen.bind(this)
        this.updateHierFormOption = this.updateHierFormOption.bind(this)
        this.submitHierForm = this.submitHierForm.bind(this)
        this.onBinFormOpen = this.onBinFormOpen.bind(this)

        this.submitNavForm = this.submitNavForm.bind(this);
        this.scrollTo = this.scrollTo.bind(this);
        this.updateBreadcrumb = this.updateBreadcrumb.bind(this);
        this.computePath = this.computePath.bind(this);
        this.jumpToHistorialView = this.jumpToHistorialView.bind(this);
        this.brushNlink = this.brushNlink.bind(this);
        this.updateHighlight = this.updateHighlight.bind(this);
    }

    onSelectFile(bookId) {
        this.setState({
            bookId: bookId,
            hasFileOpened: true
        })
    }

    componentDidUpdate() {
        if (this.grid !== null) {
            this.grid.loadBook();
        }
    }

    onNavFormOpen() {
        if (this.grid !== null) {
            fetch(this.grid.urlPrefix + '/api/getSortAttrs/' + this.state.bookId + '/' + this.grid.state.sheetName)
                .then(response => response.json())
                .then(data => {
                    console.log(data)
                    let options = [];
                    for (let i = 0; i < data.data.length; i++) {
                        options.push({
                            "text": data.data[i],
                            "value": i + 1,
                        })
                    }
                    this.navForm.setState({options: options, navFormOpen: true});
                    this.nav.setState({options: data.data});
                })
        }
    }

    updateHierFormOption(data) {
        this.toolBar.hier.updateOption(data);
    }

    submitHierForm(data) {
        this.nav.submitHierForm(data);
    }

    onBinFormOpen() {
        if (this.nav.state.navOpen) {
            let queryData = {};
            queryData.bookId = this.state.bookId;
            queryData.sheetName = this.grid.state.sheetName;
            queryData.path = this.nav.computePath();
            fetch(this.grid.urlPrefix + '/api/' + 'redefineBoundaries', {
                method: "POST",
                body: JSON.stringify(queryData),
                headers: {
                    'Content-Type': 'text/plain'
                }
            })
                .then(response => response.json())
                .then(data => {
                    console.log(data)
                    let array = [];
                    for (let i = 0; i < data.data.bucketArray.length; i++) {
                        array.push(false);
                    }
                    this.binForm.setState({
                        binFormOpen: true,
                        isNumeric: data.data.isNumeric,
                        bucketArray: data.data.bucketArray,
                        checkedArray: array,
                        checkedCount: 0,
                        checkedIndex: 0,
                        dropdownIndex: 0,
                    });

                })
        }
    }

    submitBinForm = () => {
        console.log(this.binForm)
        this.binForm.setState({
            binFormOpen: false,
        });

        let queryData = {};
        queryData.bookId = this.state.bookId;
        queryData.sheetName = this.grid.state.sheetName;
        queryData.path = this.nav.computePath();
        if (this.binForm.state.isNumeric) {
            queryData.bucketArray = this.binForm.state.bucketArray;
        } else {
            let temp = [];
            let dataBucket = this.binForm.state.bucketArray;
            for (let i = 0; i < dataBucket.length; i++) {
                temp.push([dataBucket[i][0], dataBucket[i][dataBucket[i].length - 1]]);
            }
            queryData.bucketArray = temp;
        }
        console.log(queryData)
        fetch(this.grid.urlPrefix + '/api/' + 'updateBoundaries', {
            method: "POST",
            body: JSON.stringify(queryData),
            headers: {
                'Content-Type': 'text/plain'
            }
        })
            .then(response => response.json())
            .then(data => {
                console.log(data)
                console.log(this)
                if (this.nav.state.currLevel > 0) {
                    this.nav.jumpToHistorialView(this.nav.computePath());
                } else {
                    this.submitNavForm(this.grid.state.exploreCol + 1);
                    // if (hieraOpen) {
                    //     getAggregateValue();
                    //
                    // }
                }
            });
    }

    submitNavForm(attribute) {
        if (attribute) {
            fetch(this.grid.urlPrefix + '/api/startNav/' + this.state.bookId + '/' + this.grid.state.sheetName + '/' + attribute)
                .then(response => response.json())
                .then(data => {
                    console.log(data);
                    this.navForm.setState({navFormOpen: false, processing: false});
                    this.nav.setState({
                        navOpen: true,
                        sheetName: this.grid.state.sheetName,
                        urlPrefix: this.grid.urlPrefix
                    });
                    this.updateHierFormOption(this.navForm.state.options);
                    this.toolBar.setState({
                        navOpen: true,
                    })
                    this.nav.startNav(data);
                    this.navBar.setState({
                        breadcrumb_ls: [],
                      //  attribute: 0,
                        open: true,
                        navHistoryPathIndex: {},
                        navHistoryTable: {},
                        historyList: [],
                    });
                    this.grid.setState({
                        exploreCol: attribute - 1,
                    })
                    console.log(this.grid)
                    this.nav.brushNlink(this.grid.rowStartIndex, this.grid.rowStopIndex);
                })
        }
    }

    updateBreadcrumb(breadcrumb_ls, path_index) {
        this.navBar.updateNavPath(breadcrumb_ls, path_index);
    }

    jumpToHistorialView(path) {
        this.nav.jumpToHistorialView(path);
    }

    computePath() {
        return this.nav.computePath();
    }

    scrollTo(lowerRange) {
        this.grid.grid.scrollToCell({columnIndex: 0, rowIndex: lowerRange});
    }

    brushNlink(lower, upper) {
        if (this.nav.state.navOpen) {
            this.nav.brushNlink(lower, upper);
        }
    }


    updateHighlight(colNum, brushNLinkRows) {
        console.log(colNum);
        console.log(brushNLinkRows);
        this.grid.setState({
            hierarchicalCol: colNum,
            brushNLinkRows: brushNLinkRows,
        })

    }

    render() {
        // console.log(this)
        this.grid = null;
        if (!this.state.hasFileOpened) {
            return (
                <div>
                    <Toolbar username={this.state.username} onSelectFile={this.onSelectFile}
                             onNavFormOpen={this.onNavFormOpen}/>
                    <Stylebar/>
                    <StartupBox username={this.state.username} onSelectFile={this.onSelectFile}/>
                </div>
            )
        } else {
            return (
                <div>
                    <Toolbar username={this.state.username} onSelectFile={this.onSelectFile}
                             onNavFormOpen={this.onNavFormOpen} ref={ref => this.toolBar = ref}
                             submitHierForm={this.submitHierForm} onBinFormOpen={this.onBinFormOpen}/>
                    <Stylebar/>
                    <HistoryBar ref={ref => this.navBar = ref} computePath={this.computePath}
                                jumpToHistorialView={this.jumpToHistorialView}/>
                    <ExplorationForm grid={this.grid} submitNavForm={this.submitNavForm}
                                     ref={ref => this.navForm = ref}/>
                    <Navigation bookId={this.state.bookId} scrollTo={this.scrollTo} ref={ref => this.nav = ref}
                                updateBreadcrumb={this.updateBreadcrumb} updateHighlight={this.updateHighlight}/>
                    <BinCustomizationForm ref={ref => this.binForm = ref} submitBinForm={this.submitBinForm}/>
                    <DSGrid bookId={this.state.bookId} ref={ref => this.grid = ref} brushNlink={this.brushNlink}
                            updateHierFormOption={this.updateHierFormOption}/>

                </div>
            )
        }

    }
}

export default App;

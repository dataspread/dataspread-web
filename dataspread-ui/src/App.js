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
        //this.onBinFormOpen = this.onBinFormOpen.bind(this)

        this.submitNavForm = this.submitNavForm.bind(this);
        this.openBinForm = this.openBinForm.bind(this);
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

    // onBinFormOpen(){
    //     if (this.grid !== null) {
    //         this.grid.openBinForm();
    //     }
    // }

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
                    this.nav.startNav(data);
                    this.navBar.setState({
                        breadcrumb_ls: [],
                        attribute: 0,
                        open: true,
                        navHistoryPathIndex: {},
                        navHistoryTable: {},
                        historyList: [],
                    });
                    this.grid.setState({
                        exploreCol: attribute - 1,
                    })
                })
        }
    }

    updateBreadcrumb(breadcrumb_ls, path_index) {
        this.navBar.updateNavPath(breadcrumb_ls, path_index);
    }

    jumpToHistorialView(path) {
        // console.log(path)
        this.nav.jumpToHistorialView(path);
    }

    computePath() {
        return this.nav.computePath();
    }

    openBinForm() {
        if (this.state.navOpen) {
            this.setState({binFormOpen: true});
        }
    }

    scrollTo(lowerRange) {
        this.grid.grid.scrollToCell({columnIndex: 0, rowIndex: lowerRange + 20});
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
                             submitHierForm={this.submitHierForm}/>
                    <Stylebar/>
                    <HistoryBar ref={ref => this.navBar = ref} computePath={this.computePath}
                                jumpToHistorialView={this.jumpToHistorialView}/>
                    <Navigation bookId={this.state.bookId} scrollTo={this.scrollTo} ref={ref => this.nav = ref}
                                updateBreadcrumb={this.updateBreadcrumb} updateHighlight={this.updateHighlight}/>
                    <ExplorationForm grid={this.grid} submitNavForm={this.submitNavForm}
                                     ref={ref => this.navForm = ref}/>
                    <DSGrid bookId={this.state.bookId} ref={ref => this.grid = ref} brushNlink={this.brushNlink}
                            updateHierFormOption={this.updateHierFormOption}/>
                </div>
            )
        }

    }
}

export default App;

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
            fetch(this.grid.urlPrefix + '/api/getSortAttrs/'+ this.state.bookId+'/' + this.grid.state.sheetName)
                .then(response => response.json())
                .then(data => {
                    console.log(data);
                    this.navForm.setState({options:data.data,navFormOpen: true});
                    this.nav.setState({options:data.data});
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

    submitNavForm(attribute){
        if(attribute) {
            fetch(this.grid.urlPrefix + '/api/startNav/'+ this.state.bookId+'/' + this.grid.state.sheetName+'/' + attribute)
                .then(response => response.json())
                .then(data => {
                    console.log(data);
                    this.navForm.setState({navFormOpen:false});
                    this.nav.setState({navOpen:true,sheetName:this.grid.state.sheetName,urlPrefix:this.grid.urlPrefix});
                    this.updateHierFormOption(this.navForm.state.options);
                    this.nav.startNav(data);
                })
        }
    }


    openBinForm(){
        if(this.state.navOpen){
            this.setState({binFormOpen:true});
        }
    }

    scrollTo(lowerRange){
        this.grid.grid.scrollToCell({columnIndex: 0, rowIndex: lowerRange + 26});
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
                             submitHierForm = {this.submitHierForm} />
                    <Stylebar/>
                    <HistoryBar/>
                    <Navigation bookId={this.state.bookId} scrollTo={this.scrollTo} ref={ref => this.nav = ref} />
                    <ExplorationForm grid = {this.grid} submitNavForm = {this.submitNavForm} ref={ref => this.navForm = ref}/>
                    <DSGrid bookId={this.state.bookId} ref={ref => this.grid = ref}
                            updateHierFormOption={this.updateHierFormOption}/>
                </div>
            )
        }

    }
}

export default App;

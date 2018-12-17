import React, {Component} from 'react';
import {Sidebar} from 'semantic-ui-react'
import './App.css';
import DSGrid from './dsgrid';
import Toolbar from './Components/Menu/toolbar'
import Stylebar from './Components/Stylebar'
import StartupBox from './Components/StatupBox'
import TableSidebar from "./Components/Sidebar/TableSidebar";

class App extends Component {

    constructor(props){
        super(props);

        this.state = {
            bookId:"",
            filename:"",
            hasFileOpened: false,
            username:""
        };
        this.onSelectFile = this.onSelectFile.bind(this);
        this.onSelectionChange = this.onSelectionChange.bind(this);
        this.onFormSubmit = this.onFormSubmit.bind(this);

        // TODO: propagate operations from dsgrid up here
        // this.urlPrefix = ""; // Only for testing.

        if (typeof process.env.REACT_APP_BASE_HOST === 'undefined') {
            this.urlPrefix = "";
        }
        else
        {
            this.urlPrefix = "http://" + process.env.REACT_APP_BASE_HOST;
        }

        //this.urlPrefix = process.env.REACT_APP_BASE_URL ; // Only for testing.
        console.log("urlPrefix:" +  this.urlPrefix);

    }

    onSelectFile (bookId) {
        this.setState({
            bookId: bookId,
            hasFileOpened: true
        })
    }

    onSelectionChange(selectionString) {
        this.tableSidebar.handleSelectionChange(selectionString);
    }

    onFormSubmit ({
        cellRange,
        tableName,
        schema
    }) {
        const zBookId = this.state.bookId;
        const zSheetName = 'Sheet1';
        fetch(this.urlPrefix + "/api/createTable", {
            method: "POST",
            cache: "no-cache",
            headers: {
                "Content-Type": "application/json; charset=utf-8",
                "auth-token": this.state.username
            },
            body: JSON.stringify({
                bookId: zBookId,
                sheetName: zSheetName,
                tableName: tableName,
                row1: cellRange[0],
                col1: cellRange[1],
                row2: cellRange[2],
                col2: cellRange[3],
                schema: schema
            })
        })
            .then(res => res.json())
            .then(
                (result) => {
                    console.log(result);
                }
            )
            .catch((error) => {
                console.error(error);
            });

    }

    componentDidUpdate() {
        if (this.grid!==null)
        {
            this.grid.loadBook();
        }
    }

    render () {
        // console.log(this)
        this.grid = null;
        this.tableSidebar = null;
        if (!this.state.hasFileOpened) {
            return (
                <div>
                    <Toolbar username={this.state.username} onSelectFile={this.onSelectFile} />
                    <Stylebar />
                    <StartupBox username={this.state.username} onSelectFile={this.onSelectFile}/>
                </div>
            )
        } else {
            return (
                <div>
                    <Toolbar username={this.state.username} onSelectFile={this.onSelectFile} />
                    <Stylebar />
                    <Sidebar.Pushable>
                        <TableSidebar ref={ref => this.tableSidebar = ref} onFormSubmit={this.onFormSubmit} />
                        <Sidebar.Pusher>
                            <DSGrid bookId={this.state.bookId} ref={ref => this.grid = ref} onSelectionChange={this.onSelectionChange}/>
                        </Sidebar.Pusher>
                    </Sidebar.Pushable>
                </div>
            )
        }
        
    }
}

export default App;

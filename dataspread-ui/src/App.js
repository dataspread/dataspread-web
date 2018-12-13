import React, {Component} from 'react';
import './App.css';
import DSGrid from './dsgrid';
import Toolbar from './Components/Menu/toolbar'
import Stylebar from './Components/Stylebar'
import StartupBox from './Components/StatupBox'
import HierarchiForm from "./Components/HierarchiForm";

class App extends Component {

    constructor(props){
        super(props);

        this.state = {
            bookId:"",
            filename:"",
            hasFileOpened: false,
            username:"",

        }
        this.onSelectFile = this.onSelectFile.bind(this)
        this.onNavFormOpen = this.onNavFormOpen.bind(this)
        // this.onHierFormOpen = this.onHierFormOpen.bind(this)

    }

    onSelectFile (bookId) {
        this.setState({
            bookId: bookId,
            hasFileOpened: true
        })
    }

    componentDidUpdate() {
        if (this.grid!==null)
        {
            this.grid.loadBook();
        }
    }

    onNavFormOpen(){
        if(this.grid !== null) {
            this.grid.openNavForm();
        }
    }
    // onHierFormOpen(){
    //     if(this.grid !== null) {
    //         this.grid.openHierForm();
    //     }
    // }



    render () {
        // console.log(this)
        this.grid = null;
        if (!this.state.hasFileOpened) {
            return (
                <div>
                    <Toolbar username={this.state.username} onSelectFile={this.onSelectFile} onNavFormOpen={this.onNavFormOpen} grid = {this.grid}/>
                    <Stylebar />
                    <StartupBox username={this.state.username} onSelectFile={this.onSelectFile}/>
                </div>
            )
        } else {
            return (
                <div>
                    <Toolbar username={this.state.username} onSelectFile={this.onSelectFile} onNavFormOpen={this.onNavFormOpen} grid = {this.grid}/>
                    <Stylebar />
                    <DSGrid bookId={this.state.bookId} ref={ref => this.grid = ref} />
                </div>
            )
        }

    }
}

export default App;

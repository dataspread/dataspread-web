import React, {Component} from 'react';
import './App.css';
import DSGrid from './dsgrid';
import Toolbar from './Components/Menu/toolbar'
import StartupBox from './Components/StatupBox'

class App extends Component {

    constructor(props){
        super(props);
        // this.setFileId = ''
        // this.filename = ''
        // this.hasFileOpened = false
        this.state = {
            fileId:"",
            filename:"",
            hasFileOpened: false,
            username:""

        }
        this.onSelectFile = this.onSelectFile.bind(this)

    }

    onSelectFile (fileId) {
        this.setState({
            fileId: fileId,
            hasFileOpened: true
        })
    }

    render () {
        
        if (!this.state.hasFileOpened) {
            return (
                <div>
                    <Toolbar username={this.state.username} onSelectFile={this.onSelectFile}/>
                    <StartupBox username={this.state.username} onSelectFile={this.onSelectFile}/>
                </div>
            )
        } else {
            return (
                <div>
                    <Toolbar username={this.state.username}/>
                    <DSGrid filename={this.state.fileId} />
                </div>
            )
        }
        
    }
}

export default App;

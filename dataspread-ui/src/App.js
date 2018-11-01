import React, {Component} from 'react';
import './App.css';
import DSGrid from './dsgrid';
import Toolbar from './Components/Menu/toolbar'
import StartupBox from './Components/StatupBox'

class App extends Component {

    constructor(props){
        super(props);
        this.filename = 'hello.txt'
        this.hasFileOpened = false
    }

    render () {

        if (!this.hasFileOpened) {
            return (
                <div>
                    <Toolbar username={this.username}/>
                    <StartupBox></StartupBox>
                </div>
            )
        } else {
            return (
                <div>
                    <Toolbar username={this.username}/>
                    <DSGrid filename={this.filename}/>
                </div>
            )
        }
        
    }
}

export default App;

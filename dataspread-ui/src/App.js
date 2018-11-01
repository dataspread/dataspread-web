import React, {Component} from 'react';
import {Dropdown, Menu, Modal} from 'semantic-ui-react'
import './App.css';
import DSGrid from './dsgrid';
import Toolbar from './Components/Menu/toolbar'
// import ModalAboutUs from './Components/Menu/Help/about'
// import ModalOpenFile from './Components/Menu/File/load'
// import ModalImportFile from './Components/Menu/File/import'

function WorkingArea(props) {
    const hasFileOpened = props.hasFileOpened;
    if (hasFileOpened) {
      return <DSGrid filename={this.props.filename}/>
    }
    return (
        <div>Hello You</div>
    );
  }
  
class App extends Component {

    constructor(props){
        super(props);
        this.filename = 'hello.txt'
        this.hasFileOpened = true
    }

    
    render () {
        return (

            <div>
                <Toolbar username={this.username}/>
                <WorkingArea hasFileOpened={this} />
            </div>
        )
    }
}

export default App;

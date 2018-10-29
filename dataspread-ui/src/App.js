import React, {Component} from 'react';
import {Dropdown, Menu, Modal} from 'semantic-ui-react'
import './App.css';
import DSGrid from './dsgrid';
import ModalAboutUs from './Components/Menu/Help/about'
import ModalOpenFile from './Components/Menu/File/file'
import ModalImportFile from './Components/Menu/File/import'


class App extends Component {

    constructor(props){
        super(props);
        this.filename = 'hello.txt'
    }

    render () {
        return (
            <div>
                <Modal>
                    <Modal.Header>Select a Spreadsheet</Modal.Header>
                    <Modal.Content>
                        <Dropdown placeholder='Select Country' fluid search selection options={['a', 'b']}/>
                    </Modal.Content>
                </Modal>


                <Menu size='tiny'>
                    <Menu.Item>
                        <img src='favicon.ico' alt='DS'/>
                    </Menu.Item>

                    <Dropdown item text='File'>
                        <Dropdown.Menu>
                            <ModalOpenFile></ModalOpenFile>
                            <ModalImportFile></ModalImportFile>
                        </Dropdown.Menu>
                    </Dropdown>

                    <Dropdown item text='Edit'>
                        <Dropdown.Menu>
                            <Dropdown.Item>Copy</Dropdown.Item>
                            <Dropdown.Item>Paste</Dropdown.Item>
                        </Dropdown.Menu>
                    </Dropdown>

                    <Dropdown item text='Help'>
                        <Dropdown.Menu>
                            <ModalAboutUs></ModalAboutUs>
                        </Dropdown.Menu>
                    </Dropdown>


                    <Menu.Menu position='right'>
                        <Menu.Item>
                            Sign In
                        </Menu.Item>
                    </Menu.Menu>
                </Menu>


                <DSGrid filename={this.filename}/>
                
            </div>
        )
    }
}

export default App;

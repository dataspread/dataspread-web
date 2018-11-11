import React, {Component} from 'react';
import {Dropdown, Menu, Modal} from 'semantic-ui-react';
import './App.css';
import DSGrid from './dsgrid';


class App extends Component {
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
                            <Dropdown.Item>Open</Dropdown.Item>
                            <Dropdown.Item>Import</Dropdown.Item>
                        </Dropdown.Menu>
                    </Dropdown>

                    <Dropdown item text='Edit'>

                    </Dropdown>

                    <Dropdown item text='Help'>
                        <Dropdown.Menu>
                            <Dropdown.Item>About</Dropdown.Item>
                        </Dropdown.Menu>
                    </Dropdown>


                    <Menu.Menu position='right'>
                        <Menu.Item>
                            Sign In
                        </Menu.Item>
                    </Menu.Menu>
                </Menu>


                <DSGrid/>
            </div>
        )
    }
}

export default App;

import React, {Component} from 'react';
import {Dropdown, Menu} from 'semantic-ui-react'
import ModalAboutUs from './about'
import ModalOpenFile from './load'
import ModalImportFile from './import'
import HierarchiForm from ".././HierarchiForm";
import NameForm from '.././NameForm'


const favstyle = {
    height: '1.5em',
    width: '1.5em',
};

export default class LoadedToolbar extends Component {
    constructor(props) {
        super(props)
        // this.handler = this.handler.bind(this);
        this.handleNav = this.handleNav.bind(this);
        this.handleBin = this.handleBin.bind(this);
        this.state = {
            navOpen: false
        }
    }

    handleNav(e) {
        this.props.onNavFormOpen();
    }

    handleBin(e) {
        this.props.onBinFormOpen();
    }


    render() {
        return (
            <Menu size='mini' className='toolbar'>
                <Menu.Item>
                    <img src='favicon.ico' style={favstyle} alt='DS'/>
                </Menu.Item>

                <Dropdown item text='File'>
                    <Dropdown.Menu>
                        <Dropdown.Item>New</Dropdown.Item>
                        <ModalOpenFile inMenu={true} onSelectFile={this.props.onSelectFile}/>
                        <ModalImportFile inMenu={true}/>
                    </Dropdown.Menu>
                </Dropdown>

                <Dropdown item text='Edit'>
                    <Dropdown.Menu>
                        <Dropdown.Item>Copy</Dropdown.Item>
                        <Dropdown.Item>Paste</Dropdown.Item>
                    </Dropdown.Menu>
                </Dropdown>

                <Dropdown item text='Nav'>
                    <Dropdown.Menu>
                        <Dropdown.Item onClick={this.handleNav}>Explore</Dropdown.Item>
                        <HierarchiForm ref={ref => this.hier = ref} submitHierForm={this.props.submitHierForm}/>
                        {this.state.navOpen ?
                            <Dropdown.Item onClick={this.handleBin}>Customize Bins</Dropdown.Item> : null}
                    </Dropdown.Menu>
                </Dropdown>
                <Dropdown item text='Help'>
                    <Dropdown.Menu>
                        <ModalAboutUs/>
                    </Dropdown.Menu>
                </Dropdown>
                <Menu.Menu position='right'>
                    <Menu.Item>
                        <NameForm bookId={this.props.bookId} key={new Date().getTime()}/>
                    </Menu.Item>

                </Menu.Menu>
            </Menu>
        )
    }
}




                
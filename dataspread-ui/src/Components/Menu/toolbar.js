import React, {Component} from 'react';
import {Dropdown, Menu, Modal} from 'semantic-ui-react'
import ModalAboutUs from './Help/about'
import ModalOpenFile from './File/load'
import ModalImportFile from './File/import'

export default class Toolbar extends Component {
    render() {
      return (
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
      )
    }
  }




                
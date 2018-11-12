import React, {Component} from 'react';
import {Dropdown, Menu, Button} from 'semantic-ui-react'

const favstyle = {
    height: '1.5em',
    width: '1.5em',
  };
  
export default class Stylebar extends Component {
    constructor(props) {
        super(props)
        this.handler = this.handler.bind(this)
    }

    handler(e) {
        e.preventDefault()
        this.setState({
            open: false
        })
    }


    render() {
      return (
        <Menu size='mini' borderless='true' attached='bottom'>

            <div class='item stylebar-padding'>
                <Button size='mini' className='no-border' basic='true' icon='undo'/>
                <Button size='mini' className='no-border' basic='true' icon='redo' />
                <Button size='mini' className='no-border' basic='true' icon='print' />
            </div>

            <Button size='mini' className='no-border' basic='true' icon='redo' />
        
            
            
            <Dropdown item text='Edit'>
                <Dropdown.Menu>
                    <Dropdown.Item>Copy</Dropdown.Item>
                    <Dropdown.Item>Paste</Dropdown.Item>
                </Dropdown.Menu>
            </Dropdown>

            <Dropdown item text='Help'>
                <Dropdown.Menu>
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




                
import React, {Component} from 'react';
import {Dropdown, Menu, Button} from 'semantic-ui-react'

// const favstyle = {
//     height: '1.5em',
//     width: '1.5em',
//   };
  
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
        console.log("style")
      return (
        <Menu size='mini' borderless={true} attached='bottom'>

            <div className='item stylebar-padding'>
                <Button size='mini' className='no-border' basic={true} icon='undo'/>
                <Button size='mini' className='no-border' basic={true} icon='redo' />
                <Button size='mini' className='no-border' basic={true} icon='print' />
            </div>

            <div className='item stylebar-padding'>
                <Button size='mini' className='no-border' basic={true} icon='copy'/>
                <Button size='mini' className='no-border' basic={true} icon='cut' />
                <Button size='mini' className='no-border' basic={true} icon='paste' />
            </div>

            <div className='item stylebar-padding'>
                <Dropdown item text='Font'>
                    <Dropdown.Menu>
                        <Dropdown.Item>Copy</Dropdown.Item>
                        <Dropdown.Item>Paste</Dropdown.Item>
                    </Dropdown.Menu>
                </Dropdown>
                <Dropdown item text='Size'>
                    <Dropdown.Menu>
                        <Dropdown.Item>Copy</Dropdown.Item>
                        <Dropdown.Item>Paste</Dropdown.Item>
                    </Dropdown.Menu>
                </Dropdown>
            </div>          

            <div className='item stylebar-padding'>
                <Button size='mini' className='no-border' basic={true} icon='bold'/>
                <Button size='mini' className='no-border' basic={true} icon='italic' />
                <Button size='mini' className='no-border' basic={true} icon='underline' />                
                <Button size='mini' className='no-border' basic={true} icon='strikethrough' />
                <Button size='mini' className='no-border' basic={true} icon='subscript' />
                <Button size='mini' className='no-border' basic={true} icon='superscript' />
                <Button size='mini' className='no-border' basic={true} icon='text height' />
                <Button size='mini' className='no-border' basic={true} icon='text width' />
                
                
            </div>

            <div className='item stylebar-padding'>
                <Button size='mini' className='no-border' basic={true} icon='align right' />
                <Button size='mini' className='no-border' basic={true} icon='align center'/>
                <Button size='mini' className='no-border' basic={true} icon='align left' />
                <Button size='mini' className='no-border' basic={true} icon='align justify' />            
            </div>

            <div className='item stylebar-padding'>
                <Button size='mini' className='no-border' basic={true} icon='table' />
                <Button size='mini' className='no-border' basic={true} icon='align center'/>
                <Button size='mini' className='no-border' basic={true} icon='align left' />
                <Button size='mini' className='no-border' basic={true} icon='align justify' />            
            </div>        

            <div className='item stylebar-padding'>
                <Button size='mini' className='no-border' basic={true} icon='linkify' />
                <Button size='mini' className='no-border' basic={true} icon='chart area'/>
                <Button size='mini' className='no-border' basic={true} icon='sort' />
                <Button size='mini' className='no-border' basic={true} icon='code' />
                <Button size='mini' className='no-border' basic={true} icon='filter' />            
            </div>        

        </Menu>
      )
    }
  }




                
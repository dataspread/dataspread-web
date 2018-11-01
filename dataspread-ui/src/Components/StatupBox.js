import React, {Component} from 'react'
import { Button, Divider, Segment } from 'semantic-ui-react'
import ModalOpenFile from './Menu/File/load'
import ModalImportFile from './Menu/File/import'


export default class StartupBox extends Component {

  render() {
      return (
        <div style={center_screen}>
            <Segment padded>
                <Button secondary fluid>
                    <div>
                        New File
                    </div>
                </Button>

                <Divider horizontal>Or</Divider>
                
                <Button secondary fluid>
                    <ModalOpenFile></ModalOpenFile>
                </Button>
            
                <Divider horizontal>Or</Divider>
            
                <Button secondary fluid>
                    <ModalImportFile></ModalImportFile>
                </Button>
            
            </Segment>
        </div>
    )
  }
}

//TODO: console log errors here
const center_screen = {
    'display': 'flex',
    'flex-direction': 'column',
    'justify-content': 'center',
    'align-items': 'center',
    'text-align': 'center',
    'min-height': '100vh',
};
  
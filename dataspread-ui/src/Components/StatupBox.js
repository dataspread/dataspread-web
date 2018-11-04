import React, {Component} from 'react'
import { Button, Divider, Segment } from 'semantic-ui-react'
import ModalOpenFile from './Menu/load'
import ModalImportFile from './Menu/import'


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
                    <ModalOpenFile {...this.props}/>
                </Button>
            
                <Divider horizontal>Or</Divider>
            
                <Button secondary fluid>
                    <ModalImportFile/>
                </Button>
            
            </Segment>
        </div>
    )
  }
}

//TODO: console log errors here
const center_screen = {
    'display': 'flex',
    'flexDirection': 'column',
    'justifyContent': 'center',
    'alignItems': 'center',
    'textAlign': 'center',
    'minHeight': '100vh',
};
  
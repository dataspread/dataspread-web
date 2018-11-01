import React, {Component} from 'react'
import { Button, Divider, Grid, Header, Icon, Search, Segment } from 'semantic-ui-react'



export default class StartupBox extends Component {

  render() {
      return (
        <div style={center_screen}>
            <Segment padded>
                <Button primary fluid>
                    Login
                </Button>
                <Divider horizontal>Or</Divider>
                <Button secondary fluid>
                    Sign Up Now
                </Button>
            </Segment>
{/*             
            <Segment vertical>
                <Grid.Column>
                <Header icon>
                    <Icon name='search' />
                    Find Country
                </Header>
                <Search placeholder='Search countries...' />
                </Grid.Column>

                <Divider horizontal>Or</Divider>

                <Grid.Column>
                <Header icon>
                    <Icon name='search' />
                    Find Country
                </Header>
                <Search placeholder='Search countries...' />
                </Grid.Column>

            </Segment> */}
        </div>
    )
  }
}

const center_screen = {
    'display': 'flex',
    'flex-direction': 'column',
    'justify-content': 'center',
    'align-items': 'center',
    'text-align': 'center',
    'min-height': '100vh',
};
  
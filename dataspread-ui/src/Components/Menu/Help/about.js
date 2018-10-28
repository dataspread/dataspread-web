import React, {Component} from 'react'
import {Dropdown, Button, Header, Icon, Modal } from 'semantic-ui-react'

export default class ModalAboutUs extends Component {
  state = { modalOpen: false }

  handleOpen = () => this.setState({ modalOpen: true })

  handleClose = () => this.setState({ modalOpen: false })

  render() {
    return (
      <Modal
        trigger={<Dropdown.Item onClick={this.handleOpen}>About Us</Dropdown.Item>}
        open={this.state.modalOpen}
        onClose={this.handleClose}
        basic
        size='small'
      >
        <Header icon='info' content='About DataSpread' />
        <Modal.Content>
        <img src='http://dataspread.github.io/images/dataspread-fiverr2-cropped.png' height="50em"></img>
        <p>
          <br></br>
        Spreadsheets have found ubiquitous use by scientists, business and financial analysts, researchers,and lay users. However, spreadsheets cannot express complex operations (e.g., joins), cannot handle large datasets, do not support collaboration, and foster errors, redundancy, and stale data. On the other hand, relational databases are well-known to be powerful and scalable, but are not flexible, intuitive, and interactive. <br></br><br></br> DataSpread addresses these limitations by holistically unifying spread-sheets with databases: preserving spreadsheets as the front-end, and databases as the back-end.
        </p>
        </Modal.Content>
        <Modal.Actions>
          <Button color='blue' onClick={this.handleClose} inverted>
            <Icon name='checkmark' /> Close
          </Button>
          <Button color='green' target="_blank" href='https://http://dataspread.github.io' inverted>
            <Icon name='external alternate' /> Learn More
          </Button>
        </Modal.Actions>
      </Modal>
    )
  }
}
import React, {Component} from 'react'
import {Dropdown, Button, Header, Icon, Modal, Input, Loader } from 'semantic-ui-react'

export default class ModalOpenFile extends Component {
  state = { modalOpen: false }

  handleOpen = () => this.setState({ modalOpen: true })

  handleClose = () => this.setState({ modalOpen: false })

  render() {
    return (
      <Modal
        trigger={<Dropdown.Item onClick={this.handleOpen}>Open File</Dropdown.Item>}
        open={this.state.modalOpen}
        onClose={this.handleClose}
      >
        <Header icon='folder open outline' content='Open File' />

        <Modal.Content>

          <div>

                <div>
                  <Input
                    placeholder='Book Name...'
                    name="bookName"
                    onChange={this._handleEvent}
                    action={
                      <Button
                      name="bookLoadButton"
                      onClick={this._handleEvent}>
                      Load
                      </Button>
                    }
                  />
                </div>
            </div>


        </Modal.Content>

        <Modal.Actions>
          <Button color='blue' onClick={this.handleClose} inverted>
            <Icon name='checkmark' /> Close
          </Button>
        </Modal.Actions>
      </Modal>
    )
  }

  // Todo: resove the passing of this

  // _handleEvent(event) {
  //   const target = event.target;
  //   const name = target.name;
  //   if (name === "bookName") {
  //       this.bookName = target.value;
  //       console.log(this.bookName);
  //   }
  //   else if (name === "bookLoadButton") {
  //       fetch(this.urlPrefix + "/api/getSheets/" + this.bookName)
  //           .then(res => res.json())
  //           .then(
  //               (result) => {
  //                   this.dataCache.reset();
  //                   this.setState({
  //                       bookName: this.bookName,
  //                       sheetName: 'Sheet1',
  //                       rows: result['data']['sheets'][0]['numRow'],
  //                       columns: result['data']['sheets'][0]['numCol']
  //                   });
  //                   this.subscribed = false;
  //                   this.grid.scrollToCell ({ columnIndex: 0, rowIndex: 0 });
  //                   if (this.stompSubscription!=null)
  //                       this.stompSubscription.unsubscribe();
  //                   this.stompSubscription = this.stompClient
  //                       .subscribe('/user/push/updates',
  //                           this._processUpdates, {bookName: this.state.bookName,
  //                                   sheetName: this.state.sheetName,
  //                                   fetchSize: this.fetchSize});
  //                   console.log("book loaded rows:" + result['data']['sheets'][0]['numRow']);
  //               }
  //           )
  //           .catch((error) => {
  //               console.error(error);
  //           });
  //   }
  // }

}
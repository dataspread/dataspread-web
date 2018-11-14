import React, {Component} from 'react'
import ReactResumableJs from 'react-resumable-js'
import {Dropdown, Button, Header, Icon, Modal} from 'semantic-ui-react'
import Stomp from "stompjs";

export default class ModalImportFile extends Component {
  constructor(props) {
   	super(props);
   	if (typeof process.env.REACT_APP_BASE_HOST === 'undefined') {
   		this.urlPrefix = "";
   		this.stompClient = Stomp.client("ws://" + window.location.host + "/ds-push/websocket");
   	} else {
   		this.urlPrefix = "http://" + process.env.REACT_APP_BASE_HOST;
   		this.stompClient = Stomp.client("ws://" + process.env.REACT_APP_BASE_HOST + "/ds-push/websocket");
   	}
  }

  state = { modalOpen: false }

  handleOpen = () => this.setState({ modalOpen: true })

  handleClose = () => this.setState({ modalOpen: false})

  render() {
    return (
		<Modal
		trigger={<Dropdown.Item onClick={this.handleOpen}>Import File</Dropdown.Item>}
		open={this.state.modalOpen}
		onClose={this.handleClose}>


        <Header icon='upload' content='Import File' />
		
		{/* button still ugly, doesn't know where to config or override setting 
			Also not sure if API works.
		*/}

        <Modal.Content>
			<ReactResumableJs
			uploaderID="importBook"
			filetypes={["csv"]}
			fileAccept="text/csv"
			maxFileSize={100000000000}
			simultaneousUploads={4}
			fileAddedMessage="Started!"
			completedMessage="Complete!"
			service= {this.urlPrefix + "/api/importFile"}
			disableDragAndDrop={true}
			showFileList={false}
			onFileSuccess={(file, message) => {
				console.log(file, message);
			}}
			onFileAdded={(file, resumable) => {
				resumable.upload();
			}}
			maxFiles={1}
			onStartUpload={() => {
				console.log("Start upload");
			}}
			/>
        </Modal.Content>

        <Modal.Actions>
          <Button color='blue' onClick={this.handleClose} inverted>
            <Icon name='checkmark' /> Close
          </Button>
        </Modal.Actions>
      </Modal>
    )
  }

}
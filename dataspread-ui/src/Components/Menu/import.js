import React, {Component} from 'react'
import ReactResumableJs from 'react-resumable-js'
import {Dropdown, Button, Header, Icon, Modal} from 'semantic-ui-react'
import Stomp from "stompjs";

export default class ModalImportFile extends Component {
  constructor(props) {
  		super(props);
  		this.state = {
  			loadModalOpen: false,
  			fileStatus:"None"
  		};
      if (props.inMenu) {
        this.triggerObject = (<Dropdown.Item onClick={this.handleOpen}>Import</Dropdown.Item>);
      } else {
        this.triggerObject = (<Button secondary fluid onClick={this.handleOpen}>Import File</Button>);
			}
			
			this._handleload = this._handleload.bind(this);
			this.handleClose = this.handleClose.bind(this);

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

	onChange = (e, data) => {
		console.log(data);
		//console.log(data.value);
		//this.setState({ BooksSelected: data.value });
	}

	_handleLoad = (filename) => {
		this.setState({ loadModalOpen: false });
		// change filename to dsgrid
		this.props.onSelectFile(filename);
	}

  render() {
    return (
		<Modal
		trigger={this.triggerObject}
		open={this.state.modalOpen}
		onClose={this.handleClose}>


        <Header icon='upload' content='Import File' />
				
		
		{/* button still ugly, doesn't know where to config or override setting 
			Also not sure if API works.
		*/}

        <Modal.Content>
					<Header content={"Status: " + this.state.fileStatus} />
					
					<ReactResumableJs
					uploaderID="importBook"
					filetypes={["csv"]}
					startButton={true}
					fileAccept="text/csv"
					maxFileSize={100000000000}
					simultaneousUploads={4}
					fileAddedMessage="Started!"
					completedMessage="Complete!"
					service= {this.urlPrefix + "/api/importFile"}
					disableDragAndDrop={true}
					showFileList={false}
					onFileSuccess={(file, message) => {
						//pass in bookname
						this._handleLoad(file)
						console.log(file, message);
					}}
					onFileAdded={(file, resumable) => {
						console.log(file.file);
						this.setState({fileStatus:"selected "+ file.file.name})
						console.log("File added.");
						//resumable.upload();
					}}
					maxFiles={1}
					onStartUpload={() => {
						this.setState({fileStatus:"Uploading..."})
						console.log("Start upload");
					}}
					onUploadErrorCallback ={(file, message)=>{
						this.setState({fileStatus:"File upload ERROR"})
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
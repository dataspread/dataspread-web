import React, {Component} from 'react'
import ReactResumableJs from '../Elements/ReactResumableJs'
import {Dropdown, Button, Header, Icon, Modal} from 'semantic-ui-react'
import Stomp from "stompjs";

export default class ModalImportFile extends Component {
  constructor(props) {
  		super(props);
  		this.state = {
  			loadModalOpen: false,
				fileStatus:"Waiting For File",
				filename:"",
				resumable:undefined,
				fileId:""
  		};
      if (props.inMenu) {
        this.triggerObject = (<Dropdown.Item onClick={this.handleOpen}>Import</Dropdown.Item>);
      } else {
        this.triggerObject = (<Button secondary fluid onClick={this.handleOpen}>Import File</Button>);
			}
			
			this._handleLoad = this._handleLoad.bind(this);
			this.handleClose = this.handleClose.bind(this);
			this.handleOpen = this.handleOpen.bind(this);
			this._startUpload = this._startUpload.bind(this);
			this.sleep = this.sleep.bind(this);
			//this._loadFile = this._loadFile.bind(this);
   		if (typeof process.env.REACT_APP_BASE_HOST === 'undefined') {
				this.urlPrefix = "";
				this.stompClient = Stomp.client("ws://" + window.location.host + "/ds-push/websocket");
			} else {
				this.urlPrefix = "http://" + process.env.REACT_APP_BASE_HOST;
				this.stompClient = Stomp.client("ws://" + process.env.REACT_APP_BASE_HOST + "/ds-push/websocket");
   	}
  }

	// state = { 
	// 	modalOpen: false
	// }

  handleOpen = () => this.setState({ modalOpen: true })

  handleClose = () => this.setState({ modalOpen: false})

	onChange = (e, data) => {
		console.log(data);
		//console.log(data.value);
		//this.setState({ BooksSelected: data.value });
	}

	_handleLoad () {
		this.setState({ loadModalOpen: false });
		// change filename to dsgrid
		this.props.onSelectFile(this.state.filename);
	}

	_startUpload () {
		console.log("Uploading...")
		
		console.log(this.state)
		this.state.resumable.upload()
	}

	sleep = (milliseconds) => {
		return new Promise(resolve => setTimeout(resolve, milliseconds))
	}

  render() {
    return (
		<Modal
		className='importFileModel'
		trigger={this.triggerObject}
		open={this.state.modalOpen}
		onClose={this.handleClose}>


        <Header icon='upload' content='Import File' />

        <Modal.Content>
					<h4>Please Select File to Upload</h4>
									
					<ReactResumableJs
					uploaderID="importBook"
					filetypes={["csv", "xls", "xlsx"]}
					startButton={true}
					fileAccept="text/csv/xls/xlsx"
					maxFileSize={100000000000}
					simultaneousUploads={4}
					fileAddedMessage="Started!"
					completedMessage="Complete!"
					service= {this.urlPrefix + "/api/importFile"}
					disableDragAndDrop={true}
					showFileList={false}
					onFileSuccess={(file, fileServer) => {
						console.log('Success');
						this.setState({
							fileStatus: 'Done. Redirecting...',
							fileId: 'bjryj0wh7'
						},
							() => {
								this.sleep(2000).then(() => {
									console.log("Will load bookId: " + JSON.parse(fileServer).data)
									this.props.onSelectFile(JSON.parse(fileServer).data);
								})
							}
						)
					}}
					onFileAdded={(file, resumable) => {
						this.setState({
							fileStatus:"selected "+ file.file.name,
							resumable: resumable
						})
						console.log("File added.");
					}}
					maxFiles={1}
					// onStartUpload={(file, resumable) => {
					// 	this.setState({fileStatus:"Uploading..."})
					// 	console.log("Start upload");
					// 	resumable.upload();
					// }}
					onUploadErrorCallback ={(file, message)=>{
						this.setState({fileStatus:"File upload ERROR"})
					}}
					/>
					
        </Modal.Content>

        <Modal.Actions>
					
					<Header className='uploadTextStatusBar' content={"Status: " + this.state.fileStatus} />
					<Button onClick={this._startUpload}>Upload</Button>
          <Button color='blue' onClick={this.handleClose} inverted>
            <Icon name='checkmark' /> Close
          </Button>
        </Modal.Actions>
      </Modal>
    )
  }

}
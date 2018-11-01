import React, {Component} from 'react'
import {Dropdown, Button, Header, Icon, Modal, Input, Loader } from 'semantic-ui-react'



export default class ModalOpenFile extends Component {
  constructor(props) {
    super(props);

	this.state = {
      	modalOpen: false,
	  	data: null,
	  	BooksOptions: [],
		BooksSelected: ""
    };
  }

	handleOpen = () => this.setState({ modalOpen: true })

	handleClose = () => this.setState({ modalOpen: false })

	// fetch data from api
	componentDidMount() {
		fetch('http://kite.cs.illinois.edu:8080/api/getBooks')
		.then(response => response.json())
		.then(data => this.transform(data))
		.then(data => this.setState({ BooksOptions: data }));
	}

  	//transform data
  	transform = (raw_data) => {
		var data = []
		for (var book in raw_data) {
			var d = {
				"text": raw_data[book].text,
				"value": raw_data[book].value,
				"description": raw_data[book].description,
				"content": <Header icon='table' content={raw_data[book].text} subheader={raw_data[book].content} />
			}
			data.push(d)
		}
		return data
	}

	onChange = (e, data) => {
		console.log(data)
		console.log(data.value);
		this.setState({ BooksSelected: data.value });
	}

	_handleEvent = () =>{
		//
	}

	render() {
		console.log(this.state)
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
						<Dropdown placeholder='Select File' fluid search selection 
						options={this.state.BooksOptions} onChange={this.onChange}  />
					</div>
				</div>
			</Modal.Content>

			<Modal.Actions>
				<Button name="bookLoadButton" onClick={this._handleEvent}>
				<Icon name='checkmark' /> Load
				</Button>
				<Button color='blue' onClick={this.handleClose} inverted>
				<Icon name='close' /> Close
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
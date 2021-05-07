import React, {Component} from 'react'
import {Dropdown, Button, Header, Icon, Modal} from 'semantic-ui-react'
import Stomp from "stompjs";


export default class ModalCloseFile extends Component {
    constructor(props) {
        super(props);
        this.state = {
            loadModalOpen: false,
            data: null,
            BooksOptions: [],
            BooksSelected: ""
        };
        if (props.inMenu) {
        	this.triggerObject = (<Dropdown.Item onClick={this.handleOpen} text='Close' icon='trash'/>);
		} else {
            this.triggerObject = (<Button secondary fluid onClick={this.handleOpen}>Close Book</Button>);
		}
        this._handleDelete = this._handleDelete.bind(this);
        this.handleClose = this.handleClose.bind(this);
        if (typeof process.env.REACT_APP_BASE_HOST === 'undefined') {
            this.urlPrefix = "";
            this.stompClient = Stomp.client("ws://" + window.location.host + "/ds-push/websocket");
        } else {
            this.urlPrefix = "http://" + process.env.REACT_APP_BASE_HOST;
            console.log('error?: ' + this.urlPrefix)
            this.stompClient = Stomp.client("ws://" + process.env.REACT_APP_BASE_HOST + "/ds-push/websocket");
        }
    }

	handleOpen = () =>
	{
		// fetch data from api
		fetch(this.urlPrefix + '/api/getBooks')
			.then(response => response.json())
			.then(data => this.transform(data))
			.then(data => this.setState({
				BooksOptions: data,
				loadModalOpen: true
			}))
			.catch(()=> {
				alert("Lost connection to server.")
			});

	}

	handleClose = () => this.setState({ loadModalOpen: false})


  	//transform data
  	transform = (raw_data) => {
        let data = [];
        for (let book in raw_data) {
            let d = {
                "text": raw_data[book].text,
                "value": raw_data[book].value,
                "description": raw_data[book].description,
                "content": <Header icon='table' content={raw_data[book].text} subheader={raw_data[book].content}/>
			};
			delete d["description"];
            data.push(d)
		}
		return data
	}

	onChange = (e, data) => {
		console.log(data);
		console.log(data.value);
		this.setState({ BooksSelected: data.value });
	}

    _handleDelete () {
        this.setState({ loadModalOpen: false });

		console.log(this.state.BooksSelected);

        let data = {'bookId': this.state.BooksSelected}
        let req = {
			method: 'DELETE',
			headers: {
				'auth-token': 'guest'
			},
			body: JSON.stringify(data)
		};
		console.log(JSON.stringify(req));

		fetch(this.urlPrefix + '/api/deleteBook', req)
			.then(response => response.json())
			.then(data => console.log(data))
			.catch((response)=> {
				alert(response)
			});
	}

	render() {
		console.log(this.urlPrefix + '/api/getBooks')
		return (
		<Modal
			trigger={this.triggerObject}
			open={this.state.loadModalOpen}
			onClose={this.handleClose}>
			<Header icon='folder open outline' content='Close File' />

			<Modal.Content>
			    <div>
					<div>
						<Dropdown placeholder='Select File' fluid multiple search selection
						options={this.state.BooksOptions} onChange={this.onChange}  />
					</div>
				</div>
			</Modal.Content>

			<Modal.Actions>
				<Button name="bookLoadButton" onClick={this._handleDelete}>
				<Icon name='checkmark' /> Delete Book
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
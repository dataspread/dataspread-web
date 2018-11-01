import React, {Component} from 'react'
import {Dropdown, Button, Header, Icon, Modal, Input, Loader } from 'semantic-ui-react'

export default class ModalOpenFile extends Component {
  constructor(props) {
    super(props);

	//content: <Header icon='table' content='Mobile' subheader='The smallest size' />

    this.state = {
      modalOpen: false,
	  data: null,
	  BooksOptions: [
		{
			"text": "book6540",
			"value": "pjmzvqw7k",
			"lastModified": "2018-10-08T05:52:48.410+0000",
			"createdTime": "2018-10-08T05:52:48.410+0000",
			"content":<Header icon='table' content='book6540' 
				subheader='Last Modified: 2018-10-08	|	Created: 2018-10-08' />
		},
		{
			"text": "book2117",
			"value": "qjmzy9as8",
			"lastModified": "2018-10-08T07:03:06.337+0000",
			"createdTime": "2018-10-08T07:03:06.337+0000",
			"content":<Header icon='table' content='book2117' 
				subheader='Last Modified: 2018-10-08	|	Created: 2018-10-08' />
		},
		{
			"text": "book3098",
			"value": "mjn4n6vv5",
			"lastModified": "2018-10-11T13:52:08.666+0000",
			"createdTime": "2018-10-11T13:52:08.666+0000",
			"content":<Header icon='table' content='book3098' 
				subheader='Last Modified: 2018-10-11	|	Created: 2018-10-11' />
		},
		{
			"text": "book5195",
			"value": "jjn4n7653",
			"lastModified": "2018-10-11T13:52:21.977+0000",
			"createdTime": "2018-10-11T13:52:21.977+0000",
			"content":<Header icon='table' content='book5195' 
				subheader='Last Modified: 2018-10-11	|	Created: 2018-10-11' />
		},
		{
			"text": "book5740",
			"value": "fjn4n83ao",
			"lastModified": "2018-10-11T13:53:04.945+0000",
			"createdTime": "2018-10-11T13:53:04.945+0000",
			"content":<Header icon='table' content='book5740' 
				subheader='Last Modified: 2018-10-11	|	Created: 2018-10-11' />
		},
		{
			"text": "book5379",
			"value": "bjnc4ncu9",
			"lastModified": "2018-10-16T19:35:14.756+0000",
			"createdTime": "2018-10-16T19:35:14.756+0000",
			"content":<Header icon='table' content='book5379' 
				subheader='Last Modified: 2018-10-16	|	Created: 2018-10-16' />
		},
		{
			"text": "book1852",
			"value": "ojnc4nh47",
			"lastModified": "2018-10-16T19:35:21.399+0000",
			"createdTime": "2018-10-16T19:35:21.399+0000",
			"content":<Header icon='table' content='book1852' 
				subheader='Last Modified: 2018-10-16	|	Created: 2018-10-16' />
		},
		{
			"text": "book3757",
			"value": "jjnc4nn82",
			"lastModified": "2018-10-16T19:35:27.335+0000",
			"createdTime": "2018-10-16T19:35:27.335+0000",
			"content":<Header icon='table' content='book3757' 
				subheader='Last Modified: 2018-10-16	|	Created: 2018-10-16' />
		},
		{
			"text": "book4937",
			"value": "gjnc4nuzn",
			"lastModified": "2018-10-16T19:35:37.388+0000",
			"createdTime": "2018-10-16T19:35:37.388+0000",
			"content":<Header icon='table' content='book4937' 
				subheader='Last Modified: 2018-10-16	|	Created: 2018-10-16' />
		},
		{
			"text": "book3487",
			"value": "mjnvesevn",
			"lastModified": "2018-10-30T07:26:43.486+0000",
			"createdTime": "2018-10-30T07:26:43.486+0000",
			"content":<Header icon='table' content='book3487' 
				subheader='Last Modified: 2018-10-30	|	Created: 2018-10-30' />
		},
		{
			"text": "book8236",
			"value": "cjnvesjlc",
			"lastModified": "2018-10-30T07:26:49.401+0000",
			"createdTime": "2018-10-30T07:26:49.401+0000",
			"content":<Header icon='table' content='book8236' 
				subheader='Last Modified: 2018-10-30	|	Created: 2018-10-30' />
		},
		{
			"text": "book6707",
			"value": "ajnvesp0i",
			"lastModified": "2018-10-30T07:26:56.423+0000",
			"createdTime": "2018-10-30T07:26:56.423+0000",
			"content":<Header icon='table' content='book6707' 
				subheader='Last Modified: 2018-10-30	|	Created: 2018-10-30' />
		},
		{
			"text": "book5196",
			"value": "yjnvet5ek",
			"lastModified": "2018-10-30T07:27:17.665+0000",
			"createdTime": "2018-10-30T07:27:17.665+0000",
			"content":<Header icon='table' content='book5196' 
				subheader='Last Modified: 2018-10-30	|	Created: 2018-10-30' />
		},
		{
			"text": "book4734",
			"value": "jjnxg8n3k",
			"lastModified": "2018-10-31T17:42:52.463+0000",
			"createdTime": "2018-10-31T17:42:52.463+0000",
			"content":<Header icon='table' content='book4734' 
				subheader='Last Modified: 2018-10-31	|	Created: 2018-10-31' />
		}
	]
    };
  }

  handleOpen = () => this.setState({ modalOpen: true })

  handleClose = () => this.setState({ modalOpen: false })

  componentDidMount() {
    fetch('https://crossorigin.me/http://kite.cs.illinois.edu:8080/api/getBooks')
      .then(response => response.json())
      .then(data => this.setState({ data }));
  }

  render() {
    // console.log("hi")
    // console.log(this.data)
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
					<Dropdown placeholder='Select File' fluid search selection options={this.state.BooksOptions} />
				</div>
				<div>
					<Button
                      name="bookLoadButton"
                      onClick={this._handleEvent}>
                      Load
                     </Button>
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
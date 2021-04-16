import React, {Component} from 'react'
import {Breadcrumb, Dropdown} from 'semantic-ui-react'
import './Navigation.css';
export default class NameForm extends Component {
    constructor(props) {
        super(props);
        console.log(props.bookId);
        this.state = { toggle: true, text: "New Document" };

        this.toggleInput = this.toggleInput.bind(this);
        this.isenter = this.isenter.bind(this);
        this.isenter2 = this.isenter2.bind(this);
        this.handleChange = this.handleChange.bind(this);
        this.fetch2 = this.fetch2.bind(this);
        this.fetchn = this.fetchn.bind(this);
        this.fetchn();
    }

    toggleInput() {
        this.setState({ toggle: false });
    }
    isenter(event) {
        if (event.key === "Enter") {
            this.setState({ toggle: true });
            this.fetch2();
        }
    }
    isenter2(event) {
        this.setState({ toggle: true });
        this.fetch2();
    }

    handleChange(event) {
        this.setState({ text: event.target.value });

    }
    fetch2() {
        console.log(this.props.bookId);
        let queryData = {
            "bookId": this.props.bookId,
            "newBookName":this.state.text
        };
        console.log(queryData);
        console.log(JSON.stringify(queryData));
        let tmp = (this.state.urlPrefix+'').slice(0,-10);
        fetch(tmp + "/api/changeBookName", {
            method: "PUT",
            body: JSON.stringify(queryData),
            headers: {
                "Content-Type": "text/plain"
            }
        })
            .then((response) => response.json())
            .then((data) => {console.log("newname"),
                console.log(data)});
            //.then((data) => this.transform(data))
            //.catch(() => {
               // alert("Lost connection to server.");
           // });

    }
    fetchn() {
        let queryData = {
            "bookId": this.props.bookId,
        };
        let tmp = (this.state.urlPrefix+'').slice(0,-10);
        console.log(queryData);
        console.log(JSON.stringify(queryData));
        fetch(tmp + "/api/getname",{
            method: "POST",
            body: JSON.stringify(queryData),
            headers: {
                "Content-Type": "text/plain"
            }
        })
            .then(response => response.json())
            .then(
                data => {
                    console.log(data),
                        console.log(data.name),
                    this.setState({ text: data.name })
                });
        console.log("reset");
        console.log(this.state.text);

    }

    render() {
        return (
            <div className="App">
                {this.state.toggle ? (
                    <p onDoubleClick={this.toggleInput}>{this.state.text}</p>
                ) : (
                    <input
                        type="text"
                        value={this.state.text}
                        onChange={this.handleChange}
                        onKeyDown={this.isenter}
                        onDoubleClick={this.isenter2}
                    />
                )}
            </div>
        );
    }
}

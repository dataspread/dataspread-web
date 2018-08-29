import React, {Component} from 'react';
import { Input } from 'semantic-ui-react'


export default class Cell extends Component {

    constructor(props) {
        super(props);
        this.state = {
            editing: false,
            value: '',
            formula: ''
        }
        this.handleClick = this.handleClick.bind(this);
        this.handleBlur = this.handleBlur.bind(this);
    }

    componentDidMount() {
        if (this.state.editing)
            this.input.focus();
    }

    handleClick(e) {
        this.setState({
            editing: true
        })
    }

    handleBlur(e) {
        this.setState({
            editing: false
        })
    }

    render() {
            if (this.state.editing) {
                return (<Input
                    transparent
                    ref={(input) => { this.input = input;}}
                    onBlur={this.handleBlur}/>)
            }
            else {
                return (
                    <div
                        style={{
                            display: 'flex',
                            flex:'1 1'
                        }}
                        onClick={this.handleClick}>
                    {this.state.value}</div>);
            }

    }
}
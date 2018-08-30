import React, {Component} from 'react';
import { Input } from 'semantic-ui-react'
import 'semantic-ui-css/semantic.min.css';

export default class Cell extends Component {

    constructor(props) {
        super(props);
        this.state = {
            editing: false,
            value: '',
            formula: ''
        }
        console.log('Mangesh ' + props.style);
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
                    style={this.props.style}
                    transparent
                    ref={(input) => { this.input = input;}}
                    onBlur={this.handleBlur}/>)
            }
            else {
                return (
                    <div
                        key={this.props.key}
                        style={this.props.style}
                        className={this.props.className}
                        onClick={this.handleClick}>
                        {this.props.value}
                    </div>);
            }

    }
}
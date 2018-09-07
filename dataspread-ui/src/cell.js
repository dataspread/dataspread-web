import React, {Component} from 'react';
import {Input} from 'semantic-ui-react'
import 'semantic-ui-css/semantic.min.css';

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
        this._handleKeyPress = this._handleKeyPress.bind(this);
    }

    componentDidUpdate () {
        if (this.state.editing)
            this.input.focus();
    }

    _handleKeyPress(e) {
        if (e.key === 'Enter') {
            this.handleBlur(e);
        }
    }


    handleClick(e) {
        this.setState({
            editing: true
        })
    }

    handleBlur(e) {
        this.props.onUpdate({
            rowIndex:this.props.rowIndex,
            columnIndex:this.props.columnIndex,
            value:e.target.value});
        this.setState({
            editing: false
        })
    }

    render() {
            if (this.state.editing) {
                return (<Input
                        style={this.props.style}
                        ref={(input) => { this.input = input;}}
                        onBlur={this.handleBlur}
                        onKeyPress={this._handleKeyPress}
                        defaultValue={this.props.formula == null ? this.props.value : this.props.formula}/>
                )
            }
            else {
                return (
                    <div
                        style={this.props.style}
                        className={this.props.className}
                        onDoubleClick={this.handleClick}>
                        {this.props.value}
                    </div>);
            }

    }
}
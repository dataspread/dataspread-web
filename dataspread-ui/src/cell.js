import React, {Component} from 'react';
import {Input, Progress} from 'semantic-ui-react'
import 'semantic-ui-css/semantic.min.css';

export default class Cell extends Component {

    constructor(props) {
        super(props);
        this.state = {
            editing: false,
        }
        this.handleMouseOver = this.handleMouseOver.bind(this);
        this.handleMouseDown = this.handleMouseDown.bind(this);
        this.handleMouseUp = this.handleMouseUp.bind(this);
        this.handleDoubleClick = this.handleDoubleClick.bind(this);
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

    handleMouseOver() {
        this.props.onCellMouseOver({
            rowIndex: this.props.rowIndex,
            columnIndex: this.props.columnIndex
        });
    }

    handleMouseDown() {
        this.props.onCellMouseDown({
            rowIndex: this.props.rowIndex,
            columnIndex: this.props.columnIndex
        });
        this.props.onSelectFormula({
            value: this.props.formula == null ? this.props.value : this.props.formula
        });
    }

    handleMouseUp() {
        this.props.onCellMouseUp({
            rowIndex: this.props.rowIndex,
            columnIndex: this.props.columnIndex
        });
    }

    handleDoubleClick() {
        this.setState({
            editing: true
        });
    }

    handleBlur(e) {
        if (this.props.value !== e.target.value)
            this.props.onUpdate({
                rowIndex: this.props.rowIndex,
                columnIndex: this.props.columnIndex,
                value: e.target.value
            });
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
            // selection code goes here
            else if (this.props.isProcessing) {
                return (
                    <div
                        style={this.props.style}
                        className={this.props.className}
                        onDoubleClick={this.handleDoubleClick}>
                        <Progress
                            style={{
                                height: '32px',
                                left: '0px',
                                top: '0px',
                                width: '150px',
                                position: 'absolute',
                                fontSize: '0.9rem'
                            }}
                            size={'large'}
                            percent={this.props.pctProgress}/>
                    </div>);
            }
            else {
                return (
                    <div
                        style={this.props.style}
                        className={this.props.className}
                        onMouseOver={this.handleMouseOver}
                        onMouseDown={this.handleMouseDown}
                        onMouseUp={this.handleMouseUp}
                        onDoubleClick={this.handleDoubleClick}>
                        {this.props.value}
                    </div>);
            }

    }
}
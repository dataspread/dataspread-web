import React, {Component} from 'react';
import {Radio, Input, Checkbox, Container, Dropdown, Button, Form, Sidebar} from 'semantic-ui-react'

const favstyle = {
    height: '1.5em',
    width: '1.5em',
  };
  
export default class TableSidebar extends Component {
    toColumnNum(name) {
        let ret = 0;
        for (let i = 0; i < name.length; i++) {
            ret *= 26;
            ret += name.charCodeAt(i)-64;
        }
        return ret;
    }

    toCellIndex(name) {
        const numStart = name.search(/[1-9]/);
        return [parseInt(name.slice(numStart), 10)-1, this.toColumnNum(name.slice(0, numStart))-1];
    }

    toRangeIndex(range) {
        const rangeSplit = range.split(':');
        return rangeSplit.map(this.toCellIndex).reduce((sum, e) => sum.concat(e))
    }

    constructor(props) {
        super(props);
        this.state = {
            cellRange: '',
            tableName: '',
            allSchema: '',
            selectionString: '',
            selectionSync: false,
            cellRangeError: false
        };
        this.handleInputChange = this.handleInputChange.bind(this);
        this.handleSelectionSyncToggle = this.handleSelectionSyncToggle.bind(this);
        this.submitForm = this.submitForm.bind(this);

        this.toColumnNum = this.toColumnNum.bind(this);
        this.toCellIndex = this.toCellIndex.bind(this);
        this.toRangeIndex = this.toRangeIndex.bind(this);
    }



    submitForm() {
        const zCellRange = this.state.cellRange;
        const zTableName = this.state.tableName;
        const zSchema = this.state.allSchema.split(",");
        if (!/^[A-Z]+[1-9]([0-9])*:[A-Z]+[1-9]([0-9])*$/.test(zCellRange)) {
            this.setState({
                cellRangeError: true
            });
        } else {
            this.setState({
                cellRangeError: false
            });
            this.props.onFormSubmit({
                cellRange: this.toRangeIndex(zCellRange),
                tableName: zTableName,
                schema: zSchema
            });
        }
    }

    handleSelectionSyncToggle() {
        if (this.state.selectionSync) {
            this.setState({
                selectionSync: false
            });
        } else {
            this.setState({
                cellRange: this.state.selectionString,
                selectionSync: true
            });
        }
    }

    handleInputChange(e, {name, value}) {
        this.setState({ [name]: value });
    }

    handleSelectionChange(selectionString) {
        if(this.state.selectionSync) {
            this.setState({
                cellRange: selectionString,
                selectionString: selectionString
            });
        } else {
            this.setState({
                selectionString: selectionString
            });
        }
    }



    render() {
        const syncOptions = [
            { key: 1, text: 'Create new table from current values', value: 1 },
            { key: 2, text: 'Import values from existing table', value: 2 },
        ];
        return (
        <Sidebar
            as={Container}
            animation='push'
            direction='left'
            style={{
                padding: '12px'
            }}
            vertical
            visible={true}
            width='wide'
        >
            <h3>Sync Table</h3>
            <Form onSubmit={this.submitForm}>
                <Form.Field control={Input}
                            name='cellRange'
                            label='Range'
                            value={this.state.cellRange}
                            error={this.state.cellRangeError}
                            onChange={this.handleInputChange}
                            disabled={this.state.selectionSync} />
                <Form.Field control={Checkbox}
                            toggle
                            name='selectionSync'
                            label='Use Selection'
                            checked={this.state.selectionSync}
                            onChange={this.handleSelectionSyncToggle} />
                <Form.Field control={Input} name='tableName' label='Table Name' onChange={this.handleInputChange} />
                <Form.Field control={Input} name='allSchema' label='Schema' onChange={this.handleInputChange} />
                <Form.Field control={Button}>Submit</Form.Field>
            </Form>
        </Sidebar>
        )
    }
}

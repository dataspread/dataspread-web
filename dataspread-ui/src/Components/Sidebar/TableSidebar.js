import React, {Component} from 'react';
import {Radio, Input, Checkbox, Dropdown, Button, Form, Sidebar} from 'semantic-ui-react'

const favstyle = {
    height: '1.5em',
    width: '1.5em',
  };
  
export default class TableSidebar extends Component {
    constructor(props) {
        super(props);
        this.state = {
            cellRange: '',
            tableName: '',
            allSchema: '',
            selectionString: '',
            selectionSync: false
        };
        this.handleInputChange = this.handleInputChange.bind(this);
        this.handleSelectionSyncToggle = this.handleSelectionSyncToggle.bind(this);
        this.submitForm = this.submitForm.bind(this);
    }

    submitForm() {
        const zCellRange = this.state.cellRange;
        const zTableName = this.state.tableName;
        const zSchema = this.state.allSchema.split(",");
        this.props.onFormSubmit({
            cellRange: zCellRange,
            tableName: zTableName,
            schema: zSchema
        });
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
                  animation='push'
                  direction='left'
                  vertical
                  visible={true}
                  width='wide'
              >
                  <h3>Sync Table</h3>
                  <Form style={{
                      padding: '8px'
                  }} onSubmit={this.submitForm}>
                      <Form.Field control={Input}
                                  name='cellRange'
                                  label='Range'
                                  value={this.state.cellRange}
                                  onChange={this.handleInputChange}
                                  disabled={this.state.selectionSync} />
                      <Form.Field control={Checkbox}
                                  toggle
                                  name='matchRangeToSelection'
                                  label='Match to Selection'
                                  checked={this.state.selectionSync}
                                  onChange={this.handleSelectionSyncToggle} />
                      <Form.Field control={Input} name='tableName' label='Table Name' onChange={this.handleInputChange} />
                      <Form.Field control={Input} name='allSchema' label='Schema' onChange={this.handleInputChange} />
                      <Form.Group>
                          <Form.Group inline>
                              <Form.Field control={Input} width='5' />
                              <Form.Field control={Radio} label='One' value='1' />
                              <Form.Field control={Radio} label='Two' value='2' />
                          </Form.Group>
                      </Form.Group>
                      <Form.Field control={Button}>Submit</Form.Field>
                  </Form>
              </Sidebar>
      )
    }
  }




                
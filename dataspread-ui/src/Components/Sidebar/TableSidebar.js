import React, {Component} from 'react';
import {Radio, Input, Button, Form, Sidebar} from 'semantic-ui-react'

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
            allSchema: ''
        };
        this.handleChange = this.handleChange.bind(this);
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

    handleChange(e, {name, value}) {
        this.setState({ [name]: value });
    }


    render() {
      return (
              <Sidebar
                  animation='push'
                  direction='left'
                  vertical
                  visible={true}
                  width='wide'
              >
                  <Form style={{
                      padding: '8px'
                  }} onSubmit={this.submitForm}>
                      <Form.Field control={Input} name='cellRange' label='Range' onChange={this.handleChange}/>
                      <Form.Field control={Input} name='tableName' label='Table Name' onChange={this.handleChange} />
                      <Form.Field control={Input} name='allSchema' label='Schema' onChange={this.handleChange} />
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




                
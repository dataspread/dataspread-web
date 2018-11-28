import React, {Component} from 'react'
import {Form, Button,Radio} from 'semantic-ui-react'
import './Navigation.css';

export default class ExplorationForm extends Component {
    constructor(props) {
        super(props);
        console.log(this);
        this.state = {
            options:["city","price","availability","longitude","latitude"],
            attribute:2
        }
        this.handleChange = this.handleChange.bind(this);
        fetch('http://localhost:9999' + '/api/getSortAttrs/'+ this.props.grid.props.bookId+'/Sheet1')
            .then(response => response.json())
            .then(data => {
                console.log(data);
                this.setState({options:data.data});
            })



    }
    handleChange = (e, { value }) => {
        this.setState({ attribute: value });
        console.log(value);
    }


    render(){
        var optionList = this.state.options;
        return(
            <Form id = "explore-form">
            <Button icon='close' />
                {optionList.map((opt, index) =>{
                    let idx = index + 1;
                    return(
                        <Form.Field key = {idx}>
                            <Radio
                                label = {opt}
                                name='radioGroup'
                                value= {idx}
                                checked={this.state.attribute === idx}
                                onChange={this.handleChange}
                            />
                        </Form.Field>
                    );

                })}
                <Button type='submit'>Submit</Button>
            </Form>
    );
    }
}


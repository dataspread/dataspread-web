import React, {Component} from 'react'
import {Form, Button,Radio} from 'semantic-ui-react'
import './Navigation.css';

export default class ExplorationForm extends Component {
    constructor(props) {
        super(props);
        console.log(this);
        this.state = {
            options:[],
            attribute:0,
        }
        this.handleChange = this.handleChange.bind(this);
        this.handleSubmit = this.handleSubmit.bind(this);
        this.handleClose = this.handleClose.bind(this);

    }

    componentDidMount() {
        console.log( "componentmoutn explore form")
        console.log(this.props.grid.state.sheetName)
    }

    handleChange = (e, { value }) => {
        this.setState({ attribute: value });
        console.log(value);
    }

    handleSubmit = () =>{
        console.log("submit")
        this.props.submitNavForm(this.state.attribute);
    }

    handleClose = () =>{
        console.log("close")
        this.props.closeNavForm();
    }
    render(){
        if(this.props.grid.state.navFormOpen){
            var optionList = this.state.options;
            return(<div id = "explore-form">

                <Form >
                    <Button icon='close' id="formClose" onClick = {this.handleClose}/>
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
                    <Form.Button onClick={this.handleSubmit}>Submit</Form.Button>
                </Form>
            </div>
            );
        } else {
            return null;
        }

    }
}


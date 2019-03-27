import React, {Component} from 'react'
import {Form, Button,Radio} from 'semantic-ui-react'
import './Navigation.css';

export default class BinCustomizationForm extends Component {
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
        console.log( "componentmout binform form")
        console.log(this.props.grid.state.sheetName)
    }

    handleChange = (e, { value }) => {

    }

    handleSubmit = () =>{

    }

    handleClose = () =>{

    }
    render(){
        if(this.props.grid.state.binFormOpen){
            var optionList = this.state.options;
            return(<div id = "bin-form">
                </div>
            );
        } else {
            return null;
        }

    }
}


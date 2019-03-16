import React, {Component} from 'react'
import {Form, Dimmer, Loader, Modal} from 'semantic-ui-react'
import './Navigation.css';

export default class ExplorationForm extends Component {
    constructor(props) {
        super(props);
        console.log(this);
        this.state = {
            options: [],
            attribute: 1,
            processing: false,
        }
        this.handleChange = this.handleChange.bind(this);
        this.handleSubmit = this.handleSubmit.bind(this);
        this.handleClose = this.handleClose.bind(this);

    }

    componentDidMount() {
        console.log("componentmoutn explore form")
        //console.log(this.props.grid.state.sheetName)
    }

    handleChange = (e, {value}) => {
        this.setState({attribute: value});
        console.log(value);
    }

    handleSubmit = () => {
        console.log("submit")
        console.log(this.state.attribute)
        this.setState({
            processing:true,
        })
        this.props.submitNavForm(this.state.attribute);
    }

    handleClose = () => {
        console.log("close")
        this.setState({
            navFormOpen: false,
        })
    }

    render() {
        if (this.state.navFormOpen) {
            var optionList = this.state.options;
            return (<Modal
                    closeIcon
                    onClose={this.handleClose}
                    open={this.state.navFormOpen}>
                    <Modal.Header>Exploration Form</Modal.Header>
                    <Modal.Content>
                        <Form onSubmit={this.handleSubmit}>
                            <div>
                                <Form.Group>
                                    <Form.Dropdown
                                        width={14}
                                        options={optionList} selection
                                        defaultValue={optionList[0].value}
                                        onChange={this.handleChange}
                                    />
                                </Form.Group>
                            </div>
                            <Form.Button>Start Explore!</Form.Button>
                        </Form>
                        <Dimmer active ={this.state.processing}>
                            <Loader>Loading</Loader>
                        </Dimmer>
                    </Modal.Content>
                </Modal>
            );
        } else {
            return null;
        }

    }
}


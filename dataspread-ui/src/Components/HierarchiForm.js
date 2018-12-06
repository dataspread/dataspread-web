import React, {Component} from 'react'
import {Form, Button,Select} from 'semantic-ui-react'
import './Navigation.css';

export default class HierarchiForm extends Component {
    constructor(props) {
        super(props);
        console.log(this);
        this.state = {
            getChart:false,
            options:[],
        }

    }
    handleSubmit = (e) =>{
        console.log(e)
        console.log("submit")
        console.log(this.state.getChart)

    }
    handleChartChange = (e, { value }) => {
        this.setState({ getChart: value==2 });
    }
    render(){
        const chartOpt = [
            { key: 'r', text: 'Raw Value', value: '1' },
            { key: 'c', text: 'Chart', value: '2' },
        ]
        const funcOptions =
            [
                "AVEDEV", "AVERAGE", "COUNT", "COUNTA", "COUNTBLANK", "COUNTIF",
                "DEVSQ", "LARGE", "MAX", "MAXA", "MIN", "MINA",
                "MEDIAN", "MODE", "RANK", "SMALL", "STDEV", "SUBTOTAL",
                "SUM", "SUMIF", "SUMSQ", "VAR", "VARP"
            ]
        const subtotalFunc =
            [
                "AVERAGE", "COUNT", "COUNTA", "MAX", "MIN", "PRODUCT", "STDEV", "SUM",
                "VAR", "VARP"
            ];
        return (
            <div>
                <Form onSubmit={this.handleSubmit}>
                    <Button icon='close' id="formClose" onClick = {this.handleClose}/>

                    <Form.Field
                        inline
                        control={Select}
                        options={chartOpt}
                        label={{ children: 'Show result by', htmlFor: 'form-select-control-chart' }}
                        search
                        searchInput={{ id: 'form-select-control-chart' }}
                        onChange={this.handleChartChange}
                    />
                    <Form.Button >Done</Form.Button>
                </Form>
            </div>
        );
    }

}
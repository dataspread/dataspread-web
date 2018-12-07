import React, {Component} from 'react'
import {Form, Button, Select, Input} from 'semantic-ui-react'
import './Navigation.css';

export default class HierarchiForm extends Component {
    constructor(props) {
        super(props);
        console.log(this);
        this.state = {
            getChart: false,
            options: [{text: 'city', value: '0'},
                {text: 'id', value: '1'},
                {text: 'name', value: '2'},],
            formula_ls: [{
                attr_index: 0,
                function: "AVEDEV",
                param_ls: [],
            }, {
                attr_index: 0,
                function: "AVEDEV",
                param_ls: [],
            }]
        }
    }

    handleSubmit = (e) => {
        console.log(e)
        console.log("submit")
        console.log(this.state.getChart)

    }
    handleChartChange = (e, {value}) => {
        this.setState({getChart: value == 2});
    }

    handleFuncChange = (e,data) => {
        console.log(e);
        console.log(data)
        let formula = this.state.formula_ls;
        formula[data.id].function = data.value;
        this.setState({
            formula_ls:formula,
        })
    }
    handleAttrChange = (e,data) =>{
        console.log(data)
        let formula = this.state.formula_ls;
        formula[data.id].attr_index = data.value;
        this.setState({
            formula_ls:formula,
        })
    }

    handleAdd = e => {
        console.log(e)
        let formula = this.state.formula_ls;
        let temp = {
            attr_index: 0,
            function: "AVEDEV",
            param_ls: [],
        }
        formula.splice(e + 1, 0,temp );
        this.setState({
            formula_ls:formula,
        })
    }

    handleRemove = e => {
        console.log(e)
        let formula = this.state.formula_ls;
        if(formula.length == 1) {
            alert("You cannot remove all Options");
            return;
        }
        formula.splice(e, 1 );
        this.setState({
            formula_ls:formula,
        })

    }
    renderPara(index) {
        console.log("renderpar");
        return(
            <Form.Field inline>
                <label>Predicate:</label>
                <Input placeholder=''  size="mini"/>
            </Form.Field>);
    }
    render() {
        const chartOpt = [
            {key: 'r', text: 'Raw Value', value: '1'},
            {key: 'c', text: 'Chart', value: '2'},
        ]
        const funcOptions = [
            {text: 'AVEDEV', value: 'AVEDEV'},
            {text: 'AVERAGE', value: 'AVERAGE'},
            {text: 'COUNT', value: 'COUNT'},
            {text: 'COUNTA', value: 'COUNTA'},
            {text: 'COUNTBLANK', value: 'COUNTBLANK'},
            {text: 'COUNTIF', value: 'COUNTIF'},
            {text: 'DEVSQ', value: 'DEVSQ'},
            {text: 'LARGE', value: 'LARGE'},
            {text: 'MAX', value: 'MAX'},
            {text: 'MAXA', value: 'MAXA'},
            {text: 'MIN', value: 'MIN'},
            {text: 'MINA', value: 'MINA'},
            {text: 'MEDIAN', value: 'MEDIAN'},
            {text: 'MODE', value: 'MODE'},
            {text: 'RANK', value: 'RANK'},
            {text: 'SMALL', value: 'SMALL'},
            {text: 'STDEV', value: 'STDEV'},
            {text: 'SUBTOTAL', value: 'SUBTOTAL'},
            {text: 'SUM', value: 'SUM'},
            {text: 'SUMIF', value: 'SUMIF'},
            {text: 'SUMSQ', value: 'SUMSQ'},
            {text: 'VAR', value: 'VAR'},
            {text: 'VARP', value: 'VARP'}]
        const subtotalFunc =
            [
                "AVERAGE", "COUNT", "COUNTA", "MAX", "MIN", "PRODUCT", "STDEV", "SUM",
                "VAR", "VARP"
            ];
        var formula_ls = this.state.formula_ls;
        const selected = ['1']
        return (
            <div style={{"width": "20%"}} id="hierarchi-form">
                <Form onSubmit={this.handleSubmit}>
                    <Form.Group>
                        <legend id="hierarchi-title">Hierarchical
                            form
                        </legend>
                        <Button icon='close' id="formClose" onClick={this.handleClose}/>
                    </Form.Group>
                    {formula_ls.map((line, index) => {
                        console.log(line)
                        console.log(selected)
                        return (<div>
                            <Form.Group stackable>
                                <i class="fa fa-minus-circle hierRemove"  id="rm1"
                                   aria-hidden="true" onClick={this.handleRemove.bind(this,index)}/>
                                <Form.Dropdown id = {index}
                                               width={7}
                                    style={{"minWidth": "6em", "maxWidth": "8.5em",}}
                                    options={this.state.options}
                                               selection
                                            value={this.state.options[line.attr_index].value}
                                    onChange={this.handleAttrChange}
                                />
                                <Form.Dropdown id = {index}
                                    style={{"minWidth": "6em", "maxWidth": "8.em"}}
                                               width={7}
                                    // inline
                                    // control={Select}
                                    options={funcOptions} selection
                                    value={line.function}
                                    onChange={this.handleFuncChange}
                                />
                                <i class="fa fa-plus-circle fa-1x hierAdd" id='add" + targetId + "' aria-hidden="true"
                                   onClick={this.handleAdd.bind(this,index)}/>
                            </Form.Group>
                                {this.renderPara(index)}
                            </div>
                        );

                    })}
                    <Form.Field
                        inline
                        control={Select}
                        options={chartOpt}
                        label={{children: 'Show result by', htmlFor: 'form-select-control-chart'}}
                        search
                        searchInput={{id: 'form-select-control-chart'}}
                        onChange={this.handleChartChange}
                    />
                    <Form.Button>Done</Form.Button>
                </Form>
            </div>
        );
    }

}
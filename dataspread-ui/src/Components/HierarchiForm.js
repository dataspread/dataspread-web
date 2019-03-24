import React, {Component} from 'react'
import {Form, Button, Select, Input, Modal, Dropdown} from 'semantic-ui-react'
import './Navigation.css';

export default class HierarchiForm extends Component {
    constructor(props) {
        super(props);
        //console.log(this);
        this.state = {
            navPanelOpen: false,
            modalOpen: false,
            getChart: false,
            options: [{text: 'city', value: '0'},
                {text: 'id', value: '1'},
                {text: 'name', value: '2'},],
            formula_ls: [{
                attr_index: 1,
                function: "AVEDEV",
                param_ls: [""],
            },]
        };
        this.handleRankOrder = this.handleRankOrder.bind(this);
        this.handleSubTotalFunc = this.handleSubTotalFunc.bind(this);
        this.updateOption = this.updateOption.bind(this);
    }

    updateOption(data) {
        this.setState({
            navPanelOpen: true,
            options: data
        });
    }

    handleOpen = () => {
        if (this.state.navPanelOpen !== true) {
            //console.log(this)
            alert("There is no navigation panel.")
            return;
        } else {
            //console.log(this);
            this.setState({modalOpen: true});
        }
    }
    handleClose = () => this.setState({modalOpen: false})

    handleSubmit = (e) => {
        //console.log(e);
        //console.log("submit");
        //console.log(this.state.getChart)
        let formula_ls = this.state.formula_ls;
        for (let i = 0; i < formula_ls.length; i++) {
            formula_ls[i].getChart = this.state.getChart;
            formula_ls[i].attr_index = "" + formula_ls[i].attr_index + ""
            switch (formula_ls[i].function) {
                case "COUNTIF":
                case "SUMIF":
                case "LARGE":
                case "SMALL":
                    if (formula_ls[i].param_ls[1] == "") {
                        alert("Please fill in the parameter")
                        return;
                    }else if(formula_ls[i].param_ls[1].charAt(0) === '\''){
                        formula_ls[i].param_ls[1] = formula_ls[i].param_ls[1].replace(/'/g,'\"');
                        console.log(formula_ls[i].param_ls[1])
                    } else if(formula_ls[i].param_ls[1].charAt(0) !== '\"'){
                        formula_ls[i].param_ls[1] = "\"" + formula_ls[i].param_ls[1] +"\"";
                    }
                    break;
                case "RANK":
                    if (formula_ls[i].param_ls[0] == "") {
                        alert("Please fill in the parameter")
                        return;
                    }
                    break;

            }
        }

        this.props.submitHierForm(formula_ls);
        this.setState({
            modalOpen: false
        })

    };
    handleChartChange = (e, {value}) => {
        this.setState({getChart: value == 2});
    };

    handleFuncChange = (e, data) => {
        //console.log(e);
        //console.log(data);
        let formula = this.state.formula_ls;
        formula[data.id].function = data.value;
        let paraList = [""];
        switch (data.value) {
            case "COUNTIF":
            case "SUMIF":
            case "LARGE":
            case "SMALL":
                paraList.push("");
                break;
            case "SUBTOTAL":
                paraList.splice(0, 0, "1");
                break;
            case "RANK":
                paraList.push("");
                paraList.push("1");
        }
        formula[data.id].param_ls = paraList;
        this.setState({
            formula_ls: formula,
        })
    };
    handleAttrChange = (e, data) => {
        //console.log(data);
        let formula = this.state.formula_ls;
        formula[data.id].attr_index = data.value;
        this.setState({
            formula_ls: formula,
        })
    };

    handleAdd = e => {
        //console.log(e);
        let formula = this.state.formula_ls;
        let temp = {
            attr_index: 1,
            function: "AVEDEV",
            param_ls: [""],
        };
        formula.splice(e + 1, 0, temp);
        this.setState({
            formula_ls: formula,
        })
    };

    handleRemove = e => {
        //console.log(e);
        let formula = this.state.formula_ls;
        if (formula.length == 1) {
            alert("You cannot remove all Options");
            return;
        }
        formula.splice(e, 1);
        this.setState({
            formula_ls: formula,
        })

    };
    handleRankInput = (e, data) => {
        //console.log(data);
        let index = data.name.substring(4);
        //console.log(index);
        let formula_ls = this.state.formula_ls;
        formula_ls[index].param_ls[0] = data.value;
        this.setState({
            formula_ls: formula_ls,
        })
    };
    handleRankOrder = (e, data) => {
        //console.log(data);
        let index = data.id.substring(4);
        //console.log(index);
        let formula_ls = this.state.formula_ls;
        formula_ls[index].param_ls[2] = data.value;
        this.setState({
            formula_ls: formula_ls,
        })
    };
    handleSubTotalFunc = (e, data) => {
        //console.log(e.target.value);
        let index = data.id.substring(4);
        //console.log(index);
        let formula_ls = this.state.formula_ls;
        formula_ls[index].param_ls[0] = data.value;
        this.setState({
            formula_ls: formula_ls,
        })
    };

    handleManualInput = (e, data) => {
        //console.log(data);
        let index = data.id.substring(4);
        //console.log(index);
        let formula_ls = this.state.formula_ls;
        formula_ls[index].param_ls[1] = data.value;
        this.setState({
            formula_ls: formula_ls,
        })
    };

    renderPara(index) {
        const subtotalFunc = [
            {text: 'AVERAGE', value: '1'},
            {text: 'COUNT', value: '2'},
            {text: 'COUNTA', value: '3'},
            {text: 'MAX', value: '4'},
            {text: 'MIN', value: '5'},
            {text: 'PRODUCT', value: '6'},
            {text: 'STDEV', value: '7'},
            {text: 'SUM', value: '8'},
            {text: 'VAR', value: '9'},
            {text: 'VARP', value: '10'}];
        const orderOpt = [{text: 'ascending', value: '1'},
            {text: 'descending', value: '0'},];

        //console.log("renderpar");
        //console.log(index);
        let formula_ls = this.state.formula_ls;
        switch (formula_ls[index].function) {
            case "COUNTIF":
            case "SUMIF":
                return (
                    <Form.Field inline>
                        <label>Predicate: </label>
                        <Input placeholder='' size="mini" value={formula_ls[index].param_ls[1]} id={"para" + index}
                               onChange={this.handleManualInput}/>
                    </Form.Field>);
            case "LARGE":
            case "SMALL":
                return (<Form.Field inline>
                    <label>Int: </label>
                    <Input placeholder='' size="mini" value={formula_ls[index].param_ls[1]} id={"para" + index}
                           onChange={this.handleManualInput}/>
                </Form.Field>);
            case "SUBTOTAL":
                return (<Form.Field inline>
                    <label>Function_num </label>
                    <Select value={formula_ls[index].param_ls[0]} options={subtotalFunc} size="mini" id={"para" + index}
                            onChange={this.handleSubTotalFunc}/>
                </Form.Field>);
            case "RANK":
                return (<Form.Field inline>
                    <label> Value: </label>
                    <Input value={formula_ls[index].param_ls[0]} size="mini" style={{"maxWidth": "8em",}}
                           name={"para" + index} onChange={this.handleRankInput}/>
                    <Select value={formula_ls[index].param_ls[2]} options={orderOpt} size="mini" id={"para" + index}
                            style={{"minWidth": "6em", "maxWidth": "8em", "marginLeft": "2em"}}
                            onChange={this.handleRankOrder}/>
                </Form.Field>);
        }
        return null;
    }
    render() {

        if(this.state.navPanelOpen !== true){
            return null;
        }
        const chartOpt = [
            {key: 'r', text: 'Raw Value', value: '1'},
            {key: 'c', text: 'Chart', value: '2'},
        ];
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
            {text: 'VARP', value: 'VARP'}];

        var formula_ls = this.state.formula_ls;
        const selected = ['1'];
        return (
            <Modal
                closeIcon
                onClose={this.handleClose}
                trigger={<Dropdown.Item onClick={this.handleOpen}>Add Hierarchical Column</Dropdown.Item>}
                open={this.state.modalOpen}>
                <Modal.Header>Hierarchical form</Modal.Header>
                <Modal.Content>
                    <Form onSubmit={this.handleSubmit}>
                        {formula_ls.map((line, index) => {
                            // console.log(line)
                            // console.log(this.state.options)
                            return (<div>
                                    <Form.Group>
                                        <i className="fa fa-minus-circle hierRemove" id="rm1"
                                           aria-hidden="true" onClick={this.handleRemove.bind(this, index)}/>
                                        <Form.Dropdown id={index}
                                                       width={7}
                                            // style={{"minWidth": "6em", "maxWidth": "8.5em",}}
                                                       options={this.state.options}
                                                       selection
                                                       value={this.state.options[line.attr_index - 1].value}
                                                       onChange={this.handleAttrChange}
                                        />
                                        <Form.Dropdown id={index}
                                            // style={{"minWidth": "6em", "maxWidth": "8.em"}}
                                                       width={7}
                                            // inline
                                            // control={Select}
                                                       options={funcOptions} selection
                                            //defaultValue={funcOptions[0].value}
                                                       value={line.function}
                                                       onChange={this.handleFuncChange}
                                        />
                                        <i className="fa fa-plus-circle fa-1x hierAdd" id='add" + targetId + "'
                                           aria-hidden="true"
                                           onClick={this.handleAdd.bind(this, index)}/>
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
                            defaultValue={chartOpt[0].value}
                            searchInput={{id: 'form-select-control-chart'}}
                            onChange={this.handleChartChange}
                        />
                        <Form.Button>Done</Form.Button>
                    </Form>
                </Modal.Content>
            </Modal>
        );
    }

}
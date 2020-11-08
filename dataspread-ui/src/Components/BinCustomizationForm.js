import React, {Component} from 'react'
import {Form, Button, Header, Dropdown, Input, Checkbox} from 'semantic-ui-react'
import './Navigation.css';

export default class BinCustomizationForm extends Component {
    constructor(props) {
        super(props);
        console.log(this);
        this.state = {
            binFormOpen: false,
            isNumeric: 0,
            //  bucketArray: [["Ashville", "Austin", "Boston"], ["Chicago", "Denver"], ["LA"], ["Nashville", "New Orleans"], ["New York City"], ["Oakland", "Portland", "San Diego"], ["San Francisco", "Santacruz", "Seattle"], ["Washington D.C."]],
            bucketArray: [["0.0", "53.0"], ["54.0", "69.0"], ["70.0", "85.0"], ["86.0", "99.0"], ["100.0", "120.0"], ["121.0", "149.0"], ["150.0", "185.0"], ["186.0", "250.0"], ["251.0", "750.0"], ["775.0", "10000.0"]],
            checkedCount: 0,
            checkedIndex: 0,
            checkedArray: [false, false, false, false, false, false, false, false],
            dropdownIndex: 0,
            splitNum: '',
            focusLine: -1,
            lower: 0,
            focusValue: '',

        }
        this.handleClose = this.handleClose.bind(this);
    }

    componentDidMount() {

    }

    handleOptChange = (e, para) => {
        this.setState({
            dropdownIndex: para.value,
        })
    }

    handleClose = () => {
        this.setState({
            binFormOpen: false,
        })

    }
    handleCheckBoxClick = (e, para) => {
        let tempArray = this.state.checkedArray;
        let tempCount = this.state.checkedCount;
        let tempIndex = this.state.checkedIndex;
        tempArray[parseInt(para.name, 10)] = para.checked;
        if (para.checked) {
            tempCount += 1;
        } else {
            tempCount -= 1;
        }
        if (tempCount === 1) {
            for (let i = 0; i < tempArray.length; i++) {
                if (tempArray[i] === true) {
                    tempIndex = i;
                }
            }
        }
        this.setState({
            checkedArray: tempArray,
            checkedCount: tempCount,
            checkedIndex: tempIndex,
        })
    }

    handleSplitNum = (e, para) => {
        this.setState({
            splitNum: para.value,
        })
    }
    handleNumericSplit = () => {
        let targetValue = this.state.splitNum; // a string
        if (targetValue === '') {
            alert("You have to input the number of buckets");
            return;
        }
        let dataBucket = this.state.bucketArray;
        let curr = this.state.checkedIndex;
        let oldlower = isNaN(dataBucket[curr][0]) ? parseFloat(dataBucket[curr][0].slice(0, -1)) : +dataBucket[curr][0]; // a number

        if (targetValue >= 10 || targetValue >= (dataBucket[curr][1] - oldlower)) {
            console.log()
            alert("The number of buckets specified is too many");
        } else {
            let indivisualSize = parseFloat(((dataBucket[curr][1] - oldlower) / targetValue).toFixed(2));
            let last = dataBucket[curr][1];
            let lower = dataBucket[curr][0];
            dataBucket.splice(curr, 1,);
            dataBucket.splice(curr, 0, [lower, parseFloat((oldlower + indivisualSize).toFixed(2))]);
            lower = oldlower + indivisualSize;
            for (let i = 1; i < targetValue - 1; i++) {
                dataBucket.splice(curr + i, 0, [lower.toFixed(2) + "+", parseFloat((lower + indivisualSize).toFixed(2))]);
                lower += indivisualSize;
                console.log(lower);
            }
            dataBucket.splice(curr + Number(targetValue) - 1, 0, [lower.toFixed(2) + "+", last]);
            console.log(dataBucket)
            let tempArray = [];
            for (let i = 0; i < dataBucket.length; i++) {
                tempArray.push(false);
            }
            this.setState({
                bucketArray: dataBucket,
                checkedCount: 0,
                checkedArray: tempArray,
                splitNum: '',
                checkedIndex: 0,
            })
        }
    }
    handleLowerValue = (e, para) => {
        this.setState({
            focusValue: para.value,
            focusLine: parseInt(para.name, 10),
            lower: 1
        })

    }
    handleLowerValueBlur = (e, para) => {
        console.log('blur lower')
        console.log(this)
        if (this.state.focusLine === -1 || this.state.focusLine === 0) return;
        let line = this.state.focusLine;
        let dataBucket = this.state.bucketArray;
        let prevLow = isNaN(dataBucket[line - 1][0]) ? parseFloat(dataBucket[line - 1][0].slice(0, -1)) : +dataBucket[line - 1][0];
        if (this.state.focusValue > prevLow && this.state.focusValue < dataBucket[line][1]) {
            dataBucket[line - 1][1] = this.state.focusValue;
            dataBucket[line][0] = this.state.focusValue + "+";
            this.setState({
                bucketArray: dataBucket,
                focusLine: -1,
            })
        } else {
            alert("The modified lower range is too high or too low");
            this.setState({
                focusLine: -1,
                lower: 0,
                focusValue: '',
            })
        }
    }
    handleUpperValue = (e, para) => {
        this.setState({
            focusValue: para.value,
            focusLine: parseInt(para.name, 10),
            lower: 0
        })
    }
    handleUpperValueBlur = (e, para) => {
        console.log('blur upper')
        if (this.state.focusLine === -1 || this.state.focusLine === this.state.bucketArray.length - 1) return;
        let line = this.state.focusLine;
        let dataBucket = this.state.bucketArray;
        let currLow = isNaN(dataBucket[line][0]) ? parseFloat(dataBucket[line][0].slice(0, -1)) : +dataBucket[line][0];
        let nextUpp = isNaN(dataBucket[line + 1][1]) ? parseFloat(dataBucket[line + 1][1].slice(0, -1)) : +dataBucket[line + 1][1];
        if (this.state.focusValue < nextUpp && this.state.focusValue > currLow) {
            dataBucket[line + 1][0] = this.state.focusValue + "+";
            dataBucket[line][1] = this.state.focusValue;
            this.setState({
                bucketArray: dataBucket,
                focusLine: -1,
            })
        } else {
            alert("The modified upper range is too high");
            this.setState({
                focusLine: -1,
                lower: 0,
                focusValue: '',
            })
        }
    }

    submit = (e) => {
        this.props.submitBinForm();
    }
    handleTextSplit = () => {
        if (this.state.dropdownIndex === 0) {
            alert('You have not choose the number of bins to split');
        } else {
            let dataBucket = this.state.bucketArray;
            let size = Math.floor(dataBucket[this.state.checkedIndex].length / this.state.dropdownIndex);
            for (let i = 0; i < this.state.dropdownIndex - 1; i++) {
                let temp = dataBucket[this.state.checkedIndex + i].splice(size);
                dataBucket.splice(this.state.checkedIndex + i + 1, 0, temp);
            }
            let tempArray = [];
            for (let i = 0; i < dataBucket.length; i++) {
                tempArray.push(false);
            }
            this.setState({
                bucketArray: dataBucket,
                dropdownIndex: 0,
                checkedCount: 0,
                checkedArray: tempArray,
            })
        }
    }
    handleMerge = () => {
        let valid = true;
        let start = null;
        let end = null;
        for (let i = 0; i < this.state.checkedArray.length; i++) {
            if (this.state.checkedArray[i]) {
                if (start == null) {
                    start = i;
                } else if (end == null && i == start + 1) {
                    end = i;
                } else if (i == end + 1) {
                    end = i;
                } else {
                    valid = false;
                }
            }
        }
        if (start == null || end == null || valid == false) {
            alert("Invalid merge, you may choose non-continuous bucket or only one bucket");
            return;
        }
        let dataBucket = this.state.bucketArray;
        if (this.state.isNumeric === 1) {
            let newup = dataBucket[end][1];
            dataBucket[start][1] = newup;
            dataBucket.splice(start + 1, end - start,);
        } else {
            for (let i = start + 1; i < end + 1; i++) {
                let temp = dataBucket[i];
                for (let j = 0; j < temp.length; j++) {
                    dataBucket[start].push(temp[j]);
                }
            }
            dataBucket.splice(start + 1, end - start,);
        }
        let tempArray = [];
        for (let i = 0; i < dataBucket.length; i++) {
            tempArray.push(false);
        }
        this.setState({
            bucketArray: dataBucket,
            dropdownIndex: 0,
            checkedCount: 0,
            checkedArray: tempArray
        })
    }

    handleMergeAll = () => {
        let temp = [];
        let dataBucket = this.state.bucketArray;
        for (let i = 0; i < dataBucket.length; i++) {
            for (let j = 0; j < dataBucket[i].length; j++) {
                temp.push(dataBucket[i][j]);
            }
        }
        dataBucket = [temp];
        this.setState({
            bucketArray: dataBucket,
            dropdownIndex: 0,
            checkedCount: 0,
            checkedArray: [false],
        })
    }

    handleSplitAll = () => {
        let temp = [];
        let dataBucket = this.state.bucketArray;
        for (let i = 0; i < dataBucket.length; i++) {
            for (let j = 0; j < dataBucket[i].length; j++) {
                temp.push([dataBucket[i][j]]);
            }
        }
        let tempArray = [];
        for (let i = 0; i < temp.length; i++) {
            tempArray.push(false);
        }
        this.setState({
            bucketArray: temp,
            dropdownIndex: 0,
            checkedCount: 0,
            checkedArray: tempArray
        })
    }

    onKeyPress(event) {
        console.log(event)
        if (event.which === 13 /* Enter */) {
            event.preventDefault();
        }
    }

    render() {
        console.log("bincustom")
        if (this.state.binFormOpen !== true) {
            return null;
        } else {
            let opts = [{text: '# of Buckets', value: 0}];
            if (this.state.checkedCount === 1) {
                for (let i = 2; i <= this.state.bucketArray[this.state.checkedIndex].length; i++) {
                    opts.push({text: i, value: i})
                }
            }

            var numericSplit = (
                <Input type='text' placeholder='# of Buckets' disabled={this.state.checkedCount !== 1} action
                       value={this.state.splitNum}
                       onChange={this.handleSplitNum}>
                    <input style={{maxWidth: '145px', paddingRight: '0px'}}
                    />
                    <Button style={{padding: '5px'}}
                            onClick={this.handleNumericSplit}
                    >Split</Button>
                </Input>);
            var textSplit = (
                <div style={{display: 'flex'}}>
                    <Dropdown style={{maxWidth: '145px', minWidth: '130px'}} selection
                              options={opts}
                              value={this.state.dropdownIndex}
                              onChange={this.handleOptChange}
                              disabled={this.state.checkedCount !== 1 || this.state.bucketArray[this.state.checkedIndex].length === 1}>
                    </Dropdown>
                    <Button style={{padding: '5px'}}
                            onClick={this.handleTextSplit}
                            disabled={this.state.checkedCount !== 1 || this.state.bucketArray[this.state.checkedIndex].length === 1}>Split</Button>
                </div>
            );
            return <div id="bucket-col" style={{overflowY: 'auto', height: '91vh'}}>
                <span style={{float: 'right', fontSize: '1.28em',}} onClick={this.handleClose}>x</span>
                <Header size='medium' style={{maxWidth: '17vw', display: 'inline'}}>Redefine Bin Boundaries</Header>

                <div style={{display: 'flex', justifyContent: 'space-around'}}>
                    <Button style={{padding: '5px'}} onClick={this.handleMerge}>Merge</Button>
                    {this.state.isNumeric ? numericSplit : textSplit}
                    {this.state.isNumeric ? null : <Dropdown button floating className='icon' direction={'left'}>
                        <Dropdown.Menu>
                            <Dropdown.Item text='Merge All Bins' onClick={this.handleMergeAll}/>
                            <Dropdown.Item text='Split All Bins' onClick={this.handleSplitAll}/>
                        </Dropdown.Menu>
                    </Dropdown>}
                </div>
                <Form style={{marginTop: '20px'}} onKeyPress={this.onKeyPress}>
                    {this.state.bucketArray.map((line, index) => {
                        if (this.state.isNumeric) {
                            return (
                                <Form.Group inline style={{justifyContent: 'center'}}>
                                    <Form.Field width={2}>
                                        <Checkbox name={index.toString()} checked={this.state.checkedArray[index]}
                                                  onClick={this.handleCheckBoxClick}/>
                                    </Form.Field>
                                    <Form.Field width={6}>
                                        <Input
                                            value={index === this.state.focusLine && this.state.lower === 1 ? this.state.focusValue : line[0]}
                                            onChange={this.handleLowerValue}
                                            onBlur={this.handleLowerValueBlur}
                                            name={index.toString()}
                                            readOnly={index === 0}/>
                                    </Form.Field>
                                    <Form.Field width={6}>
                                        <Input
                                            value={index === this.state.focusLine && this.state.lower === 0 ? this.state.focusValue : line[line.length - 1]}
                                            onChange={this.handleUpperValue}
                                            onBlur={this.handleUpperValueBlur}
                                            name={index.toString()}
                                            readOnly={index === this.state.bucketArray.length - 1}/>
                                    </Form.Field>
                                </Form.Group>
                            );
                        } else {
                            return (
                                <Form.Group inline style={{justifyContent: 'center'}}>
                                    <Form.Field width={2}>
                                        <Checkbox name={index.toString()} checked={this.state.checkedArray[index]}
                                                  onClick={this.handleCheckBoxClick}/>
                                    </Form.Field>
                                    <Form.Field width={6}>
                                        <Input value={line[0]} readOnly/>
                                    </Form.Field>
                                    <Form.Field width={6}>
                                        <Input value={line[line.length - 1]} readOnly/>
                                    </Form.Field>
                                </Form.Group>
                            );

                        }

                    })}
                    <Button onClick={this.submit}>Apply</Button>
                </Form>
            </div>
        }

    }
}


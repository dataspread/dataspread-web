import React, {Component} from 'react'
import {Breadcrumb, Dropdown} from 'semantic-ui-react'
import './Navigation.css';

export default class HistoryBar extends Component {

    constructor(props) {
        super(props);
        console.log(this);
        this.state = {
            breadcrumb_ls: [],
            attribute: 0,
            open: false,
            navHistoryPathIndex: {},
            navHistoryTable: {},
            historyList: [],
        };
        this.updateNavPath = this.updateNavPath.bind(this);

    }

    componentDidMount() {
        console.log("componentmout binform form")
        console.log(this.props)
    }

    handleHistClick = (row) =>{
        console.log(row)
        console.log(this.state.navHistoryPathIndex[row])
        this.props.jumpToHistorialView(this.state.navHistoryPathIndex[row])
    }
    handleBreadcrumbClick = (index) => {
        console.log(index)
        console.log(this.state.breadCrumbHistoryPathIndex[parseInt(index)])
        this.props.jumpToHistorialView(this.state.breadCrumbHistoryPathIndex[parseInt(index)])
    }

    updateNavPath(breadcrumb_ls,path_index) {
        // add to navigation history
        let navHistoryPath = "Home";
        for (let j = 0; j < breadcrumb_ls.length; j++) {
            navHistoryPath += " > " + breadcrumb_ls[j];
        }
        let navHistoryPathIndex = this.state.navHistoryPathIndex;
        navHistoryPathIndex[navHistoryPath] = path_index;

        let child_str = navHistoryPathIndex[navHistoryPath];
        child_str = child_str.substring(0, child_str.lastIndexOf(","));

        let breadCrumbHistoryPathIndex = [];
        for (let j = breadcrumb_ls.length - 2; j >= 0; j--) {
            breadCrumbHistoryPathIndex[j + 1] = child_str;
            child_str = child_str.substring(0, child_str.lastIndexOf(","));
        }
        breadCrumbHistoryPathIndex[0] = "";
        if (breadcrumb_ls.length === 0) {
            this.setState({
                breadcrumb_ls: breadcrumb_ls,
                breadCrumbHistoryPathIndex: breadCrumbHistoryPathIndex,
            });
            return;
        }

        let navHistoryTable = this.state.navHistoryTable;
        let history = this.state.historyList;
        if (navHistoryTable[navHistoryPath] === undefined) // if new path
        {
            navHistoryTable[navHistoryPath] = true;
            history.splice(0, 0, navHistoryPath);

        } else // if existing path, delete from dropdown and prepend
        {
            let temp_ls = history.filter(item => item !== navHistoryPath);
            temp_ls.splice(0,0,navHistoryPath);
            history = temp_ls;
        }
        this.setState({
            breadcrumb_ls: breadcrumb_ls,
            breadCrumbHistoryPathIndex: breadCrumbHistoryPathIndex,
            historyList: history,
        });
    }

    render() {
        if(this.state.open == false) return null;
        var breadCrumb = [];
        if(this.state.breadcrumb_ls.length > 0) {
            breadCrumb.push(<Breadcrumb.Section link onClick={() => this.handleBreadcrumbClick(0)}>Home</Breadcrumb.Section>);
            for (let i = 0; i < this.state.breadcrumb_ls.length - 1; i++) {
                breadCrumb.push(<Breadcrumb.Divider icon='right angle'/>);
                breadCrumb.push(<Breadcrumb.Section link
                                                    onClick={() => this.handleBreadcrumbClick(i + 1)}>{this.state.breadcrumb_ls[i]}</Breadcrumb.Section>)
            }
            breadCrumb.push(<Breadcrumb.Divider icon='right angle'/>);
            breadCrumb.push(<Breadcrumb.Section>{this.state.breadcrumb_ls[this.state.breadcrumb_ls.length - 1]}</Breadcrumb.Section>)
        }else {
            breadCrumb.push(<Breadcrumb.Section>Home</Breadcrumb.Section>);
        }
        // console.log(this.state.historyList)
        return (
            <div style={{display: "flex", height: "4vh", padding: '0', alignItems: 'center'}}>
                <Breadcrumb size='big' style={{padding: '0px 0px 0px 10px'}}>
                    {breadCrumb}</Breadcrumb>
                <Dropdown text='Navigation History' id="nav-history">
                    <Dropdown.Menu>
                    {this.state.historyList.map((line,index)=>{
                        console.log(line)
                        console.log(index)
                        return (<Dropdown.Item text={line} onClick={()=> this.handleHistClick(line)}/>);
                    })
                    }
                    </Dropdown.Menu>
                </Dropdown>

            </div>);

    }
}


import React, {Component} from 'react'
import { Breadcrumb } from 'semantic-ui-react'
import './Navigation.css';

export default class HistoryBar extends Component {
    constructor(props) {
        super(props);
        console.log(this);
        this.state = {
            breadcrumb_ls:[],
            attribute:0,
        }
        this.handleChange = this.handleChange.bind(this);
        this.handleSubmit = this.handleSubmit.bind(this);
        this.handleClose = this.handleClose.bind(this);

    }

    componentDidMount() {
        console.log( "componentmout binform form")
        console.log(this.props)
    }

    handleChange = (e, { value }) => {

    }

    handleSubmit = () =>{

    }

    handleClose = () =>{

    }
    render(){
        var breadCrumb = [];
        for(let i = 0; i < this.state.breadcrumb_ls.length; i++){
            breadCrumb.push(<Breadcrumb.Divider icon='right angle' />);
            breadCrumb.push(<Breadcrumb.Section link>{this.state.breadcrumb_ls[i]}</Breadcrumb.Section>)
        }
        return(
            <div style={{display:"flex",height:"28px"}}>
                <Breadcrumb><Breadcrumb.Section link>Home</Breadcrumb.Section>
                    {breadCrumb}</Breadcrumb>
            <div className="dropdown" id="nav-history" >
                <a className="nav-link dropdown-toggle" href="#" id="historyDropdown" role="button"
                   data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">Navigation History</a>
                <div className="dropdown-menu" id="history-option" aria-labelledby="historyDropdown">

                </div>
            </div>

        </div>);

    }
}


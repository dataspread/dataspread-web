import React, {Component} from 'react';
import './App.css';
import DSGrid from './dsgrid';


class App extends Component {
    constructor (props) {
        super(props)
        this.state = {
            grid: [
                [{value:  1}, {value:  3}],
                [{value:  2}, {value:  4}]
            ]
        }
    }
    render () {
        return (
            <DSGrid/>
        )
    }
}

export default App;

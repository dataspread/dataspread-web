import React, {Component} from 'react';
import './App.css';
import ReactDataSheet from 'react-datasheet';
import 'react-datasheet/lib/react-datasheet.css';
import GridExample from './ScrollSync.example';

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
            <div>
            <ReactDataSheet
                data={this.state.grid}
                valueRenderer={(cell) => cell.value}
                onCellsChanged={changes => {
                    const grid = this.state.grid.map(row => [...row])
                    changes.forEach(({cell, row, col, value}) => {
                        grid[row][col] = {...grid[row][col], value}
                    })
                    this.setState({grid})
                }}
            />
            <GridExample/>
            </div>
        )
    }
}

export default App;

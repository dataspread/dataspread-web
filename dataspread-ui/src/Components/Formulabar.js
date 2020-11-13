import React, {Component} from 'react';
import {Label} from 'semantic-ui-react'

// const favstyle = {
//     height: '1.5em',
//     width: '1.5em',
//   };
  
export default class Formulabar extends Component {
    constructor(props) {
        super(props)
    }

    render() {
      return (
          <div className='formula' style={{display: "flex", height: "4vh", padding: '0', alignItems: 'center'}}>
              <span style={{padding: "0em 0.5em"}}>Formula:</span>
              <Label color='blue' size='big'>
                  {this.props.currentFormula}
              </Label>
          </div>
      )
    }
  }




                
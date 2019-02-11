import React, {Component} from 'react';
import 'semantic-ui-css/semantic.min.css';
import { Container, Menu, Message } from "semantic-ui-react";
import { ContextMenu, MenuItem, ContextMenuTrigger } from "react-contextmenu";

export default class RowHeaderCell extends Component {

    constructor(props) {
        super(props);
        this.state = {
            rowHeaderMenuVisible: false,
        }

        this.handleClick = this.handleClick.bind(this);
    }

    handleClick = (e, data) => {
        console.log(data);
    };

    render() {
        return (
                <Container>
                    <ContextMenuTrigger id="some_unique_identifier">
                        <Message>{this.props.value}</Message>
                    </ContextMenuTrigger>

                    <Menu className='rowHeaderMenu' as={ContextMenu} id="some_unique_identifier" vertical >
                        <MenuItem data={"above"} onClick={this.handleClick}>
                            <Menu.Item>ContextMenu Item 1</Menu.Item>
                        </MenuItem>

                        <MenuItem data={"below"} onClick={this.handleClick}>
                            <Menu.Item>ContextMenu Item 2</Menu.Item>
                        </MenuItem>
                    </Menu>
                </Container>
            )
    }
}
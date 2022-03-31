import logo from '../logo.svg';
import React from 'react';
import ReactDOM from 'react-dom';
import '../App.css';
import './style.css'
import Stone from "./stone";

class Pit extends React.Component {
    constructor(props) {
        super(props);
        this.state = {userId: props.userId, index: props.index, stones: props.stones, onClick: props.onClick};

        // This binding is necessary to make `this` work in the callback
        this.handleClick = this.handleClick.bind(this);
    }

    handleClick() {
        this.state.onClick(this.state.userId, this.state.index);
    }

    render() {

        const divStyle = {
            background: 'radial-gradient(rgb(229, 78, 208), rgb(255, 114, 255))'
        };

        return (
            <div className="hole-1" onClick={this.handleClick}>
                <div className="marble-layer">
                    {this.state.stones}
                </div>
                <div className="hover-number">4</div>
            </div>
        );
    }
}

export default Pit;

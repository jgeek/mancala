import React from 'react';
import '../App.css';
import './style.css'
import './stone-colors'

const stoneColors = {
    1: 'radial-gradient(rgb(229, 78, 208), rgb(255, 114, 255))',
    2: 'radial-gradient(rgb(180, 0, 162), rgb(158, 0, 49))',
    3: 'radial-gradient(rgb(66, 225, 0), rgb(246, 255, 106))',
    4: 'radial-gradient(rgb(250, 179, 64), rgb(254, 135, 135))',
    5: 'radial-gradient(rgb(30, 72, 226), rgb(6, 14, 45))',
    6: 'radial-gradient(rgb(177, 6, 223), rgb(6, 223, 177))',
};


class Stone extends React.Component {
    constructor(props) {
        super(props);
        this.state = {color: 33};

        // This binding is necessary to make `this` work in the callback
        this.handleClick = this.handleClick.bind(this);
    }

    handleClick() {
        this.setState(prevState => ({
            isToggleOn: !prevState.isToggleOn
        }));
    }

    render() {

        const divStyle = {
            background: 'radial-gradient(rgb(66, 225, 0), rgb(246, 255, 106))'
            // background: stoneColors[this.state.color]
        };

        return (
            <div className="marble" style={{divStyle}}/>
        );
    }
}

export default Stone;

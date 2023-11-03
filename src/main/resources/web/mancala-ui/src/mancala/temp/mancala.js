import logo from '../logo.svg';
import React from 'react';
import ReactDOM from 'react-dom';
import '../App.css';

class Mancala extends React.Component {
  constructor(props) {
    super(props);
    this.state = {isToggleOn: true};

    // This binding is necessary to make `this` work in the callback
    this.handleClick = this.handleClick.bind(this);
  }

  handleClick() {
    this.setState(prevState => ({
      isToggleOn: !prevState.isToggleOn
    }));
  }

  render() {
    return (
        <div id="mancala-2">
          <div className="player-number">P 1</div>
        </div>
    );
  }
}

export default Mancala;

import logo from '../logo.svg';
import React from 'react';
import ReactDOM from 'react-dom';
import '../App.css';
import './style.css'
import './stone-colors'

const defaultPlayer = {
    'id': 0,
    'username': 'test user',
    'currentPlayer': false
}

class Player extends React.Component {
    constructor(props) {
        super(props);
        this.state = {player: props.player, currentPlayer: props.currentPlayer, score: 0, styleClass: ''};
        console.log('player constructor');
        // This binding is necessary to make `this` work in the callback
        // this.handleClick = this.handleClick.bind(this);
    }

    render() {
        return (
            <div id="player-1"
                 className={this.state.player.id === this.state.currentPlayer.id ? 'currentUserBox' : ''}>{this.state.player.username}</div>

        );
    }
}

export default Player;

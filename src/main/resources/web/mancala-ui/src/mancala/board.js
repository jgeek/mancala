import React from 'react';
import './style.css'

const defaultPlayer = {
    'id': 0,
    'username': 'test user',
    'currentPlayer': false
}

const BASE_URL = 'http://localhost:8080/';

class Board extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            fetched: false,
            player1: defaultPlayer,
            player2: defaultPlayer,
            user1Pits: [],
            user2Pits: []
        };

        this.fetchData = this.fetchData.bind(this);
        this.onGrab = this.onGrab.bind(this);

    }

    componentDidMount() {
        console.log('mounted')
        this.fetchData();
    }

    fetchData() {
        fetch(BASE_URL + 'games/start', {
            method: 'POST',
            headers: {
                'Accept': 'application/json',
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({'player1': 'behnia', 'player2': 'sepanta'})
        })
            .then(res => res.json())
            .then(
                (result) => {
                    console.log(result);
                    const data = result.data;
                    this.setState({
                        'fetched': true,
                        'player1': data.player1,
                        'player2': data.player2,
                        'user1Pits': data.user1Pits,
                        'user2Pits': data.user2Pits,
                        'gameId': data.gameId
                    });
                    if (!result.success) {
                        console.log(result.message);
                        alert(result.message);
                    }
                },
                (error) => {
                    console.log(error)
                    this.setState({});
                }
            )
    }

    onGrab(userId, pitIndex) {

        console.log('userId ' + userId);
        console.log('pitIndex ' + pitIndex);
        const gameId = this.state.gameId;
        const url = BASE_URL + 'games/' + gameId + '/move';

        fetch(url, {
            method: 'POST',
            headers: {
                'Accept': 'application/json',
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({'userId': userId, 'pitIndex': pitIndex})
        })
            .then(res => res.json())
            .then(
                (result) => {
                    console.log(result);
                    const data = result.data;
                    this.setState({
                        'player1': data.player1,
                        'player2': data.player2,
                        'currentPlayer': data.currentPlayer,
                        'user1Pits': data.user1Pits,
                        'user2Pits': data.user2Pits,
                        'gameId': data.gameId
                    });
                    if(data.winner){
                        alert(data.winner.username+ ' wins the game ;)')
                    }
                    if (!result.success) {
                        console.log(result.message);
                        this.setState({'message': result.message});
                        alert(result.message);
                    }
                },
                (error) => {
                    console.log(error)
                    this.setState({});
                }
            )
    }

    render() {
        return (
            <div id="wrapper">
                <header>
                    <nav>
                        <div>
                            <h1>Mancala</h1>
                        </div>
                        <div>
                            <button id="restart" onClick={this.fetchData}>Restart the game</button>
                        </div>
                    </nav>
                </header>
                <main>
                    {/*<Player user = {this.state.fetched == true ?  this.state.user1 : defaultPlayer}/>*/}
                    <div id="player-1"
                         className={this.state.player1.currentPlayer ? 'currentUserBox' : ''}>
                        {this.state.fetched ? this.state.player1.username : defaultPlayer.username}
                    </div>
                    {/*<Player player = {this.state.fetched ? this.state.player1 : defaultPlayer} currentPlayer={this.state.currentPlayer}/>*/}

                    <div id="board">
                        <div id="col-1">
                            {
                                this.state.user1Pits.filter(p => p.big).map((p, i) => (
                                    <div id="mancala-2" key={p.id}>
                                        {p.stones}
                                    </div>
                                ))
                            }
                        </div>


                        <div id="col-2">
                            <div id="row-1">
                                {
                                    this.state.user1Pits.sort((p1, p2) => p2.index - p1.index).filter(p => !p.big).map((p, i) => (
                                        <div className="hole-1" onClick={() => this.onGrab(p.user.id, p.index)}
                                             key={p.id}>
                                            <div className="marble-layer">
                                                {p.stones}
                                            </div>
                                            <div className="hover-number">4</div>
                                        </div>
                                    ))
                                }
                            </div>
                            <div id="row-2">
                                {
                                    this.state.user2Pits.filter(p => !p.big).map((p, i) => (
                                        <div className="hole-1" onClick={() => this.onGrab(p.user.id, p.index)}
                                             key={p.id}>
                                            <div className="marble-layer">
                                                {p.stones}
                                            </div>
                                            {/*<div className="hover-number">4</div>*/}
                                        </div>
                                    ))
                                }
                            </div>
                        </div>

                        <div id="col-3">
                            {
                                this.state.user2Pits.filter(p => p.big).map((p, i) => (
                                    <div id="mancala-2" key={p.id}>
                                        {p.stones}
                                    </div>
                                ))
                            }
                        </div>
                    </div>

                    {/*     <Player user = {this.state.fetched ?  this.state.data.user2 : defaultPlayer}/>*/}
                    <div id="player-2"
                         className={this.state.player2.currentPlayer ? 'currentUserBox' : ''}>{this.state.fetched ? this.state.player2.username : defaultPlayer.username}</div>


                </main>
                <footer>
                    <div id='footer-notes'> Created by Mohammad Reza Kargar (UI structure got from</div>
                    <a href='https://paige1381.github.io/Mancala/'>here</a> )
                </footer>
            </div>
        );
    }
}

export default Board;

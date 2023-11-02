package me.moreka.mancala.dto;

import me.moreka.mancala.entity.User;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 * Actually this class is not used because move transitions of stones is not implemented.
 * Maybe in the future.
 */
@Getter
@Setter
public class UserActionResult {
    private User currentPlayer;
    // normal moves in addition to bonus moves when a player grab his opponent stones and put them to his big pit.
    private List<List<Move>> moves;
    // after a player wins, his stones should be moved to big pit.
    private List<Move> remainStonesMoves;

    public UserActionResult(List<List<Move>> moves) {
        this.moves = moves;
    }
}

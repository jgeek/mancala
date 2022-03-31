package com.bol.assignment.dto;

import static java.util.stream.Collectors.toList;

import com.bol.assignment.entity.Game;
import com.bol.assignment.entity.Pit;
import com.bol.assignment.entity.Player;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GameDto {
    private long gameId;
    private PlayerDto player1;
    private PlayerDto player2;
    private List<Pit> user1Pits;
    private List<Pit> user2Pits;
    private PlayerDto winner;

    public static GameDto toDto(Game game) {
        var user1Pits = game.getPits().stream()
                .filter(p -> p.getUser().equals(game.getPlayer1()))
                .collect(toList());
        var user2Pits = game.getPits().stream()
                .filter(p -> p.getUser().equals(game.getPlayer2()))
                .collect(toList());
        return new GameDto(game.getId(), PlayerDto.toDto(game.getPlayer1(), game.getCurrentPlayer()),
                PlayerDto.toDto(game.getPlayer2(), game.getCurrentPlayer()),
                user1Pits,
                user2Pits,
                game.getWinner() != null ? PlayerDto.toDto(game.getWinner(), game.getCurrentPlayer()) : null);
    }
}

package com.bol.assignment.controller;

import com.bol.assignment.dto.*;
import com.bol.assignment.entity.Game;
import com.bol.assignment.service.GameFactory;
import com.bol.assignment.entity.Pit;
import com.bol.assignment.entity.Player;
import com.bol.assignment.repository.GameRepository;
import com.bol.assignment.repository.PitRepository;
import com.bol.assignment.repository.PlayerRepository;
import com.bol.assignment.service.GamePlayService;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.web.bind.annotation.*;

@RestController
@Data
@AllArgsConstructor
public class GamePlayController {

    private final GameRepository gameRepo;
    private final PlayerRepository userRepo;
    private final PitRepository pitRepo;
    private final GameFactory gameFactory;
    private final GamePlayService gamePlayService;

    @PostMapping("/games/start")
    public Result<GameDto> start(@RequestBody PlayersDto users) {
        String username1 = users.getPlayer1().getUsername();
        var player1 = userRepo.findByUsername(username1);
        if (player1 == null) {
            player1 = userRepo.save(new Player(username1));
        }
        String username2 = users.getPlayer2().getUsername();
        var player2 = userRepo.findByUsername(username2);
        if (player2 == null) {
            player2 = userRepo.save(new Player(username2));
        }
        var game = gameFactory.create(player1, player2);
        return new Result<>(true, "", GameDto.toDto(game));
    }

    @PostMapping("/games/{gameId}/move")
    public Result<GameDto> move(@PathVariable Long gameId, @RequestBody SelectedPit selectedPit) {
        Game game = gameRepo.findById(gameId).orElseThrow();
        Player player = userRepo.findById(selectedPit.getUserId()).orElseThrow();
        Pit pit =
                game.getPits().stream().filter(p -> p.getUser().equals(player))
                        .filter(p -> p.getIndex() == selectedPit.getPitIndex()).findFirst().get();
        String errorMessage = validateMove(game, player, pit);
        if (errorMessage == null) {
            var result = gamePlayService.generateMoves(game, player, pit);
        }
        return new Result<>(errorMessage == null, errorMessage, GameDto.toDto(game));
    }

    private String validateMove(Game game, Player player, Pit pit) {
        if (!gamePlayService.isUserTurn(game, player)) {
            return String.format("%s! It's not your turn", player.getUsername());
        }
        if (!gamePlayService.isUserPit(player, pit)) {
            return String.format("%s! %s is not your pit.", player.getUsername(), pit.getIndex());
        }
        if (!gamePlayService.hasEnoughStone(pit)) {
            return String.format("Pit %s has no stone. Try another pit ;)", pit.getIndex());
        }
        return null;
    }

    @GetMapping("/games")
    public List<Game> getAllGames() {
        return gameRepo.findAll();
    }

    @GetMapping("/games/load/{id}")
    public Result<GameDto> loadGame(@PathVariable(required = true) Long id) {
        var game = gameRepo.findById(id).orElseThrow();
        return new Result<>(true, "", GameDto.toDto(game));
    }
}
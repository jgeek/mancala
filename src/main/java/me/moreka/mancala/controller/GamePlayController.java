package me.moreka.mancala.controller;

import me.moreka.mancala.dto.GameDto;
import me.moreka.mancala.dto.SelectedPit;
import me.moreka.mancala.entity.Game;
import me.moreka.mancala.service.GameService;
import me.moreka.mancala.entity.Pit;
import me.moreka.mancala.entity.User;
import me.moreka.mancala.repository.GameRepository;
import me.moreka.mancala.repository.PitRepository;
import me.moreka.mancala.repository.UserRepository;
import me.moreka.mancala.service.GamePlayService;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import me.moreka.mancala.dto.Result;
import me.moreka.mancala.dto.UsersDto;
import org.springframework.web.bind.annotation.*;

@RestController
@Data
@AllArgsConstructor
public class GamePlayController {

    private final GameRepository gameRepo;
    private final UserRepository userRepo;
    private final PitRepository pitRepo;
    private final GameService gameService;
    private final GamePlayService gamePlayService;

    @PostMapping("/games/start")
    public Result<GameDto> start(@RequestBody UsersDto users) {
        var game = gameService.create2(users.getPlayer1().getUsername(), users.getPlayer2().getUsername());
        return new Result<>(true, "", GameDto.toDto(game));
    }

    @PostMapping("/games/{gameId}/move")
    public Result<GameDto> move(@PathVariable Long gameId, @RequestBody SelectedPit selectedPit) {
        Game game = gameRepo.findById(gameId).orElseThrow();
        User player = userRepo.findById(selectedPit.getUserId()).orElseThrow();
        Pit pit =
                game.getPits().stream().filter(p -> p.getUser().equals(player))
                        .filter(p -> p.getIndex() == selectedPit.getPitIndex()).findFirst().get();
        String errorMessage = validateMove(game, player, pit);
        if (errorMessage == null) {
            var result = gamePlayService.generateMoves(game, player, pit);
        }
        return new Result<>(errorMessage == null, errorMessage, GameDto.toDto(game));
    }

    private String validateMove(Game game, User player, Pit pit) {
        if (!gamePlayService.isUserTurn(game, player)) {
            return String.format("%s! It's not your turn", player.getUsername());
        }
        if (!pit.isPitOf(player)) {
            return String.format("%s! %s is not your pit.", player.getUsername(), pit.getIndex());
        }
        if (!pit.hasStone()) {
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
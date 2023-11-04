package me.moreka.mancala.controller;

import me.moreka.mancala.dto.GameDto;
import me.moreka.mancala.dto.SelectedPit;
import me.moreka.mancala.entity.Game;
import me.moreka.mancala.service.GameService;
import me.moreka.mancala.entity.Pit;
import me.moreka.mancala.entity.User;
import me.moreka.mancala.repository.GameRepository;
import me.moreka.mancala.repository.UserRepository;
import me.moreka.mancala.service.GamePlayService;

import lombok.AllArgsConstructor;
import me.moreka.mancala.dto.Result;
import me.moreka.mancala.dto.UsersDto;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
public class GamePlayController {

    private final GameRepository gameRepo;
    private final UserRepository userRepo;
    private final GameService gameService;
    private final GamePlayService gamePlayService;

    @PostMapping("/games/start")
    public Result<GameDto> start(@RequestBody UsersDto users) {
        var game = gameService.createGame(users.getPlayer1().getUsername(), users.getPlayer2().getUsername());
        return new Result<>(true, "Game started successfully.", GameDto.toDto(game));
    }

    @PostMapping("/games/{gameId}/move")
    public Result<GameDto> move(@PathVariable Long gameId, @RequestBody SelectedPit selectedPit) {
        Game game = gameRepo.findById(gameId).orElseThrow();
        User user = userRepo.findById(selectedPit.getUserId()).orElseThrow();
        Pit pit = game.pitsOf(user).stream()
                .filter(p -> p.getIndex() == selectedPit.getPitIndex()).findFirst().get();

        game.validateMove(user, pit);
        gamePlayService.generateMoves(game, user, pit);
        return new Result<>(true, "", GameDto.toDto(game));
    }
}
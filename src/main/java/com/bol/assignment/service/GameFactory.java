package com.bol.assignment.service;

import static java.util.stream.Collectors.*;

import com.bol.assignment.entity.Game;
import com.bol.assignment.entity.Pit;
import com.bol.assignment.entity.Player;
import com.bol.assignment.repository.GameRepository;
import com.bol.assignment.repository.PitRepository;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class GameFactory {

    public static int PITS = 6;
    public static int STONES = 6;
    private final GameRepository gameRepo;
    private final PitRepository pitRepo;

    public Game create(Player player1, Player player2) {

        Game game = gameRepo.save(new Game("mancala-" + UUID.randomUUID().getMostSignificantBits(), player1, player2,
                new Date()));
        gameRepo.save(game);
        List<Pit> pits = new ArrayList<>();
        List<Pit> player1Pits = createUserPits(game, player1);
        List<Pit> player2Pits = createUserPits(game, player2);
        pits.addAll(player1Pits);
        pits.addAll(player2Pits);
        pitRepo.saveAll(pits);
        game.setPits(pits);
        game.setCurrentPlayer(player1);
        gameRepo.save(game);
        return game;
    }

    private List<Pit> createUserPits(Game game, Player player) {
        List<Pit> pits = new ArrayList<>();
        List<Pit> normalPits = IntStream.rangeClosed(0, 5).mapToObj(i -> new Pit(game, player, false, STONES, i))
                .collect(toList());
        Pit mancala = new Pit(game, player, true, 0, 6);
        pits.addAll(normalPits);
        pits.add(mancala);
        return pits;
    }
}

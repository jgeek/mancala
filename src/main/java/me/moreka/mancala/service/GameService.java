package me.moreka.mancala.service;

import static java.util.stream.Collectors.*;

import me.moreka.mancala.entity.Game;
import me.moreka.mancala.entity.Pit;
import me.moreka.mancala.entity.User;
import me.moreka.mancala.repository.GameRepository;
import me.moreka.mancala.repository.PitRepository;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

import me.moreka.mancala.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class GameService {

    public static int PITS = 6;
    public static int STONES = 6;
    private final GameRepository gameRepo;
    private final PitRepository pitRepo;
    private final UserRepository userRepository;
    private final UserService userService;
    public Game create2(String username1, String username2) {

        var user1 = userService.findOrCreateUser(username1);
        var user2 = userService.findOrCreateUser(username2);

        Game game = gameRepo.save(new Game("mancala-" + UUID.randomUUID().getMostSignificantBits(), user1, user2,
                new Date()));
        gameRepo.save(game);

        List<Pit> pits = new ArrayList<>();
        List<Pit> player1Pits = createUserPits(game, user1);
        List<Pit> player2Pits = createUserPits(game, user2);
        pits.addAll(player1Pits);
        pits.addAll(player2Pits);
        pitRepo.saveAll(pits);
        game.setPits(pits);
        game.setCurrentPlayer(user1);
        gameRepo.save(game);
        return game;
    }


    private List<Pit> createUserPits(Game game, User player) {
        List<Pit> pits = new ArrayList<>();
        List<Pit> normalPits = IntStream.rangeClosed(0, 5).mapToObj(i -> new Pit(game, player, false, STONES, i))
                .collect(toList());
        Pit mancala = new Pit(game, player, true, 0, 6);
        pits.addAll(normalPits);
        pits.add(mancala);
        return pits;
    }
}

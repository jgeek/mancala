package me.moreka.mancala.service;

import static java.util.stream.Collectors.*;

import me.moreka.mancala.dto.Move;
import me.moreka.mancala.dto.UserActionResult;
import me.moreka.mancala.entity.Game;
import me.moreka.mancala.entity.Pit;
import me.moreka.mancala.entity.User;
import me.moreka.mancala.repository.GameRepository;
import me.moreka.mancala.repository.PitRepository;
import java.util.*;
import java.util.stream.IntStream;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class GamePlayService {

    private final GameRepository gameRepo;
    private final PitRepository pitRepo;

    public boolean isUserTurn(Game game, User user) {
        return game.getCurrentPlayer().equals(user);
    }

    public boolean isUserPit(User user, Pit pit) {
        return pit.getUser().equals(user);
    }

    /**
     * @param game   Game entity
     * @param player current player
     * @param pit    selected pit
     * @return A wrapper contains all if moves needed to cover the player action. It also save new states of the game
     * and the pits.
     */
    public UserActionResult generateMoves(Game game, User player, Pit pit) {
        var stones = pit.getStones();
        List<Pit> userPits = game.getPits().stream().filter(p -> p.getUser().equals(player)).collect(toList());
        List<Pit> userPitsAfter = userPits.stream().filter(p -> p.getIndex() > pit.getIndex()).collect(toList());
        List<Pit> candidatePits = new ArrayList<>();
        candidatePits.addAll(userPitsAfter);
        List<Pit> opponentPits = game.getPits().stream().filter(p -> !p.getUser().equals(player))
                .filter(p -> !p.isBig())
                .collect(toList());
        candidatePits.addAll(opponentPits);
        List<Pit> userPitsBefore = userPits.stream().filter(p -> p.getIndex() <= pit.getIndex()).collect(toList());
        candidatePits.addAll(userPitsBefore);

        Map<Pit, Integer> pitsMap = new LinkedHashMap<>();
        candidatePits.forEach(p -> pitsMap.put(p, 0));

        pit.setStones(0);
        pitRepo.save(pit);

        Pit lastPit = null;
        Set<Pit> changedPits = new HashSet<>();
        for (int i = 0; stones > 0; i++) {
            var currentPit = candidatePits.get(i);
            pitsMap.put(currentPit, pitsMap.get(currentPit) + 1);
            currentPit.setStones(currentPit.getStones() + 1);
            changedPits.add(currentPit);
            lastPit = currentPit;
            stones--;
            if (i == candidatePits.size() - 1) {
                i = 0;
            }
        }
        changedPits.forEach(pitRepo::save);

        List<List<Move>> moves = new ArrayList<>();
        boolean keepGoing = true;
        List<Move> gameMoves = new ArrayList<>();
        while (keepGoing) {
            for (Map.Entry<Pit, Integer> e : pitsMap.entrySet()) {
                if (e.getValue() > 0) {
                    e.setValue(e.getValue() - 1);
                    gameMoves.add(new Move(pit, e.getKey(), 1));
                } else {
                    keepGoing = false;
                    break;
                }
            }

        }
        moves.add(gameMoves);

        List<Move> grabMoves = grabOpponentStonesIfLastPitIsEmpty(player, userPits, opponentPits, lastPit);
        moves.add(grabMoves);

        User nextTurnPlayer = determineTheTurn(game, player, lastPit);
        game.setCurrentPlayer(nextTurnPlayer);

        var result = new UserActionResult(moves);
        User hasStoneUser = checkIfGameEnded(game);
        if (hasStoneUser != null) {
            User winner = findWinner(game);
            game.setWinner(winner);

            List<Move> hasStoneUserMoves = generateHasStoneUserMoves(game, hasStoneUser);
            result.setRemainStonesMoves(hasStoneUserMoves);
        }
        gameRepo.save(game);
        result.setCurrentPlayer(nextTurnPlayer);
        return result;
    }

    private List<Move> grabOpponentStonesIfLastPitIsEmpty(User user, List<Pit> userPits,
                                                          List<Pit> opponentPits, Pit lastPit) {
        Pit bigPit = userPits.stream().filter(Pit::isBig).findFirst().get();

        Optional<Pit> toGrabPit = Optional.empty();
        if (lastPit.getStones() == 1 && lastPit.getUser().equals(user) && !lastPit.isBig()) {
            toGrabPit = opponentPits.stream().filter(p -> p.getIndex() == GameService.PITS - 1 - lastPit.getIndex())
                    .findFirst();
        }
        Optional<Pit> finalToGrabPit = toGrabPit;
        List<Move> grabMoves = toGrabPit.stream().flatMap(p -> {
            List<Move> moves = new ArrayList<>();
            bigPit.setStones(p.getStones() + bigPit.getStones() + 1);
            var bonusMoves = p.getStones();
            List<Move> extraMoves = IntStream.range(0, bonusMoves)
                    .mapToObj(i -> new Move(finalToGrabPit.get(), bigPit, 1)).collect(toList());
            extraMoves.add(new Move(lastPit, bigPit, 1));
            p.setStones(0);
            pitRepo.save(p);
            pitRepo.save(bigPit);
            moves.addAll(extraMoves);
            lastPit.setStones(0);
            pitRepo.save(lastPit);
            return moves.stream();
        }).collect(toList());
        return grabMoves;
    }

    private User findWinner(Game game) {
        List<Pit> mancalas = game.getMancalas();
        if (mancalas.get(0).getStones() > mancalas.get(1).getStones()) {
            return mancalas.get(0).getUser();
        } else {
            return mancalas.get(1).getUser();
        }
    }

    private List<Move> generateHasStoneUserMoves(Game game, User winner) {
        List<Pit> pits = game.getPits().stream().filter(p -> p.getUser().equals(winner)).collect(toList());
        Pit bigPit = pits.stream().filter(Pit::isBig).findFirst().get();
        List<Move> moves = new ArrayList<>();
        for (Pit pit : pits) {
            if (pit.equals(bigPit) || pit.getStones() == 0) {
                continue;
            }
            moves.add(new Move(pit, bigPit, pit.getStones()));
            bigPit.setStones(bigPit.getStones() + pit.getStones());
            pit.setStones(0);
            pitRepo.save(pit);
            pitRepo.save(bigPit);
        }
        return moves;
    }

    private User determineTheTurn(Game game, User player, Pit lastPit) {
        if (lastPit.getUser().equals(player) && lastPit.isBig()) {
            return player;
        } else {
            return game.getPlayer1().equals(player) ? game.getPlayer2() : game.getPlayer1();
        }
    }

    public User checkIfGameEnded(Game game) {
        Map<User, List<Pit>> collect = game.getPits().stream().collect(groupingBy(Pit::getUser));
        User hasRemainedStonesUser = null;
        for (Map.Entry<User, List<Pit>> e : collect.entrySet()) {
            int sum = e.getValue().stream().filter(p -> !p.isBig()).mapToInt(Pit::getStones).sum();
            if (sum == 0) {
                hasRemainedStonesUser = collect.keySet().stream().filter(u -> !u.equals(e.getKey())).findFirst().get();
            }
        }
        return hasRemainedStonesUser;
    }
}

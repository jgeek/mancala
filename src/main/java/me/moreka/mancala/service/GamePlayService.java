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
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class GamePlayService {

    private final GameRepository gameRepo;
    private final PitRepository pitRepo;

    /**
     * @param game Game entity
     * @param user current player
     * @param pit  selected pit
     * @return A wrapper contains all if moves needed to cover the player action. It also save new states of the game
     * and the pits.
     */
    @Transactional
    public UserActionResult generateMoves(Game game, User user, Pit pit) {
        var stones = pit.getStones();
        List<Pit> userPits = game.getPits().stream().filter(p -> p.getUser().equals(user)).collect(toList());
        List<Pit> pitsWhichGetStones = findToGetStonePits(game, userPits, pit, user);

        Map<Pit, Integer> pitsMap = new LinkedHashMap<>();
        pitsWhichGetStones.forEach(p -> pitsMap.put(p, 0));

        pit.setStones(0);
        pitRepo.save(pit);

        Set<Pit> changedPits = new HashSet<>();
        Pit lastPit = putStoneInPits(pit, stones, pitsWhichGetStones, pitsMap, changedPits);
        pitRepo.saveAll(changedPits);

        List<List<Move>> moves = new ArrayList<>();
        List<Move> normalMoves = generateStoneMoves(pit, pitsMap);
        moves.add(normalMoves);

        List<Pit> opponentPits = game.pitsExcludeBigOnOf(game.opponentOf(user));

        Pit toGrabPit = findToGrabPit(user, opponentPits, lastPit);
        if (toGrabPit != null) {
            Pit bigPit = userPits.stream().filter(Pit::isBig).findFirst().get();
            bigPit.setStones(toGrabPit.getStones() + bigPit.getStones() + 1);
            List<Move> grabMoves = generateGrabbedOpponentStonesMoves(lastPit, toGrabPit, bigPit);
            toGrabPit.setStones(0);
            lastPit.setStones(0);
            pitRepo.save(toGrabPit);
            pitRepo.save(bigPit);
            pitRepo.save(lastPit);
            moves.add(grabMoves);
        }
        User nextTurnPlayer = game.determineTheTurn(user, lastPit);
        game.setCurrentPlayer(nextTurnPlayer);

        var result = new UserActionResult(moves);
        User hasStoneUser = checkIfGameEnded(game);
        if (hasStoneUser != null) {
            User winner = findWinner(game);
            game.setWinner(winner);
            List<Pit> pits = game.pitsExcludeBigOnOf(winner);
            Pit bigPit = game.bigPitOf(winner);
            List<Move> hasStoneUserMoves = generateHasStoneUserMoves(pits, bigPit);
            for (Move move : hasStoneUserMoves) {
                bigPit.setStones(bigPit.getStones() + move.getStone());
                move.getSource().setStones(0);
                pitRepo.save(move.getSource());
            }
            pitRepo.save(bigPit);
            result.setRemainStonesMoves(hasStoneUserMoves);
        }
        game.updateUserStones();
        gameRepo.save(game);
        result.setCurrentPlayer(nextTurnPlayer);
        return result;
    }

    private List<Move> generateStoneMoves(Pit pit, Map<Pit, Integer> pitsMap) {
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
        return gameMoves;
    }

    private Pit putStoneInPits(Pit selectedPit, int stones, List<Pit> toGetStonePits, Map<Pit, Integer> pitsMap, Set<Pit> changedPits) {
        Pit lastPit = null;
        for (int i = 0; stones > 0; i++) {
            var destinationPit = toGetStonePits.get(i);
            pitsMap.put(destinationPit, pitsMap.get(destinationPit) + 1);
            destinationPit.setStones(destinationPit.getStones() + 1);
            changedPits.add(destinationPit);
            lastPit = destinationPit;
            stones--;
            if (lastPitIsThePitItself(destinationPit, selectedPit)) {
                i = 0;
            }
        }
        return lastPit;
    }

    private boolean lastPitIsThePitItself(Pit currentPit, Pit selectedPit) {
        return currentPit.equals(selectedPit);
    }


    private List<Pit> findToGetStonePits(Game game, List<Pit> userPits, Pit currentPit, User user) {
        List<Pit> toGetStonePits = new ArrayList<>();
        List<Pit> userPitsAfter = userPits.stream().filter(p -> p.getIndex() > currentPit.getIndex()).collect(toList());
        toGetStonePits.addAll(userPitsAfter);
        List<Pit> opponentPits = game.pitsOf(game.opponentOf(user)).stream().filter(p -> !p.isBig()).collect(toList());
        toGetStonePits.addAll(opponentPits);
        List<Pit> userPitsBefore = userPits.stream().filter(p -> p.getIndex() < currentPit.getIndex()).collect(toList());
        toGetStonePits.addAll(userPitsBefore);
        toGetStonePits.add(currentPit);
        return toGetStonePits;
    }

    private List<Move> generateGrabbedOpponentStonesMoves(Pit lastPit, Pit toGrabPit, Pit bigPit) {
        var bonusMoves = toGrabPit.getStones();
        List<Move> extraMoves = IntStream.range(0, bonusMoves).mapToObj(i -> new Move(toGrabPit, bigPit, 1)).collect(toList());
        extraMoves.add(new Move(lastPit, bigPit, 1));
        return extraMoves;
    }

    private Pit findToGrabPit(User user, List<Pit> opponentPits, Pit lastPit) {
        Pit toGrabPit;
        if (shouldGrabOpponentStones(user, lastPit)) {
            toGrabPit = pitOfOpponentToGrabStones(opponentPits, lastPit);
        } else {
            toGrabPit = null;
        }
        return toGrabPit;
    }

    private Pit pitOfOpponentToGrabStones(List<Pit> opponentPits, Pit lastPit) {
        return opponentPits.stream().filter(p -> p.getIndex() == GameService.PITS - 1 - lastPit.getIndex()).findFirst().get();
    }

    private boolean shouldGrabOpponentStones(User user, Pit lastPit) {
        return lastPit.getStones() == 1 && lastPit.getUser().equals(user) && !lastPit.isBig();
    }

    private User findWinner(Game game) {
        Map<User, Integer> userSumOfStones = game.getPits().stream()
                .map(p -> Map.entry(p.getUser(), p.getStones()))
                .collect(groupingBy(Map.Entry::getKey, summingInt(Map.Entry::getValue)));
        if (userSumOfStones.get(game.getPlayer1()) > userSumOfStones.get(game.getPlayer2())) {
            return game.getPlayer1();
        } else {
            return game.getPlayer2();
        }
    }

    private List<Move> generateHasStoneUserMoves(List<Pit> pits, Pit bigPit) {
        List<Move> moves = new ArrayList<>();
        for (Pit pit : pits) {
            if (pit.getStones() == 0) {
                continue;
            }
            moves.add(new Move(pit, bigPit, pit.getStones()));
        }
        return moves;
    }

    public User checkIfGameEnded(Game game) {
        Map<User, List<Pit>> userPits = game.getPits().stream().filter(p -> !p.isBig()).collect(groupingBy(Pit::getUser));
        User stillHasStoneUser = null;
        for (Map.Entry<User, List<Pit>> e : userPits.entrySet()) {
            int userStones = e.getValue().stream().mapToInt(Pit::getStones).sum();
            if (userStones == 0) {
                stillHasStoneUser = game.opponentOf(e.getKey());
            }
        }
        return stillHasStoneUser;
    }
}

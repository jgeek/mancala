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
     * @param game        Game entity
     * @param user        current player
     * @param selectedPit selected pit
     * @return A wrapper contains all if moves needed to cover the player action. It also save new states of the game
     * and the pits.
     */
    @Transactional
    public UserActionResult generateMoves(Game game, User user, Pit selectedPit) {
        List<Pit> userPits = game.pitsOf(user);
        List<Pit> pitsWillGetStones = findToGetStonePits(game, userPits, selectedPit, user);

        var stones = selectedPit.getStones();
        selectedPit.setStones(0);
        pitRepo.save(selectedPit);

        Set<Pit> changedPits = new HashSet<>();
        Map<Pit, Integer> pitsMap = new LinkedHashMap<>();
        pitsWillGetStones.forEach(p -> pitsMap.put(p, 0));
        Pit lastPit = putStoneInPits(selectedPit, stones, pitsWillGetStones, pitsMap, changedPits);
        pitRepo.saveAll(changedPits);

        List<List<Move>> moves = new ArrayList<>();
        List<Move> normalMoves = generateStoneMoves(selectedPit, pitsMap);
        moves.add(normalMoves);

        List<Pit> opponentPits = game.pitsExcludeBigOnOf(game.opponentOf(user));

        Pit toGrabPit = findToGrabPit(user, opponentPits, lastPit);
        if (toGrabPit != null) {
//            Pit bigPit = userPits.stream().filter(Pit::isBig).findFirst().get();
            Pit bigPit = game.bigPitOf(user);
            bigPit.setStones(toGrabPit.getStones() + bigPit.getStones() + 1);
            List<Move> grabMoves = generateGrabbedOpponentStonesMoves(lastPit, toGrabPit, bigPit);
            toGrabPit.setStones(0);
            lastPit.setStones(0);
            pitRepo.save(toGrabPit);
            pitRepo.save(bigPit);
            pitRepo.save(lastPit);
            moves.add(grabMoves);
        }
        var result = new UserActionResult(moves);

        User zeroStoneUser = findUserWithZeroStone(game);
        if (zeroStoneUser != null) {
            User winner = findWinner(game);
            game.setWinner(winner);
            var hasStoneUser = game.opponentOf(zeroStoneUser);
            List<Pit> pits = game.pitsExcludeBigOnOf(hasStoneUser);
            Pit bigPit = game.bigPitOf(hasStoneUser);
            List<Move> hasStoneUserMoves = generateHasStoneUserMoves(pits, bigPit);
            for (Move move : hasStoneUserMoves) {
                bigPit.setStones(bigPit.getStones() + move.getStones());
                move.getSource().setStones(0);
                pitRepo.save(move.getSource());
            }
            pitRepo.save(bigPit);
            result.setRemainStonesMoves(hasStoneUserMoves);
        }
        game.updateUserStones();
        User nextTurnPlayer = game.determineTheTurn(user, lastPit);
        game.setCurrentPlayer(nextTurnPlayer);
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

    private User findUserWithZeroStone(Game game) {
        Map<User, List<Pit>> userPits = game.getPits().stream().filter(p -> !p.isBig()).collect(groupingBy(Pit::getUser));
        User zeroStoneUser = null;
        for (Map.Entry<User, List<Pit>> e : userPits.entrySet()) {
            int userStones = e.getValue().stream().mapToInt(Pit::getStones).sum();
            if (userStones == 0) {
                zeroStoneUser = e.getKey();
            }
        }
        return zeroStoneUser;
    }
}

package me.moreka.mancala.service;

import lombok.AllArgsConstructor;
import me.moreka.mancala.dto.Move;
import me.moreka.mancala.entity.Game;
import me.moreka.mancala.entity.Pit;
import me.moreka.mancala.entity.User;
import me.moreka.mancala.repository.GameRepository;
import me.moreka.mancala.repository.PitRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Stream;

import static java.util.stream.Collectors.*;

@Service
@AllArgsConstructor
public class MoveService {

    private final GameRepository gameRepository;
    private final PitRepository pitRepository;

    @Transactional
    public void move(Game game, User user, Pit selectedPit) {

        List<Move> normalMoves = generateNormalMoves(game, user, selectedPit);
        applyMove(normalMoves);
        List<Pit> firstStepPits = getPitsOf(normalMoves);
        pitRepository.saveAll(firstStepPits);

        List<Move> grabMoves = generateGrabMoves(game, user, normalMoves);
        if (!grabMoves.isEmpty()) {
            applyMove(grabMoves);
            List<Pit> grabStepPits = getPitsOf(normalMoves);
            pitRepository.saveAll(grabStepPits);
        }
        List<Move> hasStoneMoves = generateHasStoneMoves(game);
        if (!hasStoneMoves.isEmpty()) {
            applyMove(hasStoneMoves);
            List<Pit> hasStonePits = getPitsOf(normalMoves);
            pitRepository.saveAll(hasStonePits);
        }
        if (!hasStoneMoves.isEmpty()) {
            User winner = findWinner(game);
            if (winner != null) {
                game.setWinner(winner);
            }
        }
        game.updateUserStones();
        Pit lastPit = getLastPitsOf(normalMoves);
        User nextTurnPlayer = game.determineTheTurn(user, lastPit);
        game.setCurrentPlayer(nextTurnPlayer);
        gameRepository.save(game);
    }

    private Pit getLastPitsOf(List<Move> moves) {
        return moves.get(moves.size() - 1).getDestination();
    }

    private List<Pit> getPitsOf(List<Move> normalMoves) {
        return normalMoves.stream().flatMap(m -> Stream.of(m.getSource(), m.getDestination())).collect(toList());
    }

    private void applyMove(List<Move> moves) {
        for (Move move : moves) {
            move.apply();
        }
    }

    public List<Move> generateNormalMoves(Game game, User user, Pit selectedPit) {

        List<Pit> userPits = game.pitsOf(user);
        List<Pit> candidatePitsToGetPits = findCandidatePitsToGetStone(game, userPits, selectedPit, user);
        List<Move> moves = putStoneInPits(selectedPit, candidatePitsToGetPits);
        return moves;
    }

    public List<Move> generateGrabMoves(Game game, User user, List<Move> normalMoves) {
        Pit lastPit = lastPitOfMoves(normalMoves);

        List<Pit> opponentPits = game.pitsExcludeBigOnOf(game.opponentOf(user));
        Pit toGrabPit = findToGrabPit(user, opponentPits, lastPit);
        if (toGrabPit != null) {
            Pit bigPit = game.bigPitOf(user);
            return generateGrabbedOpponentStonesMoves(lastPit, toGrabPit, bigPit);
        }
        return Collections.emptyList();
    }

    public List<Move> generateHasStoneMoves(Game game) {

        User zeroStoneUser = findUserWithZeroStone(game);
        if (zeroStoneUser != null) {
            var hasStoneUser = game.opponentOf(zeroStoneUser);
            List<Pit> pits = game.pitsExcludeBigOnOf(hasStoneUser);
            Pit bigPit = game.bigPitOf(hasStoneUser);
            return generateHasStoneUserMoves(pits, bigPit);
        }
        return Collections.emptyList();
    }

    private Pit lastPitOfMoves(List<Move> moves) {
        return moves.get(moves.size() - 1).getDestination();
    }

    private List<Pit> findCandidatePitsToGetStone(Game game, List<Pit> userPits, Pit currentPit, User user) {
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

    private Pit findToGrabPit(User user, List<Pit> opponentPits, Pit lastPit) {
        Pit toGrabPit;
        if (shouldGrabOpponentStones(user, lastPit)) {
            toGrabPit = pitOfOpponentToGrabStones(opponentPits, lastPit);
        } else {
            toGrabPit = null;
        }
        return toGrabPit;
    }

    private boolean shouldGrabOpponentStones(User user, Pit lastPit) {
        return lastPit.getStones() == 1 && lastPit.getUser().equals(user) && !lastPit.isBig();
    }

    private Pit pitOfOpponentToGrabStones(List<Pit> opponentPits, Pit lastPit) {
        return opponentPits.stream().filter(p -> p.getIndex() == GameService.PITS - 1 - lastPit.getIndex()).findFirst().get();
    }

    private List<Move> generateGrabbedOpponentStonesMoves(Pit lastPit, Pit toGrabPit, Pit bigPit) {
        var bonusMoves = toGrabPit.getStones();
//        List<Move> extraMoves = IntStream.range(0, bonusMoves).mapToObj(i -> new Move(toGrabPit, bigPit, 1)).collect(toList());
        List<Move> extraMoves = new ArrayList<>();
        extraMoves.add(new Move(toGrabPit, bigPit, bonusMoves));
        extraMoves.add(new Move(lastPit, bigPit, 1));
        return extraMoves;
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

    private List<Move> putStoneInPits(Pit selectedPit, List<Pit> toGetStonePits) {
        int stones = selectedPit.getStones();
        List<Move> moves = new ArrayList<>();
        int index = 0;
        while (stones > 0) {
            var destinationPit = toGetStonePits.get(index);
            moves.add(new Move(selectedPit, destinationPit, 1));
            stones--;
            if (lastPitIsThePitItself(destinationPit, selectedPit)) {
                index = 0;
                continue;
            }
            index++;
        }
        return moves;
    }

    private boolean lastPitIsThePitItself(Pit currentPit, Pit selectedPit) {
        return currentPit.equals(selectedPit);
    }
}

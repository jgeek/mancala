package me.moreka.mancala.entity;

import static java.util.stream.Collectors.toList;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Date;
import java.util.List;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import lombok.*;
import me.moreka.mancala.exception.InvalidMoveException;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Game extends BaseEntity {

    private String name;
    @OneToOne
    private User player1;
    @OneToOne
    private User player2;
    @OneToOne
    private User currentPlayer;
    @OneToOne
    private User winner;
    private int player1Score;
    private int player2Score;
    @OneToMany(mappedBy = "game")
    private List<Pit> pits;
    private Date date;

    public Game(String name, User user1, User user2, Date date) {
        this.name = name;
        this.player1 = user1;
        this.player2 = user2;
        this.date = date;
    }

    public Game(long id, String name) {
        super(id);
        this.name = name;
    }

    @JsonIgnore
    public List<Pit> getMancalas() {
        return getPits().stream().filter(Pit::isBig).collect(toList());
    }

    public boolean isUserTurn(User user) {
        return currentPlayer.equals(user);
    }

    public User opponentOf(User user) {
        if (user.equals(player1))
            return player2;
        return player1;
    }

    public List<Pit> pitsOf(User user) {
        return pits.stream().filter(p -> p.getUser().equals(user)).collect(toList());
    }

    public List<Pit> pitsExcludeBigOnOf(User user) {
        return pitsOf(user).stream().filter(p -> !p.isBig()).collect(toList());
    }

    public Pit bigPitOf(User user) {
        return pitsOf(user).stream().filter(Pit::isBig).findFirst().get();
    }

    public int stonesOf(User user) {
        return pitsOf(user).stream().mapToInt(Pit::getStones).sum();
    }

    public void validateMove(User user, Pit pit) {
        if (!isUserTurn(user)) {
            throw new InvalidMoveException(String.format("%s! It's not your turn", user.getUsername()));
        }
        if (!pit.isPitOf(user)) {
            throw new InvalidMoveException(String.format("%s! %s is not your pit.", user.getUsername(), pit.getIndex()));
        }
        if (!pit.hasStone()) {
            throw new InvalidMoveException(String.format("Pit %s has no stone. Try another pit ;)", pit.getIndex()));
        }
    }

    public User determineTheTurn(User user, Pit lastPit) {
        if (lastPit.getUser().equals(user) && lastPit.isBig()) {
            return user;
        } else {
            return opponentOf(user);
        }
    }

    public void updateUserStones() {
        setPlayer1Score(bigPitOf(player1).getStones());
        setPlayer2Score(bigPitOf(player2).getStones());
    }
}

package com.bol.assignment.entity;

import static java.util.stream.Collectors.toList;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Date;
import java.util.List;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import lombok.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Game extends BaseEntity {

    private String name;
    @OneToOne
    private Player player1;
    @OneToOne
    private Player player2;
    @OneToOne
    private Player currentPlayer;
    @OneToOne
    private Player winner;
    private int player1Score;
    private int player2Score;
    @OneToMany(mappedBy = "game")
    private List<Pit> pits;
    private Date date;

    public Game(String name, Player user1, Player user2, Date date) {
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
}

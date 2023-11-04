package me.moreka.mancala.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import me.moreka.mancala.dto.Move;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Pit extends BaseEntity {

    @ManyToOne
    @JsonIgnore
    private Game game;
    @OneToOne
    private User user;
    private boolean big;
    private int stones;
    private int index;

    public boolean hasStone() {
        return stones > 0;
    }

    public boolean isPitOf(User user) {
        return this.user.equals(user);
    }

    public void increase(int stones) {
        this.stones += stones;
    }

    public void decrease(int stones) {
        this.stones -= stones;
    }
}

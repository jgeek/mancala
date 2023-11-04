package me.moreka.mancala.dto;

import me.moreka.mancala.entity.Pit;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class Move {
    private Pit source;
    private Pit destination;
    private int stones;

    @Override
    public String toString() {
        return String.format("User %s, pit %s -> User %s, pit %s", source.getUser(), source.getIndex(),
                destination.getUser(), destination.getIndex());
    }

    public void apply() {
        if (!source.equals(destination)) {
            source.decrease(stones);
            destination.increase(stones);
        }
    }
}

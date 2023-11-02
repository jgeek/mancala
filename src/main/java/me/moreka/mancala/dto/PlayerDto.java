package me.moreka.mancala.dto;

import me.moreka.mancala.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PlayerDto {
    private long id;
    private String username;
    private boolean currentPlayer;

    public static PlayerDto toDto(User user, User currentUser) {
        return new PlayerDto(user.getId(), user.getUsername(), user.getId().equals(currentUser.getId()));
    }
}

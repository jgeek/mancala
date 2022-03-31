package com.bol.assignment.dto;

import com.bol.assignment.entity.Player;
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

    public static PlayerDto toDto(Player user, Player currentUser) {
        return new PlayerDto(user.getId(), user.getUsername(), user.getId().equals(currentUser.getId()));
    }
}

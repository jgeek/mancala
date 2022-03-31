package com.bol.assignment.dto;

import com.bol.assignment.entity.Player;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlayersDto {
    private Player player1;
    private Player player2;
}

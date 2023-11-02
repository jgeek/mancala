package me.moreka.mancala.dto;

import me.moreka.mancala.entity.User;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsersDto {
    private User player1;
    private User player2;
}

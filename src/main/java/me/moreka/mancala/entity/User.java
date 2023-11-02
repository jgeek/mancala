package me.moreka.mancala.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import lombok.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class User extends BaseEntity {
    @Column(unique = true)
    private String username;

    @Override
    public String toString() {
        return getUsername();
    }
}

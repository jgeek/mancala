package com.bol.assignment.entity;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Entity;
import lombok.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Player extends BaseEntity {
    @Column(unique = true)
    private String username;

    @Override
    public String toString() {
        return getUsername();
    }
}

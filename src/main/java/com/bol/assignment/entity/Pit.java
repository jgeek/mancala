package com.bol.assignment.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
    private Player user;
    private boolean big;
    private int stones;
    private int index;
}

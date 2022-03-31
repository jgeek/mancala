package com.bol.assignment.repository;

import com.bol.assignment.entity.Game;
import com.bol.assignment.entity.Pit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PitRepository extends JpaRepository<Pit, Long> {
    public Pit findByGameIdAndIndex(long gameId, int index);
}
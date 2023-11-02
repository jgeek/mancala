package me.moreka.mancala.repository;

import me.moreka.mancala.entity.Pit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PitRepository extends JpaRepository<Pit, Long> {
    public Pit findByGameIdAndIndex(long gameId, int index);
}
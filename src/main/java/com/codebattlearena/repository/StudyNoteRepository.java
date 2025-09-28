package com.codebattlearena.repository;

import com.codebattlearena.model.StudyNote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudyNoteRepository extends JpaRepository<StudyNote, Long> {
    List<StudyNote> findByUserIdOrderByCreatedAtDesc(Long userId);
    Optional<StudyNote> findByUserIdAndTitle(Long userId, String title);
}
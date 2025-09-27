package com.codebattlearena.repository;

import com.codebattlearena.model.StudyNote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StudyNoteRepository extends JpaRepository<StudyNote, Long> {
    List<StudyNote> findByUserIdOrderByCreatedAtDesc(Long userId);
}

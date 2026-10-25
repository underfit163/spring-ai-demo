package ru.t1.springaidemo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.t1.springaidemo.model.LoadedDocument;

public interface LoadedDocumentRepository extends JpaRepository<LoadedDocument, Long> {

    boolean existsByFilenameAndContentHash(String filename, String contentHash);

}

package org.example;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.stream.Stream;
import lombok.Builder;
import lombok.Data;

/**
 * For implement this task focus on clear code, and make this solution as simple readable as
 * possible Don't worry about performance, concurrency, etc You can use in Memory collection for
 * sore data
 * <p>
 * Please, don't change class name, and signature for methods save, search, findById Implementations
 * should be in a single class This class could be auto tested
 */
public class DocumentManager {

  Set<Document> documents = new TreeSet<>(Comparator.comparing(Document::getId));

  /**
   * Implementation of this method should upsert the document to your storage And generate unique id
   * if it does not exist
   *
   * @param document - document content and author data
   * @return saved document
   */
  public Document save(Document document) {
    var documentOptional = findById(document.getId());

    if (documentOptional.isPresent()) {
      throw new RuntimeException("Document with such id is already exists!");
    }

    if (document.id.isEmpty()) {
      document.setId(UUID.randomUUID().toString());
    }

    documents.add(document);
    return document;
  }

  private boolean idIsEquals(String id, Document savedDocument) {
    return savedDocument.id.contentEquals(id);
  }

  /**
   * Implementation this method should find documents which match with request
   *
   * @param request - search request, each field could be null
   * @return list matched documents
   */
  public List<Document> search(SearchRequest request) {
    Stream<Document> documentStream = documents.stream();

    Optional.ofNullable(request.titlePrefixes).ifPresent(
        prefixes -> documentStream.filter(document ->
            prefixes.stream().anyMatch(prefix -> document.title.startsWith(prefix))
        )
    );

    Optional.ofNullable(request.containsContents).
        ifPresent(content -> documentStream.filter(
            document -> content.stream().anyMatch(value -> document.content.contains(value))));

    Optional.ofNullable(request.authorIds).ifPresent(
        authorsIds -> documentStream.filter(document -> authorsIds.contains(document.author.id)));

    Optional.ofNullable(request.createdFrom).ifPresent(
        createdFrom -> documentStream.filter(document -> document.created.isAfter(createdFrom)));

    Optional.ofNullable(request.createdTo).ifPresent(
        createdTo -> documentStream.filter(document -> document.created.isBefore(createdTo)));

    return documentStream.toList();
  }

  /**
   * Implementation this method should find document by id
   *
   * @param id - document id
   * @return optional document
   */
  public Optional<Document> findById(String id) {
    return documents.stream().filter(savedDocument -> idIsEquals(id, savedDocument)).findAny();
  }

  @Data
  @Builder
  public static class SearchRequest {

    private List<String> titlePrefixes;
    private List<String> containsContents;
    private List<String> authorIds;
    private Instant createdFrom;
    private Instant createdTo;
  }

  @Data
  @Builder
  public static class Document {

    private String id;
    private String title;
    private String content;
    private Author author;
    private Instant created;
  }

  @Data
  @Builder
  public static class Author {

    private String id;
    private String name;
  }
}
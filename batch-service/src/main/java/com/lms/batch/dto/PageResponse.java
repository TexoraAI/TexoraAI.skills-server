
package com.lms.batch.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

// OPTIMIZATION: Added @JsonIgnoreProperties(ignoreUnknown = true).
// user-service returns a full Spring Data Page<T> envelope (pageable, sort, first,
// last, empty, etc.) but this DTO only models the 4 fields batch-service actually uses.
// Without this annotation, ANY extra field Spring's Page serializer includes — and
// there are several beyond just "pageable" — will throw the same UnrecognizedPropertyException
// one at a time. This fixes the whole class of errors at once instead of patching per-field.
@JsonIgnoreProperties(ignoreUnknown = true)
public class PageResponse<T> {
    private List<T> content;
    private int totalElements;
    private int totalPages;
    private int number;

    public List<T> getContent() { return content; }
    public void setContent(List<T> content) { this.content = content; }

    public int getTotalElements() { return totalElements; }
    public void setTotalElements(int totalElements) { this.totalElements = totalElements; }

    public int getTotalPages() { return totalPages; }
    public void setTotalPages(int totalPages) { this.totalPages = totalPages; }

    public int getNumber() { return number; }
    public void setNumber(int number) { this.number = number; }
}
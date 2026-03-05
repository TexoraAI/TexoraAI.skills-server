package com.lms.search.service;

import com.lms.search.kafka.SearchEventProducer;
import com.lms.search.model.SearchIndex;
import com.lms.search.repository.SearchRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SearchService {

    private final SearchRepository repo;
    private final SearchEventProducer producer;

    public SearchService(SearchRepository repo, SearchEventProducer producer) {
        this.repo = repo;
        this.producer = producer;
    }

    public SearchIndex addCourse(SearchIndex index) {
        SearchIndex saved = repo.save(index);
        producer.sendEvent("Course indexed: " + saved.getCourseTitle());
        return saved;
    }
//fjkdjkfn
    public List<SearchIndex> search(String keyword) {

        List<SearchIndex> results =
                repo.findByCourseTitleContainingIgnoreCaseOrCourseDescriptionContainingIgnoreCase(
                        keyword,
                        keyword
                );

        producer.sendEvent("Search performed for keyword: " + keyword);
        return results;
    }

}

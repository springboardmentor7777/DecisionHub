package com.decisionhub.service;

import com.decisionhub.dto.CategoryResponse;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.IntStream;

@Service
public class CategoryService {

    public List<CategoryResponse> listAll() {
        return IntStream.range(0, CategoryCatalog.all().size())
                .mapToObj(index -> new CategoryResponse(
                        (long) index + 1,
                        CategoryCatalog.all().get(index)))
                .toList();
    }
}
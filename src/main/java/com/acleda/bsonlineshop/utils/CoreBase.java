package com.acleda.bsonlineshop.utils;

import com.acleda.bsonlineshop.dto.common.ListRequest;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CoreBase {

    public static <T> Specification<T> filter(ListRequest request, Class<T> entityClass) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Always filter deleted
            try {
                root.get("deleted");
                predicates.add(cb.isFalse(root.get("deleted")));
            } catch (Exception ignored) {}

            // Dynamic filters from map
            if (request.getFilters() != null) {
                for (Map.Entry<String, String> entry : request.getFilters().entrySet()) {
                    String key = entry.getKey();
                    String value = entry.getValue();
                    if (value != null && !value.isBlank()) {
                        try {
                            predicates.add(cb.equal(
                                    cb.lower(root.get(key).as(String.class)),
                                    value.toLowerCase()
                            ));
                        } catch (Exception ignored) {}
                    }
                }
            }

            // Global search on name and description
            if (request.getSearch() != null && !request.getSearch().isBlank()) {
                String pattern = "%" + request.getSearch().toLowerCase() + "%";
                List<Predicate> searchPredicates = new ArrayList<>();
                for (String field : List.of("name", "brand", "description")) {
                    try {
                        root.get(field);
                        searchPredicates.add(cb.like(cb.lower(root.get(field)), pattern));
                    } catch (Exception ignored) {}
                }
                if (!searchPredicates.isEmpty()) {
                    predicates.add(cb.or(searchPredicates.toArray(new Predicate[0])));
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
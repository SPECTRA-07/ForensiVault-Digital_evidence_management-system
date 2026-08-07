package com.dems.cases;

import com.dems.enums.CaseStatus;
import com.dems.enums.CrimeSeverity;
import com.dems.enums.CrimeType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility specification builder for dynamic multi-criteria searching and filtering on Case records.
 */
public class CaseSpecification {

    private CaseSpecification() {
        throw new UnsupportedOperationException("Utility class should not be instantiated");
    }

    public static Specification<CaseEntity> filterCases(
            String caseNumber,
            String crimeNumber,
            String caseName,
            CaseStatus status,
            CrimeType crimeType,
            CrimeSeverity severity,
            Long assignedOfficerId,
            LocalDate incidentDate,
            Boolean active,
            String searchTerm
    ) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (caseNumber != null && !caseNumber.trim().isEmpty()) {
                String pattern = "%" + caseNumber.trim().toLowerCase() + "%";
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("caseNumber")), pattern));
            }

            if (crimeNumber != null && !crimeNumber.trim().isEmpty()) {
                String pattern = "%" + crimeNumber.trim().toLowerCase() + "%";
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("crimeNumber")), pattern));
            }

            if (caseName != null && !caseName.trim().isEmpty()) {
                String pattern = "%" + caseName.trim().toLowerCase() + "%";
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("caseName")), pattern));
            }

            if (status != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), status));
            }

            if (crimeType != null) {
                predicates.add(criteriaBuilder.equal(root.get("crimeType"), crimeType));
            }

            if (severity != null) {
                predicates.add(criteriaBuilder.equal(root.get("severity"), severity));
            }

            if (assignedOfficerId != null) {
                predicates.add(criteriaBuilder.equal(root.get("assignedOfficer").get("id"), assignedOfficerId));
            }

            if (incidentDate != null) {
                predicates.add(criteriaBuilder.equal(root.get("incidentDate"), incidentDate));
            }

            if (active != null) {
                predicates.add(criteriaBuilder.equal(root.get("active"), active));
            }

            if (searchTerm != null && !searchTerm.trim().isEmpty()) {
                String pattern = "%" + searchTerm.trim().toLowerCase() + "%";
                Predicate nameLike = criteriaBuilder.like(criteriaBuilder.lower(root.get("caseName")), pattern);
                Predicate caseNumLike = criteriaBuilder.like(criteriaBuilder.lower(root.get("caseNumber")), pattern);
                Predicate crimeNumLike = criteriaBuilder.like(criteriaBuilder.lower(root.get("crimeNumber")), pattern);
                Predicate locationLike = criteriaBuilder.like(criteriaBuilder.lower(root.get("crimeSceneLocation")), pattern);

                predicates.add(criteriaBuilder.or(nameLike, caseNumLike, crimeNumLike, locationLike));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}

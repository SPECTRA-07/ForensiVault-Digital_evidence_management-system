package com.dems.evidence;

import com.dems.enums.EvidenceStatus;
import com.dems.enums.EvidenceType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility specification builder for dynamic multi-criteria searching and filtering on Evidence records.
 */
public class EvidenceSpecification {

    private EvidenceSpecification() {
        throw new UnsupportedOperationException("Utility class should not be instantiated");
    }

    public static Specification<EvidenceEntity> filterEvidence(
            EvidenceStatus status,
            EvidenceType evidenceType,
            Long caseId,
            Long uploadedById,
            LocalDate uploadDate,
            String evidenceNumber,
            String evidenceName,
            String searchTerm,
            Boolean active
    ) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (status != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), status));
            }

            if (evidenceType != null) {
                predicates.add(criteriaBuilder.equal(root.get("evidenceType"), evidenceType));
            }

            if (caseId != null) {
                predicates.add(criteriaBuilder.equal(root.get("caseEntity").get("id"), caseId));
            }

            if (uploadedById != null) {
                predicates.add(criteriaBuilder.equal(root.get("uploadedBy").get("id"), uploadedById));
            }

            if (uploadDate != null) {
                LocalDateTime startOfDay = uploadDate.atStartOfDay();
                LocalDateTime endOfDay = uploadDate.atTime(LocalTime.MAX);
                predicates.add(criteriaBuilder.between(root.get("uploadedAt"), startOfDay, endOfDay));
            }

            if (evidenceNumber != null && !evidenceNumber.trim().isEmpty()) {
                String pattern = "%" + evidenceNumber.trim().toLowerCase() + "%";
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("evidenceNumber")), pattern));
            }

            if (evidenceName != null && !evidenceName.trim().isEmpty()) {
                String pattern = "%" + evidenceName.trim().toLowerCase() + "%";
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("evidenceName")), pattern));
            }

            if (active != null) {
                predicates.add(criteriaBuilder.equal(root.get("active"), active));
            }

            if (searchTerm != null && !searchTerm.trim().isEmpty()) {
                String pattern = "%" + searchTerm.trim().toLowerCase() + "%";
                Predicate nameLike = criteriaBuilder.like(criteriaBuilder.lower(root.get("evidenceName")), pattern);
                Predicate numberLike = criteriaBuilder.like(criteriaBuilder.lower(root.get("evidenceNumber")), pattern);
                Predicate origFileLike = criteriaBuilder.like(criteriaBuilder.lower(root.get("originalFileName")), pattern);
                Predicate displayLike = criteriaBuilder.like(criteriaBuilder.lower(root.get("displayName")), pattern);

                predicates.add(criteriaBuilder.or(nameLike, numberLike, origFileLike, displayLike));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}

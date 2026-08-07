package com.dems.custody;

import com.dems.enums.TransferPurpose;
import com.dems.enums.TransferStatus;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility specification builder for dynamic multi-criteria searching and filtering on Chain of Custody records.
 */
public class CustodyRecordSpecification {

    private CustodyRecordSpecification() {
        throw new UnsupportedOperationException("Utility class should not be instantiated");
    }

    public static Specification<CustodyRecordEntity> filterCustody(
            Long evidenceId,
            Long transferredById,
            Long transferredToId,
            TransferStatus transferStatus,
            TransferPurpose transferPurpose,
            OffsetDateTime startDate,
            OffsetDateTime endDate,
            Boolean active
    ) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (evidenceId != null) {
                predicates.add(criteriaBuilder.equal(root.get("evidence").get("id"), evidenceId));
            }

            if (transferredById != null) {
                predicates.add(criteriaBuilder.equal(root.get("transferredBy").get("id"), transferredById));
            }

            if (transferredToId != null) {
                predicates.add(criteriaBuilder.equal(root.get("transferredTo").get("id"), transferredToId));
            }

            if (transferStatus != null) {
                predicates.add(criteriaBuilder.equal(root.get("transferStatus"), transferStatus));
            }

            if (transferPurpose != null) {
                predicates.add(criteriaBuilder.equal(root.get("transferPurpose"), transferPurpose));
            }

            if (startDate != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("transferredAt"), startDate));
            }

            if (endDate != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("transferredAt"), endDate));
            }

            if (active != null) {
                predicates.add(criteriaBuilder.equal(root.get("active"), active));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}

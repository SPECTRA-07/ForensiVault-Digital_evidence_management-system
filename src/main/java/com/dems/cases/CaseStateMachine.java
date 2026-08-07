package com.dems.cases;

import com.dems.enums.CaseStatus;
import com.dems.exception.BadRequestException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * Dedicated component enforcing Case status transition rules and managing investigation lifecycle dates.
 */
@Slf4j
@Component
public class CaseStateMachine {

    public void validateAndHandleTransition(CaseEntity caseEntity, CaseStatus newStatus) {
        CaseStatus currentStatus = caseEntity.getStatus();

        if (currentStatus == newStatus) {
            return;
        }

        // Rule 1: Closed or Archived cases cannot return to OPEN
        if ((currentStatus == CaseStatus.CLOSED || currentStatus == CaseStatus.ARCHIVED) && newStatus == CaseStatus.OPEN) {
            log.warn("Invalid Status Transition Attempt: Case ID [{}] cannot transition from [{}] to OPEN",
                    caseEntity.getId(), currentStatus);
            throw new BadRequestException("Invalid status transition: A closed or archived case cannot be reopened to OPEN.");
        }

        // Rule 2: READY_FOR_COURT is only permitted after UNDER_INVESTIGATION or SUBMITTED_TO_FORENSICS
        if (newStatus == CaseStatus.READY_FOR_COURT &&
                (currentStatus != CaseStatus.UNDER_INVESTIGATION && currentStatus != CaseStatus.SUBMITTED_TO_FORENSICS)) {
            log.warn("Invalid Status Transition Attempt: Case ID [{}] cannot transition from [{}] to READY_FOR_COURT",
                    caseEntity.getId(), currentStatus);
            throw new BadRequestException("Invalid status transition: Case must be UNDER_INVESTIGATION or SUBMITTED_TO_FORENSICS before moving to READY_FOR_COURT.");
        }

        // Handle Investigation Start Date side-effect
        if (newStatus == CaseStatus.UNDER_INVESTIGATION && caseEntity.getInvestigationStartDate() == null) {
            caseEntity.setInvestigationStartDate(LocalDate.now());
            log.info("Auto-set investigationStartDate to [{}] for Case ID [{}]", LocalDate.now(), caseEntity.getId());
        }

        // Handle Investigation End Date side-effect
        if ((newStatus == CaseStatus.CLOSED || newStatus == CaseStatus.ARCHIVED) && caseEntity.getInvestigationEndDate() == null) {
            caseEntity.setInvestigationEndDate(LocalDate.now());
            log.info("Auto-set investigationEndDate to [{}] for Case ID [{}]", LocalDate.now(), caseEntity.getId());
        }

        caseEntity.setStatus(newStatus);
    }

    public void validateDates(LocalDate startDate, LocalDate endDate) {
        if (startDate != null && endDate != null && endDate.isBefore(startDate)) {
            throw new BadRequestException("Investigation end date (" + endDate + ") cannot be before start date (" + startDate + ").");
        }
    }
}

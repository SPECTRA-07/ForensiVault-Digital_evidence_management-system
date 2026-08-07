package com.dems.cases;

import com.dems.enums.CaseStatus;
import com.dems.exception.BadRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CaseStateMachineTest {

    private CaseStateMachine stateMachine;
    private CaseEntity caseEntity;

    @BeforeEach
    void setUp() {
        stateMachine = new CaseStateMachine();
        caseEntity = CaseEntity.builder()
                .id(1L)
                .caseNumber("CASE-001")
                .status(CaseStatus.OPEN)
                .build();
    }

    @Test
    void transitionToUnderInvestigation_AutoSetsStartDate() {
        stateMachine.validateAndHandleTransition(caseEntity, CaseStatus.UNDER_INVESTIGATION);

        assertEquals(CaseStatus.UNDER_INVESTIGATION, caseEntity.getStatus());
        assertNotNull(caseEntity.getInvestigationStartDate());
    }

    @Test
    void transitionToClosed_AutoSetsEndDate() {
        caseEntity.setStatus(CaseStatus.UNDER_INVESTIGATION);
        stateMachine.validateAndHandleTransition(caseEntity, CaseStatus.CLOSED);

        assertEquals(CaseStatus.CLOSED, caseEntity.getStatus());
        assertNotNull(caseEntity.getInvestigationEndDate());
    }

    @Test
    void transitionFromClosedToOpen_ThrowsBadRequestException() {
        caseEntity.setStatus(CaseStatus.CLOSED);

        assertThrows(BadRequestException.class, () ->
                stateMachine.validateAndHandleTransition(caseEntity, CaseStatus.OPEN));
    }

    @Test
    void transitionFromArchivedToOpen_ThrowsBadRequestException() {
        caseEntity.setStatus(CaseStatus.ARCHIVED);

        assertThrows(BadRequestException.class, () ->
                stateMachine.validateAndHandleTransition(caseEntity, CaseStatus.OPEN));
    }

    @Test
    void transitionToReadyForCourt_FromOpen_ThrowsBadRequestException() {
        caseEntity.setStatus(CaseStatus.OPEN);

        assertThrows(BadRequestException.class, () ->
                stateMachine.validateAndHandleTransition(caseEntity, CaseStatus.READY_FOR_COURT));
    }

    @Test
    void transitionToReadyForCourt_FromUnderInvestigation_Success() {
        caseEntity.setStatus(CaseStatus.UNDER_INVESTIGATION);

        stateMachine.validateAndHandleTransition(caseEntity, CaseStatus.READY_FOR_COURT);

        assertEquals(CaseStatus.READY_FOR_COURT, caseEntity.getStatus());
    }

    @Test
    void validateDates_EndDateBeforeStartDate_ThrowsBadRequestException() {
        LocalDate start = LocalDate.now();
        LocalDate end = start.minusDays(5);

        assertThrows(BadRequestException.class, () -> stateMachine.validateDates(start, end));
    }
}

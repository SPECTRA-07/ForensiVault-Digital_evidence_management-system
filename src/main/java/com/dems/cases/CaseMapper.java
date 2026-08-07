package com.dems.cases;

import com.dems.user.UserMapper;
import org.springframework.stereotype.Component;

/**
 * Mapper component converting Case entities to response DTOs.
 */
@Component
public class CaseMapper {

    private final UserMapper userMapper;

    public CaseMapper(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    public CaseResponse toResponse(CaseEntity entity) {
        if (entity == null) {
            return null;
        }
        return CaseResponse.builder()
                .id(entity.getId())
                .caseNumber(entity.getCaseNumber())
                .crimeNumber(entity.getCrimeNumber())
                .caseName(entity.getCaseName())
                .caseSummary(entity.getCaseSummary())
                .crimeType(entity.getCrimeType())
                .severity(entity.getSeverity())
                .status(entity.getStatus())
                .incidentDate(entity.getIncidentDate())
                .crimeSceneLocation(entity.getCrimeSceneLocation())
                .investigationStartDate(entity.getInvestigationStartDate())
                .investigationEndDate(entity.getInvestigationEndDate())
                .assignedOfficer(userMapper.toResponse(entity.getAssignedOfficer()))
                .active(entity.getActive())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public CaseSummaryResponse toSummaryResponse(CaseEntity entity) {
        if (entity == null) {
            return null;
        }
        String officerName = entity.getAssignedOfficer() != null ? entity.getAssignedOfficer().getFullName() : "Unassigned";
        Long officerId = entity.getAssignedOfficer() != null ? entity.getAssignedOfficer().getId() : null;

        return CaseSummaryResponse.builder()
                .id(entity.getId())
                .caseNumber(entity.getCaseNumber())
                .crimeNumber(entity.getCrimeNumber())
                .caseName(entity.getCaseName())
                .crimeType(entity.getCrimeType())
                .severity(entity.getSeverity())
                .status(entity.getStatus())
                .incidentDate(entity.getIncidentDate())
                .crimeSceneLocation(entity.getCrimeSceneLocation())
                .assignedOfficerName(officerName)
                .assignedOfficerId(officerId)
                .active(entity.getActive())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}

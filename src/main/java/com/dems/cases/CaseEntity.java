package com.dems.cases;

import com.dems.entity.BaseEntity;
import com.dems.enums.CaseStatus;
import com.dems.enums.CrimeSeverity;
import com.dems.enums.CrimeType;
import com.dems.user.UserEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Case Entity representing criminal investigation records in DEMS.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "cases")
public class CaseEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "case_number", unique = true, nullable = false)
    private String caseNumber;

    @Column(name = "crime_number", unique = true, nullable = false)
    private String crimeNumber;

    @Column(name = "case_name", nullable = false)
    private String caseName;

    @Column(name = "case_summary", columnDefinition = "TEXT")
    private String caseSummary;

    @Enumerated(EnumType.STRING)
    @Column(name = "crime_type", nullable = false)
    private CrimeType crimeType;

    @Enumerated(EnumType.STRING)
    @Column(name = "severity", nullable = false)
    private CrimeSeverity severity;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private CaseStatus status;

    @Column(name = "incident_date", nullable = false)
    private LocalDate incidentDate;

    @Column(name = "crime_scene_location", nullable = false)
    private String crimeSceneLocation;

    @Column(name = "investigation_start_date")
    private LocalDate investigationStartDate;

    @Column(name = "investigation_end_date")
    private LocalDate investigationEndDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_officer_id")
    private UserEntity assignedOfficer;

    @Builder.Default
    @Column(name = "active", nullable = false)
    private Boolean active = true;
}

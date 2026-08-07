package com.dems.custody;

import com.dems.enums.TransferPurpose;
import com.dems.enums.TransferStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.OffsetDateTime;

/**
 * Service interface managing evidence custody transfers, handshakes, immutable audit records, and timeline generation.
 */
public interface CustodyService {

    CustodyResponse initiateTransfer(CustodyTransferRequest request, String initiatorEmail);

    CustodyResponse acceptOrRejectTransfer(Long custodyId, CustodyAcceptRequest request, String recipientEmail);

    CustodyResponse getCustodyById(Long id);

    Page<CustodyResponse> getCustodyByEvidenceId(Long evidenceId, Pageable pageable);

    CustodyTimelineResponse getCustodyTimeline(Long evidenceId);

    Page<CustodyResponse> searchCustodyRecords(
            Long evidenceId,
            Long transferredById,
            Long transferredToId,
            TransferStatus transferStatus,
            TransferPurpose transferPurpose,
            OffsetDateTime startDate,
            OffsetDateTime endDate,
            Boolean active,
            Pageable pageable
    );
}

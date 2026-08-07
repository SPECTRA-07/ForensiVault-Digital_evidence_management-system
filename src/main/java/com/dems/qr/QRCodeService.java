package com.dems.qr;

import com.dems.evidence.EvidenceEntity;
import org.springframework.core.io.Resource;

/**
 * Service interface for generating 250x250 PNG QR codes, managing local disk storage under uploads/qr/,
 * generating safe resolution metadata, and auditing actions.
 */
public interface QRCodeService {

    QRCodeResponse generateQRCode(EvidenceEntity evidence);

    QRCodeResponse regenerateQRCode(Long evidenceId);

    QRCodeResponse getQRCodeInfo(Long evidenceId);

    Resource getQRCodeImageResource(Long evidenceId);

    QRResolveResponse resolveQRCode(String evidenceNumber);
}

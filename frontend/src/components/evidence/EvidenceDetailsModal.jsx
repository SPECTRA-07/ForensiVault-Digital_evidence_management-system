import React, { useState, useEffect } from 'react';
import { FileText, Download, ShieldCheck, GitCommit, ArrowRightLeft, Copy, Check, QrCode, RefreshCw, Eye } from 'lucide-react';
import Modal from '../Modal';
import Button from '../Button';
import Badge from '../Badge';
import LoadingSpinner from '../LoadingSpinner';
import QRCodeViewer from '../qr/QRCodeViewer';
import QRRegenerateModal from '../qr/QRRegenerateModal';
import evidenceService from '../../services/evidenceService';
import { useAuth } from '../../hooks/useAuth';

export const EvidenceDetailsModal = ({
  isOpen,
  onClose,
  evidence,
  onVerifyIntegrity,
  onInitiateTransfer,
  onViewTimeline,
}) => {
  const { user } = useAuth();
  const isAdmin = user?.role === 'ADMIN';

  const [detailedData, setDetailedData] = useState(null);
  const [loading, setLoading] = useState(false);
  const [copiedHash, setCopiedHash] = useState(false);
  const [downloading, setDownloading] = useState(false);

  // QR Modals state
  const [qrViewerOpen, setQrViewerOpen] = useState(false);
  const [qrRegenerateOpen, setQrRegenerateOpen] = useState(false);

  useEffect(() => {
    if (isOpen && evidence?.id) {
      const fetchFullDetails = async () => {
        setLoading(true);
        try {
          const res = await evidenceService.getEvidenceById(evidence.id);
          if (res && res.data) {
            setDetailedData(res.data);
          } else {
            setDetailedData(evidence);
          }
        } catch (e) {
          setDetailedData(evidence);
        } finally {
          setLoading(false);
        }
      };
      fetchFullDetails();
    } else {
      setDetailedData(evidence);
    }
  }, [isOpen, evidence]);

  if (!isOpen || !evidence) return null;

  const data = detailedData || evidence;

  const handleDownload = async () => {
    setDownloading(true);
    try {
      const response = await evidenceService.downloadEvidence(data.id);
      const url = window.URL.createObjectURL(new Blob([response]));
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', data.originalFileName || `evidence-${data.id}`);
      document.body.appendChild(link);
      link.click();
      link.remove();
    } catch (e) {
      console.warn('File download failed:', e);
    } finally {
      setDownloading(false);
    }
  };

  const copyHash = (hashText) => {
    if (navigator.clipboard && hashText) {
      navigator.clipboard.writeText(hashText);
      setCopiedHash(true);
      setTimeout(() => setCopiedHash(false), 2000);
    }
  };

  const formatBytes = (bytes) => {
    if (!bytes) return '--';
    if (bytes < 1024) return bytes + ' B';
    if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB';
    return (bytes / (1024 * 1024)).toFixed(2) + ' MB';
  };

  return (
    <>
      <Modal isOpen={isOpen} onClose={onClose} title="Digital Evidence Comprehensive Metadata Record" maxWidth="760px">
        {loading ? (
          <LoadingSpinner message="Retrieving complete evidence metadata record..." />
        ) : (
          <div style={{ display: 'flex', flexDirection: 'column', gap: '1.25rem' }}>
            {/* Header Summary Banner */}
            <div style={{ backgroundColor: 'var(--color-slate-50)', padding: '1rem', borderRadius: 'var(--border-radius)', border: '1px solid var(--color-slate-200)' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', flexWrap: 'wrap', gap: '0.5rem' }}>
                <div>
                  <div className="font-mono" style={{ fontWeight: 800, fontSize: '1.15rem', color: 'var(--color-navy-900)' }}>
                    {data.evidenceNumber || `#EV-${data.id}`}
                  </div>
                  <h3 style={{ margin: '0.2rem 0 0 0', fontSize: '1rem' }}>{data.evidenceName}</h3>
                  {data.displayName && data.displayName !== data.evidenceName && (
                    <span style={{ fontSize: '0.75rem', color: 'var(--color-slate-500)' }}>Display Label: {data.displayName}</span>
                  )}
                </div>
                <div style={{ display: 'flex', gap: '0.5rem' }}>
                  <Badge status={data.evidenceType}>{data.evidenceType}</Badge>
                  <Badge status={data.status}>{data.status}</Badge>
                </div>
              </div>
            </div>

            {/* SECTION 1: Evidence Metadata Information */}
            <div className="card" style={{ padding: '1rem' }}>
              <h4 style={{ fontSize: '0.8125rem', color: 'var(--color-slate-500)', textTransform: 'uppercase', letterSpacing: '0.05em', marginBottom: '0.75rem' }}>
                Case & Investigation Metadata
              </h4>
              <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(210px, 1fr))', gap: '0.875rem', fontSize: '0.8125rem' }}>
                <div>
                  <span style={{ color: 'var(--color-slate-500)', fontSize: '0.75rem' }}>Case Number:</span>
                  <div className="font-mono" style={{ fontWeight: 600 }}>{data.caseNumber || '--'}</div>
                </div>
                <div>
                  <span style={{ color: 'var(--color-slate-500)', fontSize: '0.75rem' }}>Case Title:</span>
                  <div style={{ fontWeight: 500 }}>{data.caseName || '--'}</div>
                </div>
                <div>
                  <span style={{ color: 'var(--color-slate-500)', fontSize: '0.75rem' }}>Evidence Type:</span>
                  <div><Badge status="INFO">{data.evidenceType || '--'}</Badge></div>
                </div>
                <div>
                  <span style={{ color: 'var(--color-slate-500)', fontSize: '0.75rem' }}>Evidence Status:</span>
                  <div><Badge status={data.status}>{data.status || '--'}</Badge></div>
                </div>
                <div>
                  <span style={{ color: 'var(--color-slate-500)', fontSize: '0.75rem' }}>Collected From Location:</span>
                  <div style={{ fontWeight: 500 }}>{data.collectedFrom || '--'}</div>
                </div>
                <div>
                  <span style={{ color: 'var(--color-slate-500)', fontSize: '0.75rem' }}>Collection Method:</span>
                  <div>{data.collectionMethod || '--'}</div>
                </div>
                <div>
                  <span style={{ color: 'var(--color-slate-500)', fontSize: '0.75rem' }}>Collected Timestamp:</span>
                  <div className="font-mono">{data.collectedAt ? new Date(data.collectedAt).toLocaleString() : '--'}</div>
                </div>
                <div>
                  <span style={{ color: 'var(--color-slate-500)', fontSize: '0.75rem' }}>Collector / Officer:</span>
                  <div>{data.collectedBy || data.uploadedBy?.fullName || data.uploadedByName || '--'}</div>
                </div>
              </div>

              {(data.description || data.remarks) && (
                <div style={{ marginTop: '0.875rem', paddingTop: '0.75rem', borderTop: '1px solid var(--color-slate-100)', fontSize: '0.8125rem' }}>
                  <span style={{ color: 'var(--color-slate-500)', fontSize: '0.75rem', display: 'block', fontWeight: 600 }}>Investigation Description / Remarks:</span>
                  <div style={{ marginTop: '0.25rem', color: 'var(--color-navy-900)', backgroundColor: 'var(--color-slate-50)', padding: '0.5rem 0.75rem', borderRadius: '4px' }}>
                    {data.description || data.remarks}
                  </div>
                </div>
              )}
            </div>

            {/* SECTION 2: File Payload Information */}
            <div className="card" style={{ padding: '1rem' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '0.75rem' }}>
                <h4 style={{ fontSize: '0.8125rem', color: 'var(--color-slate-500)', textTransform: 'uppercase', letterSpacing: '0.05em', margin: 0 }}>
                  File Payload & Storage Attributes
                </h4>
                <Button variant="secondary" size="sm" icon={Download} onClick={handleDownload} disabled={downloading}>
                  {downloading ? 'Downloading...' : 'Download Payload'}
                </Button>
              </div>
              <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(210px, 1fr))', gap: '0.875rem', fontSize: '0.8125rem' }}>
                <div>
                  <span style={{ color: 'var(--color-slate-500)', fontSize: '0.75rem' }}>Original File Name:</span>
                  <div style={{ fontWeight: 600 }}>{data.originalFileName || '--'}</div>
                </div>
                <div>
                  <span style={{ color: 'var(--color-slate-500)', fontSize: '0.75rem' }}>File Size:</span>
                  <div className="font-mono">{data.fileSizeFormatted || formatBytes(data.fileSize)}</div>
                </div>
                <div>
                  <span style={{ color: 'var(--color-slate-500)', fontSize: '0.75rem' }}>MIME Content-Type:</span>
                  <div className="font-mono" style={{ fontSize: '0.75rem' }}>{data.mimeType || '--'}</div>
                </div>
                <div>
                  <span style={{ color: 'var(--color-slate-500)', fontSize: '0.75rem' }}>File Extension:</span>
                  <div className="font-mono">{data.fileExtension || '--'}</div>
                </div>
                <div>
                  <span style={{ color: 'var(--color-slate-500)', fontSize: '0.75rem' }}>Uploaded Timestamp:</span>
                  <div className="font-mono">{data.uploadedAt ? new Date(data.uploadedAt).toLocaleString() : '--'}</div>
                </div>
                <div>
                  <span style={{ color: 'var(--color-slate-500)', fontSize: '0.75rem' }}>Uploaded By User:</span>
                  <div>{data.uploadedBy?.fullName || data.uploadedBy?.email || data.uploadedByName || '--'}</div>
                </div>
              </div>
            </div>

            {/* SECTION 3: Cryptographic Integrity */}
            <div className="card" style={{ padding: '1rem' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '0.75rem' }}>
                <h4 style={{ fontSize: '0.8125rem', color: 'var(--color-slate-500)', textTransform: 'uppercase', letterSpacing: '0.05em', margin: 0 }}>
                  Cryptographic Integrity & Baseline Hash
                </h4>
                {onVerifyIntegrity && (
                  <Button variant="primary" size="sm" icon={ShieldCheck} onClick={() => onVerifyIntegrity(data)}>
                    Verify Integrity
                  </Button>
                )}
              </div>

              <div style={{ fontSize: '0.8125rem' }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '1rem', marginBottom: '0.5rem' }}>
                  <div>
                    <span style={{ color: 'var(--color-slate-500)', fontSize: '0.75rem' }}>Algorithm: </span>
                    <strong className="font-mono">{data.hashAlgorithm || 'SHA-256'}</strong>
                  </div>
                  <div>
                    <span style={{ color: 'var(--color-slate-500)', fontSize: '0.75rem' }}>Integrity Status: </span>
                    <Badge status={data.status}>{data.status || 'REGISTERED'}</Badge>
                  </div>
                </div>

                {data.fileHash ? (
                  <div style={{ backgroundColor: 'var(--color-slate-50)', padding: '0.625rem 0.75rem', borderRadius: 'var(--border-radius)', border: '1px solid var(--color-slate-200)' }}>
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '0.25rem' }}>
                      <span style={{ fontSize: '0.7rem', color: 'var(--color-slate-500)', fontWeight: 600 }}>STORED BASELINE SHA-256 HASH:</span>
                      <button
                        type="button"
                        onClick={() => copyHash(data.fileHash)}
                        style={{ background: 'none', border: 'none', cursor: 'pointer', display: 'flex', alignItems: 'center', gap: '0.25rem', fontSize: '0.7rem', color: 'var(--color-primary-600)' }}
                      >
                        {copiedHash ? <Check size={12} color="green" /> : <Copy size={12} />}
                        <span>{copiedHash ? 'Copied' : 'Copy Hash'}</span>
                      </button>
                    </div>
                    <div className="font-mono" style={{ fontSize: '0.75rem', wordBreak: 'break-all', color: 'var(--color-navy-900)' }}>
                      {data.fileHash}
                    </div>
                  </div>
                ) : (
                  <span style={{ color: 'var(--color-warning-700)', fontStyle: 'italic' }}>No baseline hash recorded.</span>
                )}
              </div>
            </div>

            {/* SECTION 4: Chain of Custody Information */}
            <div className="card" style={{ padding: '1rem' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '0.75rem' }}>
                <h4 style={{ fontSize: '0.8125rem', color: 'var(--color-slate-500)', textTransform: 'uppercase', letterSpacing: '0.05em', margin: 0 }}>
                  Chain of Custody Legal Status
                </h4>
                <div style={{ display: 'flex', gap: '0.5rem' }}>
                  {onViewTimeline && (
                    <Button variant="outline" size="sm" icon={GitCommit} onClick={() => onViewTimeline(data)}>
                      View Timeline
                    </Button>
                  )}
                  {onInitiateTransfer && (
                    <Button variant="secondary" size="sm" icon={ArrowRightLeft} onClick={() => onInitiateTransfer(data)}>
                      Transfer Evidence
                    </Button>
                  )}
                </div>
              </div>

              <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '0.875rem', fontSize: '0.8125rem' }}>
                <div>
                  <span style={{ color: 'var(--color-slate-500)', fontSize: '0.75rem' }}>Current Custodian Name:</span>
                  <div style={{ fontWeight: 600, color: 'var(--color-primary-700)' }}>
                    {data.currentCustodian?.fullName || data.currentCustodian?.email || data.currentCustodianName || 'Vault Storage'}
                  </div>
                </div>
                <div>
                  <span style={{ color: 'var(--color-slate-500)', fontSize: '0.75rem' }}>Custodian Employee ID:</span>
                  <div className="font-mono" style={{ fontWeight: 600 }}>
                    {data.currentCustodian?.employeeId || data.currentCustodianId || '--'}
                  </div>
                </div>
                <div>
                  <span style={{ color: 'var(--color-slate-500)', fontSize: '0.75rem' }}>Last Transferred Timestamp:</span>
                  <div className="font-mono">
                    {data.lastTransferredAt ? new Date(data.lastTransferredAt).toLocaleString() : 'Original Registration'}
                  </div>
                </div>
                <div>
                  <span style={{ color: 'var(--color-slate-500)', fontSize: '0.75rem' }}>Latest Workflow Status:</span>
                  <div><Badge status={data.status}>{data.status}</Badge></div>
                </div>
              </div>
            </div>

            {/* SECTION 5: Physical Barcode & QR Tracking */}
            <div className="card" style={{ padding: '1rem' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '0.75rem' }}>
                <h4 style={{ fontSize: '0.8125rem', color: 'var(--color-slate-500)', textTransform: 'uppercase', letterSpacing: '0.05em', margin: 0 }}>
                  Physical Barcode & QR Tracking
                </h4>
                <div style={{ display: 'flex', gap: '0.5rem' }}>
                  <Button variant="outline" size="sm" icon={QrCode} onClick={() => setQrViewerOpen(true)}>
                    View QR Barcode
                  </Button>
                  {isAdmin && (
                    <Button variant="secondary" size="sm" icon={RefreshCw} onClick={() => setQrRegenerateOpen(true)}>
                      Regenerate QR
                    </Button>
                  )}
                </div>
              </div>

              <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '0.875rem', fontSize: '0.8125rem' }}>
                <div>
                  <span style={{ color: 'var(--color-slate-500)', fontSize: '0.75rem' }}>QR Code Status:</span>
                  <div><Badge status="SECURE">GENERATED & VERIFIED</Badge></div>
                </div>
                <div>
                  <span style={{ color: 'var(--color-slate-500)', fontSize: '0.75rem' }}>Evidence Tag Number:</span>
                  <div className="font-mono" style={{ fontWeight: 600 }}>{data.evidenceNumber || `#EV-${data.id}`}</div>
                </div>
                <div>
                  <span style={{ color: 'var(--color-slate-500)', fontSize: '0.75rem' }}>Tag Image Format:</span>
                  <div className="font-mono">250x250 PNG</div>
                </div>
                <div>
                  <span style={{ color: 'var(--color-slate-500)', fontSize: '0.75rem' }}>Uploaded / Registered:</span>
                  <div className="font-mono">{data.uploadedAt ? new Date(data.uploadedAt).toLocaleString() : '--'}</div>
                </div>
              </div>
            </div>
          </div>
        )}
      </Modal>

      {/* QR Modals */}
      {data && (
        <QRCodeViewer
          isOpen={qrViewerOpen}
          onClose={() => setQrViewerOpen(false)}
          evidenceId={data.id}
          evidenceNumber={data.evidenceNumber}
        />
      )}

      {data && (
        <QRRegenerateModal
          isOpen={qrRegenerateOpen}
          onClose={() => setQrRegenerateOpen(false)}
          evidenceId={data.id}
          evidenceNumber={data.evidenceNumber}
          onSuccess={() => {
            // Re-fetch evidence details if needed
          }}
        />
      )}
    </>
  );
};

export default EvidenceDetailsModal;

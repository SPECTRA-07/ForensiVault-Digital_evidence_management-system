import React, { useState } from 'react';
import { QrCode, Search, Download, RefreshCw, ShieldCheck, Eye, FileCheck2, AlertCircle, Hash, Tag } from 'lucide-react';
import PageHeader from '../components/PageHeader';
import Button from '../components/Button';
import Input from '../components/Input';
import Badge from '../components/Badge';
import LoadingSpinner from '../components/LoadingSpinner';
import ErrorMessage from '../components/ErrorMessage';
import QRCodeViewer from '../components/qr/QRCodeViewer';
import QRResolveModal from '../components/qr/QRResolveModal';
import QRRegenerateModal from '../components/qr/QRRegenerateModal';
import qrService from '../services/qrService';
import { useAuth } from '../hooks/useAuth';

export const QrPage = () => {
  const { user } = useAuth();
  const isAdmin = user?.role === 'ADMIN';

  // Lookup Mode: 'EVIDENCE_ID' vs 'EVIDENCE_NUMBER'
  const [lookupMode, setLookupMode] = useState('EVIDENCE_ID');

  // State for Inspector
  const [evidenceInput, setEvidenceInput] = useState('');
  const [qrData, setQrData] = useState(null);
  const [qrImageUrl, setQrImageUrl] = useState(null);
  const [loadingQr, setLoadingQr] = useState(false);
  const [qrError, setQrError] = useState(null);

  // State for Resolver
  const [resolveNumberInput, setResolveNumberInput] = useState('');
  const [resolveResult, setResolveResult] = useState(null);
  const [loadingResolve, setLoadingResolve] = useState(false);
  const [resolveError, setResolveError] = useState(null);

  // Modals state
  const [regenerateOpen, setRegenerateOpen] = useState(false);

  const handleFetchQR = async (e) => {
    if (e) e.preventDefault();
    const inputVal = evidenceInput.trim();
    if (!inputVal) return;

    setLoadingQr(true);
    setQrError(null);
    setQrData(null);
    if (qrImageUrl) {
      URL.revokeObjectURL(qrImageUrl);
      setQrImageUrl(null);
    }

    let targetId = inputVal;

    try {
      if (lookupMode === 'EVIDENCE_NUMBER') {
        // Mode 2: Resolve Evidence Number string first to obtain numeric evidenceId
        const resolveRes = await qrService.resolveQRCode(inputVal);
        const resolvedData = resolveRes?.data || resolveRes;
        const resolvedId = resolvedData?.evidenceId;
        if (resolvedId) {
          targetId = resolvedId;
        } else {
          throw new Error(`Evidence record not found with number: ${inputVal}`);
        }
      }

      // Mode 1 or resolved numeric ID: GET /qr/evidence/{evidenceId}
      const infoRes = await qrService.getQRCodeInfo(targetId);
      if (infoRes) {
        const metadata = infoRes.data || infoRes;
        setQrData(metadata);
      }

      // GET /qr/evidence/{evidenceId}/image (produces image/png blob)
      const imageBlob = await qrService.getQRCodeImage(targetId);
      if (imageBlob && imageBlob instanceof Blob) {
        const url = URL.createObjectURL(imageBlob);
        setQrImageUrl(url);
      } else {
        throw new Error('Failed to retrieve binary QR barcode image payload.');
      }
    } catch (err) {
      const msg = typeof err === 'string' ? err : err.message || err.error || 'Failed to load QR barcode payload.';
      setQrError(msg);
    } finally {
      setLoadingQr(false);
    }
  };

  const handleResolveTag = async (e) => {
    if (e) e.preventDefault();
    if (!resolveNumberInput.trim()) return;

    setLoadingResolve(true);
    setResolveError(null);
    setResolveResult(null);

    try {
      const response = await qrService.resolveQRCode(resolveNumberInput.trim());
      if (response) {
        const resolvedData = response.data || response;
        setResolveResult(resolvedData);
      }
    } catch (err) {
      const msg = typeof err === 'string' ? err : err.message || err.error || 'Physical barcode tag resolution failed.';
      setResolveError(msg);
    } finally {
      setLoadingResolve(false);
    }
  };

  const handleDownloadImage = () => {
    if (!qrImageUrl) return;
    const link = document.createElement('a');
    link.href = qrImageUrl;
    link.download = qrData?.qrFileName || `QR-evidence-${qrData?.evidenceId || 'barcode'}.png`;
    document.body.appendChild(link);
    link.click();
    link.remove();
  };

  return (
    <div>
      <PageHeader
        title="Physical Evidence Barcode & QR Tracking"
        subtitle="Inspect, stream 250x250 PNG tags, scan, and resolve physical evidence barcodes."
      />

      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(360px, 1fr))', gap: '1.5rem' }}>
        {/* SECTION 1: QR Image Preview & Barcode Metadata */}
        <div className="card" style={{ padding: '1.25rem' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1rem' }}>
            <h3 style={{ margin: 0, fontSize: '1rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
              <QrCode size={18} color="var(--color-primary-600)" />
              <span>Physical Barcode Image Inspection</span>
            </h3>

            {/* Mode Switcher Buttons */}
            <div style={{ display: 'flex', backgroundColor: 'var(--color-slate-100)', padding: '0.2rem', borderRadius: 'var(--border-radius)', gap: '0.25rem' }}>
              <button
                type="button"
                onClick={() => {
                  setLookupMode('EVIDENCE_ID');
                  setEvidenceInput('');
                  setQrData(null);
                  setQrError(null);
                  if (qrImageUrl) {
                    URL.revokeObjectURL(qrImageUrl);
                    setQrImageUrl(null);
                  }
                }}
                style={{
                  border: 'none',
                  backgroundColor: lookupMode === 'EVIDENCE_ID' ? 'var(--color-white)' : 'transparent',
                  color: lookupMode === 'EVIDENCE_ID' ? 'var(--color-primary-700)' : 'var(--color-slate-600)',
                  fontWeight: lookupMode === 'EVIDENCE_ID' ? 600 : 400,
                  fontSize: '0.75rem',
                  padding: '0.3rem 0.6rem',
                  borderRadius: '4px',
                  cursor: 'pointer',
                  boxShadow: lookupMode === 'EVIDENCE_ID' ? 'var(--shadow-sm)' : 'none',
                }}
              >
                Evidence ID Mode
              </button>
              <button
                type="button"
                onClick={() => {
                  setLookupMode('EVIDENCE_NUMBER');
                  setEvidenceInput('');
                  setQrData(null);
                  setQrError(null);
                  if (qrImageUrl) {
                    URL.revokeObjectURL(qrImageUrl);
                    setQrImageUrl(null);
                  }
                }}
                style={{
                  border: 'none',
                  backgroundColor: lookupMode === 'EVIDENCE_NUMBER' ? 'var(--color-white)' : 'transparent',
                  color: lookupMode === 'EVIDENCE_NUMBER' ? 'var(--color-primary-700)' : 'var(--color-slate-600)',
                  fontWeight: lookupMode === 'EVIDENCE_NUMBER' ? 600 : 400,
                  fontSize: '0.75rem',
                  padding: '0.3rem 0.6rem',
                  borderRadius: '4px',
                  cursor: 'pointer',
                  boxShadow: lookupMode === 'EVIDENCE_NUMBER' ? 'var(--shadow-sm)' : 'none',
                }}
              >
                Evidence Number Mode
              </button>
            </div>
          </div>

          <form onSubmit={handleFetchQR} style={{ marginBottom: '1.25rem' }}>
            <div style={{ display: 'flex', gap: '0.5rem' }}>
              <div style={{ flex: 1 }}>
                <Input
                  label={lookupMode === 'EVIDENCE_ID' ? 'Numeric Evidence ID' : 'Formatted Evidence Number'}
                  placeholder={lookupMode === 'EVIDENCE_ID' ? 'Enter numeric Evidence ID (e.g. 1)...' : 'Enter Evidence Number (e.g. EVD-1786039549120-92ED)...'}
                  value={evidenceInput}
                  onChange={(e) => setEvidenceInput(e.target.value)}
                  icon={lookupMode === 'EVIDENCE_ID' ? Hash : Tag}
                  required
                />
              </div>
              <div style={{ alignSelf: 'flex-end', marginBottom: '1rem' }}>
                <Button type="submit" variant="primary" disabled={loadingQr}>
                  Load Barcode
                </Button>
              </div>
            </div>
          </form>

          {qrError && (
            <div style={{ backgroundColor: 'var(--color-danger-50)', color: 'var(--color-danger-700)', padding: '0.75rem 1rem', borderRadius: '4px', marginBottom: '1rem', fontSize: '0.8125rem', borderLeft: '4px solid var(--color-danger-600)' }}>
              <div style={{ fontWeight: 600, display: 'flex', alignItems: 'center', gap: '0.35rem' }}>
                <AlertCircle size={16} />
                <span>Barcode Lookup Error</span>
              </div>
              <div style={{ marginTop: '0.2rem' }}>{qrError}</div>
            </div>
          )}

          {loadingQr ? (
            <LoadingSpinner message="Querying physical barcode payload from backend..." />
          ) : qrData && qrImageUrl ? (
            <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: '1rem' }}>
              <div style={{ padding: '1rem', backgroundColor: '#ffffff', border: '2px solid var(--color-slate-200)', borderRadius: '8px' }}>
                <img
                  src={qrImageUrl}
                  alt={`QR Barcode for ${qrData.evidenceNumber}`}
                  style={{ width: '200px', height: '200px', display: 'block' }}
                />
              </div>

              <div style={{ width: '100%', fontSize: '0.8125rem', backgroundColor: 'var(--color-slate-50)', padding: '0.875rem', borderRadius: 'var(--border-radius)', border: '1px solid var(--color-slate-200)' }}>
                <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '0.5rem' }}>
                  <div>
                    <span style={{ color: 'var(--color-slate-500)', fontSize: '0.75rem' }}>Evidence Number:</span>
                    <div className="font-mono" style={{ fontWeight: 700 }}>{qrData.evidenceNumber}</div>
                  </div>
                  <div>
                    <span style={{ color: 'var(--color-slate-500)', fontSize: '0.75rem' }}>QR Filename:</span>
                    <div className="font-mono" style={{ fontSize: '0.75rem' }}>{qrData.qrFileName}</div>
                  </div>
                  <div>
                    <span style={{ color: 'var(--color-slate-500)', fontSize: '0.75rem' }}>Generated At:</span>
                    <div className="font-mono" style={{ fontSize: '0.75rem' }}>
                      {qrData.generatedAt ? new Date(qrData.generatedAt).toLocaleString() : '--'}
                    </div>
                  </div>
                  <div>
                    <span style={{ color: 'var(--color-slate-500)', fontSize: '0.75rem' }}>Tag Format:</span>
                    <div><Badge status="SECURE">250x250 PNG</Badge></div>
                  </div>
                </div>
              </div>

              <div style={{ display: 'flex', gap: '0.5rem', width: '100%', justifyContent: 'flex-end' }}>
                <Button variant="outline" size="sm" icon={Download} onClick={handleDownloadImage}>
                  Download PNG
                </Button>
                {isAdmin && (
                  <Button variant="secondary" size="sm" icon={RefreshCw} onClick={() => setRegenerateOpen(true)}>
                    Regenerate QR
                  </Button>
                )}
              </div>
            </div>
          ) : (
            <div style={{ textAlign: 'center', padding: '2rem 1rem', color: 'var(--color-slate-400)', fontSize: '0.875rem', backgroundColor: 'var(--color-slate-50)', borderRadius: 'var(--border-radius)' }}>
              Select lookup mode ({lookupMode === 'EVIDENCE_ID' ? 'Evidence ID' : 'Evidence Number'}) and submit to stream the 250x250 PNG barcode tag.
            </div>
          )}
        </div>

        {/* SECTION 2: Physical Barcode Resolver */}
        <div className="card" style={{ padding: '1.25rem' }}>
          <h3 style={{ margin: '0 0 1rem 0', fontSize: '1rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
            <ShieldCheck size={18} color="var(--color-success-600)" />
            <span>Physical Barcode Tag Resolver Scanner</span>
          </h3>

          <form onSubmit={handleResolveTag} style={{ marginBottom: '1.25rem' }}>
            <div style={{ display: 'flex', gap: '0.5rem' }}>
              <div style={{ flex: 1 }}>
                <Input
                  label="Scanned Barcode Tag Number"
                  placeholder="Scan or enter Evidence Number (e.g. EVD-1786039549120-92ED)..."
                  value={resolveNumberInput}
                  onChange={(e) => setResolveNumberInput(e.target.value)}
                  icon={Search}
                  required
                />
              </div>
              <div style={{ alignSelf: 'flex-end', marginBottom: '1rem' }}>
                <Button type="submit" variant="primary" disabled={loadingResolve}>
                  Resolve Tag
                </Button>
              </div>
            </div>
          </form>

          {resolveError && (
            <div style={{ backgroundColor: 'var(--color-danger-50)', color: 'var(--color-danger-700)', padding: '0.75rem 1rem', borderRadius: '4px', marginBottom: '1rem', fontSize: '0.8125rem', borderLeft: '4px solid var(--color-danger-600)' }}>
              <div style={{ fontWeight: 600, display: 'flex', alignItems: 'center', gap: '0.35rem' }}>
                <AlertCircle size={16} />
                <span>Tag Resolution Error</span>
              </div>
              <div style={{ marginTop: '0.2rem' }}>{resolveError}</div>
            </div>
          )}

          {loadingResolve ? (
            <LoadingSpinner message="Resolving physical barcode payload..." />
          ) : resolveResult ? (
            <div className="card" style={{ borderLeft: '4px solid var(--color-success-600)', padding: '1rem', backgroundColor: 'var(--color-slate-50)' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '0.75rem' }}>
                <div>
                  <span className="font-mono" style={{ fontWeight: 800, fontSize: '1.05rem', color: 'var(--color-navy-900)' }}>
                    {resolveResult.evidenceNumber}
                  </span>
                  <div style={{ fontWeight: 600, color: 'var(--color-navy-800)', fontSize: '0.9rem' }}>
                    {resolveResult.evidenceName}
                  </div>
                </div>
                <Badge status="SECURE">AUTHENTICATED TAG</Badge>
              </div>

              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '0.75rem', fontSize: '0.8125rem' }}>
                <div>
                  <span style={{ color: 'var(--color-slate-500)', fontSize: '0.75rem' }}>Case Number:</span>
                  <div className="font-mono" style={{ fontWeight: 600 }}>{resolveResult.caseNumber || '--'}</div>
                </div>
                <div>
                  <span style={{ color: 'var(--color-slate-500)', fontSize: '0.75rem' }}>Evidence Status:</span>
                  <div><Badge status={resolveResult.evidenceStatus}>{resolveResult.evidenceStatus}</Badge></div>
                </div>
                <div>
                  <span style={{ color: 'var(--color-slate-500)', fontSize: '0.75rem' }}>Current Custodian:</span>
                  <div style={{ fontWeight: 600, color: 'var(--color-primary-700)' }}>{resolveResult.currentCustodian || 'Vault Storage'}</div>
                </div>
                <div>
                  <span style={{ color: 'var(--color-slate-500)', fontSize: '0.75rem' }}>Integrity Status:</span>
                  <div><Badge status={resolveResult.integrityStatus}>{resolveResult.integrityStatus}</Badge></div>
                </div>
                <div style={{ gridColumn: '1 / -1' }}>
                  <span style={{ color: 'var(--color-slate-500)', fontSize: '0.75rem' }}>QR Generated At:</span>
                  <div className="font-mono" style={{ fontSize: '0.75rem' }}>
                    {resolveResult.qrGeneratedAt ? new Date(resolveResult.qrGeneratedAt).toLocaleString() : '--'}
                  </div>
                </div>
              </div>
            </div>
          ) : (
            <div style={{ textAlign: 'center', padding: '2rem 1rem', color: 'var(--color-slate-400)', fontSize: '0.875rem', backgroundColor: 'var(--color-slate-50)', borderRadius: 'var(--border-radius)' }}>
              Scan or type a physical barcode evidence number above to verify safe authenticity payload.
            </div>
          )}
        </div>
      </div>

      {/* Admin QR Regeneration Modal */}
      {qrData && (
        <QRRegenerateModal
          isOpen={regenerateOpen}
          onClose={() => setRegenerateOpen(false)}
          evidenceId={qrData.evidenceId}
          evidenceNumber={qrData.evidenceNumber}
          onSuccess={() => handleFetchQR()}
        />
      )}
    </div>
  );
};

export default QrPage;

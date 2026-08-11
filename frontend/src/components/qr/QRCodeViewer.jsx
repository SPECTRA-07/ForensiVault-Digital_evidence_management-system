import React, { useState, useEffect } from 'react';
import { QrCode, Download, Calendar, FileText, RefreshCw, AlertCircle } from 'lucide-react';
import Modal from '../Modal';
import Button from '../Button';
import Badge from '../Badge';
import LoadingSpinner from '../LoadingSpinner';
import qrService from '../../services/qrService';

export const QRCodeViewer = ({ isOpen, onClose, evidenceId, evidenceNumber }) => {
  const [qrInfo, setQrInfo] = useState(null);
  const [imageUrl, setImageUrl] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    let objectUrl = null;

    if (isOpen && evidenceId) {
      const fetchQR = async () => {
        setLoading(true);
        setError(null);
        try {
          // Fetch QR metadata: GET /qr/evidence/{evidenceId}
          const infoRes = await qrService.getQRCodeInfo(evidenceId);
          if (infoRes) {
            const metadata = infoRes.data || infoRes;
            setQrInfo(metadata);
          }

          // Fetch QR PNG image blob: GET /qr/evidence/{evidenceId}/image
          const imageBlob = await qrService.getQRCodeImage(evidenceId);
          if (imageBlob && imageBlob instanceof Blob) {
            objectUrl = URL.createObjectURL(imageBlob);
            setImageUrl(objectUrl);
          } else {
            throw new Error('Failed to retrieve binary QR barcode image payload.');
          }
        } catch (err) {
          const msg = typeof err === 'string' ? err : err.message || err.error || 'Failed to retrieve QR code image.';
          setError(msg);
        } finally {
          setLoading(false);
        }
      };

      fetchQR();
    }

    return () => {
      if (objectUrl) {
        URL.revokeObjectURL(objectUrl);
      }
    };
  }, [isOpen, evidenceId]);

  if (!isOpen) return null;

  const handleDownloadImage = () => {
    if (!imageUrl) return;
    const link = document.createElement('a');
    link.href = imageUrl;
    link.download = qrInfo?.qrFileName || `QR-evidence-${evidenceId}.png`;
    document.body.appendChild(link);
    link.click();
    link.remove();
  };

  return (
    <Modal isOpen={isOpen} onClose={onClose} title="Physical Evidence QR Barcode Tag" maxWidth="520px">
      {loading ? (
        <LoadingSpinner message="Retrieving 250x250 physical QR barcode tag..." />
      ) : error ? (
        <div style={{ backgroundColor: 'var(--color-danger-50)', color: 'var(--color-danger-700)', padding: '1rem', borderRadius: '4px', textAlign: 'center', fontSize: '0.875rem' }}>
          <AlertCircle size={32} style={{ marginBottom: '0.5rem', color: 'var(--color-danger-600)' }} />
          <div>{error}</div>
        </div>
      ) : (
        <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: '1.25rem' }}>
          {/* QR Image Frame */}
          <div style={{ padding: '1.25rem', backgroundColor: '#ffffff', border: '2px solid var(--color-slate-200)', borderRadius: '8px', boxShadow: 'var(--shadow-sm)' }}>
            {imageUrl ? (
              <img
                src={imageUrl}
                alt={`QR Barcode for ${evidenceNumber || evidenceId}`}
                style={{ width: '250px', height: '250px', display: 'block' }}
              />
            ) : (
              <div style={{ width: '250px', height: '250px', display: 'flex', alignItems: 'center', justifyContent: 'center', backgroundColor: 'var(--color-slate-100)', color: 'var(--color-slate-400)' }}>
                No QR Image Available
              </div>
            )}
          </div>

          {/* QR Code Metadata */}
          <div className="card" style={{ width: '100%', padding: '1rem' }}>
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '0.75rem', fontSize: '0.8125rem' }}>
              <div>
                <span style={{ color: 'var(--color-slate-500)', fontSize: '0.75rem' }}>Evidence Number:</span>
                <div className="font-mono" style={{ fontWeight: 700, color: 'var(--color-navy-900)' }}>
                  {qrInfo?.evidenceNumber || evidenceNumber || `#EV-${evidenceId}`}
                </div>
              </div>
              <div>
                <span style={{ color: 'var(--color-slate-500)', fontSize: '0.75rem' }}>QR Filename:</span>
                <div className="font-mono" style={{ fontSize: '0.75rem' }}>
                  {qrInfo?.qrFileName || `QR-EVD-${evidenceId}.png`}
                </div>
              </div>
              <div>
                <span style={{ color: 'var(--color-slate-500)', fontSize: '0.75rem' }}>Generated Timestamp:</span>
                <div className="font-mono" style={{ fontSize: '0.75rem' }}>
                  {qrInfo?.generatedAt ? new Date(qrInfo.generatedAt).toLocaleString() : '--'}
                </div>
              </div>
              <div>
                <span style={{ color: 'var(--color-slate-500)', fontSize: '0.75rem' }}>Format:</span>
                <div><Badge status="SECURE">250x250 PNG</Badge></div>
              </div>
            </div>
          </div>

          {/* Actions */}
          <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '0.75rem', width: '100%' }}>
            <Button variant="outline" onClick={onClose}>
              Close
            </Button>
            <Button variant="primary" icon={Download} onClick={handleDownloadImage} disabled={!imageUrl}>
              Download Barcode PNG
            </Button>
          </div>
        </div>
      )}
    </Modal>
  );
};

export default QRCodeViewer;

import React, { useState, useEffect, useCallback } from 'react';
import { Briefcase, Calendar, MapPin, User, FileText, FolderCheck, Eye, Clock, Shield, Upload } from 'lucide-react';
import Modal from '../Modal';
import Button from '../Button';
import Badge from '../Badge';
import LoadingSpinner from '../LoadingSpinner';
import EvidenceUploadModal from '../evidence/EvidenceUploadModal';
import caseService from '../../services/caseService';
import evidenceService from '../../services/evidenceService';
import { useAuth } from '../../hooks/useAuth';

export const CaseDetailsModal = ({ isOpen, onClose, caseId, onOpenEvidenceDetails }) => {
  const { user } = useAuth();
  const canUpload = user?.role !== 'COURT_OFFICIAL';

  const [caseData, setCaseData] = useState(null);
  const [evidenceList, setEvidenceList] = useState([]);
  const [loading, setLoading] = useState(true);
  const [loadingEvidence, setLoadingEvidence] = useState(false);
  const [error, setError] = useState(null);
  const [uploadModalOpen, setUploadModalOpen] = useState(false);

  const fetchEvidence = useCallback(async () => {
    if (!caseId) return;
    setLoadingEvidence(true);
    try {
      const res = await evidenceService.getEvidenceByCaseId(caseId, { size: 100 });
      if (res && res.data) {
        setEvidenceList(res.data.content || res.data || []);
      }
    } catch (err) {
      console.warn('Failed to load case evidence:', err);
      setEvidenceList([]);
    } finally {
      setLoadingEvidence(false);
    }
  }, [caseId]);

  useEffect(() => {
    if (isOpen && caseId) {
      const fetchData = async () => {
        setLoading(true);
        setError(null);
        try {
          const res = await caseService.getCaseById(caseId);
          if (res && res.data) {
            setCaseData(res.data);
          }
        } catch (err) {
          setError(err);
        } finally {
          setLoading(false);
        }
      };

      fetchData();
      fetchEvidence();
    }
  }, [isOpen, caseId, fetchEvidence]);

  if (!isOpen) return null;

  return (
    <>
      <Modal isOpen={isOpen} onClose={onClose} title="Criminal Case File & Evidence Record" maxWidth="760px">
        {loading ? (
          <LoadingSpinner message="Retrieving detailed case investigation payload..." />
        ) : caseData ? (
          <div style={{ display: 'flex', flexDirection: 'column', gap: '1.25rem' }}>
            {/* Header Summary Banner */}
            <div style={{ backgroundColor: 'var(--color-slate-50)', padding: '1rem', borderRadius: 'var(--border-radius)', border: '1px solid var(--color-slate-200)' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', flexWrap: 'wrap', gap: '0.5rem' }}>
                <div>
                  <div className="font-mono" style={{ fontWeight: 800, fontSize: '1.15rem', color: 'var(--color-navy-900)' }}>
                    {caseData.caseNumber} {caseData.crimeNumber && `(${caseData.crimeNumber})`}
                  </div>
                  <h3 style={{ margin: '0.2rem 0 0 0', fontSize: '1.05rem' }}>{caseData.caseName}</h3>
                </div>
                <div style={{ display: 'flex', gap: '0.5rem' }}>
                  <Badge status="SUBMITTED">{caseData.crimeType}</Badge>
                  <Badge status={caseData.severity}>{caseData.severity}</Badge>
                  <Badge status={caseData.status}>{caseData.status}</Badge>
                </div>
              </div>
            </div>

            {/* Case Metadata Section */}
            <div className="card" style={{ padding: '1rem' }}>
              <h4 style={{ fontSize: '0.8125rem', color: 'var(--color-slate-500)', textTransform: 'uppercase', letterSpacing: '0.05em', marginBottom: '0.75rem' }}>
                Investigation File Metadata
              </h4>
              <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(210px, 1fr))', gap: '0.875rem', fontSize: '0.8125rem' }}>
                <div>
                  <span style={{ color: 'var(--color-slate-500)', fontSize: '0.75rem' }}>Crime Number:</span>
                  <div className="font-mono" style={{ fontWeight: 600 }}>{caseData.crimeNumber || '--'}</div>
                </div>
                <div>
                  <span style={{ color: 'var(--color-slate-500)', fontSize: '0.75rem' }}>Crime Type Classification:</span>
                  <div><Badge status="INFO">{caseData.crimeType || '--'}</Badge></div>
                </div>
                <div>
                  <span style={{ color: 'var(--color-slate-500)', fontSize: '0.75rem' }}>Crime Severity Level:</span>
                  <div><Badge status={caseData.severity}>{caseData.severity || '--'}</Badge></div>
                </div>
                <div>
                  <span style={{ color: 'var(--color-slate-500)', fontSize: '0.75rem' }}>Investigation Status:</span>
                  <div><Badge status={caseData.status}>{caseData.status || '--'}</Badge></div>
                </div>
                <div>
                  <span style={{ color: 'var(--color-slate-500)', fontSize: '0.75rem' }}>Incident Date:</span>
                  <div className="font-mono">{caseData.incidentDate || '--'}</div>
                </div>
                <div>
                  <span style={{ color: 'var(--color-slate-500)', fontSize: '0.75rem' }}>Crime Scene Location:</span>
                  <div style={{ fontWeight: 500 }}>{caseData.crimeSceneLocation || '--'}</div>
                </div>
                <div>
                  <span style={{ color: 'var(--color-slate-500)', fontSize: '0.75rem' }}>Investigation Start Date:</span>
                  <div className="font-mono">{caseData.investigationStartDate || '--'}</div>
                </div>
                <div>
                  <span style={{ color: 'var(--color-slate-500)', fontSize: '0.75rem' }}>Assigned Lead Officer:</span>
                  <div style={{ fontWeight: 600, color: 'var(--color-primary-700)' }}>
                    {caseData.assignedOfficer?.fullName
                      ? `${caseData.assignedOfficer.fullName} (${caseData.assignedOfficer.employeeId || caseData.assignedOfficer.id})`
                      : 'Unassigned'}
                  </div>
                </div>
              </div>

              {caseData.caseSummary && (
                <div style={{ marginTop: '0.875rem', paddingTop: '0.75rem', borderTop: '1px solid var(--color-slate-100)', fontSize: '0.8125rem' }}>
                  <span style={{ color: 'var(--color-slate-500)', fontSize: '0.75rem', display: 'block', fontWeight: 600 }}>Case Synopsis / Summary:</span>
                  <div style={{ marginTop: '0.25rem', color: 'var(--color-navy-900)', backgroundColor: 'var(--color-slate-50)', padding: '0.625rem 0.75rem', borderRadius: '4px' }}>
                    {caseData.caseSummary}
                  </div>
                </div>
              )}
            </div>

            {/* Case Evidence List Section */}
            <div className="card" style={{ padding: '1rem' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '0.75rem' }}>
                <h4 style={{ fontSize: '0.8125rem', color: 'var(--color-slate-500)', textTransform: 'uppercase', letterSpacing: '0.05em', margin: 0 }}>
                  Associated Digital Evidence ({evidenceList.length})
                </h4>

                {canUpload && (
                  <Button variant="secondary" size="sm" icon={Upload} onClick={() => setUploadModalOpen(true)}>
                    Upload Evidence File
                  </Button>
                )}
              </div>

              {loadingEvidence ? (
                <LoadingSpinner message="Querying evidence vault items for this case..." />
              ) : evidenceList.length > 0 ? (
                <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
                  {evidenceList.map((item) => (
                    <div
                      key={item.id}
                      style={{
                        display: 'flex',
                        justifyContent: 'space-between',
                        alignItems: 'center',
                        padding: '0.75rem',
                        backgroundColor: 'var(--color-slate-50)',
                        borderRadius: 'var(--border-radius)',
                        border: '1px solid var(--color-slate-200)',
                        fontSize: '0.8125rem',
                      }}
                    >
                      <div>
                        <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                          <span className="font-mono" style={{ fontWeight: 700, color: 'var(--color-navy-900)' }}>
                            {item.evidenceNumber || `#EV-${item.id}`}
                          </span>
                          <Badge status={item.evidenceType}>{item.evidenceType}</Badge>
                          <Badge status={item.status}>{item.status}</Badge>
                        </div>
                        <div style={{ fontWeight: 500, marginTop: '0.2rem', color: 'var(--color-navy-800)' }}>
                          {item.evidenceName}
                        </div>
                        <div style={{ fontSize: '0.75rem', color: 'var(--color-slate-500)', marginTop: '0.15rem' }}>
                          Custodian: {item.currentCustodian?.fullName || item.currentCustodianName || 'Vault'} | Uploaded: {item.uploadedAt ? new Date(item.uploadedAt).toLocaleDateString() : '--'}
                        </div>
                      </div>

                      {onOpenEvidenceDetails && (
                        <Button
                          variant="secondary"
                          size="sm"
                          icon={Eye}
                          onClick={() => {
                            onClose();
                            onOpenEvidenceDetails(item);
                          }}
                        >
                          Inspect Evidence
                        </Button>
                      )}
                    </div>
                  ))}
                </div>
              ) : (
                <div style={{ padding: '1.5rem', textAlign: 'center', color: 'var(--color-slate-500)', fontSize: '0.8125rem', backgroundColor: 'var(--color-slate-50)', borderRadius: 'var(--border-radius)' }}>
                  No digital evidence payloads registered under this case yet.
                </div>
              )}
            </div>
          </div>
        ) : null}
      </Modal>

      {/* Embedded Evidence Upload Modal */}
      {caseData && (
        <EvidenceUploadModal
          isOpen={uploadModalOpen}
          onClose={() => setUploadModalOpen(false)}
          onSuccess={() => fetchEvidence()}
        />
      )}
    </>
  );
};

export default CaseDetailsModal;

import React, { useState, useEffect } from 'react';
import { GitCommit, User, Calendar, MapPin, CheckCircle2, XCircle, Clock, ShieldCheck } from 'lucide-react';
import Modal from '../Modal';
import Badge from '../Badge';
import LoadingSpinner from '../LoadingSpinner';
import ErrorMessage from '../ErrorMessage';
import custodyService from '../../services/custodyService';

export const CustodyTimelineModal = ({ isOpen, onClose, evidenceId, evidenceNumber, evidenceName }) => {
  const [timelineData, setTimelineData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    if (isOpen && evidenceId) {
      const fetchTimeline = async () => {
        setLoading(true);
        setError(null);
        try {
          const res = await custodyService.getCustodyTimeline(evidenceId);
          if (res && res.data) {
            setTimelineData(res.data);
          }
        } catch (err) {
          setError(err);
        } finally {
          setLoading(false);
        }
      };
      fetchTimeline();
    }
  }, [isOpen, evidenceId]);

  if (!isOpen) return null;

  return (
    <Modal isOpen={isOpen} onClose={onClose} title="Forensic Chain of Custody Timeline" maxWidth="680px">
      <div style={{ backgroundColor: 'var(--color-slate-50)', padding: '1rem', borderRadius: 'var(--border-radius)', marginBottom: '1.25rem', border: '1px solid var(--color-slate-200)' }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', flexWrap: 'wrap', gap: '0.5rem' }}>
          <div>
            <div className="font-mono" style={{ fontWeight: 800, fontSize: '1.1rem', color: 'var(--color-navy-900)' }}>
              {evidenceNumber || timelineData?.evidenceNumber || `#EV-${evidenceId}`}
            </div>
            <div style={{ fontWeight: 600, fontSize: '0.95rem', color: 'var(--color-navy-800)', marginTop: '0.15rem' }}>
              {evidenceName || timelineData?.evidenceName || 'Digital Evidence Item'}
            </div>
          </div>
          <div style={{ textAlign: 'right' }}>
            <span style={{ fontSize: '0.75rem', color: 'var(--color-slate-500)', display: 'block' }}>Current Legal Custodian:</span>
            <strong style={{ fontSize: '0.875rem', color: 'var(--color-primary-700)' }}>
              {timelineData?.currentCustodianName || 'Vault Storage'}
            </strong>
          </div>
        </div>
      </div>

      {error && <ErrorMessage error={error} />}

      {loading ? (
        <LoadingSpinner message="Generating forensic audit trail timeline..." />
      ) : timelineData && timelineData.timeline && timelineData.timeline.length > 0 ? (
        <div style={{ position: 'relative', paddingLeft: '1.5rem', marginTop: '1rem' }}>
          {/* Vertical timeline line */}
          <div style={{ position: 'absolute', left: '7px', top: '10px', bottom: '10px', width: '2px', backgroundColor: 'var(--color-slate-300)' }} />

          {timelineData.timeline.map((event, idx) => (
            <div key={event.id || idx} style={{ position: 'relative', marginBottom: '1.5rem' }}>
              {/* Timeline marker icon */}
              <div
                style={{
                  position: 'absolute',
                  left: '-1.5rem',
                  top: '2px',
                  width: '16px',
                  height: '16px',
                  borderRadius: '50%',
                  backgroundColor: event.transferStatus === 'ACCEPTED' ? 'var(--color-success-600)' : event.transferStatus === 'PENDING' ? 'var(--color-warning-600)' : 'var(--color-danger-600)',
                  border: '3px solid #ffffff',
                  boxShadow: '0 0 0 1px var(--color-slate-300)',
                }}
              />

              <div className="card" style={{ padding: '0.875rem 1rem', backgroundColor: '#ffffff' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '0.35rem' }}>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                    <span className="font-mono" style={{ fontWeight: 700, fontSize: '0.8125rem' }}>
                      Seq #{event.custodySequence || idx + 1}
                    </span>
                    <Badge status={event.transferStatus}>{event.transferStatus}</Badge>
                    <Badge status="SUBMITTED">{event.transferPurpose}</Badge>
                  </div>
                  <span className="font-mono" style={{ fontSize: '0.75rem', color: 'var(--color-slate-500)' }}>
                    {event.transferredAt ? new Date(event.transferredAt).toLocaleString() : '--'}
                  </span>
                </div>

                <div style={{ fontSize: '0.8125rem', color: 'var(--color-navy-900)', marginTop: '0.35rem' }}>
                  <strong>From:</strong> {event.transferredBy?.fullName || event.transferredBy?.email || 'Origin Custodian'}{' '}
                  &rarr; <strong>To:</strong> {event.transferredTo?.fullName || event.transferredTo?.email || 'Recipient Custodian'}
                </div>

                {event.transferLocation && (
                  <div style={{ fontSize: '0.75rem', color: 'var(--color-slate-600)', marginTop: '0.25rem', display: 'flex', alignItems: 'center', gap: '0.25rem' }}>
                    <MapPin size={12} />
                    <span>Location: {event.transferLocation}</span>
                  </div>
                )}

                {event.transferRemarks && (
                  <div style={{ fontSize: '0.75rem', color: 'var(--color-slate-600)', marginTop: '0.35rem', fontStyle: 'italic', backgroundColor: 'var(--color-slate-50)', padding: '0.35rem 0.5rem', borderRadius: '4px' }}>
                    "{event.transferRemarks}"
                  </div>
                )}

                {event.acceptedAt && (
                  <div style={{ fontSize: '0.7rem', color: 'var(--color-success-700)', marginTop: '0.35rem', fontWeight: 600 }}>
                    Handshake Completed: {new Date(event.acceptedAt).toLocaleString()}
                    {event.acceptanceRemarks && ` — "${event.acceptanceRemarks}"`}
                  </div>
                )}
              </div>
            </div>
          ))}
        </div>
      ) : (
        <div style={{ textAlign: 'center', padding: '2rem', color: 'var(--color-slate-500)', fontSize: '0.875rem' }}>
          No transfer events recorded for this evidence item. Initial custody established upon registration.
        </div>
      )}
    </Modal>
  );
};

export default CustodyTimelineModal;

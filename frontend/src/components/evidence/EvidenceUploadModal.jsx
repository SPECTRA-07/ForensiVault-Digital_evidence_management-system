import React, { useState, useEffect } from 'react';
import { Upload, FileCheck2, AlertCircle, Save, FolderCheck } from 'lucide-react';
import Modal from '../Modal';
import Button from '../Button';
import Input from '../Input';
import Select from '../Select';
import evidenceService from '../../services/evidenceService';
import caseService from '../../services/caseService';

export const EvidenceUploadModal = ({ isOpen, onClose, onSuccess }) => {
  const [file, setFile] = useState(null);
  const [caseId, setCaseId] = useState('');
  const [evidenceName, setEvidenceName] = useState('');
  const [displayName, setDisplayName] = useState('');
  const [evidenceType, setEvidenceType] = useState('DOCUMENT');
  const [collectedFrom, setCollectedFrom] = useState('');
  const [collectionMethod, setCollectionMethod] = useState('');
  const [collectedAt, setCollectedAt] = useState('');
  const [collectedBy, setCollectedBy] = useState('');
  const [description, setDescription] = useState('');
  const [remarks, setRemarks] = useState('');

  const [casesList, setCasesList] = useState([]);
  const [loadingCases, setLoadingCases] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [error, setError] = useState(null);

  const evidenceTypeOptions = [
    { value: 'DOCUMENT', label: 'Document File / Text Payload' },
    { value: 'IMAGE', label: 'Digital Image / Photograph' },
    { value: 'VIDEO', label: 'Video Footage / CCTV Recording' },
    { value: 'AUDIO', label: 'Audio Recording / Wiretap' },
    { value: 'FORENSIC_IMAGE', label: 'Forensic Disk Image (.E01/.DD)' },
    { value: 'DISK_DUMP', label: 'Hard Drive Bitstream Dump' },
    { value: 'MEMORY_DUMP', label: 'RAM Memory Dump' },
    { value: 'NETWORK_TRACE', label: 'Network Packet Capture (.PCAP)' },
    { value: 'LOG_FILE', label: 'System Server Audit Log' },
    { value: 'DATABASE_EXPORT', label: 'Database Backup Export' },
    { value: 'OTHER', label: 'Other Digital Forensic Artifact' },
  ];

  useEffect(() => {
    if (isOpen) {
      setFile(null);
      setCaseId('');
      setEvidenceName('');
      setDisplayName('');
      setEvidenceType('DOCUMENT');
      setCollectedFrom('');
      setCollectionMethod('');
      setCollectedAt(new Date().toISOString().slice(0, 16));
      setCollectedBy('');
      setDescription('');
      setRemarks('');
      setError(null);

      const fetchCases = async () => {
        setLoadingCases(true);
        try {
          const res = await caseService.getAllCases({ size: 100 });
          if (res && res.data) {
            setCasesList(res.data.content || res.data || []);
          }
        } catch (e) {
          console.warn('Failed to load active cases:', e);
          setCasesList([]);
        } finally {
          setLoadingCases(false);
        }
      };

      fetchCases();
    }
  }, [isOpen]);

  if (!isOpen) return null;

  const handleFileChange = (e) => {
    if (e.target.files && e.target.files[0]) {
      const selectedFile = e.target.files[0];
      setFile(selectedFile);
      if (!evidenceName) {
        setEvidenceName(selectedFile.name);
      }
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    if (!file) {
      setError('Please select a digital evidence file to upload.');
      return;
    }
    if (!caseId) {
      setError('Please select a target investigation case.');
      return;
    }
    if (!evidenceName.trim()) {
      setError('Evidence title is required.');
      return;
    }

    setIsSubmitting(true);
    setError(null);

    try {
      const formData = new FormData();
      formData.append('file', file);
      formData.append('caseId', Number(caseId));
      formData.append('evidenceName', evidenceName.trim());
      formData.append('evidenceType', evidenceType);

      if (displayName.trim()) formData.append('displayName', displayName.trim());
      if (collectedFrom.trim()) formData.append('collectedFrom', collectedFrom.trim());
      if (collectionMethod.trim()) formData.append('collectionMethod', collectionMethod.trim());
      if (collectedAt) formData.append('collectedAt', `${collectedAt}:00`);
      if (collectedBy.trim()) formData.append('collectedBy', collectedBy.trim());
      if (description.trim()) formData.append('description', description.trim());
      if (remarks.trim()) formData.append('remarks', remarks.trim());

      const response = await evidenceService.uploadEvidence(formData);
      if (onSuccess) {
        onSuccess(response?.data);
      }
      onClose();
    } catch (err) {
      const msg = typeof err === 'string' ? err : err.message || 'Failed to upload digital evidence file.';
      setError(msg);
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <Modal isOpen={isOpen} onClose={onClose} title="Upload & Register Digital Evidence File" maxWidth="640px">
      {error && (
        <div style={{ backgroundColor: 'var(--color-danger-50)', borderLeft: '4px solid var(--color-danger-600)', padding: '0.75rem 1rem', marginBottom: '1rem', borderRadius: '4px', fontSize: '0.8125rem', color: 'var(--color-danger-700)', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
          <AlertCircle size={16} style={{ flexShrink: 0 }} />
          <span>{error}</span>
        </div>
      )}

      <form onSubmit={handleSubmit}>
        {/* File Input Selector */}
        <div className="form-group" style={{ marginBottom: '1rem' }}>
          <label className="form-label">
            Digital Evidence File Payload <span style={{ color: 'var(--color-danger-600)' }}>*</span>
          </label>
          <input
            type="file"
            className="form-input"
            onChange={handleFileChange}
            required
            disabled={isSubmitting}
            style={{ padding: '0.4rem' }}
          />
          {file && (
            <div style={{ marginTop: '0.35rem', fontSize: '0.75rem', color: 'var(--color-slate-500)' }}>
              Selected: <strong>{file.name}</strong> ({file.size < 1024 * 1024 ? `${(file.size / 1024).toFixed(1)} KB` : `${(file.size / (1024 * 1024)).toFixed(2)} MB`})
            </div>
          )}
        </div>

        {/* Case Selector */}
        {casesList.length > 0 ? (
          <Select
            label="Associated Criminal Investigation Case"
            value={caseId}
            onChange={(e) => setCaseId(e.target.value)}
            options={casesList.map((c) => ({
              value: c.id,
              label: `${c.caseNumber} - ${c.caseName} (${c.status})`,
            }))}
            placeholder="Select target case file..."
            required
            disabled={isSubmitting}
          />
        ) : (
          <Input
            label="Case ID"
            type="number"
            placeholder="Enter numeric Case ID (e.g. 1)..."
            value={caseId}
            onChange={(e) => setCaseId(e.target.value)}
            required
            disabled={isSubmitting}
          />
        )}

        <Input
          label="Evidence Title / File Label"
          placeholder="e.g. CCTV Surveillance Camera Stream B"
          value={evidenceName}
          onChange={(e) => setEvidenceName(e.target.value)}
          required
          disabled={isSubmitting}
        />

        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
          <Select
            label="Evidence Category Type"
            value={evidenceType}
            onChange={(e) => setEvidenceType(e.target.value)}
            options={evidenceTypeOptions}
            required
            disabled={isSubmitting}
          />

          <Input
            label="Display Name / Tag"
            placeholder="e.g. Evidence Item #4B"
            value={displayName}
            onChange={(e) => setDisplayName(e.target.value)}
            disabled={isSubmitting}
          />
        </div>

        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
          <Input
            label="Collected From Location / Device"
            placeholder="e.g. Server Rack 4, 142 Plaza"
            value={collectedFrom}
            onChange={(e) => setCollectedFrom(e.target.value)}
            disabled={isSubmitting}
          />

          <Input
            label="Collection Method"
            placeholder="e.g. Bitstream Physical Disk Image"
            value={collectionMethod}
            onChange={(e) => setCollectionMethod(e.target.value)}
            disabled={isSubmitting}
          />
        </div>

        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
          <Input
            label="Collection Timestamp"
            type="datetime-local"
            value={collectedAt}
            onChange={(e) => setCollectedAt(e.target.value)}
            disabled={isSubmitting}
          />

          <Input
            label="Seizing Officer / Collector"
            placeholder="e.g. Det. Marcus Vance"
            value={collectedBy}
            onChange={(e) => setCollectedBy(e.target.value)}
            disabled={isSubmitting}
          />
        </div>

        <div className="form-group">
          <label className="form-label">Evidence Forensic Synopsis / Description</label>
          <textarea
            className="form-textarea"
            rows={2}
            placeholder="Enter forensic description, hardware serial numbers, or crime scene context..."
            value={description}
            onChange={(e) => setDescription(e.target.value)}
            disabled={isSubmitting}
          />
        </div>

        <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '0.75rem', marginTop: '1.5rem' }}>
          <Button variant="outline" onClick={onClose} disabled={isSubmitting}>
            Cancel
          </Button>
          <Button type="submit" variant="primary" icon={Upload} disabled={isSubmitting}>
            {isSubmitting ? 'Uploading Evidence...' : 'Upload & Register Evidence'}
          </Button>
        </div>
      </form>
    </Modal>
  );
};

export default EvidenceUploadModal;

import React, { useState, useEffect } from 'react';
import { Briefcase, Calendar, MapPin, FileText, AlertCircle, Save, Hash } from 'lucide-react';
import Modal from '../Modal';
import Button from '../Button';
import Input from '../Input';
import Select from '../Select';
import caseService from '../../services/caseService';

export const CaseFormModal = ({ isOpen, onClose, editCase = null, onSuccess }) => {
  const [caseNumber, setCaseNumber] = useState('');
  const [crimeNumber, setCrimeNumber] = useState('');
  const [caseName, setCaseName] = useState('');
  const [caseSummary, setCaseSummary] = useState('');
  const [crimeType, setCrimeType] = useState('CYBER_CRIME');
  const [severity, setSeverity] = useState('MEDIUM');
  const [incidentDate, setIncidentDate] = useState('');
  const [crimeSceneLocation, setCrimeSceneLocation] = useState('');
  const [investigationStartDate, setInvestigationStartDate] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [error, setError] = useState(null);

  const crimeTypeOptions = [
    { value: 'CYBER_CRIME', label: 'Cybercrime & Digital Offense' },
    { value: 'MURDER', label: 'Homicide & Murder Investigation' },
    { value: 'FRAUD', label: 'Financial Fraud & Economic Offense' },
    { value: 'DRUG_OFFENSE', label: 'Narcotics & Drug Trafficking' },
    { value: 'THEFT', label: 'Theft, Robbery & Burglary' },
    { value: 'KIDNAPPING', label: 'Kidnapping & Abduction' },
    { value: 'OTHER', label: 'Other Criminal Offense' },
  ];

  const severityOptions = [
    { value: 'LOW', label: 'Low Severity' },
    { value: 'MEDIUM', label: 'Medium Severity' },
    { value: 'HIGH', label: 'High Severity' },
    { value: 'CRITICAL', label: 'Critical Severity' },
  ];

  useEffect(() => {
    if (isOpen) {
      if (editCase) {
        setCaseNumber(editCase.caseNumber || '');
        setCrimeNumber(editCase.crimeNumber || '');
        setCaseName(editCase.caseName || '');
        setCaseSummary(editCase.caseSummary || '');
        setCrimeType(editCase.crimeType || 'CYBER_CRIME');
        setSeverity(editCase.severity || 'MEDIUM');
        setIncidentDate(editCase.incidentDate || '');
        setCrimeSceneLocation(editCase.crimeSceneLocation || '');
        setInvestigationStartDate(editCase.investigationStartDate || '');
      } else {
        const randId = Math.floor(1000 + Math.random() * 9000);
        setCaseNumber(`CASE-2026-${randId}`);
        setCrimeNumber(`CR-2026-${randId}`);
        setCaseName('');
        setCaseSummary('');
        setCrimeType('CYBER_CRIME');
        setSeverity('MEDIUM');
        setIncidentDate(new Date().toISOString().split('T')[0]);
        setCrimeSceneLocation('');
        setInvestigationStartDate(new Date().toISOString().split('T')[0]);
      }
      setError(null);
    }
  }, [editCase, isOpen]);

  if (!isOpen) return null;

  const handleSubmit = async (e) => {
    e.preventDefault();

    if (!caseNumber.trim()) {
      setError('Case Number is required.');
      return;
    }
    if (!crimeNumber.trim()) {
      setError('FIR / Crime Number is required.');
      return;
    }
    if (!caseName.trim()) {
      setError('Case Name / Title is required.');
      return;
    }
    if (!incidentDate) {
      setError('Incident Date is required.');
      return;
    }
    if (!crimeSceneLocation.trim()) {
      setError('Crime Scene Location is required.');
      return;
    }

    setIsSubmitting(true);
    setError(null);

    try {
      let response;
      if (editCase && editCase.id) {
        // Update request payload (CaseUpdateRequest)
        const updatePayload = {
          caseName: caseName.trim(),
          caseSummary: caseSummary.trim() || undefined,
          crimeType,
          severity,
          incidentDate,
          crimeSceneLocation: crimeSceneLocation.trim(),
          investigationStartDate: investigationStartDate || undefined,
        };
        response = await caseService.updateCase(editCase.id, updatePayload);
      } else {
        // Create request payload (CaseCreateRequest)
        const createPayload = {
          caseNumber: caseNumber.trim(),
          crimeNumber: crimeNumber.trim(),
          caseName: caseName.trim(),
          caseSummary: caseSummary.trim() || undefined,
          crimeType,
          severity,
          incidentDate,
          crimeSceneLocation: crimeSceneLocation.trim(),
          investigationStartDate: investigationStartDate || undefined,
        };
        response = await caseService.createCase(createPayload);
      }

      if (onSuccess) {
        onSuccess(response?.data);
      }
      onClose();
    } catch (err) {
      const msg = typeof err === 'string' ? err : err.message || 'Failed to save case investigation file.';
      setError(msg);
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      title={editCase ? `Edit Criminal Case File #${editCase.caseNumber || editCase.id}` : 'Create New Criminal Investigation Case'}
      maxWidth="620px"
    >
      {error && (
        <div style={{ backgroundColor: 'var(--color-danger-50)', borderLeft: '4px solid var(--color-danger-600)', padding: '0.75rem 1rem', marginBottom: '1rem', borderRadius: '4px', fontSize: '0.8125rem', color: 'var(--color-danger-700)', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
          <AlertCircle size={16} style={{ flexShrink: 0 }} />
          <span>{error}</span>
        </div>
      )}

      <form onSubmit={handleSubmit}>
        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
          {!editCase && (
            <Input
              label="System Case Number"
              placeholder="e.g. CASE-2026-904"
              value={caseNumber}
              onChange={(e) => setCaseNumber(e.target.value)}
              icon={Hash}
              required
              disabled={isSubmitting}
            />
          )}

          <Input
            label="FIR / Crime Number"
            placeholder="e.g. CR-2026-904"
            value={crimeNumber}
            onChange={(e) => setCrimeNumber(e.target.value)}
            required
            disabled={isSubmitting || !!editCase}
          />
        </div>

        <Input
          label="Case Title / Name"
          placeholder="e.g. Operation Ransomware Cyber Heist"
          value={caseName}
          onChange={(e) => setCaseName(e.target.value)}
          required
          disabled={isSubmitting}
        />

        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
          <Select
            label="Crime Type Classification"
            value={crimeType}
            onChange={(e) => setCrimeType(e.target.value)}
            options={crimeTypeOptions}
            required
            disabled={isSubmitting}
          />

          <Select
            label="Crime Severity Level"
            value={severity}
            onChange={(e) => setSeverity(e.target.value)}
            options={severityOptions}
            required
            disabled={isSubmitting}
          />
        </div>

        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
          <Input
            label="Incident Date"
            type="date"
            value={incidentDate}
            onChange={(e) => setIncidentDate(e.target.value)}
            required
            disabled={isSubmitting}
          />

          <Input
            label="Investigation Start Date"
            type="date"
            value={investigationStartDate}
            onChange={(e) => setInvestigationStartDate(e.target.value)}
            disabled={isSubmitting}
          />
        </div>

        <Input
          label="Crime Scene Location"
          placeholder="e.g. Server Room B, 142 Financial Plaza"
          value={crimeSceneLocation}
          onChange={(e) => setCrimeSceneLocation(e.target.value)}
          icon={MapPin}
          required
          disabled={isSubmitting}
        />

        <div className="form-group">
          <label className="form-label">Case Investigation Synopsis / Summary</label>
          <textarea
            className="form-textarea"
            rows={3}
            placeholder="Enter initial investigation summary, suspect details, or crime scene notes..."
            value={caseSummary}
            onChange={(e) => setCaseSummary(e.target.value)}
            disabled={isSubmitting}
          />
        </div>

        <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '0.75rem', marginTop: '1.5rem' }}>
          <Button variant="outline" onClick={onClose} disabled={isSubmitting}>
            Cancel
          </Button>
          <Button type="submit" variant="primary" icon={Save} disabled={isSubmitting}>
            {isSubmitting ? 'Saving Case...' : editCase ? 'Update Case' : 'Create Case Record'}
          </Button>
        </div>
      </form>
    </Modal>
  );
};

export default CaseFormModal;

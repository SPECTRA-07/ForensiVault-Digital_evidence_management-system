import React, { useState } from 'react';
import { Calendar, Filter, RotateCcw } from 'lucide-react';
import Button from '../Button';

export const DateRangeFilter = ({ onApplyFilter, onResetFilter }) => {
  const [startDate, setStartDate] = useState('');
  const [endDate, setEndDate] = useState('');

  const handleApply = (e) => {
    e.preventDefault();
    const params = {};
    if (startDate) {
      params.startDate = new Date(startDate).toISOString();
    }
    if (endDate) {
      // Set to end of the day
      const end = new Date(endDate);
      end.setHours(23, 59, 59, 999);
      params.endDate = end.toISOString();
    }
    onApplyFilter(params);
  };

  const handleReset = () => {
    setStartDate('');
    setEndDate('');
    onResetFilter();
  };

  return (
    <div className="card" style={{ marginBottom: '1.5rem', padding: '1rem' }}>
      <form onSubmit={handleApply} style={{ display: 'flex', alignItems: 'center', gap: '1rem', flexWrap: 'wrap' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', fontWeight: 600, fontSize: '0.8125rem', color: 'var(--color-navy-800)' }}>
          <Filter size={16} style={{ color: 'var(--color-primary-600)' }} />
          <span>Analytics Timeframe:</span>
        </div>

        <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
          <label style={{ fontSize: '0.75rem', color: 'var(--color-slate-500)' }}>From:</label>
          <input
            type="date"
            className="form-input"
            value={startDate}
            onChange={(e) => setStartDate(e.target.value)}
            style={{ padding: '0.35rem 0.6rem', fontSize: '0.8125rem', width: 'auto' }}
          />
        </div>

        <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
          <label style={{ fontSize: '0.75rem', color: 'var(--color-slate-500)' }}>To:</label>
          <input
            type="date"
            className="form-input"
            value={endDate}
            onChange={(e) => setEndDate(e.target.value)}
            style={{ padding: '0.35rem 0.6rem', fontSize: '0.8125rem', width: 'auto' }}
          />
        </div>

        <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginLeft: 'auto' }}>
          <Button type="submit" variant="primary" size="sm" icon={Calendar}>
            Apply Filter
          </Button>
          {(startDate || endDate) && (
            <Button type="button" variant="outline" size="sm" onClick={handleReset} icon={RotateCcw}>
              Reset
            </Button>
          )}
        </div>
      </form>
    </div>
  );
};

export default DateRangeFilter;

import React from 'react';
import { ChevronLeft, ChevronRight } from 'lucide-react';
import Button from './Button';

export const Pagination = ({
  pageObj = {},
  onPageChange,
}) => {
  const { number = 0, totalPages = 1, totalElements = 0, size = 10 } = pageObj;

  const currentPage = number + 1; // Convert 0-index to 1-index for UI display
  const startItem = totalElements === 0 ? 0 : number * size + 1;
  const endItem = Math.min((number + 1) * size, totalElements);

  return (
    <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginTop: '1rem', padding: '0.5rem 0' }}>
      <div style={{ fontSize: '0.8125rem', color: 'var(--color-slate-600)' }}>
        Showing <strong>{startItem}</strong> to <strong>{endItem}</strong> of <strong>{totalElements}</strong> entries
      </div>
      <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
        <Button
          variant="outline"
          size="sm"
          disabled={number <= 0}
          onClick={() => onPageChange(number - 1)}
          icon={ChevronLeft}
        >
          Previous
        </Button>
        <span style={{ fontSize: '0.8125rem', fontWeight: 600, padding: '0 0.5rem' }}>
          Page {currentPage} of {Math.max(totalPages, 1)}
        </span>
        <Button
          variant="outline"
          size="sm"
          disabled={number + 1 >= totalPages}
          onClick={() => onPageChange(number + 1)}
        >
          Next
          <ChevronRight size={14} />
        </Button>
      </div>
    </div>
  );
};

export default Pagination;

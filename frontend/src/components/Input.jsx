import React from 'react';

export const Input = ({
  label,
  id,
  type = 'text',
  value,
  onChange,
  placeholder,
  error,
  required = false,
  disabled = false,
  helperText,
  icon: Icon,
  className = '',
  ...props
}) => {
  const inputId = id || (label ? label.toLowerCase().replace(/\s+/g, '-') : undefined);

  return (
    <div className={`form-group ${className}`}>
      {label && (
        <label htmlFor={inputId} className="form-label">
          {label} {required && <span style={{ color: 'var(--color-danger-600)' }}>*</span>}
        </label>
      )}
      <div style={{ position: 'relative' }}>
        {Icon && (
          <div style={{ position: 'absolute', left: '0.75rem', top: '50%', transform: 'translateY(-50%)', color: 'var(--color-slate-400)' }}>
            <Icon size={16} />
          </div>
        )}
        <input
          id={inputId}
          type={type}
          value={value}
          onChange={onChange}
          placeholder={placeholder}
          disabled={disabled}
          className={`form-input ${error ? 'is-invalid' : ''}`}
          style={Icon ? { paddingLeft: '2.35rem' } : {}}
          {...props}
        />
      </div>
      {error && <span className="form-error">{error}</span>}
      {helperText && !error && <span style={{ fontSize: '0.75rem', color: 'var(--color-slate-500)' }}>{helperText}</span>}
    </div>
  );
};

export default Input;

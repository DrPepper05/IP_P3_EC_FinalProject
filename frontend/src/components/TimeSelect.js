import React from 'react';
import './TimeSelect.css';

/**
 * Time selection component with 30-minute intervals
 * Displays times in 24-hour format (00:00 to 23:30)
 */
const TimeSelect = ({ label, value, onChange, minTime, disabled }) => {
  // Generate time options in 30-minute intervals
  const generateTimeOptions = () => {
    const times = [];
    for (let hour = 0; hour < 24; hour++) {
      for (let minute = 0; minute < 60; minute += 30) {
        const timeValue = `${String(hour).padStart(2, '0')}:${String(minute).padStart(2, '0')}`;
        times.push(timeValue);
      }
    }
    return times;
  };

  const timeOptions = generateTimeOptions();

  // Filter options based on minTime if provided
  const availableOptions = minTime
    ? timeOptions.filter((time) => time > minTime)
    : timeOptions;

  const labelId = label.toLowerCase().replace(/\s+/g, '-');

  return (
    <div className="time-select-wrapper">
      <label htmlFor={labelId} className="time-select-label">
        {label}
      </label>
      <div className="time-select-container">
        <select
          id={labelId}
          className="time-select"
          value={value}
          onChange={(e) => onChange(e.target.value)}
          disabled={disabled}
          required
        >
          <option value="">Select time</option>
          {availableOptions.map((time) => (
            <option key={time} value={time}>
              {time}
            </option>
          ))}
        </select>
        <div className="time-select-icon">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
            <circle cx="12" cy="12" r="10" />
            <polyline points="12 6 12 12 16 14" />
          </svg>
        </div>
      </div>
    </div>
  );
};

export default TimeSelect;

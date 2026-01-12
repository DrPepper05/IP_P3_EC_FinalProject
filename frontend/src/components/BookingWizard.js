import React, { useState, useEffect } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import apiService from '../services/api';
import TimeSelect from './TimeSelect';
import './BookingWizard.css';

const BookingWizard = () => {
  const location = useLocation();
  const navigate = useNavigate();

  const locker = location.state?.locker;

  // State management
  const [selectedDate, setSelectedDate] = useState(new Date().toISOString().split('T')[0]);
  const [startTime, setStartTime] = useState('');
  const [endTime, setEndTime] = useState('');
  const [showDatePicker, setShowDatePicker] = useState(false);
  const [totalPrice, setTotalPrice] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState(false);
  const [lockerAvailable, setLockerAvailable] = useState(true);
  const [checkingAvailability, setCheckingAvailability] = useState(false);

  // Check locker availability on mount and periodically
  useEffect(() => {
    if (!locker) return;

    const checkAvailability = async () => {
      setCheckingAvailability(true);
      try {
        const response = await apiService.getAvailableLockers();
        const availableLockers = response.data;
        const isAvailable = availableLockers.some(l => l.id === locker.id);
        setLockerAvailable(isAvailable);

        if (!isAvailable) {
          setError('This locker has been booked by someone else. Please select another locker.');
        }
      } catch (err) {
        console.error('Failed to check availability:', err);
      } finally {
        setCheckingAvailability(false);
      }
    };

    // Check immediately
    checkAvailability();

    // Check every 30 seconds
    const interval = setInterval(checkAvailability, 30000);

    return () => clearInterval(interval);
  }, [locker]);

  // Format date for display (e.g., "Friday, January 10, 2026")
  const formatDisplayDate = (dateString) => {
    const date = new Date(dateString);
    return date.toLocaleDateString('en-US', {
      weekday: 'long',
      year: 'numeric',
      month: 'long',
      day: 'numeric',
    });
  };

  // Calculate price whenever times change
  useEffect(() => {
    if (!locker) return;

    if (!startTime || !endTime) {
      setTotalPrice(null);
      setError('');
      return;
    }

    // Validate that end time is after start time
    if (endTime <= startTime) {
      setTotalPrice(null);
      setError('End time must be after start time');
      return;
    }

    setError(''); // Clear any previous errors

    // Calculate duration in hours
    const [startHour, startMin] = startTime.split(':').map(Number);
    const [endHour, endMin] = endTime.split(':').map(Number);

    const startMinutes = startHour * 60 + startMin;
    const endMinutes = endHour * 60 + endMin;

    const durationMinutes = endMinutes - startMinutes;
    const hours = Math.max(0.5, durationMinutes / 60); // Minimum 0.5 hours

    const price = hours * locker.hourlyRate;

    setTotalPrice({ hours: hours.toFixed(1), price: price.toFixed(2) });
  }, [startTime, endTime, locker]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    // Pre-booking validation: Check if locker is still available
    try {
      const response = await apiService.getAvailableLockers();
      const availableLockers = response.data;
      const isStillAvailable = availableLockers.some(l => l.id === locker.id);

      if (!isStillAvailable) {
        setError('This locker is no longer available. Please select another locker.');
        setLockerAvailable(false);
        setLoading(false);
        return;
      }
    } catch (err) {
      setError('Failed to verify locker availability. Please try again.');
      setLoading(false);
      return;
    }

    // Proceed with booking if locker is available
    try {
      const bookingData = {
        lockerId: locker.id,
        startDatetime: `${selectedDate}T${startTime}`,
        endDatetime: `${selectedDate}T${endTime}`,
      };

      await apiService.createBooking(bookingData);
      setSuccess(true);

      setTimeout(() => {
        navigate('/my-bookings');
      }, 2000);
    } catch (err) {
      setError(
        err.response?.data?.message ||
          'Failed to create booking. Please try again.'
      );
    } finally {
      setLoading(false);
    }
  };

  if (!locker) {
    return (
      <div className="booking-wizard">
        <div className="error-card">
          <h2>No locker selected</h2>
          <button onClick={() => navigate('/')} className="btn-primary">
            Browse Lockers
          </button>
        </div>
      </div>
    );
  }

  if (success) {
    return (
      <div className="booking-wizard">
        <div className="success-card">
          <div className="success-icon">✓</div>
          <h2>Booking Confirmed!</h2>
          <p>Your locker has been successfully booked.</p>
          <p>Redirecting to your bookings...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="booking-wizard">
      <div className="booking-card">
        <h2>Book Locker #{locker.lockerNumber}</h2>

        <div className="locker-summary">
          <div className="summary-item">
            <strong>Size:</strong> {locker.size}
          </div>
          <div className="summary-item">
            <strong>Rate:</strong> ${locker.hourlyRate}/hour
          </div>
        </div>

        {/* Date Display Section */}
        <div className="date-display-section">
          <div className="date-display">
            <div className="date-icon">
              <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                <rect x="3" y="4" width="18" height="18" rx="2" ry="2"/>
                <line x1="16" y1="2" x2="16" y2="6"/>
                <line x1="8" y1="2" x2="8" y2="6"/>
                <line x1="3" y1="10" x2="21" y2="10"/>
              </svg>
            </div>
            <div className="date-info">
              <span className="date-label">Booking for:</span>
              <span className="date-value">{formatDisplayDate(selectedDate)}</span>
            </div>
          </div>

          <button
            type="button"
            className="change-date-btn"
            onClick={() => setShowDatePicker(!showDatePicker)}
          >
            {showDatePicker ? 'Hide calendar' : 'Change date?'}
          </button>

          {showDatePicker && (
            <div className="date-picker-inline">
              <label htmlFor="date-picker">Select a different date:</label>
              <input
                id="date-picker"
                type="date"
                value={selectedDate}
                onChange={(e) => {
                  setSelectedDate(e.target.value);
                  setShowDatePicker(false);
                }}
                min={new Date().toISOString().split('T')[0]}
                className="date-input"
              />
            </div>
          )}
        </div>

        {error && <div className="error-message">{error}</div>}

        {/* Time Selection Form */}
        <form onSubmit={handleSubmit}>
          <div className="time-selection-section">
            <h3 className="section-title">Select Time</h3>

            <TimeSelect
              label="Start Time"
              value={startTime}
              onChange={setStartTime}
            />

            <TimeSelect
              label="End Time"
              value={endTime}
              onChange={setEndTime}
              minTime={startTime}
              disabled={!startTime}
            />
          </div>

          {totalPrice && (
            <div className="price-summary">
              <div className="price-detail">
                <span>Duration:</span>
                <strong>{totalPrice.hours} hours</strong>
              </div>
              <div className="price-detail">
                <span>Rate:</span>
                <strong>${locker.hourlyRate}/hour</strong>
              </div>
              <div className="price-total">
                <span>Total Price:</span>
                <strong>${totalPrice.price}</strong>
              </div>
            </div>
          )}

          <div className="button-group">
            <button
              type="button"
              onClick={() => navigate('/')}
              className="btn-secondary"
            >
              Cancel
            </button>
            <button
              type="submit"
              className="btn-primary"
              disabled={loading || !totalPrice}
            >
              {loading ? 'Creating Booking...' : 'Confirm Booking'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default BookingWizard;

import React, { useState, useEffect, useCallback } from 'react';
import { useAuth } from '../context/AuthContext';
import { useWebSocket } from '../hooks/useWebSocket';
import apiService from '../services/api';
import './MyBookings.css';

const MyBookings = () => {
  const { user } = useAuth();
  const [bookings, setBookings] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const fetchBookings = async () => {
    try {
      const response = await apiService.getMyBookings();
      setBookings(response.data);
      setError('');
    } catch (err) {
      setError('Failed to load bookings');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (user) {
      fetchBookings();
    }
  }, [user]);

  // Handle WebSocket updates for booking changes
  const handleBookingUpdate = useCallback((event) => {
    console.log('Booking update received:', event);

    // Refresh bookings when there's an update
    fetchBookings();
  }, []);

  useWebSocket('/topic/bookings', handleBookingUpdate);

  const handleCancelBooking = async (bookingId) => {
    if (!window.confirm('Are you sure you want to cancel this booking?')) {
      return;
    }

    try {
      await apiService.cancelBooking(bookingId);
      // Refresh bookings list
      fetchBookings();
    } catch (err) {
      alert('Failed to cancel booking');
      console.error(err);
    }
  };

  const formatDateTime = (dateTimeString) => {
    const date = new Date(dateTimeString);
    return date.toLocaleString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  /**
   * Get relative time string (e.g., "in 2 hours", "3 hours ago")
   */
  const getRelativeTime = (dateTimeString) => {
    const date = new Date(dateTimeString);
    const now = new Date();
    const diffMs = date - now;
    const diffMins = Math.round(diffMs / 60000);
    const diffHours = Math.round(diffMs / 3600000);
    const diffDays = Math.round(diffMs / 86400000);

    if (diffMins < -1440) { // More than a day ago
      return `${Math.abs(diffDays)} day${Math.abs(diffDays) !== 1 ? 's' : ''} ago`;
    } else if (diffMins < -60) { // More than an hour ago
      return `${Math.abs(diffHours)} hour${Math.abs(diffHours) !== 1 ? 's' : ''} ago`;
    } else if (diffMins < 0) { // Past
      return `${Math.abs(diffMins)} min${Math.abs(diffMins) !== 1 ? 's' : ''} ago`;
    } else if (diffMins < 60) { // Within an hour
      return `in ${diffMins} min${diffMins !== 1 ? 's' : ''}`;
    } else if (diffMins < 1440) { // Within a day
      return `in ${diffHours} hour${diffHours !== 1 ? 's' : ''}`;
    } else { // More than a day away
      return `in ${diffDays} day${diffDays !== 1 ? 's' : ''}`;
    }
  };

  /**
   * Determine booking time status and appropriate badge
   */
  const getBookingTimeStatus = (booking) => {
    const now = new Date();
    const start = new Date(booking.startDatetime);
    const end = new Date(booking.endDatetime);

    // If already completed or cancelled, use that status
    if (booking.status === 'COMPLETED' || booking.status === 'CANCELLED') {
      return {
        label: booking.status,
        className: booking.status === 'COMPLETED' ? 'status-completed' : 'status-cancelled',
        timeInfo: null
      };
    }

    // For ACTIVE bookings, determine actual time state
    if (now < start) {
      return {
        label: 'UPCOMING',
        className: 'status-upcoming',
        timeInfo: `Starts ${getRelativeTime(booking.startDatetime)}`
      };
    } else if (now >= start && now <= end) {
      return {
        label: 'IN PROGRESS',
        className: 'status-in-progress',
        timeInfo: `Ends ${getRelativeTime(booking.endDatetime)}`
      };
    } else { // now > end
      return {
        label: 'EXPIRED',
        className: 'status-expired',
        timeInfo: `Ended ${getRelativeTime(booking.endDatetime)} • Awaiting auto-completion`
      };
    }
  };

  /**
   * Get booking card className based on time status
   */
  const getBookingCardClass = (booking) => {
    const timeStatus = getBookingTimeStatus(booking);
    return `booking-card ${timeStatus.className}`;
  };

  const getStatusClass = (status) => {
    switch (status) {
      case 'ACTIVE':
        return 'status-active';
      case 'COMPLETED':
        return 'status-completed';
      case 'CANCELLED':
        return 'status-cancelled';
      default:
        return '';
    }
  };

  if (loading) {
    return <div className="loading">Loading your bookings...</div>;
  }

  return (
    <div className="my-bookings">
      <div className="bookings-header">
        <h1>My Bookings</h1>
        <p className="subtitle">Manage your luggage storage reservations</p>
      </div>

      {error && <div className="error-message">{error}</div>}

      {bookings.length === 0 ? (
        <div className="no-bookings">
          <h3>No bookings yet</h3>
          <p>Start by browsing available lockers</p>
          <a href="/" className="btn-primary">
            Browse Lockers
          </a>
        </div>
      ) : (
        <div className="bookings-list">
          {bookings.map((booking) => {
            const timeStatus = getBookingTimeStatus(booking);

            return (
              <div key={booking.id} className={getBookingCardClass(booking)}>
                <div className="booking-header">
                  <div>
                    <h3>Locker #{booking.locker?.lockerNumber || 'N/A'}</h3>
                    <span className={`booking-status ${timeStatus.className}`}>
                      {timeStatus.label}
                    </span>
                  </div>
                  <div className="booking-price">${booking.totalPrice}</div>
                </div>

                {timeStatus.timeInfo && (
                  <div className="time-info">
                    {timeStatus.timeInfo}
                  </div>
                )}

                <div className="booking-details">
                  <div className="detail-row">
                    <div className="detail-item">
                      <strong>Size:</strong> {booking.locker?.size || 'N/A'}
                    </div>
                    <div className="detail-item">
                      <strong>Booking ID:</strong> #{booking.id}
                    </div>
                  </div>

                  <div className="detail-row">
                    <div className="detail-item">
                      <strong>Start:</strong> {formatDateTime(booking.startDatetime)}
                    </div>
                    <div className="detail-item">
                      <strong>End:</strong> {formatDateTime(booking.endDatetime)}
                    </div>
                  </div>
                </div>

                {booking.status === 'ACTIVE' && (
                  <div className="booking-actions">
                    <button
                      onClick={() => handleCancelBooking(booking.id)}
                      className="btn-cancel"
                    >
                      Cancel Booking
                    </button>
                  </div>
                )}
              </div>
            );
          })}
        </div>
      )}
    </div>
  );
};

export default MyBookings;

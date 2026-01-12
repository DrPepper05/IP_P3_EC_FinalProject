import React, { useState, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import apiService from '../services/api';
import { useWebSocket } from '../hooks/useWebSocket';
import { useAuth } from '../context/AuthContext';
import './LockerBrowser.css';

const LockerBrowser = () => {
  const [lockers, setLockers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [filter, setFilter] = useState('all'); // all, SMALL, MEDIUM, LARGE
  const [wsConnected, setWsConnected] = useState(false);
  const [lastUpdate, setLastUpdate] = useState(Date.now());

  const navigate = useNavigate();
  const { isAuthenticated } = useAuth();

  // Fetch lockers with silent refresh option
  const fetchLockers = async (silent = false) => {
    try {
      if (!silent) setLoading(true);
      const response = await apiService.getAvailableLockers();

      // Update lockers list - this will properly remove any that are no longer available
      setLockers(response.data);
      setLastUpdate(Date.now());
      setError('');
    } catch (err) {
      if (!silent) {
        setError('Failed to load lockers');
        console.error(err);
      }
    } finally {
      if (!silent) setLoading(false);
    }
  };

  // Initial load
  useEffect(() => {
    fetchLockers();
  }, []);

  // Fallback polling - refresh every 10 seconds to catch any missed WebSocket updates
  useEffect(() => {
    // Only poll if WebSocket is not connected or as a safety net
    const pollInterval = setInterval(() => {
      // Silent refresh to avoid loading spinner
      fetchLockers(true);
    }, 10000); // Poll every 10 seconds

    return () => clearInterval(pollInterval);
  }, []);

  // Handle WebSocket updates for locker availability
  const handleLockerUpdate = useCallback((event) => {
    console.log('Locker update received:', event);
    setWsConnected(true);

    // If locker becomes OCCUPIED, remove it from available list
    // If locker becomes AVAILABLE, fetch fresh data to add it
    if (event.status === 'OCCUPIED') {
      // Remove the locker from the list since it's no longer available
      setLockers((prevLockers) =>
        prevLockers.filter((locker) => locker.id !== event.lockerId)
      );
    } else if (event.status === 'AVAILABLE') {
      // Fetch fresh data to get the newly available locker
      // This ensures we have the complete locker data
      fetchLockers(true);
    }

    // Update last update timestamp
    setLastUpdate(Date.now());

    // Show notification
    showNotification(event.message);
  }, []);

  // Connect to WebSocket for real-time updates
  useWebSocket('/topic/lockers', handleLockerUpdate);

  const showNotification = (message) => {
    // Simple notification (you can enhance with toast library)
    console.log('Notification:', message);
  };

  const handleBookLocker = (locker) => {
    if (!isAuthenticated) {
      navigate('/login');
      return;
    }
    navigate('/book', { state: { locker } });
  };

  const filteredLockers = filter === 'all'
    ? lockers
    : lockers.filter((l) => l.size === filter);

  const availableCount = lockers.filter((l) => l.status === 'AVAILABLE').length;

  if (loading) {
    return <div className="loading">Loading lockers...</div>;
  }

  return (
    <div className="locker-browser">
      <div className="browser-header">
        <h1>Available Lockers</h1>
        <p className="subtitle">
          {availableCount} of {lockers.length} lockers available
          {wsConnected && <span className="live-indicator"> • LIVE</span>}
          <span className="last-update"> • Updated {Math.floor((Date.now() - lastUpdate) / 1000)}s ago</span>
        </p>
      </div>

      {error && <div className="error-message">{error}</div>}

      <div className="filter-bar">
        <button
          className={filter === 'all' ? 'filter-btn active' : 'filter-btn'}
          onClick={() => setFilter('all')}
        >
          All Sizes
        </button>
        <button
          className={filter === 'SMALL' ? 'filter-btn active' : 'filter-btn'}
          onClick={() => setFilter('SMALL')}
        >
          Small
        </button>
        <button
          className={filter === 'MEDIUM' ? 'filter-btn active' : 'filter-btn'}
          onClick={() => setFilter('MEDIUM')}
        >
          Medium
        </button>
        <button
          className={filter === 'LARGE' ? 'filter-btn active' : 'filter-btn'}
          onClick={() => setFilter('LARGE')}
        >
          Large
        </button>
      </div>

      <div className="locker-grid">
        {filteredLockers.length === 0 ? (
          <div className="no-lockers">No lockers available with selected filter</div>
        ) : (
          filteredLockers.map((locker) => (
            <div
              key={locker.id}
              className={`locker-card ${
                locker.status === 'AVAILABLE' ? 'available' : 'occupied'
              }`}
            >
              <div className="locker-header">
                <h3>Locker #{locker.lockerNumber}</h3>
                <span className={`status-badge ${locker.status.toLowerCase()}`}>
                  {locker.status}
                </span>
              </div>

              <div className="locker-details">
                <div className="detail-item">
                  <strong>Size:</strong> {locker.size}
                </div>
                <div className="detail-item">
                  <strong>Hourly Rate:</strong> ${locker.hourlyRate}/hr
                </div>
                {locker.dailyRate && (
                  <div className="detail-item">
                    <strong>Daily Rate:</strong> ${locker.dailyRate}/day
                  </div>
                )}
              </div>

              {locker.status === 'AVAILABLE' && (
                <button
                  className="btn-book"
                  onClick={() => handleBookLocker(locker)}
                >
                  Book Now
                </button>
              )}
              {locker.status === 'OCCUPIED' && (
                <div className="occupied-message">Currently in use</div>
              )}
            </div>
          ))
        )}
      </div>
    </div>
  );
};

export default LockerBrowser;

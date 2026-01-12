import React, { useState, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import apiService from '../services/api';
import { useWebSocket } from '../hooks/useWebSocket';
import LockerGrid from './LockerGrid';
import LockerMap from './LockerMap';
import './Dashboard.css';

const Dashboard = () => {
  const [lockers, setLockers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [viewMode, setViewMode] = useState('map'); // 'map' or 'grid'
  const [wsConnected, setWsConnected] = useState(false);

  const navigate = useNavigate();

  // Fetch all lockers
  const fetchLockers = async () => {
    try {
      const response = await apiService.getAllLockers();
      setLockers(response.data);
      setError('');
    } catch (err) {
      setError('Failed to load locker statistics');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchLockers();
  }, []);

  // Handle WebSocket updates for locker changes
  const handleLockerUpdate = useCallback((event) => {
    console.log('Locker update received:', event);
    setWsConnected(true);

    // Update locker in the list
    setLockers((prevLockers) =>
      prevLockers.map((locker) =>
        locker.id === event.lockerId
          ? { ...locker, status: event.status }
          : locker
      )
    );
  }, []);

  // Connect to WebSocket for real-time updates
  useWebSocket('/topic/lockers', handleLockerUpdate);

  // Calculate statistics
  const calculateStatistics = () => {
    const stats = {
      total: lockers.length,
      available: lockers.filter((l) => l.status === 'AVAILABLE').length,
      occupied: lockers.filter((l) => l.status === 'OCCUPIED').length,
      reserved: lockers.filter((l) => l.status === 'RESERVED').length,
      maintenance: lockers.filter((l) => l.status === 'MAINTENANCE').length,
    };
    stats.availabilityRate = stats.total > 0 ? ((stats.available / stats.total) * 100).toFixed(1) : 0;
    return stats;
  };

  // Get location statistics
  const getLocationSummary = () => {
    const locationMap = {};

    lockers.forEach((locker) => {
      const location = locker.location || 'Unknown';
      if (!locationMap[location]) {
        locationMap[location] = {
          name: location,
          total: 0,
          available: 0,
        };
      }
      locationMap[location].total += 1;
      if (locker.status === 'AVAILABLE') {
        locationMap[location].available += 1;
      }
    });

    return Object.values(locationMap).sort((a, b) => a.name.localeCompare(b.name));
  };

  const statistics = calculateStatistics();
  const locationSummary = getLocationSummary();

  if (loading) {
    return <div className="loading">Loading dashboard...</div>;
  }

  return (
    <div className="dashboard">
      {/* Header Section */}
      <div className="dashboard-header">
        <div className="header-content">
          <h1>Storage Dashboard</h1>
          <p className="subtitle">
            Real-time locker availability and management
            {wsConnected && <span className="live-indicator"> LIVE</span>}
          </p>
        </div>
      </div>

      {/* Error Message */}
      {error && <div className="error-message">{error}</div>}

      {/* Statistics Panel */}
      <div className="statistics-panel">
        <div className="stat-card">
          <div className="stat-header">
            <h3>Total Lockers</h3>
            <div className="stat-icon">
              <i className="icon-box"></i>
            </div>
          </div>
          <div className="stat-value">{statistics.total}</div>
          <div className="stat-footer">All lockers in system</div>
        </div>

        <div className="stat-card available">
          <div className="stat-header">
            <h3>Available</h3>
            <div className="stat-icon">
              <i className="icon-check"></i>
            </div>
          </div>
          <div className="stat-value">{statistics.available}</div>
          <div className="stat-footer">Ready to book</div>
        </div>

        <div className="stat-card occupied">
          <div className="stat-header">
            <h3>Occupied</h3>
            <div className="stat-icon">
              <i className="icon-user"></i>
            </div>
          </div>
          <div className="stat-value">{statistics.occupied}</div>
          <div className="stat-footer">Currently in use</div>
        </div>

        <div className="stat-card reserved">
          <div className="stat-header">
            <h3>Reserved</h3>
            <div className="stat-icon">
              <i className="icon-calendar"></i>
            </div>
          </div>
          <div className="stat-value">{statistics.reserved}</div>
          <div className="stat-footer">Upcoming bookings</div>
        </div>

        <div className="stat-card maintenance">
          <div className="stat-header">
            <h3>Maintenance</h3>
            <div className="stat-icon">
              <i className="icon-wrench"></i>
            </div>
          </div>
          <div className="stat-value">{statistics.maintenance}</div>
          <div className="stat-footer">Under service</div>
        </div>

        <div className="stat-card rate">
          <div className="stat-header">
            <h3>Availability Rate</h3>
            <div className="stat-icon">
              <i className="icon-percent"></i>
            </div>
          </div>
          <div className="stat-value">{statistics.availabilityRate}%</div>
          <div className="stat-footer">
            <div className="availability-progress">
              <div
                className="progress-bar"
                style={{ width: `${statistics.availabilityRate}%` }}
              ></div>
            </div>
          </div>
        </div>
      </div>

      {/* Location Summary */}
      {locationSummary.length > 0 && (
        <div className="location-summary-section">
          <h2>Location Summary</h2>
          <div className="location-cards">
            {locationSummary.map((location) => {
              const availabilityPercentage = location.total > 0
                ? ((location.available / location.total) * 100).toFixed(0)
                : 0;
              return (
                <div key={location.name} className="location-card">
                  <div className="location-header">
                    <h3>{location.name}</h3>
                    <span className="location-count">
                      {location.available}/{location.total}
                    </span>
                  </div>
                  <div className="location-progress">
                    <div className="progress-bar-container">
                      <div
                        className="progress-bar"
                        style={{ width: `${availabilityPercentage}%` }}
                      ></div>
                    </div>
                    <p className="progress-text">
                      {availabilityPercentage}% available
                    </p>
                  </div>
                </div>
              );
            })}
          </div>
        </div>
      )}

      {/* View Toggle and Locker Management */}
      <div className="view-section">
        <div className="view-toggle">
          <h2>Lockers</h2>
          <div className="toggle-buttons">
            <button
              className={`toggle-btn ${viewMode === 'map' ? 'active' : ''}`}
              onClick={() => setViewMode('map')}
              title="Map View"
            >
              <span className="toggle-icon">🗺</span> Map View
            </button>
            <button
              className={`toggle-btn ${viewMode === 'grid' ? 'active' : ''}`}
              onClick={() => setViewMode('grid')}
              title="Grid View"
            >
              <span className="toggle-icon">⊞</span> Grid View
            </button>
          </div>
        </div>

        {/* View Content */}
        <div className="view-content">
          {viewMode === 'map' ? (
            <LockerMap
              lockers={lockers}
              onSelectLocker={(locker) => {
                navigate('/book', { state: { locker } });
              }}
              onBookingRequest={(locker) => {
                navigate('/book', { state: { locker } });
              }}
            />
          ) : (
            <LockerGrid lockers={lockers} onSelectLocker={(locker) => {
              navigate('/book', { state: { locker } });
            }} />
          )}
        </div>
      </div>
    </div>
  );
};

export default Dashboard;

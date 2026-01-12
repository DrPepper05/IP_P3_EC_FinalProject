import React, { useState } from 'react';
import { useAuth } from '../context/AuthContext';
import './LockerGrid.css';

const LockerGrid = ({ lockers, onSelectLocker }) => {
  const [filter, setFilter] = useState('all'); // all, SMALL, MEDIUM, LARGE, AVAILABLE, OCCUPIED, RESERVED
  const [sortBy, setSortBy] = useState('number'); // number, size, price
  const { isAuthenticated } = useAuth();

  // Filter lockers
  const filteredLockers = lockers.filter((locker) => {
    if (filter === 'all') return true;
    if (filter === 'AVAILABLE') return locker.status === 'AVAILABLE';
    if (filter === 'OCCUPIED') return locker.status === 'OCCUPIED';
    if (filter === 'RESERVED') return locker.status === 'RESERVED';
    return locker.size === filter;
  });

  // Sort lockers
  const sortedLockers = [...filteredLockers].sort((a, b) => {
    if (sortBy === 'number') {
      return (a.lockerNumber || 0) - (b.lockerNumber || 0);
    }
    if (sortBy === 'size') {
      const sizeOrder = { SMALL: 1, MEDIUM: 2, LARGE: 3 };
      return (sizeOrder[a.size] || 0) - (sizeOrder[b.size] || 0);
    }
    if (sortBy === 'price') {
      return (a.hourlyRate || 0) - (b.hourlyRate || 0);
    }
    return 0;
  });

  const handleBookLocker = (locker) => {
    if (!isAuthenticated) {
      alert('Please log in to book a locker');
      return;
    }
    if (locker.status !== 'AVAILABLE') {
      alert('This locker is not available for booking');
      return;
    }
    onSelectLocker(locker);
  };

  const getStatusColor = (status) => {
    const colors = {
      AVAILABLE: '#4CAF50',
      OCCUPIED: '#ff9800',
      RESERVED: '#2196F3',
      MAINTENANCE: '#f44336',
    };
    return colors[status] || '#999';
  };

  const getStatusLabel = (status) => {
    const labels = {
      AVAILABLE: 'Available',
      OCCUPIED: 'Occupied',
      RESERVED: 'Reserved',
      MAINTENANCE: 'Maintenance',
    };
    return labels[status] || status;
  };

  return (
    <div className="locker-grid-container">
      {/* Filter and Sort Bar */}
      <div className="grid-controls">
        <div className="filter-group">
          <label htmlFor="size-filter">Size:</label>
          <select
            id="size-filter"
            value={filter}
            onChange={(e) => setFilter(e.target.value)}
            className="filter-select"
          >
            <option value="all">All Sizes</option>
            <option value="SMALL">Small</option>
            <option value="MEDIUM">Medium</option>
            <option value="LARGE">Large</option>
          </select>
        </div>

        <div className="filter-group">
          <label htmlFor="status-filter">Status:</label>
          <select
            id="status-filter"
            value={filter}
            onChange={(e) => setFilter(e.target.value)}
            className="filter-select"
          >
            <option value="all">All Status</option>
            <option value="AVAILABLE">Available</option>
            <option value="OCCUPIED">Occupied</option>
            <option value="RESERVED">Reserved</option>
          </select>
        </div>

        <div className="filter-group">
          <label htmlFor="sort-select">Sort by:</label>
          <select
            id="sort-select"
            value={sortBy}
            onChange={(e) => setSortBy(e.target.value)}
            className="filter-select"
          >
            <option value="number">Locker Number</option>
            <option value="size">Size</option>
            <option value="price">Price (Low to High)</option>
          </select>
        </div>

        <div className="filter-info">
          Showing {sortedLockers.length} of {lockers.length} lockers
        </div>
      </div>

      {/* Locker Grid */}
      {sortedLockers.length === 0 ? (
        <div className="no-results">
          <p>No lockers found matching your filters</p>
        </div>
      ) : (
        <div className="locker-items">
          {sortedLockers.map((locker) => (
            <div
              key={locker.id}
              className={`locker-item ${locker.status.toLowerCase()}`}
            >
              {/* Status Badge */}
              <div className="item-status">
                <span
                  className="status-badge"
                  style={{ backgroundColor: getStatusColor(locker.status) }}
                >
                  {getStatusLabel(locker.status)}
                </span>
              </div>

              {/* Locker Number */}
              <div className="item-header">
                <h3 className="locker-number">
                  Locker #{locker.lockerNumber || 'N/A'}
                </h3>
              </div>

              {/* Details */}
              <div className="item-details">
                <div className="detail-row">
                  <span className="detail-label">Size:</span>
                  <span className="detail-value">{locker.size || 'N/A'}</span>
                </div>

                <div className="detail-row">
                  <span className="detail-label">Location:</span>
                  <span className="detail-value">{locker.location || 'N/A'}</span>
                </div>

                <div className="detail-row">
                  <span className="detail-label">Hourly Rate:</span>
                  <span className="detail-value price">
                    ${locker.hourlyRate || '0'}/hr
                  </span>
                </div>

                {locker.dailyRate && (
                  <div className="detail-row">
                    <span className="detail-label">Daily Rate:</span>
                    <span className="detail-value price">
                      ${locker.dailyRate}/day
                    </span>
                  </div>
                )}

                {locker.monthlyRate && (
                  <div className="detail-row">
                    <span className="detail-label">Monthly Rate:</span>
                    <span className="detail-value price">
                      ${locker.monthlyRate}/month
                    </span>
                  </div>
                )}
              </div>

              {/* Features */}
              {locker.features && locker.features.length > 0 && (
                <div className="item-features">
                  {locker.features.map((feature, idx) => (
                    <span key={idx} className="feature-tag">
                      {feature}
                    </span>
                  ))}
                </div>
              )}

              {/* Action Button */}
              <div className="item-action">
                {locker.status === 'AVAILABLE' ? (
                  <button
                    className="btn-book"
                    onClick={() => handleBookLocker(locker)}
                  >
                    Book Now
                  </button>
                ) : (
                  <button className="btn-unavailable" disabled>
                    Unavailable
                  </button>
                )}
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};

export default LockerGrid;

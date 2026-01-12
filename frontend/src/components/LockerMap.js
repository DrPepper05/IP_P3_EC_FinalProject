import React, { useState, useMemo, useCallback } from 'react';
import { MapContainer, TileLayer, Marker, Popup } from 'react-leaflet';
import L from 'leaflet';
import 'leaflet/dist/leaflet.css';
import './LockerMap.css';

// Function to calculate bubble size based on count
const getCountBasedSize = (count) => {
  if (count <= 2) return 0.8;      // Small bubble
  if (count <= 5) return 1.0;      // Medium bubble
  if (count <= 10) return 1.3;     // Large bubble
  return 1.6;                       // Extra large bubble for 11+ lockers
};

// Custom marker icons showing available locker count
const createMarkerIcon = (availableCount) => {
  // Use Timișoara yellow for all markers since they're all available
  const color = '#FFD100';

  // Dynamic size based on number of available lockers
  const sizeMultiplier = getCountBasedSize(availableCount);

  // Scale font size with bubble size, but ensure it remains readable
  const fontSize = Math.max(12, Math.min(18, 10 + sizeMultiplier * 4));

  return L.divIcon({
    className: 'custom-marker marker-available',
    html: `
      <div style="position: relative;">
        <div style="
          background-color: ${color};
          width: ${30 * sizeMultiplier}px;
          height: ${30 * sizeMultiplier}px;
          border-radius: 50%;
          border: 2px solid rgba(255, 255, 255, 0.9);
          display: flex;
          align-items: center;
          justify-content: center;
          font-weight: bold;
          color: #1a202c;
          font-size: ${fontSize}px;
          box-shadow: 0 3px 10px rgba(0,0,0,0.3);
          cursor: pointer;
        ">
          ${availableCount}
        </div>
      </div>
    `,
    iconSize: [30 * sizeMultiplier, 30 * sizeMultiplier],
  });
};


// Map legend component
const MapLegend = ({ sizeFilter, onSizeFilterChange, totalAvailable }) => {
  const sizes = [
    { key: 'SMALL', label: 'Small', icon: 'S' },
    { key: 'MEDIUM', label: 'Medium', icon: 'M' },
    { key: 'LARGE', label: 'Large', icon: 'L' },
  ];

  return (
    <div className="map-legend">
      <h3>Available Storage</h3>

      <div className="legend-info">
        <p className="legend-description">
          Numbers show available lockers at each location
        </p>
        {totalAvailable > 0 && (
          <div className="total-available">
            Total Available: <strong>{totalAvailable}</strong>
          </div>
        )}
      </div>

      <div className="legend-section">
        <h4>Filter by Size</h4>
        {sizes.map((size) => (
          <label key={size.key} className="legend-item">
            <input
              type="checkbox"
              checked={sizeFilter.includes(size.key)}
              onChange={(e) => {
                if (e.target.checked) {
                  onSizeFilterChange([...sizeFilter, size.key]);
                } else {
                  onSizeFilterChange(sizeFilter.filter((s) => s !== size.key));
                }
              }}
            />
            <div
              className="legend-size-icon"
              style={{
                width: size.key === 'SMALL' ? '16px' : size.key === 'LARGE' ? '26px' : '21px',
                height: size.key === 'SMALL' ? '16px' : size.key === 'LARGE' ? '26px' : '21px',
              }}
            >
              {size.icon}
            </div>
            {size.label}
          </label>
        ))}
      </div>

      <div className="legend-section">
        <h4>Bubble Size Guide</h4>
        <div className="bubble-size-guide">
          <div className="bubble-example">
            <div className="bubble-icon small-bubble">2</div>
            <span>1-2 lockers</span>
          </div>
          <div className="bubble-example">
            <div className="bubble-icon medium-bubble">5</div>
            <span>3-5 lockers</span>
          </div>
          <div className="bubble-example">
            <div className="bubble-icon large-bubble">10</div>
            <span>6-10 lockers</span>
          </div>
          <div className="bubble-example">
            <div className="bubble-icon xlarge-bubble">15</div>
            <span>11+ lockers</span>
          </div>
        </div>
      </div>
    </div>
  );
};

// Map controls component
const MapControls = ({ selectedLocker, onBooking }) => {
  if (!selectedLocker) {
    return null;
  }

  return (
    <div className="map-controls">
      <div className="control-header">
        <h3>Selected Locker</h3>
        <div className={`status-badge ${selectedLocker.status.toLowerCase()}`}>
          {selectedLocker.status}
        </div>
      </div>

      <div className="control-details">
        <div className="detail-row">
          <span className="detail-label">Locker Number:</span>
          <span className="detail-value">#{selectedLocker.lockerNumber}</span>
        </div>

        <div className="detail-row">
          <span className="detail-label">Size:</span>
          <span className="detail-value">{selectedLocker.size}</span>
        </div>

        <div className="detail-row">
          <span className="detail-label">Hourly Rate:</span>
          <span className="detail-value">${selectedLocker.hourlyRate}/hr</span>
        </div>

        {selectedLocker.dailyRate && (
          <div className="detail-row">
            <span className="detail-label">Daily Rate:</span>
            <span className="detail-value">${selectedLocker.dailyRate}/day</span>
          </div>
        )}

        {selectedLocker.status === 'AVAILABLE' && (
          <button className="btn-book-map" onClick={() => onBooking(selectedLocker)}>
            Book Now
          </button>
        )}

        {selectedLocker.status !== 'AVAILABLE' && (
          <div className="unavailable-message">
            This locker is not available for booking
          </div>
        )}
      </div>
    </div>
  );
};

// Main LockerMap component
const LockerMap = ({ lockers = [], onLockerSelect, onBookingRequest }) => {
  const [selectedLocker, setSelectedLocker] = useState(null);
  const [sizeFilter, setSizeFilter] = useState(['SMALL', 'MEDIUM', 'LARGE']);

  // Timișoara coordinates
  const TIMISOARA_CENTER = [45.7489, 21.2087];
  const DEFAULT_ZOOM = 14;

  const handleMarkerClick = useCallback(
    (locker) => {
      setSelectedLocker(locker);
      if (onLockerSelect) {
        onLockerSelect(locker);
      }
    },
    [onLockerSelect]
  );

  const handleBooking = useCallback(
    (locker) => {
      if (onBookingRequest) {
        onBookingRequest(locker);
      }
    },
    [onBookingRequest]
  );

  // Mock locker data with coordinates around Timișoara
  const lockersWithCoordinates = useMemo(() => {
    if (lockers.length === 0) {
      // Default mock data for demonstration
      return [
        {
          id: 1,
          lockerNumber: 101,
          status: 'AVAILABLE',
          size: 'SMALL',
          hourlyRate: 2.5,
          dailyRate: 15,
          latitude: 45.7489,
          longitude: 21.2087,
        },
        {
          id: 2,
          lockerNumber: 102,
          status: 'OCCUPIED',
          size: 'MEDIUM',
          hourlyRate: 4,
          dailyRate: 20,
          latitude: 45.7519,
          longitude: 21.2117,
        },
        {
          id: 3,
          lockerNumber: 103,
          status: 'AVAILABLE',
          size: 'LARGE',
          hourlyRate: 6,
          dailyRate: 30,
          latitude: 45.7459,
          longitude: 21.2057,
        },
        {
          id: 4,
          lockerNumber: 104,
          status: 'AVAILABLE',
          size: 'SMALL',
          hourlyRate: 2.5,
          dailyRate: 15,
          latitude: 45.7439,
          longitude: 21.2107,
        },
        {
          id: 5,
          lockerNumber: 105,
          status: 'MAINTENANCE',
          size: 'MEDIUM',
          hourlyRate: 4,
          dailyRate: 20,
          latitude: 45.7509,
          longitude: 21.2037,
        },
      ];
    }

    // Add mock coordinates if not present (for real data)
    return lockers.map((locker, index) => ({
      ...locker,
      latitude: locker.latitude || (45.7489 + (Math.random() - 0.5) * 0.01),
      longitude: locker.longitude || (21.2087 + (Math.random() - 0.5) * 0.01),
    }));
  }, [lockers]);

  // Group lockers by location (same coordinates) and apply size filter
  const groupedLockers = useMemo(() => {
    // First group all lockers by location
    const groups = {};
    lockersWithCoordinates.forEach((locker) => {
      const key = `${locker.latitude}_${locker.longitude}`;
      if (!groups[key]) {
        groups[key] = {
          id: `group_${key}`,
          locationName: locker.locationName || locker.address || 'Unknown Location',
          latitude: locker.latitude,
          longitude: locker.longitude,
          lockers: [],
          filteredLockers: [],
        };
      }
      groups[key].lockers.push(locker);

      // Also track filtered lockers for count display
      if (sizeFilter.includes(locker.size)) {
        groups[key].filteredLockers.push(locker);
      }
    });

    // Convert to array and calculate available counts
    return Object.values(groups)
      .filter(group => group.filteredLockers.length > 0) // Only show locations with matching sizes
      .map((group) => ({
        ...group,
        availableCount: group.filteredLockers.length,
      }));
  }, [lockersWithCoordinates, sizeFilter]);

  // Calculate total available lockers (based on current filter)
  const totalAvailable = useMemo(() => {
    return groupedLockers.reduce((sum, group) => sum + group.availableCount, 0);
  }, [groupedLockers]);

  return (
    <div className="locker-map-container">
      {/* Map Card - Left Side */}
      <div className="map-card">
        <div className="map-wrapper">
          <MapContainer
            center={TIMISOARA_CENTER}
            zoom={DEFAULT_ZOOM}
            scrollWheelZoom={true}
            className="leaflet-container-custom"
          >
            <TileLayer
              attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors &copy; <a href="https://carto.com/attributions">CARTO</a>'
              url="https://{s}.basemaps.cartocdn.com/light_all/{z}/{x}/{y}{r}.png"
              subdomains="abcd"
              maxZoom={20}
            />

            {groupedLockers.map((group) => (
              <Marker
                key={group.id}
                position={[group.latitude, group.longitude]}
                icon={createMarkerIcon(group.availableCount)}
                eventHandlers={{
                  click: () => {
                    // If only one locker, handle like before
                    if (group.filteredLockers.length === 1) {
                      handleMarkerClick(group.filteredLockers[0]);
                    }
                  },
                }}
              >
                <Popup className="locker-popup" maxHeight={300}>
                  <div className="popup-content">
                    <div className="popup-header">
                      <h4>{group.locationName}</h4>
                      <span className="locker-count-badge">
                        {group.availableCount} locker{group.availableCount > 1 ? 's' : ''} available
                      </span>
                    </div>

                    <div className="popup-lockers-list" style={{ maxHeight: '200px', overflowY: 'auto' }}>
                      {group.filteredLockers.map((locker) => (
                        <div key={locker.id} className="locker-item" style={{
                          borderBottom: '1px solid #eee',
                          padding: '8px 0',
                          marginBottom: '8px'
                        }}>
                          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                            <div>
                              <strong>#{locker.lockerNumber}</strong>
                              <span style={{ marginLeft: '10px', color: '#666' }}>
                                {locker.size} - ${locker.hourlyRate}/hr
                              </span>
                              {locker.floor && (
                                <div style={{ fontSize: '12px', color: '#999' }}>
                                  {locker.floor} - {locker.section}
                                </div>
                              )}
                            </div>
                            {locker.status === 'AVAILABLE' && (
                              <button
                                className="popup-btn-book"
                                style={{
                                  padding: '4px 8px',
                                  fontSize: '12px',
                                  marginLeft: '10px'
                                }}
                                onClick={() => handleBooking(locker)}
                              >
                                Book
                              </button>
                            )}
                          </div>
                        </div>
                      ))}
                    </div>
                  </div>
                </Popup>
              </Marker>
            ))}
          </MapContainer>
        </div>
      </div>

      {/* Legend Card - Right Side */}
      <div className="legend-card">
        <MapLegend
          sizeFilter={sizeFilter}
          onSizeFilterChange={setSizeFilter}
          totalAvailable={totalAvailable}
        />

        {selectedLocker && (
          <MapControls
            selectedLocker={selectedLocker}
            onBooking={handleBooking}
          />
        )}
      </div>
    </div>
  );
};

export default LockerMap;

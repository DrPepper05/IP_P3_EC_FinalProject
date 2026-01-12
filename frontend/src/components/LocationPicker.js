import React, { useState, useRef, useCallback, useEffect } from 'react';
import { MapContainer, TileLayer, Marker, useMapEvents } from 'react-leaflet';
import L from 'leaflet';
import 'leaflet/dist/leaflet.css';
import './LocationPicker.css';

// Custom yellow marker icon
const yellowMarkerIcon = L.divIcon({
  className: 'location-picker-marker',
  html: `
    <div style="
      background-color: #FFD100;
      width: 30px;
      height: 30px;
      border-radius: 50%;
      border: 3px solid white;
      display: flex;
      align-items: center;
      justify-content: center;
      font-weight: bold;
      color: #1a202c;
      font-size: 16px;
      box-shadow: 0 2px 8px rgba(0,0,0,0.3);
      cursor: move;
    ">
      📍
    </div>
  `,
  iconSize: [30, 30],
  iconAnchor: [15, 15],
});

// Draggable marker component
const DraggableMarker = ({ position, onPositionChange }) => {
  const markerRef = useRef(null);

  const eventHandlers = {
    dragend() {
      const marker = markerRef.current;
      if (marker != null) {
        const newPos = marker.getLatLng();
        onPositionChange(newPos.lat, newPos.lng);
      }
    },
  };

  return (
    <Marker
      draggable={true}
      eventHandlers={eventHandlers}
      position={position}
      ref={markerRef}
      icon={yellowMarkerIcon}
    />
  );
};

// Map click handler component
const MapClickHandler = ({ onMapClick }) => {
  useMapEvents({
    click(e) {
      onMapClick(e.latlng.lat, e.latlng.lng);
    },
  });
  return null;
};

const LocationPicker = ({
  initialLatitude,
  initialLongitude,
  initialAddress,
  onChange
}) => {
  // Default to Timișoara center
  const TIMISOARA_CENTER = { lat: 45.7489, lng: 21.2087 };

  const [address, setAddress] = useState(initialAddress || '');
  const [position, setPosition] = useState({
    lat: initialLatitude || TIMISOARA_CENTER.lat,
    lng: initialLongitude || TIMISOARA_CENTER.lng,
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [mapKey, setMapKey] = useState(0); // Force map re-render when position changes significantly

  const mapRef = useRef(null);
  const lastGeocodingTime = useRef(0);

  // Notify parent of changes
  useEffect(() => {
    if (onChange) {
      onChange(position.lat, position.lng, address);
    }
  }, [position.lat, position.lng, address, onChange]);

  // Auto-populate address on mount if coordinates exist but address is empty
  useEffect(() => {
    if (!address && position.lat && position.lng) {
      reverseGeocode(position.lat, position.lng);
    }
  }, []); // Run only once on mount

  // Debounce geocoding requests (Nominatim rate limit: 1 request/second)
  const canMakeRequest = () => {
    const now = Date.now();
    if (now - lastGeocodingTime.current < 1000) {
      return false;
    }
    lastGeocodingTime.current = now;
    return true;
  };

  // Forward geocoding: address → coordinates
  const handleAddressSearch = async () => {
    if (!address || address.trim() === '') {
      setError('Please enter an address');
      return;
    }

    if (!canMakeRequest()) {
      setError('Please wait a moment before searching again');
      return;
    }

    setLoading(true);
    setError('');

    try {
      const response = await fetch(
        `https://nominatim.openstreetmap.org/search?format=json&q=${encodeURIComponent(address)}&countrycodes=ro&limit=1`,
        {
          headers: {
            'User-Agent': 'LuggageStorageSystem/1.0'
          }
        }
      );

      const data = await response.json();

      if (data && data.length > 0) {
        const newLat = parseFloat(data[0].lat);
        const newLng = parseFloat(data[0].lon);

        // Validate coordinates are in Romania
        if (newLat >= 43 && newLat <= 48 && newLng >= 20 && newLng <= 29) {
          setPosition({ lat: newLat, lng: newLng });
          setAddress(data[0].display_name || address);
          setMapKey(prev => prev + 1); // Force map to recenter
        } else {
          setError('Location not found in Romania');
        }
      } else {
        setError('Address not found. Please try a different search.');
      }
    } catch (err) {
      console.error('Geocoding error:', err);
      setError('Failed to search address. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  // Reverse geocoding: coordinates → address
  const reverseGeocode = async (lat, lng) => {
    if (!canMakeRequest()) {
      return; // Silently skip if rate limited
    }

    try {
      const response = await fetch(
        `https://nominatim.openstreetmap.org/reverse?format=json&lat=${lat}&lon=${lng}`,
        {
          headers: {
            'User-Agent': 'LuggageStorageSystem/1.0'
          }
        }
      );

      const data = await response.json();

      if (data && data.display_name) {
        setAddress(data.display_name);
      }
    } catch (err) {
      console.error('Reverse geocoding error:', err);
      // Don't show error to user, just keep existing address
    }
  };

  // Handle marker drag
  const handleMarkerDrag = useCallback((lat, lng) => {
    setPosition({ lat, lng });
    setError('');
    reverseGeocode(lat, lng);
  }, []);

  // Handle map click
  const handleMapClick = useCallback((lat, lng) => {
    setPosition({ lat, lng });
    setError('');
    reverseGeocode(lat, lng);
  }, []);

  // Handle address input enter key
  const handleKeyPress = (e) => {
    if (e.key === 'Enter') {
      e.preventDefault();
      handleAddressSearch();
    }
  };

  return (
    <div className="location-picker">
      <div className="location-picker-header">
        <h4>📍 Location Picker</h4>
        <p className="location-picker-hint">
          Search for an address or click/drag the marker on the map
        </p>
      </div>

      {/* Address Search Bar */}
      <div className="address-search-bar">
        <input
          type="text"
          placeholder="Enter address (e.g., Piața Victoriei, Timișoara)"
          value={address}
          onChange={(e) => setAddress(e.target.value)}
          onKeyPress={handleKeyPress}
          className="address-input"
          disabled={loading}
        />
        <button
          type="button"
          onClick={handleAddressSearch}
          disabled={loading}
          className="btn-search-location"
        >
          {loading ? 'Searching...' : 'Search Location'}
        </button>
      </div>

      {/* Error Message */}
      {error && <div className="location-picker-error">{error}</div>}

      {/* Map Container */}
      <div className="map-picker-container">
        <MapContainer
          key={mapKey}
          center={[position.lat, position.lng]}
          zoom={15}
          scrollWheelZoom={true}
          keyboard={false}
          className="location-picker-map"
        >
          <TileLayer
            attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>'
            url="https://{s}.basemaps.cartocdn.com/light_all/{z}/{x}/{y}{r}.png"
            subdomains="abcd"
            maxZoom={20}
          />
          <DraggableMarker
            position={[position.lat, position.lng]}
            onPositionChange={handleMarkerDrag}
          />
          <MapClickHandler onMapClick={handleMapClick} />
        </MapContainer>
      </div>

      {/* Coordinate Display (Read-only) */}
      <div className="coordinates-display">
        <div className="coordinate-item">
          <label>Latitude:</label>
          <span>{position.lat.toFixed(6)}</span>
        </div>
        <div className="coordinate-item">
          <label>Longitude:</label>
          <span>{position.lng.toFixed(6)}</span>
        </div>
      </div>

      {loading && (
        <div className="location-picker-loading">
          <div className="loading-spinner-small"></div>
        </div>
      )}
    </div>
  );
};

export default LocationPicker;

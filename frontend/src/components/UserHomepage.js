import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { FiMapPin, FiCalendar, FiSearch, FiChevronRight } from 'react-icons/fi';
import { motion } from 'framer-motion';
import apiService from '../services/api';
import './UserHomepage.css';

const UserHomepage = () => {
  const [locations, setLocations] = useState([]);
  const [searchLocation, setSearchLocation] = useState('');
  const [selectedDate, setSelectedDate] = useState(new Date().toISOString().split('T')[0]);
  const [loading, setLoading] = useState(true);
  const [locationSuggestions, setLocationSuggestions] = useState([]);
  const [showDropdown, setShowDropdown] = useState(false);
  const navigate = useNavigate();

  useEffect(() => {
    fetchLocations();
  }, []);

  useEffect(() => {
    const handleClickOutside = (event) => {
      if (showDropdown && !event.target.closest('.search-field')) {
        setShowDropdown(false);
      }
    };

    document.addEventListener('mousedown', handleClickOutside);
    return () => {
      document.removeEventListener('mousedown', handleClickOutside);
    };
  }, [showDropdown]);

  const fetchLocations = async () => {
    try {
      const response = await apiService.getAllLockers();
      const lockersByLocation = response.data.reduce((acc, locker) => {
        const loc = locker.locationName || 'Unknown Location';
        if (!acc[loc]) {
          acc[loc] = {
            name: loc,
            address: locker.address || 'Address not available',
            available: 0,
            total: 0,
            minPrice: Infinity
          };
        }
        acc[loc].total++;
        if (locker.status === 'AVAILABLE') acc[loc].available++;
        if (locker.hourlyRate < acc[loc].minPrice) {
          acc[loc].minPrice = locker.hourlyRate;
        }
        return acc;
      }, {});

      setLocations(Object.values(lockersByLocation));
      setLoading(false);
    } catch (error) {
      console.error('Error fetching locations:', error);
      setLoading(false);
    }
  };

  const handleSearch = () => {
    navigate('/map', {
      state: {
        searchLocation,
        selectedDate
      }
    });
  };

  const handleLocationClick = (location) => {
    navigate('/map', {
      state: {
        selectedLocation: location.name
      }
    });
  };

  const fetchLocationSuggestions = async (query) => {
    if (!query || query.trim().length === 0) {
      setLocationSuggestions([]);
      setShowDropdown(false);
      return;
    }

    try {
      const response = await apiService.getAllLockers();

      // Extract unique locations with their addresses
      const locationMap = new Map();
      response.data.forEach(locker => {
        if (locker.locationName && !locationMap.has(locker.locationName)) {
          locationMap.set(locker.locationName, {
            name: locker.locationName,
            address: locker.address || 'Address not available'
          });
        }
      });

      const uniqueLocations = Array.from(locationMap.values());

      // Filter locations based on user input (case-insensitive)
      const queryLower = query.toLowerCase();
      const filtered = uniqueLocations.filter(loc =>
        loc.name.toLowerCase().includes(queryLower) ||
        loc.address.toLowerCase().includes(queryLower)
      );

      setLocationSuggestions(filtered);
      setShowDropdown(filtered.length > 0);
    } catch (error) {
      console.error('Error fetching location suggestions:', error);
      setLocationSuggestions([]);
      setShowDropdown(false);
    }
  };

  const handleLocationSelect = (locationName) => {
    setSearchLocation(locationName);
    setShowDropdown(false);
  };

  if (loading) {
    return (
      <div className="loading-container">
        <div className="loading-spinner"></div>
      </div>
    );
  }

  return (
    <div className="user-homepage">
      {/* Hero Section */}
      <section className="hero-section">
        <div className="hero-content">
          <motion.h1
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.6 }}
          >
            Store Your Luggage.<br />
            Explore <span className="gradient-text">Timișoara</span>
          </motion.h1>

          <motion.p
            className="hero-subtitle"
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.6, delay: 0.1 }}
          >
            Find secure storage locations near you in seconds
          </motion.p>

          {/* Search Bar */}
          <motion.div
            className="search-container"
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.6, delay: 0.2 }}
          >
            <div className="search-field">
              <FiMapPin className="field-icon" />
              <input
                type="text"
                placeholder="Where do you need storage?"
                value={searchLocation}
                onChange={(e) => {
                  setSearchLocation(e.target.value);
                  fetchLocationSuggestions(e.target.value);
                }}
                onFocus={(e) => {
                  if (e.target.value) {
                    fetchLocationSuggestions(e.target.value);
                  }
                }}
              />
              {showDropdown && (
                <div className="autocomplete-dropdown">
                  {locationSuggestions.map((location, index) => (
                    <div
                      key={index}
                      className="autocomplete-item"
                      onClick={() => handleLocationSelect(location.name)}
                    >
                      <div className="location-name">{location.name}</div>
                      <div className="location-address">{location.address}</div>
                    </div>
                  ))}
                </div>
              )}
            </div>

            <div className="search-field">
              <FiCalendar className="field-icon" />
              <input
                type="date"
                placeholder="When?"
                value={selectedDate}
                onChange={(e) => setSelectedDate(e.target.value)}
                min={new Date().toISOString().split('T')[0]}
              />
            </div>

            <button className="search-button" onClick={handleSearch}>
              <FiSearch />
              Search
            </button>
          </motion.div>
        </div>

        <div className="hero-image">
          <img
            src="/timisoara-hero.jpg"
            alt="Timișoara City Center"
          />
        </div>
      </section>

      {/* Popular Locations */}
      <section className="locations-section">
        <div className="section-header">
          <h2>Popular Locations</h2>
          <p>Choose from our most booked storage spots</p>
        </div>

        <div className="locations-grid">
          {locations.map((location, index) => (
            <motion.div
              key={location.name}
              className="location-card"
              initial={{ opacity: 0, y: 30 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ duration: 0.4, delay: index * 0.1 }}
              onClick={() => handleLocationClick(location)}
              whileHover={{ y: -8 }}
            >
              <div className="location-info">
                <div className="availability-badge-container">
                  <span className="availability-badge">
                    {location.available} available
                  </span>
                </div>
                <h3>{location.name}</h3>
                <p className="location-address">{location.address}</p>

                <div className="location-footer">
                  <div className="price">
                    <span className="from">from</span>
                    <span className="amount">${location.minPrice}</span>
                    <span className="period">/hour</span>
                  </div>

                  <button className="view-button">
                    View
                    <FiChevronRight />
                  </button>
                </div>
              </div>
            </motion.div>
          ))}
        </div>
      </section>

      {/* Features Section */}
      <section className="features-section">
        <div className="features-grid">
          <div className="feature">
            <div className="feature-number">01</div>
            <h3>Secure Storage</h3>
            <p>All locations are monitored 24/7 with security cameras</p>
          </div>

          <div className="feature">
            <div className="feature-number">02</div>
            <h3>Instant Booking</h3>
            <p>Book your storage in seconds and get instant confirmation</p>
          </div>

          <div className="feature">
            <div className="feature-number">03</div>
            <h3>Best Prices</h3>
            <p>Transparent pricing with no hidden fees</p>
          </div>
        </div>
      </section>

      {/* CTA Section */}
      <section className="cta-section">
        <h2>Ready to explore hands-free?</h2>
        <p>Join thousands of happy travelers</p>
        <button className="cta-button" onClick={() => navigate('/map')}>
          Find Storage Near Me
        </button>
      </section>
    </div>
  );
};

export default UserHomepage;
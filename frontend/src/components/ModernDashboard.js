import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  FiBox,
  FiMapPin,
  FiTrendingUp,
  FiClock,
  FiAlertCircle,
  FiCheckCircle,
  FiXCircle,
  FiPauseCircle,
  FiActivity,
  FiGrid,
  FiMap,
  FiFilter,
  FiSearch,
  FiBell,
  FiSettings,
  FiMoon,
  FiSun
} from 'react-icons/fi';
import { motion, AnimatePresence } from 'framer-motion';
import apiService from '../services/api';
import LockerMap from './LockerMap';
import './ModernDashboard.css';

const ModernDashboard = () => {
  const [lockers, setLockers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [viewMode, setViewMode] = useState('map');
  const [darkMode, setDarkMode] = useState(false);
  const [selectedLocation, setSelectedLocation] = useState('all');
  const [searchQuery, setSearchQuery] = useState('');
  const [statistics, setStatistics] = useState(null);
  const [notifications] = useState(3);
  const navigate = useNavigate();

  useEffect(() => {
    fetchData();
    // Set up real-time updates
    const interval = setInterval(fetchData, 30000); // Refresh every 30 seconds
    return () => clearInterval(interval);
  }, []);

  const fetchData = async () => {
    try {
      const [lockersRes, statsRes] = await Promise.all([
        apiService.getAllLockers(),
        apiService.get('/lockers/statistics')
      ]);

      setLockers(lockersRes.data);
      setStatistics(statsRes.data);
      calculateEnhancedStatistics(lockersRes.data);
    } catch (error) {
      console.error('Error fetching data:', error);
    } finally {
      setLoading(false);
    }
  };

  const calculateEnhancedStatistics = (lockersData) => {
    const total = lockersData.length;
    const available = lockersData.filter(l => l.status === 'AVAILABLE').length;
    const occupied = lockersData.filter(l => l.status === 'OCCUPIED').length;
    const reserved = lockersData.filter(l => l.status === 'RESERVED').length;
    const maintenance = lockersData.filter(l => l.status === 'MAINTENANCE').length;
    const outOfOrder = lockersData.filter(l => l.status === 'OUT_OF_ORDER').length;

    // Group by location
    const byLocation = lockersData.reduce((acc, locker) => {
      const loc = locker.locationName || 'Unknown';
      if (!acc[loc]) {
        acc[loc] = {
          total: 0,
          available: 0,
          occupied: 0,
          revenue: 0,
          utilization: 0
        };
      }
      acc[loc].total++;
      if (locker.status === 'AVAILABLE') acc[loc].available++;
      if (locker.status === 'OCCUPIED') {
        acc[loc].occupied++;
        acc[loc].revenue += (locker.hourlyRate || 0) * 8; // Assume 8 hours average
      }
      acc[loc].utilization = ((acc[loc].occupied / acc[loc].total) * 100).toFixed(1);
      return acc;
    }, {});

    // Calculate trending data
    const hourlyRevenue = lockersData
      .filter(l => l.status === 'OCCUPIED')
      .reduce((sum, l) => sum + (l.hourlyRate || 0), 0);

    const dailyRevenue = hourlyRevenue * 12; // Assume 12 hour operation
    const monthlyRevenue = dailyRevenue * 30;

    setStatistics(prev => ({
      ...prev,
      total,
      available,
      occupied,
      reserved,
      maintenance,
      outOfOrder,
      availabilityRate: ((available / total) * 100).toFixed(1),
      utilizationRate: ((occupied / total) * 100).toFixed(1),
      byLocation,
      hourlyRevenue: hourlyRevenue.toFixed(2),
      dailyRevenue: dailyRevenue.toFixed(2),
      monthlyRevenue: monthlyRevenue.toFixed(0),
      performanceScore: ((available * 2 + occupied * 3) / (total * 3) * 100).toFixed(0)
    }));
  };

  const handleLockerSelect = (locker) => {
    if (locker.status === 'AVAILABLE') {
      navigate('/book', { state: { locker } });
    }
  };

  const statsCards = [
    {
      title: 'Total Lockers',
      value: statistics?.total || 0,
      icon: FiBox,
      color: 'yellow',
      gradient: 'linear-gradient(135deg, #FFD100 0%, #FFC000 100%)',
      change: '+12%',
      subtitle: 'All lockers in system'
    },
    {
      title: 'Available Now',
      value: statistics?.available || 0,
      icon: FiCheckCircle,
      color: 'yellow',
      gradient: 'linear-gradient(135deg, #FFD100 0%, #FFED4E 100%)',
      change: `${statistics?.availabilityRate}%`,
      subtitle: 'Ready for booking'
    },
    {
      title: 'Occupied',
      value: statistics?.occupied || 0,
      icon: FiXCircle,
      color: 'red',
      gradient: 'linear-gradient(135deg, #eb3349 0%, #f45c43 100%)',
      change: `${statistics?.utilizationRate}%`,
      subtitle: 'Currently in use'
    },
    {
      title: 'Daily Revenue',
      value: `$${statistics?.dailyRevenue || '0'}`,
      icon: FiTrendingUp,
      color: 'yellow',
      gradient: 'linear-gradient(135deg, #FFD100 0%, #FFC000 100%)',
      change: '+23%',
      subtitle: 'Projected earnings'
    }
  ];

  const locationCards = Object.entries(statistics?.byLocation || {}).slice(0, 5);

  if (loading) {
    return (
      <div className="modern-loading">
        <div className="loading-spinner"></div>
        <h2>Loading Dashboard...</h2>
        <p>Fetching real-time locker data</p>
      </div>
    );
  }

  return (
    <div className={`modern-dashboard ${darkMode ? 'dark-mode' : 'light-mode'}`}>
      {/* Header Bar */}
      <header className="dashboard-header-bar">
        <div className="header-left">
          <h1 className="dashboard-title">
            <FiActivity className="title-icon" />
            Analytics Dashboard
          </h1>
          <span className="dashboard-subtitle">Real-time locker management</span>
        </div>

        <div className="header-center">
          <div className="search-bar">
            <FiSearch className="search-icon" />
            <input
              type="text"
              placeholder="Search lockers, locations..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
            />
            <button className="filter-btn">
              <FiFilter />
              Filters
            </button>
          </div>
        </div>

        <div className="header-right">
          <button className="icon-btn notification-btn">
            <FiBell />
            {notifications > 0 && <span className="notification-badge">{notifications}</span>}
          </button>
          <button className="icon-btn" onClick={() => setDarkMode(!darkMode)}>
            {darkMode ? <FiSun /> : <FiMoon />}
          </button>
          <button className="icon-btn">
            <FiSettings />
          </button>
          <div className="user-avatar">
            <img src={`https://ui-avatars.com/api/?name=Admin&background=667eea&color=fff`} alt="User" />
          </div>
        </div>
      </header>

      {/* Main Content */}
      <div className="dashboard-main-content">
        {/* Stats Cards */}
        <div className="stats-grid">
          {statsCards.map((card, index) => (
            <motion.div
              key={card.title}
              className="stat-card-modern"
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ delay: index * 0.1 }}
            >
              <div className="stat-card-header">
                <div
                  className="stat-icon-wrapper"
                  style={{ background: card.gradient }}
                >
                  <card.icon className="stat-icon" />
                </div>
                <div className="stat-change">
                  <FiTrendingUp className="change-icon" />
                  {card.change}
                </div>
              </div>
              <div className="stat-value">{card.value}</div>
              <div className="stat-title">{card.title}</div>
              <div className="stat-subtitle">{card.subtitle}</div>
              <div className="stat-sparkline">
                <svg viewBox="0 0 100 40" className="sparkline-svg">
                  <path
                    d="M 0,30 L 20,25 L 40,27 L 60,15 L 80,20 L 100,10"
                    fill="none"
                    stroke={darkMode ? '#667eea' : '#764ba2'}
                    strokeWidth="2"
                  />
                </svg>
              </div>
            </motion.div>
          ))}
        </div>

        {/* Performance Metrics */}
        <div className="metrics-row">
          <motion.div
            className="performance-card"
            initial={{ opacity: 0, x: -20 }}
            animate={{ opacity: 1, x: 0 }}
          >
            <h3>Performance Score</h3>
            <div className="performance-gauge">
              <div className="gauge-circle">
                <svg className="gauge-svg" viewBox="0 0 200 200">
                  <circle
                    cx="100"
                    cy="100"
                    r="90"
                    fill="none"
                    stroke="#e0e0e0"
                    strokeWidth="20"
                  />
                  <circle
                    cx="100"
                    cy="100"
                    r="90"
                    fill="none"
                    stroke="url(#gauge-gradient)"
                    strokeWidth="20"
                    strokeDasharray={`${(statistics?.performanceScore || 0) * 5.65} 565`}
                    transform="rotate(-90 100 100)"
                  />
                  <defs>
                    <linearGradient id="gauge-gradient">
                      <stop offset="0%" stopColor="#667eea" />
                      <stop offset="100%" stopColor="#764ba2" />
                    </linearGradient>
                  </defs>
                </svg>
                <div className="gauge-value">
                  {statistics?.performanceScore || 0}%
                </div>
              </div>
              <div className="gauge-labels">
                <span>Poor</span>
                <span>Good</span>
                <span>Excellent</span>
              </div>
            </div>
          </motion.div>

          <motion.div
            className="revenue-card"
            initial={{ opacity: 0, x: 20 }}
            animate={{ opacity: 1, x: 0 }}
          >
            <h3>Revenue Projection</h3>
            <div className="revenue-stats">
              <div className="revenue-item">
                <span className="revenue-label">Hourly</span>
                <span className="revenue-value">${statistics?.hourlyRevenue || '0'}</span>
              </div>
              <div className="revenue-item">
                <span className="revenue-label">Daily</span>
                <span className="revenue-value highlight">${statistics?.dailyRevenue || '0'}</span>
              </div>
              <div className="revenue-item">
                <span className="revenue-label">Monthly</span>
                <span className="revenue-value">${statistics?.monthlyRevenue || '0'}</span>
              </div>
            </div>
            <div className="revenue-chart">
              <svg viewBox="0 0 300 100" className="bar-chart">
                {[40, 65, 45, 70, 55, 80, 60, 85, 75, 90, 70, 95].map((height, i) => (
                  <rect
                    key={i}
                    x={i * 25 + 5}
                    y={100 - height}
                    width="20"
                    height={height}
                    fill={`url(#bar-gradient-${i})`}
                    rx="4"
                  />
                ))}
                <defs>
                  {[...Array(12)].map((_, i) => (
                    <linearGradient key={i} id={`bar-gradient-${i}`} x1="0" y1="0" x2="0" y2="1">
                      <stop offset="0%" stopColor="#667eea" />
                      <stop offset="100%" stopColor="#764ba2" />
                    </linearGradient>
                  ))}
                </defs>
              </svg>
            </div>
          </motion.div>
        </div>

        {/* Location Cards */}
        <div className="locations-section">
          <div className="section-header">
            <h2>Top Locations</h2>
            <div className="view-toggle-modern">
              <button
                className={viewMode === 'map' ? 'active' : ''}
                onClick={() => setViewMode('map')}
              >
                <FiMap /> Map View
              </button>
              <button
                className={viewMode === 'grid' ? 'active' : ''}
                onClick={() => setViewMode('grid')}
              >
                <FiGrid /> Grid View
              </button>
            </div>
          </div>

          <div className="location-cards-grid">
            {locationCards.map(([name, data], index) => (
              <motion.div
                key={name}
                className="location-card-modern"
                initial={{ opacity: 0, scale: 0.95 }}
                animate={{ opacity: 1, scale: 1 }}
                transition={{ delay: index * 0.05 }}
                whileHover={{ scale: 1.02 }}
              >
                <div className="location-card-header">
                  <FiMapPin className="location-icon" />
                  <h4>{name}</h4>
                </div>

                <div className="location-stats-grid">
                  <div className="location-stat">
                    <span className="stat-number">{data.available}</span>
                    <span className="stat-label">Available</span>
                  </div>
                  <div className="location-stat">
                    <span className="stat-number">{data.occupied}</span>
                    <span className="stat-label">Occupied</span>
                  </div>
                  <div className="location-stat">
                    <span className="stat-number">{data.utilization}%</span>
                    <span className="stat-label">Utilization</span>
                  </div>
                </div>

                <div className="location-progress">
                  <div className="progress-bar-modern">
                    <div
                      className="progress-fill-modern"
                      style={{ width: `${data.utilization}%` }}
                    ></div>
                  </div>
                  <span className="progress-text">
                    {data.available} of {data.total} available
                  </span>
                </div>

                <button className="location-action-btn">
                  View Details →
                </button>
              </motion.div>
            ))}
          </div>
        </div>

        {/* Map Section */}
        <motion.div
          className="map-section-modern"
          initial={{ opacity: 0 }}
          animate={{ opacity: 1 }}
          transition={{ delay: 0.3 }}
        >
          <div className="section-header">
            <h2>Interactive Map</h2>
            <div className="map-controls-modern">
              <select
                className="location-filter"
                value={selectedLocation}
                onChange={(e) => setSelectedLocation(e.target.value)}
              >
                <option value="all">All Locations</option>
                {Object.keys(statistics?.byLocation || {}).map(loc => (
                  <option key={loc} value={loc}>{loc}</option>
                ))}
              </select>
            </div>
          </div>

          {viewMode === 'map' ? (
            <div className="map-container-modern">
              <LockerMap
                lockers={lockers}
                onLockerSelect={handleLockerSelect}
              />
            </div>
          ) : (
            <div className="grid-view-modern">
              {/* Grid view implementation */}
              <div className="lockers-grid">
                {lockers
                  .filter(l => selectedLocation === 'all' || l.locationName === selectedLocation)
                  .filter(l => !searchQuery ||
                    l.lockerNumber.toLowerCase().includes(searchQuery.toLowerCase()) ||
                    l.locationName?.toLowerCase().includes(searchQuery.toLowerCase())
                  )
                  .slice(0, 12)
                  .map((locker, index) => (
                    <motion.div
                      key={locker.id}
                      className={`locker-grid-item status-${locker.status.toLowerCase()}`}
                      initial={{ opacity: 0, y: 20 }}
                      animate={{ opacity: 1, y: 0 }}
                      transition={{ delay: index * 0.03 }}
                      onClick={() => handleLockerSelect(locker)}
                    >
                      <div className="locker-number">#{locker.lockerNumber}</div>
                      <div className="locker-location">{locker.locationName}</div>
                      <div className="locker-size">{locker.size}</div>
                      <div className="locker-price">${locker.hourlyRate}/hr</div>
                      <div className={`locker-status ${locker.status.toLowerCase()}`}>
                        {locker.status}
                      </div>
                    </motion.div>
                  ))}
              </div>
            </div>
          )}
        </motion.div>
      </div>
    </div>
  );
};

export default ModernDashboard;
import React, { useState, useEffect } from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate, useNavigate } from 'react-router-dom';
import { AuthProvider, useAuth } from './context/AuthContext';
import apiService from './services/api';
import Navigation from './components/Navigation';
import Login from './components/Login';
import Register from './components/Register';
import UserHomepage from './components/UserHomepage';
import LockerBrowser from './components/LockerBrowser';
import LockerMap from './components/LockerMap';
import Dashboard from './components/Dashboard';
import BookingWizard from './components/BookingWizard';
import MyBookings from './components/MyBookings';
import AdminRoute from './components/AdminRoute';
import AdminDashboard from './components/AdminDashboard';
import './App.css';

// Protected Route Component
const ProtectedRoute = ({ children }) => {
  const { isAuthenticated, loading } = useAuth();

  if (loading) {
    return <div className="loading">Loading...</div>;
  }

  return isAuthenticated ? children : <Navigate to="/login" />;
};

// Map Route Wrapper with navigation handler and data fetching
const MapRoute = () => {
  const navigate = useNavigate();
  const [lockers, setLockers] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchLockers = async () => {
      try {
        // Fetch only available lockers from the backend
        const response = await apiService.getAvailableLockers();
        setLockers(response.data);
      } catch (err) {
        console.error('Failed to load lockers for map:', err);
      } finally {
        setLoading(false);
      }
    };

    // Initial fetch
    fetchLockers();

    // Refresh every 10 seconds to keep map updated
    const interval = setInterval(fetchLockers, 10000);

    // Cleanup interval on unmount
    return () => clearInterval(interval);
  }, []);

  if (loading) {
    return <div className="loading">Loading map...</div>;
  }

  return (
    <LockerMap
      lockers={lockers}
      onBookingRequest={(locker) => navigate('/book', { state: { locker } })}
    />
  );
};

function App() {
  return (
    <Router>
      <AuthProvider>
        <div className="App">
          <Navigation />
          <Routes>
            <Route path="/" element={<UserHomepage />} />
            <Route path="/browse" element={<LockerBrowser />} />
            <Route path="/dashboard" element={<Dashboard />} />
            <Route path="/map" element={<MapRoute />} />
            <Route path="/login" element={<Login />} />
            <Route path="/register" element={<Register />} />
            <Route
              path="/book"
              element={
                <ProtectedRoute>
                  <BookingWizard />
                </ProtectedRoute>
              }
            />
            <Route
              path="/my-bookings"
              element={
                <ProtectedRoute>
                  <MyBookings />
                </ProtectedRoute>
              }
            />
            <Route
              path="/admin"
              element={
                <AdminRoute>
                  <AdminDashboard />
                </AdminRoute>
              }
            />
          </Routes>
        </div>
      </AuthProvider>
    </Router>
  );
}

export default App;

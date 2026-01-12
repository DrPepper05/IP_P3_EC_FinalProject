import React from 'react';
import { Navigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

/**
 * AdminRoute component - protects routes that require admin access
 * Similar to ProtectedRoute but checks for ADMIN role
 */
const AdminRoute = ({ children }) => {
  const { isAuthenticated, isAdmin, loading } = useAuth();

  if (loading) {
    return <div className="loading">Loading...</div>;
  }

  // If not authenticated, redirect to login
  if (!isAuthenticated) {
    return <Navigate to="/login" />;
  }

  // If authenticated but not admin, redirect to home
  if (!isAdmin()) {
    return <Navigate to="/" />;
  }

  // User is authenticated and is admin, render the protected component
  return children;
};

export default AdminRoute;

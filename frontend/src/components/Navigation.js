import React from 'react';
import { Link, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import './Navigation.css';

const Navigation = () => {
  const { user, logout, isAuthenticated, isAdmin } = useAuth();
  const location = useLocation();

  const isActive = (path) => location.pathname === path;

  return (
    <nav className="navbar-minimal">
      <div className="nav-container-minimal">
        <Link to="/" className="nav-logo-minimal">
          <span className="logo-text-primary">Timișoara</span>
          <span className="logo-text-secondary">Lockers</span>
        </Link>

        <div className="nav-menu-minimal">
          <Link
            to="/"
            className={`nav-link-minimal ${isActive('/') ? 'active' : ''}`}
          >
            Home
          </Link>

          <Link
            to="/map"
            className={`nav-link-minimal ${isActive('/map') ? 'active' : ''}`}
          >
            Find Storage
          </Link>

          {isAuthenticated && (
            <Link
              to="/my-bookings"
              className={`nav-link-minimal ${isActive('/my-bookings') ? 'active' : ''}`}
            >
              My Bookings
            </Link>
          )}

          {isAuthenticated && isAdmin() && (
            <Link
              to="/admin"
              className={`nav-link-minimal ${isActive('/admin') ? 'active' : ''}`}
            >
              Admin
            </Link>
          )}
        </div>

        <div className="nav-actions-minimal">
          {isAuthenticated ? (
            <>
              <span className="user-greeting">
                Hello, {user.firstName}
              </span>
              <button onClick={logout} className="btn-minimal btn-logout-minimal">
                Logout
              </button>
            </>
          ) : (
            <>
              <Link to="/login" className="btn-minimal btn-secondary">
                Login
              </Link>
              <Link to="/register" className="btn-minimal btn-primary">
                Sign Up
              </Link>
            </>
          )}
        </div>
      </div>
    </nav>
  );
};

export default Navigation;

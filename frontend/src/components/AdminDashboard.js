import React, { useState, useEffect, useCallback } from 'react';
import apiService from '../services/api';
import { useWebSocket } from '../hooks/useWebSocket';
import './AdminDashboard.css';

const AdminDashboard = () => {
  const [activeTab, setActiveTab] = useState('lockers');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  // Lockers state
  const [lockers, setLockers] = useState([]);
  const [availableLocations, setAvailableLocations] = useState([]);
  const [editingLocker, setEditingLocker] = useState(null);
  const [newLocker, setNewLocker] = useState({
    lockerNumber: '',
    size: 'MEDIUM',
    status: 'AVAILABLE',
    hourlyRate: 5.00,
    locationName: '',
    address: '',
    latitude: null,
    longitude: null,
    floor: 'Ground Floor',
    section: 'A'
  });

  // Bookings state
  const [bookings, setBookings] = useState([]);
  const [statusFilter, setStatusFilter] = useState('ALL');

  // Users state
  const [users, setUsers] = useState([]);
  const [roleFilter, setRoleFilter] = useState('ALL');

  // WebSocket handlers for real-time updates
  const handleLockerUpdate = useCallback((event) => {
    console.log('Locker update received:', event);
    fetchLockers();
  }, []);

  const handleBookingUpdate = useCallback((event) => {
    console.log('Booking update received:', event);
    fetchBookings();
  }, []);

  useWebSocket('/topic/lockers', handleLockerUpdate);
  useWebSocket('/topic/bookings', handleBookingUpdate);

  // Fetch data on mount and tab change
  useEffect(() => {
    if (activeTab === 'lockers') {
      fetchLockers();
    } else if (activeTab === 'bookings') {
      fetchBookings();
    } else if (activeTab === 'users') {
      fetchUsers();
    }
  }, [activeTab]);

  // Set default location when available locations are loaded
  useEffect(() => {
    if (availableLocations.length > 0 && !newLocker.locationName) {
      const firstLocation = availableLocations[0];
      setNewLocker(prev => ({
        ...prev,
        locationName: firstLocation.locationName,
        address: firstLocation.address,
        latitude: firstLocation.latitude,
        longitude: firstLocation.longitude
      }));
    }
  }, [availableLocations]);

  // Clear messages after 3 seconds
  useEffect(() => {
    if (error || success) {
      const timer = setTimeout(() => {
        setError('');
        setSuccess('');
      }, 3000);
      return () => clearTimeout(timer);
    }
  }, [error, success]);

  // ==================== LOCKER FUNCTIONS ====================

  const fetchLockers = async () => {
    setLoading(true);
    try {
      const response = await apiService.getAllLockers();
      setLockers(response.data);

      // Extract unique locations from existing lockers
      const locations = [];
      const locationMap = new Map();

      response.data.forEach(locker => {
        if (locker.locationName && !locationMap.has(locker.locationName)) {
          locationMap.set(locker.locationName, {
            locationName: locker.locationName,
            address: locker.address,
            latitude: locker.latitude,
            longitude: locker.longitude
          });
        }
      });

      setAvailableLocations(Array.from(locationMap.values()));
      setError('');
    } catch (err) {
      setError('Failed to load lockers: ' + (err.response?.data?.message || err.message));
    } finally {
      setLoading(false);
    }
  };

  const handleCreateLocker = async (e) => {
    e.preventDefault();
    setLoading(true);
    try {
      // Ensure hourlyRate is a number
      const lockerData = {
        ...newLocker,
        hourlyRate: parseFloat(newLocker.hourlyRate) || 0
      };
      await apiService.createLocker(lockerData);
      setSuccess('Locker created successfully!');
      setNewLocker({
        lockerNumber: '',
        size: 'MEDIUM',
        status: 'AVAILABLE',
        hourlyRate: 5.00,
        locationName: availableLocations.length > 0 ? availableLocations[0].locationName : '',
        address: availableLocations.length > 0 ? availableLocations[0].address : '',
        latitude: availableLocations.length > 0 ? availableLocations[0].latitude : null,
        longitude: availableLocations.length > 0 ? availableLocations[0].longitude : null,
        floor: 'Ground Floor',
        section: 'A'
      });
      fetchLockers();
    } catch (err) {
      setError('Failed to create locker: ' + (err.response?.data?.message || err.message));
    } finally {
      setLoading(false);
    }
  };

  const handleLocationSelect = (locationName) => {
    const selectedLocation = availableLocations.find(loc => loc.locationName === locationName);
    if (selectedLocation) {
      setNewLocker(prev => ({
        ...prev,
        locationName: selectedLocation.locationName,
        address: selectedLocation.address,
        latitude: selectedLocation.latitude,
        longitude: selectedLocation.longitude
      }));
    }
  };

  const handleEditLocationSelect = (locationName) => {
    const selectedLocation = availableLocations.find(loc => loc.locationName === locationName);
    if (selectedLocation) {
      setEditingLocker(prev => ({
        ...prev,
        locationName: selectedLocation.locationName,
        address: selectedLocation.address,
        latitude: selectedLocation.latitude,
        longitude: selectedLocation.longitude
      }));
    }
  };

  const handleUpdateLocker = async (e) => {
    e.preventDefault();
    setLoading(true);
    try {
      // Ensure hourlyRate is a number
      const lockerData = {
        ...editingLocker,
        hourlyRate: parseFloat(editingLocker.hourlyRate) || 0
      };
      await apiService.updateLocker(editingLocker.id, lockerData);
      setSuccess('Locker updated successfully!');
      setEditingLocker(null);
      fetchLockers();
    } catch (err) {
      setError('Failed to update locker: ' + (err.response?.data?.message || err.message));
    } finally {
      setLoading(false);
    }
  };

  const handleDeleteLocker = async (id) => {
    if (!window.confirm('Are you sure you want to delete this locker?')) return;

    setLoading(true);
    try {
      await apiService.deleteLocker(id);
      setSuccess('Locker deleted successfully!');
      fetchLockers();
    } catch (err) {
      setError('Failed to delete locker: ' + (err.response?.data?.message || err.message));
    } finally {
      setLoading(false);
    }
  };

  const handleToggleLockerStatus = async (locker) => {
    const newStatus = locker.status === 'AVAILABLE' ? 'OCCUPIED' : 'AVAILABLE';
    setLoading(true);
    try {
      await apiService.updateLockerStatus(locker.id, newStatus);
      setSuccess('Locker status updated!');
      fetchLockers();
    } catch (err) {
      setError('Failed to update status: ' + (err.response?.data?.message || err.message));
    } finally {
      setLoading(false);
    }
  };

  // ==================== BOOKING FUNCTIONS ====================

  const fetchBookings = async () => {
    setLoading(true);
    try {
      const response = await apiService.getAllBookings();
      setBookings(response.data);
      setError('');
    } catch (err) {
      setError('Failed to load bookings: ' + (err.response?.data?.message || err.message));
    } finally {
      setLoading(false);
    }
  };

  const handleCompleteBooking = async (id) => {
    if (!window.confirm('Mark this booking as completed?')) return;

    setLoading(true);
    try {
      await apiService.completeBooking(id);
      setSuccess('Booking completed successfully!');
      fetchBookings();
    } catch (err) {
      setError('Failed to complete booking: ' + (err.response?.data?.message || err.message));
    } finally {
      setLoading(false);
    }
  };

  const handleDeleteBooking = async (id) => {
    if (!window.confirm('Are you sure you want to delete this booking?')) return;

    setLoading(true);
    try {
      await apiService.deleteBooking(id);
      setSuccess('Booking deleted successfully!');
      fetchBookings();
    } catch (err) {
      setError('Failed to delete booking: ' + (err.response?.data?.message || err.message));
    } finally {
      setLoading(false);
    }
  };

  const getFilteredBookings = () => {
    if (statusFilter === 'ALL') return bookings;
    return bookings.filter(b => b.status === statusFilter);
  };

  // ==================== USER FUNCTIONS ====================

  const fetchUsers = async () => {
    setLoading(true);
    try {
      const response = await apiService.getAllUsers();
      setUsers(response.data);
      setError('');
    } catch (err) {
      setError('Failed to load users: ' + (err.response?.data?.message || err.message));
    } finally {
      setLoading(false);
    }
  };

  const handleDeleteUser = async (id) => {
    if (!window.confirm('Are you sure you want to delete this user? This cannot be undone.')) return;

    setLoading(true);
    try {
      await apiService.deleteUser(id);
      setSuccess('User deleted successfully!');
      fetchUsers();
    } catch (err) {
      setError('Failed to delete user: ' + (err.response?.data?.message || err.message));
    } finally {
      setLoading(false);
    }
  };

  const handleChangeUserRole = async (user) => {
    const newRole = user.role === 'ADMIN' ? 'CUSTOMER' : 'ADMIN';
    const action = user.role === 'ADMIN' ? 'demote to Customer' : 'promote to Admin';

    if (!window.confirm(`Are you sure you want to ${action} ${user.firstName} ${user.lastName}?`)) return;

    setLoading(true);
    try {
      await apiService.updateUserRole(user.id, newRole);
      setSuccess(`User role updated successfully! ${user.firstName} is now ${newRole === 'ADMIN' ? 'an Admin' : 'a Customer'}.`);
      fetchUsers();
    } catch (err) {
      setError('Failed to update user role: ' + (err.response?.data?.message || err.message));
    } finally {
      setLoading(false);
    }
  };

  const getFilteredUsers = () => {
    if (roleFilter === 'ALL') return users;
    return users.filter(u => u.role === roleFilter);
  };

  // ==================== RENDER FUNCTIONS ====================

  const renderLockersTab = () => (
    <div className="admin-section">
      <h2>Manage Lockers</h2>

      {/* Create Locker Form */}
      <div className="create-form">
        <h3>Create New Locker</h3>
        <form onSubmit={handleCreateLocker}>
          <div className="form-row">
            <input
              type="text"
              placeholder="Locker Number (e.g., L001)"
              value={newLocker.lockerNumber}
              onChange={(e) => setNewLocker(prev => ({ ...prev, lockerNumber: e.target.value }))}
              required
            />
            <select
              value={newLocker.size}
              onChange={(e) => setNewLocker(prev => ({ ...prev, size: e.target.value }))}
              required
            >
              <option value="SMALL">Small</option>
              <option value="MEDIUM">Medium</option>
              <option value="LARGE">Large</option>
            </select>
            <select
              value={newLocker.status}
              onChange={(e) => setNewLocker(prev => ({ ...prev, status: e.target.value }))}
              required
            >
              <option value="AVAILABLE">Available</option>
              <option value="OCCUPIED">Occupied</option>
            </select>
            <input
              type="number"
              step="0.01"
              placeholder="Hourly Rate"
              value={newLocker.hourlyRate}
              onChange={(e) => setNewLocker(prev => ({ ...prev, hourlyRate: e.target.value }))}
              required
            />
          </div>
          <div className="form-row">
            <select
              value={newLocker.locationName}
              onChange={(e) => handleLocationSelect(e.target.value)}
              required
              className="location-select"
            >
              <option value="">Select Location</option>
              {availableLocations.map((location) => (
                <option key={location.locationName} value={location.locationName}>
                  {location.locationName}
                </option>
              ))}
            </select>
          </div>
          <div className="form-row">
            <input
              type="text"
              placeholder="Floor"
              value={newLocker.floor}
              onChange={(e) => setNewLocker(prev => ({ ...prev, floor: e.target.value }))}
              required
            />
            <input
              type="text"
              placeholder="Section"
              value={newLocker.section}
              onChange={(e) => setNewLocker(prev => ({ ...prev, section: e.target.value }))}
              required
            />
            <button type="submit" className="btn-create" disabled={loading}>
              {loading ? 'Creating...' : 'Create Locker'}
            </button>
          </div>
        </form>
      </div>

      {/* Lockers Table */}
      <div className="data-table">
        <table>
          <thead>
            <tr>
              <th>ID</th>
              <th>Number</th>
              <th>Size</th>
              <th>Status</th>
              <th>Rate/hr</th>
              <th>Location</th>
              <th>Floor/Section</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {lockers.map((locker) => (
              editingLocker?.id === locker.id ? (
                <tr key={locker.id} className="editing-row">
                  <td>{locker.id}</td>
                  <td>
                    <input
                      type="text"
                      value={editingLocker.lockerNumber}
                      onChange={(e) => setEditingLocker({ ...editingLocker, lockerNumber: e.target.value })}
                    />
                  </td>
                  <td>
                    <select
                      value={editingLocker.size}
                      onChange={(e) => setEditingLocker({ ...editingLocker, size: e.target.value })}
                    >
                      <option value="SMALL">Small</option>
                      <option value="MEDIUM">Medium</option>
                      <option value="LARGE">Large</option>
                    </select>
                  </td>
                  <td>
                    <select
                      value={editingLocker.status}
                      onChange={(e) => setEditingLocker({ ...editingLocker, status: e.target.value })}
                    >
                      <option value="AVAILABLE">Available</option>
                      <option value="OCCUPIED">Occupied</option>
                    </select>
                  </td>
                  <td>
                    <input
                      type="number"
                      step="0.01"
                      value={editingLocker.hourlyRate}
                      onChange={(e) => setEditingLocker({ ...editingLocker, hourlyRate: e.target.value })}
                    />
                  </td>
                  <td>
                    <select
                      value={editingLocker.locationName}
                      onChange={(e) => handleEditLocationSelect(e.target.value)}
                      className="location-select-small"
                    >
                      <option value="">Select Location</option>
                      {availableLocations.map((location) => (
                        <option key={location.locationName} value={location.locationName}>
                          {location.locationName}
                        </option>
                      ))}
                    </select>
                  </td>
                  <td>
                    <input
                      type="text"
                      placeholder="Floor"
                      value={editingLocker.floor}
                      onChange={(e) => setEditingLocker(prev => ({ ...prev, floor: e.target.value }))}
                      style={{ width: '80px', marginRight: '5px' }}
                    />
                    /
                    <input
                      type="text"
                      placeholder="Section"
                      value={editingLocker.section}
                      onChange={(e) => setEditingLocker(prev => ({ ...prev, section: e.target.value }))}
                      style={{ width: '60px', marginLeft: '5px' }}
                    />
                  </td>
                  <td>
                    <button onClick={handleUpdateLocker} className="btn-save">Save</button>
                    <button onClick={() => setEditingLocker(null)} className="btn-cancel">Cancel</button>
                  </td>
                </tr>
              ) : (
                <tr key={locker.id}>
                  <td>{locker.id}</td>
                  <td>{locker.lockerNumber}</td>
                  <td>{locker.size}</td>
                  <td>
                    <span className={`status-badge status-${locker.status.toLowerCase()}`}>
                      {locker.status}
                    </span>
                  </td>
                  <td>${locker.hourlyRate}</td>
                  <td>{locker.locationName}</td>
                  <td>{locker.floor} / {locker.section}</td>
                  <td>
                    <button onClick={() => setEditingLocker(locker)} className="btn-edit">Edit</button>
                    <button onClick={() => handleToggleLockerStatus(locker)} className="btn-toggle">
                      {locker.status === 'AVAILABLE' ? 'Set Occupied' : 'Set Available'}
                    </button>
                    <button onClick={() => handleDeleteLocker(locker.id)} className="btn-delete">Delete</button>
                  </td>
                </tr>
              )
            ))}
          </tbody>
        </table>
        {lockers.length === 0 && <p className="no-data">No lockers found.</p>}
      </div>
    </div>
  );

  const renderBookingsTab = () => (
    <div className="admin-section">
      <h2>Manage Bookings</h2>

      {/* Status Filter */}
      <div className="filter-bar">
        <label>Filter by Status:</label>
        <select value={statusFilter} onChange={(e) => setStatusFilter(e.target.value)}>
          <option value="ALL">All Bookings</option>
          <option value="ACTIVE">Active</option>
          <option value="COMPLETED">Completed</option>
          <option value="CANCELLED">Cancelled</option>
        </select>
      </div>

      {/* Bookings Table */}
      <div className="data-table">
        <table>
          <thead>
            <tr>
              <th>ID</th>
              <th>Customer</th>
              <th>Email</th>
              <th>Locker</th>
              <th>Start</th>
              <th>End</th>
              <th>Status</th>
              <th>Price</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {getFilteredBookings().map((booking) => (
              <tr key={booking.id}>
                <td>{booking.id}</td>
                <td>{booking.customerFirstName} {booking.customerLastName}</td>
                <td>{booking.customerEmail}</td>
                <td>{booking.lockerNumber}</td>
                <td>{new Date(booking.startDatetime).toLocaleString()}</td>
                <td>{new Date(booking.endDatetime).toLocaleString()}</td>
                <td>
                  <span className={`status-badge status-${booking.status.toLowerCase()}`}>
                    {booking.status}
                  </span>
                </td>
                <td>${booking.totalPrice}</td>
                <td>
                  {booking.status === 'ACTIVE' && (
                    <button onClick={() => handleCompleteBooking(booking.id)} className="btn-complete">
                      Complete
                    </button>
                  )}
                  <button onClick={() => handleDeleteBooking(booking.id)} className="btn-delete">
                    Delete
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
        {getFilteredBookings().length === 0 && <p className="no-data">No bookings found.</p>}
      </div>
    </div>
  );

  const renderUsersTab = () => (
    <div className="admin-section">
      <h2>Manage Users</h2>

      {/* Role Filter */}
      <div className="filter-bar">
        <label>Filter by Role:</label>
        <select value={roleFilter} onChange={(e) => setRoleFilter(e.target.value)}>
          <option value="ALL">All Users</option>
          <option value="CUSTOMER">Customers</option>
          <option value="ADMIN">Admins</option>
        </select>
      </div>

      {/* Users Table */}
      <div className="data-table">
        <table>
          <thead>
            <tr>
              <th>ID</th>
              <th>Email</th>
              <th>First Name</th>
              <th>Last Name</th>
              <th>Role</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {getFilteredUsers().map((user) => (
              <tr key={user.id}>
                <td>{user.id}</td>
                <td>{user.email}</td>
                <td>{user.firstName}</td>
                <td>{user.lastName}</td>
                <td>
                  <span className={`status-badge role-${user.role ? user.role.toLowerCase() : 'customer'}`}>
                    {user.role === 'ADMIN' ? 'Admin' : 'Customer'}
                  </span>
                </td>
                <td>
                  <button
                    onClick={() => handleChangeUserRole(user)}
                    className={user.role === 'ADMIN' ? 'btn-toggle' : 'btn-edit'}
                  >
                    {user.role === 'ADMIN' ? 'Demote to Customer' : 'Promote to Admin'}
                  </button>
                  <button onClick={() => handleDeleteUser(user.id)} className="btn-delete">
                    Delete
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
        {getFilteredUsers().length === 0 && <p className="no-data">No users found.</p>}
      </div>
    </div>
  );

  return (
    <div className="admin-dashboard">
      <div className="admin-header">
        <h1>Admin Dashboard</h1>
      </div>

      {/* Messages */}
      {error && <div className="message message-error">{error}</div>}
      {success && <div className="message message-success">{success}</div>}

      {/* Tab Navigation */}
      <div className="tab-navigation">
        <button
          className={activeTab === 'lockers' ? 'tab active' : 'tab'}
          onClick={() => setActiveTab('lockers')}
        >
          Lockers
        </button>
        <button
          className={activeTab === 'bookings' ? 'tab active' : 'tab'}
          onClick={() => setActiveTab('bookings')}
        >
          Bookings
        </button>
        <button
          className={activeTab === 'users' ? 'tab active' : 'tab'}
          onClick={() => setActiveTab('users')}
        >
          Users
        </button>
      </div>

      {/* Tab Content */}
      <div className="tab-content">
        {loading && <div className="loading">Loading...</div>}
        {!loading && activeTab === 'lockers' && renderLockersTab()}
        {!loading && activeTab === 'bookings' && renderBookingsTab()}
        {!loading && activeTab === 'users' && renderUsersTab()}
      </div>
    </div>
  );
};

export default AdminDashboard;

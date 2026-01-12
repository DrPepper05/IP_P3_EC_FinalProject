import axios from 'axios';

const API_BASE_URL = 'http://localhost:8080/api';

// Create axios instance with base configuration
const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Add token to requests if available
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Handle authentication errors
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      // Clear stale tokens and redirect to login
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

// API Service Methods
const apiService = {
  // Authentication
  register: (userData) => api.post('/auth/register', userData),
  login: (credentials) => api.post('/auth/login', credentials),

  // Lockers
  getAllLockers: () => api.get('/lockers'),
  getAvailableLockers: () => api.get('/lockers/available'),
  getLockerById: (id) => api.get(`/lockers/${id}`),
  createLocker: (lockerData) => api.post('/lockers', lockerData),
  updateLocker: (id, lockerData) => api.put(`/lockers/${id}`, lockerData),
  deleteLocker: (id) => api.delete(`/lockers/${id}`),

  // Bookings
  getAllBookings: () => api.get('/bookings'),
  getBookingById: (id) => api.get(`/bookings/${id}`),
  getCustomerBookings: (customerId) => api.get(`/bookings/customer/${customerId}`),
  getMyBookings: () => api.get('/bookings/my-bookings'),
  createBooking: (bookingData) => api.post('/bookings', bookingData),
  updateBooking: (id, bookingData) => api.put(`/bookings/${id}`, bookingData),
  cancelBooking: (id) => api.put(`/bookings/${id}/cancel`),
  completeBooking: (id) => api.put(`/bookings/${id}/complete`),
  deleteBooking: (id) => api.delete(`/bookings/${id}`),
  getBookingsByStatus: (status) => api.get(`/bookings/status/${status}`),

  // Users/Persons (Admin only)
  getAllUsers: () => api.get('/persons'),
  getAllCustomers: () => api.get('/persons/customers'),
  getUserById: (id) => api.get(`/persons/${id}`),
  updateUser: (id, userData) => api.put(`/persons/${id}`, userData),
  updateUserRole: (id, role) => api.put(`/persons/${id}/role?role=${role}`),
  deleteUser: (id) => api.delete(`/persons/${id}`),
  updateLockerStatus: (id, status) => api.put(`/lockers/${id}/status`, { status }),
};

export default apiService;

const API_BASE = 'http://localhost:8080/api';

// Get token from localStorage
const getToken = () => localStorage.getItem('token');

// API helper function
const apiRequest = async (endpoint, options = {}) => {
  const token = getToken();

  const config = {
    ...options,
    headers: {
      'Content-Type': 'application/json',
      ...(token && { Authorization: `Bearer ${token}` }),
      ...options.headers,
    },
  };

  const response = await fetch(`${API_BASE}${endpoint}`, config);

  // Handle non-JSON responses
  const contentType = response.headers.get('content-type');
  let data;
  if (contentType && contentType.includes('application/json')) {
    data = await response.json();
  } else {
    data = await response.text();
  }

  if (!response.ok) {
    throw new Error(data.message || data || 'Something went wrong');
  }

  return data;
};

// ============== AUTH APIs ==============
export const authAPI = {
  register: (userData) =>
    apiRequest('/auth/register', {
      method: 'POST',
      body: JSON.stringify(userData),
    }),

  login: (credentials) =>
    apiRequest('/auth/login', {
      method: 'POST',
      body: JSON.stringify(credentials),
    }),

  logout: () =>
    apiRequest('/auth/logout', { method: 'POST' }),

  getMe: () =>
    apiRequest('/auth/me'),

  // New OTP Registration Methods
  initiateRegistration: (userData) =>
    apiRequest('/auth/register/initiate', {
      method: 'POST',
      body: JSON.stringify(userData),
    }),

  verifyOtp: (data) =>
    apiRequest('/auth/register/verify', {
      method: 'POST',
      body: JSON.stringify(data),
    }),

  resendOtp: (email) =>
    apiRequest('/auth/register/resend-otp', {
      method: 'POST',
      body: JSON.stringify({ email }),
    }),
};

// ============== ADDRESSES APIs ==============
export const addressesAPI = {
  getAll: async () => {
    const response = await apiRequest('/addresses');
    return response.data || response;
  },

  get: async (id) => {
    const response = await apiRequest(`/addresses/${id}`);
    return response.data || response;
  },

  add: async (addressData) => {
    const response = await apiRequest('/addresses', {
      method: 'POST',
      body: JSON.stringify(addressData),
    });
    return response.data || response;
  },

  update: async (id, addressData) => {
    const response = await apiRequest(`/addresses/${id}`, {
      method: 'PUT',
      body: JSON.stringify(addressData),
    });
    return response.data || response;
  },

  delete: async (id) => {
    const response = await apiRequest(`/addresses/${id}`, {
      method: 'DELETE',
    });
    return response;
  },

  setDefault: async (id) => {
    const response = await apiRequest(`/addresses/${id}/default`, {
      method: 'PUT',
    });
    return response.data || response;
  },
};

// ============== PROFILE APIs ==============
export const profileAPI = {
  get: async () => {
    const response = await apiRequest('/profile');
    return response.data || response;
  },

  update: async (data) => {
    const response = await apiRequest('/profile', {
      method: 'PUT',
      body: JSON.stringify(data),
    });
    return response.data || response;
  },
};

// ============== PRODUCTS APIs ==============
export const productsAPI = {
  getAll: async (page = 0, size = 12) => {
    const response = await apiRequest(`/products?page=${page}&size=${size}`);
    return response.data || response;
  },

  getById: async (id) => {
    const response = await apiRequest(`/products/${id}`);
    return response.data || response;
  },

  getByCategory: async (categoryId, page = 0, size = 12) => {
    const response = await apiRequest(`/products/category/${categoryId}?page=${page}&size=${size}`);
    return response.data || response;
  },
};

// ============== CATEGORIES APIs ==============
export const categoriesAPI = {
  getAll: async () => {
    const response = await apiRequest('/categories');
    return response.data || response;
  },
};

// ============== CART APIs ==============
export const cartAPI = {
  get: async () => {
    const response = await apiRequest('/cart');
    return response.data || response;
  },

  add: async (productId, quantity) => {
    const response = await apiRequest('/cart/add', {
      method: 'POST',
      body: JSON.stringify({ productId, quantity }),
    });
    return response.data || response;
  },

  update: async (productId, quantity) => {
    const response = await apiRequest('/cart/update', {
      method: 'PUT',
      body: JSON.stringify({ productId, quantity }),
    });
    return response.data || response;
  },

  remove: async (productId) => {
    const response = await apiRequest(`/cart/remove/${productId}`, { method: 'DELETE' });
    return response.data || response;
  },

  clear: async () => {
    const response = await apiRequest('/cart/clear', { method: 'DELETE' });
    return response.data || response;
  },
};

// ============== ORDERS APIs ==============
export const ordersAPI = {
  place: async () => {
    const response = await apiRequest('/orders/place', { method: 'POST' });
    return response.data || response;
  },

  placeWithPaymentMethod: async (paymentMethod, addressId = null) => {
    const response = await apiRequest('/orders/place-with-payment', {
      method: 'POST',
      body: JSON.stringify({ paymentMethod, addressId }),
    });
    return response.data || response;
  },

  getMy: async () => {
    const response = await apiRequest('/orders/my');
    return response.data || response;
  },

  getById: async (orderId) => {
    const response = await apiRequest(`/orders/${orderId}`);
    return response.data || response;
  },

  pay: async (orderId, forceSuccess = true) => {
    const response = await apiRequest(`/orders/${orderId}/pay`, {
      method: 'POST',
      body: JSON.stringify({ forceSuccess }),
    });
    return response.data || response;
  },

  cancel: async (orderId) => {
    const response = await apiRequest(`/orders/${orderId}/cancel`, { method: 'POST' });
    return response.data || response;
  },

  getTracking: async (orderId) => {
    const response = await apiRequest(`/orders/${orderId}/tracking`);
    return response.data || response;
  },
};

// ============== PAYMENTS APIs (Stripe) ==============
export const paymentsAPI = {
  createIntent: async (orderId) => {
    const response = await apiRequest(`/payments/create-intent/${orderId}`, { method: 'POST' });
    return response.data || response;
  },

  confirm: async (orderId, paymentIntentId) => {
    const response = await apiRequest(`/payments/confirm/${orderId}`, {
      method: 'POST',
      body: JSON.stringify({ paymentIntentId }),
    });
    return response.data || response;
  },
};

// ============== ADMIN APIs ==============
export const adminAPI = {
  // Products
  getProducts: async (page = 0, size = 10) => {
    const response = await apiRequest(`/admin/products?page=${page}&size=${size}`);
    return response.data || response;
  },

  createProduct: async (product) => {
    const response = await apiRequest('/admin/products', {
      method: 'POST',
      body: JSON.stringify(product),
    });
    return response.data || response;
  },

  updateProduct: async (id, product) => {
    const response = await apiRequest(`/admin/products/${id}`, {
      method: 'PUT',
      body: JSON.stringify(product),
    });
    return response.data || response;
  },

  deleteProduct: async (id) => {
    const response = await apiRequest(`/admin/products/${id}`, { method: 'DELETE' });
    return response.data || response;
  },

  // Categories
  getCategories: async () => {
    const response = await apiRequest('/admin/categories');
    return response.data || response;
  },

  createCategory: async (category) => {
    const response = await apiRequest('/admin/categories', {
      method: 'POST',
      body: JSON.stringify(category),
    });
    return response.data || response;
  },

  updateCategory: async (id, category) => {
    const response = await apiRequest(`/admin/categories/${id}`, {
      method: 'PUT',
      body: JSON.stringify(category),
    });
    return response.data || response;
  },

  deleteCategory: async (id) => {
    const response = await apiRequest(`/admin/categories/${id}`, { method: 'DELETE' });
    return response.data || response;
  },

  reactivateCategory: async (id) => {
    const response = await apiRequest(`/admin/categories/${id}`, { method: 'POST' });
    return response.data || response;
  },

  // Orders
  getOrders: async () => {
    const response = await apiRequest('/admin/orders');
    return response.data || response;
  },

  updateOrderStatus: async (orderId, status) => {
    const response = await apiRequest(`/admin/orders/${orderId}/status`, {
      method: 'PUT',
      body: JSON.stringify({ status }),
    });
    return response.data || response;
  },

  // Users
  getUsers: async () => {
    const response = await apiRequest('/admin/users');
    return response.data || response;
  },

  updateUserRole: async (id, role) => {
    const response = await apiRequest(`/admin/users/${id}/role`, {
      method: 'PUT',
      body: JSON.stringify({ role }),
    });
    return response.data || response;
  },

  updateUserStatus: async (id, status) => {
    const response = await apiRequest(`/admin/users/${id}/status`, {
      method: 'PUT',
      body: JSON.stringify({ status }),
    });
    return response.data || response;
  },

  // Delivery Partners
  getDeliveryPartners: async () => {
    const response = await apiRequest('/admin/delivery-partners');
    return response.data || response;
  },

  getActiveDeliveryPartners: async () => {
    const response = await apiRequest('/admin/delivery-partners/active');
    return response.data || response;
  },

  assignDelivery: async (orderId, deliveryPartnerId, estimatedDeliveryDays = 3) => {
    const response = await apiRequest(`/admin/orders/${orderId}/assign-delivery`, {
      method: 'PUT',
      body: JSON.stringify({ deliveryPartnerId, estimatedDeliveryDays }),
    });
    return response.data || response;
  },
};

// ============== DELIVERY PARTNERS APIs ==============
export const deliveryAPI = {
  getAll: async () => {
    const response = await apiRequest('/admin/delivery-partners');
    return response.data || response;
  },

  getActive: async () => {
    const response = await apiRequest('/admin/delivery-partners/active');
    return response.data || response;
  },

  getById: async (id) => {
    const response = await apiRequest(`/admin/delivery-partners/${id}`);
    return response.data || response;
  },

  create: async (partnerData) => {
    const response = await apiRequest('/admin/delivery-partners', {
      method: 'POST',
      body: JSON.stringify(partnerData),
    });
    return response.data || response;
  },

  update: async (id, partnerData) => {
    const response = await apiRequest(`/admin/delivery-partners/${id}`, {
      method: 'PUT',
      body: JSON.stringify(partnerData),
    });
    return response.data || response;
  },

  delete: async (id) => {
    const response = await apiRequest(`/admin/delivery-partners/${id}`, {
      method: 'DELETE',
    });
    return response.data || response;
  },
};

// ============== DELIVERY PARTNER PORTAL APIs ==============
export const deliveryPartnerAPI = {
  // Auth
  register: async (data) => {
    const response = await apiRequest('/delivery/register', {
      method: 'POST',
      body: JSON.stringify(data),
    });
    return response.data || response;
  },

  login: async (credentials) => {
    const response = await apiRequest('/delivery/login', {
      method: 'POST',
      body: JSON.stringify(credentials),
    });
    return response.data || response;
  },

  // Profile
  getProfile: async () => {
    const response = await apiRequest('/delivery/profile');
    return response.data || response;
  },

  uploadDocuments: async (docs) => {
    const response = await apiRequest('/delivery/documents', {
      method: 'POST',
      body: JSON.stringify(docs),
    });
    return response.data || response;
  },

  updateLocation: async (location) => {
    const response = await apiRequest('/delivery/location', {
      method: 'PUT',
      body: JSON.stringify(location),
    });
    return response.data || response;
  },

  // Orders
  getMyOrders: async () => {
    const response = await apiRequest('/delivery/orders/my');
    return response.data || response;
  },

  getAvailableOrders: async () => {
    const response = await apiRequest('/delivery/orders/available');
    return response.data || response;
  },

  acceptOrder: async (orderId) => {
    const response = await apiRequest(`/delivery/orders/${orderId}/accept`, {
      method: 'PUT',
    });
    return response.data || response;
  },

  rejectOrder: async (orderId) => {
    const response = await apiRequest(`/delivery/orders/${orderId}/reject`, {
      method: 'PUT',
    });
    return response.data || response;
  },

  pickupOrder: async (orderId) => {
    const response = await apiRequest(`/delivery/orders/${orderId}/pickup`, {
      method: 'PUT',
    });
    return response.data || response;
  },

  deliverOrder: async (orderId) => {
    const response = await apiRequest(`/delivery/orders/${orderId}/deliver`, {
      method: 'PUT',
    });
    return response.data || response;
  },
};

export default {
  auth: authAPI,
  profile: profileAPI,
  addresses: addressesAPI,
  products: productsAPI,
  categories: categoriesAPI,
  cart: cartAPI,
  orders: ordersAPI,
  payments: paymentsAPI,
  admin: adminAPI,
  delivery: deliveryAPI,
  deliveryPartner: deliveryPartnerAPI,
};


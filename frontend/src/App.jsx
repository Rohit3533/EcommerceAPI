import { Routes, Route } from 'react-router-dom';

// Layout Components
import Navbar from './components/Navbar';
import Footer from './components/Footer';
import ProtectedRoute from './components/ProtectedRoute';
import AdminLayout from './admin/AdminLayout';

// Customer Pages
import Home from './pages/Home';
import Products from './pages/Products';
import ProductDetail from './pages/ProductDetail';
import Cart from './pages/Cart';
import Checkout from './pages/Checkout';
import Orders from './pages/Orders';
import OrderTracking from './pages/OrderTracking';
import Profile from './pages/Profile';
import Login from './pages/Login';
import Register from './pages/Register';

// Admin Pages
import Dashboard from './admin/Dashboard';
import ProductsAdmin from './admin/ProductsAdmin';
import CategoriesAdmin from './admin/CategoriesAdmin';
import OrdersAdmin from './admin/OrdersAdmin';
import UsersAdmin from './admin/UsersAdmin';
import DeliveryPartnersAdmin from './admin/DeliveryPartnersAdmin';

// Delivery Partner Pages
import DeliveryAuth from './delivery/DeliveryAuth';
import DeliveryLayout from './delivery/DeliveryLayout';
import DeliveryDashboard from './delivery/DeliveryDashboard';
import DeliveryOrders from './delivery/DeliveryOrders';
import DeliveryProfile from './delivery/DeliveryProfile';

import './App.css';

function App() {
  return (
    <div className="app">
      <Routes>
        {/* Delivery Partner Routes (no Navbar/Footer) */}
        <Route path="/delivery/auth" element={<DeliveryAuth />} />
        <Route path="/delivery" element={<DeliveryLayout />}>
          <Route path="dashboard" element={<DeliveryDashboard />} />
          <Route path="orders" element={<DeliveryOrders />} />
          <Route path="profile" element={<DeliveryProfile />} />
        </Route>

        {/* Customer Routes with Navbar/Footer */}
        <Route
          path="/*"
          element={
            <>
              <Navbar />
              <main className="main-content">
                <Routes>
                  <Route path="/" element={<Home />} />
                  <Route path="/products" element={<Products />} />
                  <Route path="/products/:id" element={<ProductDetail />} />
                  <Route path="/login" element={<Login />} />
                  <Route path="/register" element={<Register />} />

                  {/* Protected Customer Routes */}
                  <Route
                    path="/cart"
                    element={
                      <ProtectedRoute>
                        <Cart />
                      </ProtectedRoute>
                    }
                  />
                  <Route
                    path="/checkout"
                    element={
                      <ProtectedRoute>
                        <Checkout />
                      </ProtectedRoute>
                    }
                  />
                  <Route
                    path="/orders"
                    element={
                      <ProtectedRoute>
                        <Orders />
                      </ProtectedRoute>
                    }
                  />
                  <Route
                    path="/orders/:orderId/tracking"
                    element={
                      <ProtectedRoute>
                        <OrderTracking />
                      </ProtectedRoute>
                    }
                  />
                  <Route
                    path="/profile"
                    element={
                      <ProtectedRoute>
                        <Profile />
                      </ProtectedRoute>
                    }
                  />
                </Routes>
              </main>
              <Footer />
            </>
          }
        />

        {/* Admin Routes with AdminLayout */}
        <Route
          path="/admin/*"
          element={
            <ProtectedRoute adminOnly>
              <AdminLayout />
            </ProtectedRoute>
          }
        >
          <Route index element={<Dashboard />} />
          <Route path="products" element={<ProductsAdmin />} />
          <Route path="categories" element={<CategoriesAdmin />} />
          <Route path="orders" element={<OrdersAdmin />} />
          <Route path="users" element={<UsersAdmin />} />
          <Route path="delivery-partners" element={<DeliveryPartnersAdmin />} />
        </Route>
      </Routes>
    </div>
  );
}

export default App;


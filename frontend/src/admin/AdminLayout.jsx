import { NavLink, Outlet, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import './AdminLayout.css';

const AdminLayout = () => {
    const { user, logout } = useAuth();
    const navigate = useNavigate();

    const handleLogout = async () => {
        await logout();
        navigate('/login');
    };

    return (
        <div className="admin-layout">
            {/* Sidebar */}
            <aside className="admin-sidebar">
                <div className="sidebar-header">
                    <span className="sidebar-icon">🛒</span>
                    <span className="sidebar-title">Admin</span>
                </div>

                <nav className="sidebar-nav">
                    <NavLink to="/admin" end className="sidebar-link">
                        <span className="link-icon">📊</span>
                        <span>Dashboard</span>
                    </NavLink>
                    <NavLink to="/admin/products" className="sidebar-link">
                        <span className="link-icon">📦</span>
                        <span>Products</span>
                    </NavLink>
                    <NavLink to="/admin/categories" className="sidebar-link">
                        <span className="link-icon">🏷️</span>
                        <span>Categories</span>
                    </NavLink>
                    <NavLink to="/admin/orders" className="sidebar-link">
                        <span className="link-icon">📋</span>
                        <span>Orders</span>
                    </NavLink>
                    <NavLink to="/admin/delivery-partners" className="sidebar-link">
                        <span className="link-icon">🚚</span>
                        <span>Delivery Partners</span>
                    </NavLink>
                    <NavLink to="/admin/users" className="sidebar-link">
                        <span className="link-icon">👥</span>
                        <span>Users</span>
                    </NavLink>
                </nav>

                <div className="sidebar-footer">
                    <NavLink to="/" className="sidebar-link">
                        <span className="link-icon">🏠</span>
                        <span>Back to Shop</span>
                    </NavLink>
                    <button onClick={handleLogout} className="sidebar-link logout-link">
                        <span className="link-icon">🚪</span>
                        <span>Logout</span>
                    </button>
                </div>
            </aside>

            {/* Main Content */}
            <main className="admin-main">
                <header className="admin-header">
                    <div className="admin-user">
                        <span className="user-avatar">{user?.name?.charAt(0) || '?'}</span>
                        <span className="user-name">{user?.name}</span>
                    </div>
                </header>
                <div className="admin-content">
                    <Outlet />
                </div>
            </main>
        </div>
    );
};

export default AdminLayout;

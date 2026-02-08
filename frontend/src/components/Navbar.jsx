import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { useCart } from '../context/CartContext';
import './Navbar.css';

const Navbar = () => {
    const { user, isAuthenticated, isAdmin, logout } = useAuth();
    const { itemCount } = useCart();
    const navigate = useNavigate();

    const handleLogout = async () => {
        await logout();
        navigate('/login');
    };

    return (
        <nav className="navbar">
            <div className="container navbar-container">
                <Link to="/" className="navbar-brand">
                    <span className="brand-icon">🛒</span>
                    <span className="brand-text">ShopHub</span>
                </Link>

                <div className="navbar-search">
                    <span className="search-icon">🔍</span>
                    <input
                        type="text"
                        placeholder="Search products..."
                        className="search-input"
                    />
                </div>

                <div className="navbar-actions">
                    {isAuthenticated ? (
                        <>
                            <Link to="/cart" className="nav-link cart-link">
                                <span className="cart-icon">🛒</span>
                                {itemCount > 0 && (
                                    <span className="cart-badge">{itemCount}</span>
                                )}
                            </Link>

                            <div className="nav-dropdown">
                                <button className="nav-link user-btn">
                                    <span className="user-icon">👤</span>
                                    <span className="user-name">{user?.name?.split(' ')[0]}</span>
                                    <span className="dropdown-arrow">▼</span>
                                </button>
                                <div className="dropdown-menu">
                                    <Link to="/profile" className="dropdown-item">
                                        <span>👤</span> Profile
                                    </Link>
                                    <Link to="/orders" className="dropdown-item">
                                        <span>📋</span> My Orders
                                    </Link>
                                    {isAdmin && (
                                        <Link to="/admin" className="dropdown-item admin-link">
                                            <span>⚙️</span> Admin Dashboard
                                        </Link>
                                    )}
                                    <hr className="dropdown-divider" />
                                    <button onClick={handleLogout} className="dropdown-item logout-btn">
                                        <span>🚪</span> Logout
                                    </button>
                                </div>
                            </div>
                        </>
                    ) : (
                        <div className="auth-buttons">
                            <Link to="/login" className="btn btn-secondary btn-sm">
                                Login
                            </Link>
                            <Link to="/register" className="btn btn-primary btn-sm">
                                Register
                            </Link>
                        </div>
                    )}
                </div>
            </div>
        </nav>
    );
};

export default Navbar;

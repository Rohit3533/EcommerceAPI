import { useState, useEffect } from 'react';
import { NavLink, Outlet, useNavigate } from 'react-router-dom';
import { deliveryPartnerAPI } from '../api/api';
import './DeliveryPages.css';

const DeliveryLayout = () => {
    const [isOnline, setIsOnline] = useState(false);
    const [profile, setProfile] = useState(null);
    const navigate = useNavigate();

    useEffect(() => {
        fetchProfile();
    }, []);

    const fetchProfile = async () => {
        try {
            const data = await deliveryPartnerAPI.getProfile();
            setProfile(data);
            setIsOnline(data.isOnline || false);
        } catch (error) {
            console.error('Error fetching profile:', error);
            navigate('/delivery/auth');
        }
    };

    const toggleOnline = async () => {
        try {
            const newStatus = !isOnline;
            await deliveryPartnerAPI.updateLocation({
                isOnline: newStatus,
                latitude: null,
                longitude: null,
            });
            setIsOnline(newStatus);
        } catch (error) {
            console.error('Error updating status:', error);
        }
    };

    const handleLogout = () => {
        localStorage.removeItem('token');
        localStorage.removeItem('userRole');
        navigate('/delivery/auth');
    };

    return (
        <div className="delivery-layout">
            <aside className="delivery-sidebar">
                <div className="delivery-sidebar-header">
                    <span>🚚</span>
                    <div>
                        <h2>Delivery Portal</h2>
                        {profile && <small style={{ color: 'rgba(255,255,255,0.5)' }}>{profile.name}</small>}
                    </div>
                </div>

                <nav className="delivery-nav">
                    <NavLink to="/delivery/dashboard" className={({ isActive }) => `delivery-nav-item ${isActive ? 'active' : ''}`}>
                        📊 Dashboard
                    </NavLink>
                    <NavLink to="/delivery/orders" className={({ isActive }) => `delivery-nav-item ${isActive ? 'active' : ''}`}>
                        📦 Orders
                    </NavLink>
                    <NavLink to="/delivery/profile" className={({ isActive }) => `delivery-nav-item ${isActive ? 'active' : ''}`}>
                        👤 Profile & Documents
                    </NavLink>
                </nav>

                <div className="online-toggle">
                    <div className={`toggle-switch ${isOnline ? 'online' : ''}`} onClick={toggleOnline}>
                        <span>{isOnline ? '🟢 Online' : '⚪ Offline'}</span>
                        <div className="toggle-indicator"></div>
                    </div>
                </div>

                <button
                    onClick={handleLogout}
                    style={{
                        marginTop: '1rem',
                        padding: '0.8rem',
                        background: 'rgba(239, 68, 68, 0.2)',
                        color: '#ef4444',
                        border: '1px solid rgba(239, 68, 68, 0.3)',
                        borderRadius: '10px',
                        cursor: 'pointer',
                    }}
                >
                    🚪 Logout
                </button>
            </aside>

            <main className="delivery-content">
                <Outlet context={{ profile, fetchProfile }} />
            </main>
        </div>
    );
};

export default DeliveryLayout;

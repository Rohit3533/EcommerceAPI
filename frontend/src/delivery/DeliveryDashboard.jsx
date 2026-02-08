import { useState, useEffect } from 'react';
import { useOutletContext } from 'react-router-dom';
import { deliveryPartnerAPI } from '../api/api';
import './DeliveryPages.css';

const DeliveryDashboard = () => {
    const { profile } = useOutletContext();
    const [myOrders, setMyOrders] = useState([]);
    const [availableOrders, setAvailableOrders] = useState([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        fetchOrders();
    }, []);

    const fetchOrders = async () => {
        try {
            const [my, available] = await Promise.all([
                deliveryPartnerAPI.getMyOrders(),
                deliveryPartnerAPI.getAvailableOrders(),
            ]);
            setMyOrders(my || []);
            setAvailableOrders(available || []);
        } catch (error) {
            console.error('Error fetching orders:', error);
        } finally {
            setLoading(false);
        }
    };

    const formatCurrency = (amount) => {
        return new Intl.NumberFormat('en-IN', {
            style: 'currency',
            currency: 'INR',
        }).format(amount);
    };

    const isVerified = profile?.verificationStatus === 'APPROVED';

    return (
        <div className="delivery-dashboard">
            <div className="dashboard-header">
                <h1>👋 Welcome back, {profile?.name || 'Partner'}!</h1>
                <p>
                    {isVerified ? "You're verified and ready to deliver" : 'Complete verification to start accepting orders'}
                </p>
            </div>

            {/* Verification Status Banner */}
            {!isVerified && (
                <div style={{
                    background: 'rgba(245, 158, 11, 0.2)',
                    border: '1px solid rgba(245, 158, 11, 0.3)',
                    borderRadius: '12px',
                    padding: '1rem 1.5rem',
                    marginBottom: '1.5rem',
                    display: 'flex',
                    alignItems: 'center',
                    gap: '1rem',
                }}>
                    <span style={{ fontSize: '1.5rem' }}>⚠️</span>
                    <div>
                        <strong style={{ color: '#f59e0b' }}>Verification Pending</strong>
                        <p style={{ color: 'rgba(255,255,255,0.7)', margin: '0.3rem 0 0' }}>
                            {profile?.verificationStatus === 'REJECTED'
                                ? `Rejected: ${profile.rejectionReason || 'Please contact support'}`
                                : 'Please upload your documents for verification'}
                        </p>
                    </div>
                </div>
            )}

            {/* Stats Grid */}
            <div className="stats-grid">
                <div className="stat-card">
                    <div className="icon">📦</div>
                    <div className="value">{profile?.currentOrders || 0}</div>
                    <div className="label">Active Orders</div>
                </div>
                <div className="stat-card">
                    <div className="icon">✅</div>
                    <div className="value">{profile?.totalDeliveries || 0}</div>
                    <div className="label">Total Deliveries</div>
                </div>
                <div className="stat-card">
                    <div className="icon">⭐</div>
                    <div className="value">{profile?.averageRating?.toFixed(1) || '0.0'}</div>
                    <div className="label">Rating</div>
                </div>
                <div className="stat-card earnings">
                    <div className="icon">💰</div>
                    <div className="value">{availableOrders.length}</div>
                    <div className="label">Available Orders</div>
                </div>
            </div>

            {/* Quick Actions */}
            <div className="orders-section">
                <div className="orders-header">
                    <h2>📋 Quick Overview</h2>
                </div>
                <div className="orders-list">
                    {loading ? (
                        <div className="empty-orders">
                            <span>⏳</span>
                            <p>Loading orders...</p>
                        </div>
                    ) : myOrders.length === 0 && availableOrders.length === 0 ? (
                        <div className="empty-orders">
                            <span>📭</span>
                            <p>No orders at the moment</p>
                        </div>
                    ) : (
                        <>
                            {myOrders.slice(0, 3).map(order => (
                                <div key={order.orderId} className="order-card">
                                    <div className="order-card-header">
                                        <span className="order-id">#{order.orderNumber || order.orderId}</span>
                                        <span className="order-amount">{formatCurrency(order.totalAmount)}</span>
                                    </div>
                                    <div className="order-customer">👤 {order.customerName}</div>
                                    <div className="order-address">📍 {order.shippingAddress}</div>
                                    <div style={{
                                        display: 'inline-block',
                                        padding: '0.3rem 0.8rem',
                                        background: 'rgba(102, 126, 234, 0.2)',
                                        color: '#667eea',
                                        borderRadius: '20px',
                                        fontSize: '0.8rem'
                                    }}>
                                        {order.status}
                                    </div>
                                </div>
                            ))}
                            {availableOrders.length > 0 && (
                                <div style={{
                                    textAlign: 'center',
                                    padding: '1rem',
                                    color: 'rgba(255,255,255,0.6)'
                                }}>
                                    <p>🔔 {availableOrders.length} order(s) available nearby</p>
                                </div>
                            )}
                        </>
                    )}
                </div>
            </div>
        </div>
    );
};

export default DeliveryDashboard;

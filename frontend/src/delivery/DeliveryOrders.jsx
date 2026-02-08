import { useState, useEffect } from 'react';
import { useOutletContext } from 'react-router-dom';
import { deliveryPartnerAPI } from '../api/api';
import './DeliveryPages.css';

const DeliveryOrders = () => {
    const { profile } = useOutletContext();
    const [activeTab, setActiveTab] = useState('my');
    const [myOrders, setMyOrders] = useState([]);
    const [availableOrders, setAvailableOrders] = useState([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        fetchOrders();
    }, []);

    const fetchOrders = async () => {
        setLoading(true);
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

    const handleAccept = async (orderId) => {
        try {
            await deliveryPartnerAPI.acceptOrder(orderId);
            fetchOrders();
        } catch (error) {
            alert(error.message || 'Failed to accept order');
        }
    };

    const handleReject = async (orderId) => {
        if (!confirm('Are you sure you want to release this order?')) return;
        try {
            await deliveryPartnerAPI.rejectOrder(orderId);
            fetchOrders();
        } catch (error) {
            alert(error.message || 'Failed to release order');
        }
    };

    const handlePickup = async (orderId) => {
        try {
            await deliveryPartnerAPI.pickupOrder(orderId);
            fetchOrders();
        } catch (error) {
            alert(error.message || 'Failed to update order');
        }
    };

    const handleDeliver = async (orderId) => {
        try {
            await deliveryPartnerAPI.deliverOrder(orderId);
            alert('🎉 Order delivered successfully!');
            fetchOrders();
        } catch (error) {
            alert(error.message || 'Failed to mark as delivered');
        }
    };

    const formatCurrency = (amount) => {
        return new Intl.NumberFormat('en-IN', {
            style: 'currency',
            currency: 'INR',
        }).format(amount);
    };

    const isVerified = profile?.verificationStatus === 'APPROVED';
    const orders = activeTab === 'my' ? myOrders : availableOrders;

    return (
        <div className="delivery-dashboard">
            <div className="dashboard-header">
                <h1>📦 Orders</h1>
                <p>Manage your deliveries and accept new orders</p>
            </div>

            {!isVerified && (
                <div style={{
                    background: 'rgba(239, 68, 68, 0.2)',
                    border: '1px solid rgba(239, 68, 68, 0.3)',
                    borderRadius: '12px',
                    padding: '1rem',
                    marginBottom: '1.5rem',
                    color: '#ef4444',
                }}>
                    ⚠️ You need to be verified before you can accept orders.
                </div>
            )}

            <div className="orders-section">
                <div className="orders-header">
                    <h2>🚚 Delivery Queue</h2>
                    <div className="orders-tabs">
                        <button
                            className={activeTab === 'my' ? 'active' : ''}
                            onClick={() => setActiveTab('my')}
                        >
                            My Orders ({myOrders.length})
                        </button>
                        <button
                            className={activeTab === 'available' ? 'active' : ''}
                            onClick={() => setActiveTab('available')}
                        >
                            Available ({availableOrders.length})
                        </button>
                    </div>
                </div>

                <div className="orders-list">
                    {loading ? (
                        <div className="empty-orders">
                            <span>⏳</span>
                            <p>Loading orders...</p>
                        </div>
                    ) : orders.length === 0 ? (
                        <div className="empty-orders">
                            <span>{activeTab === 'my' ? '📭' : '🔍'}</span>
                            <p>{activeTab === 'my' ? 'No active orders' : 'No available orders nearby'}</p>
                        </div>
                    ) : (
                        orders.map(order => (
                            <div key={order.orderId} className="order-card">
                                <div className="order-card-header">
                                    <span className="order-id">#{order.orderNumber || order.orderId}</span>
                                    <span className="order-amount">{formatCurrency(order.totalAmount)}</span>
                                </div>
                                <div className="order-customer">
                                    👤 {order.customerName} | 📞 {order.customerPhone}
                                </div>
                                <div className="order-address">📍 {order.shippingAddress}</div>

                                <div className="order-actions">
                                    {activeTab === 'available' ? (
                                        <button
                                            className="btn-accept"
                                            onClick={() => handleAccept(order.orderId)}
                                            disabled={!isVerified}
                                        >
                                            ✅ Accept Order
                                        </button>
                                    ) : (
                                        <>
                                            {order.status === 'OUT_FOR_DELIVERY' && !order.pickedUpAt && (
                                                <button
                                                    className="btn-pickup"
                                                    onClick={() => handlePickup(order.orderId)}
                                                >
                                                    📦 Mark Picked Up
                                                </button>
                                            )}
                                            {(order.status === 'OUT_FOR_DELIVERY' || order.pickedUpAt) && (
                                                <button
                                                    className="btn-deliver"
                                                    onClick={() => handleDeliver(order.orderId)}
                                                >
                                                    ✅ Mark Delivered
                                                </button>
                                            )}
                                            <button
                                                className="btn-reject"
                                                onClick={() => handleReject(order.orderId)}
                                            >
                                                ✕ Release
                                            </button>
                                        </>
                                    )}
                                </div>
                            </div>
                        ))
                    )}
                </div>
            </div>
        </div>
    );
};

export default DeliveryOrders;

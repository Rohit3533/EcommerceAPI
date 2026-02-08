import { useState, useEffect } from 'react';
import { useParams, Link } from 'react-router-dom';
import { ordersAPI } from '../api/api';
import './OrderTracking.css';

const OrderTracking = () => {
    const { orderId } = useParams();
    const [order, setOrder] = useState(null);
    const [statusHistory, setStatusHistory] = useState([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        fetchOrderTracking();
    }, [orderId]);

    const fetchOrderTracking = async () => {
        try {
            const data = await ordersAPI.getTracking(orderId);
            setOrder(data.order || data);
            setStatusHistory(data.statusHistory || []);
        } catch (error) {
            console.error('Error fetching order tracking:', error);
        } finally {
            setLoading(false);
        }
    };

    const formatDate = (date) => {
        if (!date) return '';
        return new Date(date).toLocaleString('en-US', {
            year: 'numeric',
            month: 'short',
            day: 'numeric',
            hour: '2-digit',
            minute: '2-digit',
        });
    };

    const statusSteps = [
        { key: 'PLACED', label: 'Order Placed', icon: '📋', description: 'Your order has been placed successfully' },
        { key: 'CONFIRMED', label: 'Confirmed', icon: '✅', description: 'Order confirmed and being prepared' },
        { key: 'PROCESSING', label: 'Processing', icon: '⚙️', description: 'Your order is being processed' },
        { key: 'SHIPPED', label: 'Shipped', icon: '📦', description: 'Package handed to delivery partner' },
        { key: 'OUT_FOR_DELIVERY', label: 'Out for Delivery', icon: '🚚', description: 'Your package is on the way' },
        { key: 'DELIVERED', label: 'Delivered', icon: '🎉', description: 'Package delivered successfully' },
    ];

    const getStatusIndex = (status) => {
        if (status === 'CANCELLED') return -1;
        return statusSteps.findIndex(s => s.key === status);
    };

    const currentStatusIndex = order ? getStatusIndex(order.status) : -1;

    if (loading) {
        return (
            <div className="loading-container">
                <div className="spinner"></div>
            </div>
        );
    }

    if (!order) {
        return (
            <div className="tracking-page page">
                <div className="container">
                    <div className="error-state">
                        <h2>Order not found</h2>
                        <Link to="/orders" className="btn btn-primary">Back to Orders</Link>
                    </div>
                </div>
            </div>
        );
    }

    return (
        <div className="tracking-page page">
            <div className="container">
                <div className="tracking-header">
                    <Link to="/orders" className="back-link">← Back to Orders</Link>
                    <h1 className="page-title">Track Order</h1>
                </div>

                {/* Order Summary Card */}
                <div className="order-summary-card card">
                    <div className="order-summary-header">
                        <div className="order-ids">
                            <div className="order-number">
                                <span className="label">Order Number</span>
                                <span className="value">{order.orderNumber || `#${order.orderId || order.id}`}</span>
                            </div>
                            {order.trackingId && (
                                <div className="tracking-id">
                                    <span className="label">Tracking ID</span>
                                    <span className="value">{order.trackingId}</span>
                                </div>
                            )}
                        </div>
                        {order.estimatedDelivery && (
                            <div className="estimated-delivery">
                                <span className="label">Estimated Delivery</span>
                                <span className="value">{formatDate(order.estimatedDelivery)}</span>
                            </div>
                        )}
                    </div>

                    {order.status === 'CANCELLED' && (
                        <div className="cancelled-notice">
                            <span className="cancelled-icon">🚫</span>
                            <span>This order has been cancelled</span>
                        </div>
                    )}
                </div>

                {/* Timeline */}
                {order.status !== 'CANCELLED' && (
                    <div className="tracking-timeline card">
                        <h2 className="timeline-title">Order Status</h2>
                        <div className="timeline">
                            {statusSteps.map((step, index) => {
                                const isCompleted = index <= currentStatusIndex;
                                const isCurrent = index === currentStatusIndex;
                                const historyEntry = statusHistory.find(h => h.status === step.key);

                                return (
                                    <div
                                        key={step.key}
                                        className={`timeline-step ${isCompleted ? 'completed' : ''} ${isCurrent ? 'current' : ''}`}
                                    >
                                        <div className="timeline-marker">
                                            <div className="marker-icon">
                                                {isCompleted ? '✓' : step.icon}
                                            </div>
                                            {index < statusSteps.length - 1 && <div className="timeline-line"></div>}
                                        </div>
                                        <div className="timeline-content">
                                            <h3 className="step-label">{step.label}</h3>
                                            <p className="step-description">{step.description}</p>
                                            {historyEntry && (
                                                <span className="step-time">{formatDate(historyEntry.createdAt)}</span>
                                            )}
                                            {historyEntry?.note && (
                                                <p className="step-note">{historyEntry.note}</p>
                                            )}
                                        </div>
                                    </div>
                                );
                            })}
                        </div>
                    </div>
                )}

                {/* Delivery Partner Info */}
                {order.deliveryPartner && (
                    <div className="delivery-partner-card card">
                        <h2>Delivery Partner</h2>
                        <div className="partner-info">
                            <div className="partner-avatar">🚚</div>
                            <div className="partner-details">
                                <h3>{order.deliveryPartner.name}</h3>
                                <p className="partner-phone">📞 {order.deliveryPartner.phone}</p>
                                <p className="partner-vehicle">
                                    {order.deliveryPartner.vehicleType}
                                    {order.deliveryPartner.vehicleNumber && ` • ${order.deliveryPartner.vehicleNumber}`}
                                </p>
                            </div>
                        </div>
                    </div>
                )}

                {/* Order Items */}
                <div className="order-items-card card">
                    <h2>Order Items</h2>
                    <div className="items-list">
                        {order.items?.map((item, index) => (
                            <div key={index} className="order-item">
                                <img
                                    src={item.imageUrl || 'https://via.placeholder.com/60x60?text=No+Image'}
                                    alt={item.productName}
                                    className="item-image"
                                />
                                <div className="item-details">
                                    <span className="item-name">{item.productName}</span>
                                    <span className="item-qty">Qty: {item.quantity}</span>
                                </div>
                                <span className="item-price">
                                    ${(item.price * item.quantity).toFixed(2)}
                                </span>
                            </div>
                        ))}
                    </div>
                    <div className="order-total">
                        <span>Total</span>
                        <span className="total-amount">${order.totalAmount?.toFixed(2)}</span>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default OrderTracking;

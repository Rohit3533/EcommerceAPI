import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { ordersAPI } from '../api/api';
import './Orders.css';

const Orders = () => {
    const [orders, setOrders] = useState([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        fetchOrders();
    }, []);

    const fetchOrders = async () => {
        try {
            const data = await ordersAPI.getMy();
            setOrders(data || []);
        } catch (error) {
            console.error('Error fetching orders:', error);
        } finally {
            setLoading(false);
        }
    };

    const handleCancel = async (orderId) => {
        if (!window.confirm('Are you sure you want to cancel this order?')) return;
        try {
            await ordersAPI.cancel(orderId);
            fetchOrders();
        } catch (error) {
            alert(error.message || 'Failed to cancel order');
        }
    };

    const formatPrice = (price) => {
        return new Intl.NumberFormat('en-US', {
            style: 'currency',
            currency: 'USD',
        }).format(price);
    };

    const formatDate = (date) => {
        return new Date(date).toLocaleDateString('en-US', {
            year: 'numeric',
            month: 'short',
            day: 'numeric',
        });
    };

    const getStatusBadge = (status) => {
        const statusConfig = {
            PENDING_PAYMENT: { class: 'badge-warning', icon: '⏳', label: 'Pending Payment' },
            PAYMENT_FAILED: { class: 'badge-danger', icon: '❌', label: 'Payment Failed' },
            PLACED: { class: 'badge-success', icon: '✅', label: 'Placed' },
            SHIPPED: { class: 'badge-info', icon: '🚚', label: 'Shipped' },
            DELIVERED: { class: 'badge-success', icon: '📦', label: 'Delivered' },
            CANCELLED: { class: 'badge-danger', icon: '🚫', label: 'Cancelled' },
        };
        const config = statusConfig[status] || { class: 'badge-info', icon: '📋', label: status };
        return (
            <span className={`badge ${config.class}`}>
                {config.icon} {config.label}
            </span>
        );
    };

    if (loading) {
        return (
            <div className="loading-container">
                <div className="spinner"></div>
            </div>
        );
    }

    return (
        <div className="orders-page page">
            <div className="container">
                <h1 className="page-title">My Orders</h1>
                <p className="page-subtitle">Track and manage your orders</p>

                {orders.length === 0 ? (
                    <div className="empty-state">
                        <p className="empty-state-icon">📋</p>
                        <p className="empty-state-title">No orders yet</p>
                        <p className="empty-state-text">Start shopping to see your orders here!</p>
                        <Link to="/products" className="btn btn-primary">
                            Browse Products
                        </Link>
                    </div>
                ) : (
                    <div className="orders-list">
                        {orders.map((order) => (
                            <div key={order.orderId} className="order-card card">
                                <div className="order-header">
                                    <div className="order-info">
                                        <span className="order-id">{order.orderNumber || `Order #${order.orderId}`}</span>
                                        <span className="order-date">{formatDate(order.createdAt)}</span>
                                    </div>
                                    <div className="order-status">
                                        {getStatusBadge(order.status)}
                                    </div>
                                </div>

                                <div className="order-items">
                                    {order.items?.slice(0, 3).map((item, index) => (
                                        <div key={index} className="order-item">
                                            <img
                                                src={item.imageUrl || 'https://via.placeholder.com/50x50?text=No+Image'}
                                                alt={item.productName}
                                                className="order-item-image"
                                            />
                                            <div className="order-item-info">
                                                <span className="order-item-name">{item.productName}</span>
                                                <span className="order-item-qty">x{item.quantity}</span>
                                            </div>
                                        </div>
                                    ))}
                                    {order.items?.length > 3 && (
                                        <span className="more-items">+{order.items.length - 3} more</span>
                                    )}
                                </div>

                                <div className="order-footer">
                                    <div className="order-total">
                                        <span className="total-label">Total:</span>
                                        <span className="total-amount">{formatPrice(order.totalAmount)}</span>
                                    </div>
                                    <div className="order-actions">
                                        <Link to={`/orders/${order.orderId}/tracking`} className="btn btn-primary btn-sm">
                                            📍 Track Order
                                        </Link>
                                        <Link to={`/orders/${order.orderId}`} className="btn btn-secondary btn-sm">
                                            View Details
                                        </Link>
                                        {(order.status === 'PLACED' || order.status === 'PENDING_PAYMENT') && (
                                            <button
                                                className="btn btn-danger btn-sm"
                                                onClick={() => handleCancel(order.orderId)}
                                            >
                                                Cancel
                                            </button>
                                        )}
                                    </div>
                                </div>
                            </div>
                        ))}
                    </div>
                )}
            </div>
        </div>
    );
};

export default Orders;

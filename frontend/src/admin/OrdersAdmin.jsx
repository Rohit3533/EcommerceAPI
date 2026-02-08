import { useState, useEffect } from 'react';
import { adminAPI } from '../api/api';
import './AdminPages.css';

const OrdersAdmin = () => {
    const [orders, setOrders] = useState([]);
    const [deliveryPartners, setDeliveryPartners] = useState([]);
    const [loading, setLoading] = useState(true);
    const [assigningOrder, setAssigningOrder] = useState(null);

    useEffect(() => {
        fetchOrders();
        fetchDeliveryPartners();
    }, []);

    const fetchOrders = async () => {
        try {
            const data = await adminAPI.getOrders();
            setOrders(data || []);
        } catch (error) {
            console.error('Error fetching orders:', error);
        } finally {
            setLoading(false);
        }
    };

    const fetchDeliveryPartners = async () => {
        try {
            const data = await adminAPI.getActiveDeliveryPartners();
            setDeliveryPartners(data || []);
        } catch (error) {
            console.error('Error fetching delivery partners:', error);
        }
    };

    const handleStatusChange = async (orderId, newStatus) => {
        try {
            await adminAPI.updateOrderStatus(orderId, newStatus);
            fetchOrders();
        } catch (error) {
            alert(error.message || 'Failed to update status');
        }
    };

    const handleAssignDelivery = async (orderId, deliveryPartnerId) => {
        if (!deliveryPartnerId) return;
        try {
            await adminAPI.assignDelivery(orderId, deliveryPartnerId);
            setAssigningOrder(null);
            fetchOrders();
        } catch (error) {
            alert(error.message || 'Failed to assign delivery partner');
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

    const statusOptions = [
        'PENDING_PAYMENT',
        'PLACED',
        'CONFIRMED',
        'PROCESSING',
        'SHIPPED',
        'OUT_FOR_DELIVERY',
        'DELIVERED',
        'CANCELLED'
    ];

    const getStatusClass = (status) => {
        const classes = {
            PENDING_PAYMENT: 'badge-warning',
            PAYMENT_FAILED: 'badge-danger',
            PLACED: 'badge-success',
            CONFIRMED: 'badge-info',
            PROCESSING: 'badge-info',
            SHIPPED: 'badge-info',
            OUT_FOR_DELIVERY: 'badge-primary',
            DELIVERED: 'badge-success',
            CANCELLED: 'badge-danger',
        };
        return classes[status] || 'badge-info';
    };

    const canAssignDelivery = (status, hasDeliveryPartner) => {
        // Allow assignment for orders that are paid and not yet delivered/cancelled
        const eligibleStatuses = ['PLACED', 'CONFIRMED', 'PROCESSING', 'SHIPPED', 'OUT_FOR_DELIVERY'];
        return eligibleStatuses.includes(status) && !hasDeliveryPartner;
    };

    if (loading) {
        return (
            <div className="loading-container">
                <div className="spinner"></div>
            </div>
        );
    }

    return (
        <div className="admin-page">
            <div className="page-header">
                <div>
                    <h1 className="page-title">Orders</h1>
                    <p className="page-subtitle">Manage customer orders and assign delivery partners</p>
                </div>
            </div>

            <div className="table-container card">
                <table className="table">
                    <thead>
                        <tr>
                            <th>Order</th>
                            <th>Customer</th>
                            <th>Items</th>
                            <th>Amount</th>
                            <th>Payment</th>
                            <th>Status</th>
                            <th>Delivery</th>
                            <th>Date</th>
                        </tr>
                    </thead>
                    <tbody>
                        {orders.map((order) => (
                            <tr key={order.orderId}>
                                <td>
                                    <div className="order-id-cell">
                                        <span className="order-number">{order.orderNumber || `#${order.orderId}`}</span>
                                        {order.trackingId && (
                                            <span className="tracking-id">{order.trackingId}</span>
                                        )}
                                    </div>
                                </td>
                                <td>
                                    <div className="customer-cell">
                                        <span className="customer-name">{order.userName || 'N/A'}</span>
                                        <span className="customer-email">{order.userEmail}</span>
                                    </div>
                                </td>
                                <td>{order.items?.length || 0} items</td>
                                <td className="price">{formatPrice(order.totalAmount)}</td>
                                <td>
                                    <span className={`badge ${order.paymentStatus === 'SUCCESS' ? 'badge-success' : 'badge-warning'}`}>
                                        {order.paymentStatus}
                                    </span>
                                </td>
                                <td>
                                    <select
                                        className="form-select status-select"
                                        value={order.status}
                                        onChange={(e) => handleStatusChange(order.orderId, e.target.value)}
                                    >
                                        {statusOptions.map((status) => (
                                            <option key={status} value={status}>
                                                {status.replace(/_/g, ' ')}
                                            </option>
                                        ))}
                                    </select>
                                </td>
                                <td>
                                    {order.deliveryPartnerName ? (
                                        <div className="delivery-assigned">
                                            <span className="partner-name">🚚 {order.deliveryPartnerName}</span>
                                        </div>
                                    ) : canAssignDelivery(order.status, order.deliveryPartnerName) ? (
                                        assigningOrder === order.orderId ? (
                                            <div className="assign-delivery-dropdown">
                                                <select
                                                    className="form-select"
                                                    onChange={(e) => handleAssignDelivery(order.orderId, e.target.value)}
                                                    defaultValue=""
                                                >
                                                    <option value="" disabled>Select partner</option>
                                                    {deliveryPartners.map((partner) => (
                                                        <option key={partner.id} value={partner.id}>
                                                            {partner.name} ({partner.vehicleType})
                                                        </option>
                                                    ))}
                                                </select>
                                                <button
                                                    className="btn btn-secondary btn-sm"
                                                    onClick={() => setAssigningOrder(null)}
                                                >
                                                    ✕
                                                </button>
                                            </div>
                                        ) : (
                                            <button
                                                className="btn btn-primary btn-sm"
                                                onClick={() => setAssigningOrder(order.orderId)}
                                            >
                                                🚚 Assign
                                            </button>
                                        )
                                    ) : (
                                        <span className="text-muted">-</span>
                                    )}
                                </td>
                                <td>{formatDate(order.createdAt)}</td>
                            </tr>
                        ))}
                    </tbody>
                </table>
                {orders.length === 0 && <p className="no-data">No orders yet</p>}
            </div>
        </div>
    );
};

export default OrdersAdmin;

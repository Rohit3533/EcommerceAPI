import { useState, useEffect } from 'react';
import { adminAPI } from '../api/api';
import './Dashboard.css';

const Dashboard = () => {
    const [stats, setStats] = useState({
        products: 0,
        orders: 0,
        users: 0,
        revenue: 0,
    });
    const [recentOrders, setRecentOrders] = useState([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        fetchData();
    }, []);

    const fetchData = async () => {
        try {
            const [products, orders, users] = await Promise.all([
                adminAPI.getProducts(0, 1),
                adminAPI.getOrders(),
                adminAPI.getUsers(),
            ]);

            const totalProducts = products.totalElements || products.length || 0;
            const ordersList = orders || [];
            const usersList = users || [];

            const revenue = ordersList
                .filter((o) => o.paymentStatus === 'SUCCESS')
                .reduce((sum, o) => sum + (o.totalAmount || 0), 0);

            setStats({
                products: totalProducts,
                orders: ordersList.length,
                users: usersList.length,
                revenue,
            });

            setRecentOrders(ordersList.slice(0, 5));
        } catch (error) {
            console.error('Error fetching dashboard data:', error);
        } finally {
            setLoading(false);
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
            month: 'short',
            day: 'numeric',
        });
    };

    const getStatusBadge = (status) => {
        const statusConfig = {
            PENDING_PAYMENT: { class: 'badge-warning', label: 'Pending' },
            PLACED: { class: 'badge-success', label: 'Placed' },
            SHIPPED: { class: 'badge-info', label: 'Shipped' },
            DELIVERED: { class: 'badge-success', label: 'Delivered' },
            CANCELLED: { class: 'badge-danger', label: 'Cancelled' },
        };
        const config = statusConfig[status] || { class: 'badge-info', label: status };
        return <span className={`badge ${config.class}`}>{config.label}</span>;
    };

    if (loading) {
        return (
            <div className="loading-container">
                <div className="spinner"></div>
            </div>
        );
    }

    return (
        <div className="dashboard">
            <h1 className="page-title">Dashboard</h1>
            <p className="page-subtitle">Welcome to your admin dashboard</p>

            {/* Stats Grid */}
            <div className="stats-grid">
                <div className="stat-card">
                    <div className="stat-icon">📦</div>
                    <div className="stat-info">
                        <span className="stat-value">{stats.products}</span>
                        <span className="stat-label">Products</span>
                    </div>
                </div>
                <div className="stat-card">
                    <div className="stat-icon">📋</div>
                    <div className="stat-info">
                        <span className="stat-value">{stats.orders}</span>
                        <span className="stat-label">Orders</span>
                    </div>
                </div>
                <div className="stat-card">
                    <div className="stat-icon">👥</div>
                    <div className="stat-info">
                        <span className="stat-value">{stats.users}</span>
                        <span className="stat-label">Users</span>
                    </div>
                </div>
                <div className="stat-card revenue">
                    <div className="stat-icon">💰</div>
                    <div className="stat-info">
                        <span className="stat-value">{formatPrice(stats.revenue)}</span>
                        <span className="stat-label">Revenue</span>
                    </div>
                </div>
            </div>

            {/* Recent Orders */}
            <div className="recent-orders card">
                <h2 className="section-title">Recent Orders</h2>
                {recentOrders.length > 0 ? (
                    <div className="table-container">
                        <table className="table">
                            <thead>
                                <tr>
                                    <th>Order ID</th>
                                    <th>Customer</th>
                                    <th>Amount</th>
                                    <th>Status</th>
                                    <th>Date</th>
                                </tr>
                            </thead>
                            <tbody>
                                {recentOrders.map((order) => (
                                    <tr key={order.id}>
                                        <td>#{order.id}</td>
                                        <td>{order.userName || order.userEmail || 'N/A'}</td>
                                        <td className="amount">{formatPrice(order.totalAmount)}</td>
                                        <td>{getStatusBadge(order.status)}</td>
                                        <td>{formatDate(order.createdAt)}</td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    </div>
                ) : (
                    <p className="no-data">No orders yet</p>
                )}
            </div>
        </div>
    );
};

export default Dashboard;

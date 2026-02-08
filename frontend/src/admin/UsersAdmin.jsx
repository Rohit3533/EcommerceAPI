import { useState, useEffect } from 'react';
import { adminAPI } from '../api/api';
import './AdminPages.css';

const UsersAdmin = () => {
    const [users, setUsers] = useState([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        fetchUsers();
    }, []);

    const fetchUsers = async () => {
        try {
            const data = await adminAPI.getUsers();
            setUsers(data || []);
        } catch (error) {
            console.error('Error fetching users:', error);
        } finally {
            setLoading(false);
        }
    };

    const handleRoleChange = async (userId, newRole) => {
        try {
            await adminAPI.updateUserRole(userId, newRole);
            fetchUsers();
        } catch (error) {
            alert(error.message || 'Failed to update role');
        }
    };

    const handleStatusChange = async (userId, newStatus) => {
        try {
            await adminAPI.updateUserStatus(userId, newStatus);
            fetchUsers();
        } catch (error) {
            alert(error.message || 'Failed to update status');
        }
    };

    const formatDate = (date) => {
        return new Date(date).toLocaleDateString('en-US', {
            year: 'numeric',
            month: 'short',
            day: 'numeric',
        });
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
                    <h1 className="page-title">Users</h1>
                    <p className="page-subtitle">Manage user accounts</p>
                </div>
            </div>

            <div className="table-container card">
                <table className="table">
                    <thead>
                        <tr>
                            <th>User</th>
                            <th>Email</th>
                            <th>Role</th>
                            <th>Status</th>
                            <th>Joined</th>
                        </tr>
                    </thead>
                    <tbody>
                        {users.map((user) => (
                            <tr key={user.id}>
                                <td>
                                    <div className="user-cell">
                                        <span className="user-avatar">{user.name?.charAt(0) || '?'}</span>
                                        <span className="user-name">{user.name}</span>
                                    </div>
                                </td>
                                <td>{user.email}</td>
                                <td>
                                    <select
                                        className="form-select role-select"
                                        value={user.role}
                                        onChange={(e) => handleRoleChange(user.id, e.target.value)}
                                    >
                                        <option value="CUSTOMER">CUSTOMER</option>
                                        <option value="ADMIN">ADMIN</option>
                                    </select>
                                </td>
                                <td>
                                    <select
                                        className={`form-select status-select ${user.status === 'ACTIVE' ? 'status-active' : 'status-blocked'}`}
                                        value={user.status}
                                        onChange={(e) => handleStatusChange(user.id, e.target.value)}
                                    >
                                        <option value="ACTIVE">ACTIVE</option>
                                        <option value="BLOCKED">BLOCKED</option>
                                    </select>
                                </td>
                                <td>{formatDate(user.createdAt)}</td>
                            </tr>
                        ))}
                    </tbody>
                </table>
                {users.length === 0 && <p className="no-data">No users yet</p>}
            </div>
        </div>
    );
};

export default UsersAdmin;

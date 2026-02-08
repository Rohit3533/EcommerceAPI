import { useState, useEffect } from 'react';
import { deliveryAPI } from '../api/api';
import './AdminPages.css';

const API_BASE = 'http://localhost:8080/api';

// Helper for admin verification API
const adminVerifyAPI = {
    getPending: async () => {
        const token = localStorage.getItem('token');
        const response = await fetch(`${API_BASE}/admin/delivery-partners/pending`, {
            headers: { Authorization: `Bearer ${token}` },
        });
        const data = await response.json();
        if (!response.ok) throw new Error(data.message || 'Failed to fetch');
        return data.data || data;
    },
    verify: async (id, approved, rejectionReason) => {
        const token = localStorage.getItem('token');
        const response = await fetch(`${API_BASE}/admin/delivery-partners/${id}/verify`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
                Authorization: `Bearer ${token}`,
            },
            body: JSON.stringify({ approved, rejectionReason }),
        });
        const data = await response.json();
        if (!response.ok) throw new Error(data.message || 'Failed to verify');
        return data.data || data;
    },
};

const DeliveryPartnersAdmin = () => {
    const [activeTab, setActiveTab] = useState('all');
    const [partners, setPartners] = useState([]);
    const [pendingPartners, setPendingPartners] = useState([]);
    const [loading, setLoading] = useState(true);
    const [showModal, setShowModal] = useState(false);
    const [showRejectModal, setShowRejectModal] = useState(false);
    const [rejectingPartner, setRejectingPartner] = useState(null);
    const [rejectionReason, setRejectionReason] = useState('');
    const [editingPartner, setEditingPartner] = useState(null);
    const [formData, setFormData] = useState({
        name: '',
        phone: '',
        email: '',
        vehicleNumber: '',
        vehicleType: 'BIKE',
        status: 'ACTIVE',
    });

    useEffect(() => {
        fetchData();
    }, []);

    const fetchData = async () => {
        try {
            const [allData, pendingData] = await Promise.all([
                deliveryAPI.getAll(),
                adminVerifyAPI.getPending().catch(() => []),
            ]);
            setPartners(allData || []);
            setPendingPartners(pendingData || []);
        } catch (error) {
            console.error('Error fetching delivery partners:', error);
        } finally {
            setLoading(false);
        }
    };

    const handleApprove = async (id) => {
        if (!window.confirm('Approve this delivery partner?')) return;
        try {
            await adminVerifyAPI.verify(id, true, null);
            fetchData();
            alert('Partner approved successfully!');
        } catch (error) {
            alert(error.message || 'Failed to approve');
        }
    };

    const handleRejectClick = (partner) => {
        setRejectingPartner(partner);
        setRejectionReason('');
        setShowRejectModal(true);
    };

    const handleRejectSubmit = async () => {
        if (!rejectionReason.trim()) {
            alert('Please provide a rejection reason');
            return;
        }
        try {
            await adminVerifyAPI.verify(rejectingPartner.id, false, rejectionReason);
            setShowRejectModal(false);
            setRejectingPartner(null);
            fetchData();
            alert('Partner rejected.');
        } catch (error) {
            alert(error.message || 'Failed to reject');
        }
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        try {
            if (editingPartner) {
                await deliveryAPI.update(editingPartner.id, formData);
            } else {
                await deliveryAPI.create(formData);
            }
            setShowModal(false);
            resetForm();
            fetchData();
        } catch (error) {
            alert(error.message || 'Failed to save delivery partner');
        }
    };

    const handleEdit = (partner) => {
        setEditingPartner(partner);
        setFormData({
            name: partner.name,
            phone: partner.phone,
            email: partner.email || '',
            vehicleNumber: partner.vehicleNumber || '',
            vehicleType: partner.vehicleType || 'BIKE',
            status: partner.status,
        });
        setShowModal(true);
    };

    const handleDelete = async (id) => {
        if (!window.confirm('Are you sure you want to delete this delivery partner?')) return;
        try {
            await deliveryAPI.delete(id);
            fetchData();
        } catch (error) {
            alert(error.message || 'Failed to delete delivery partner');
        }
    };

    const resetForm = () => {
        setEditingPartner(null);
        setFormData({
            name: '',
            phone: '',
            email: '',
            vehicleNumber: '',
            vehicleType: 'BIKE',
            status: 'ACTIVE',
        });
    };

    const openAddModal = () => {
        resetForm();
        setShowModal(true);
    };

    const getStatusClass = (status) => {
        return status === 'ACTIVE' ? 'badge-success' : 'badge-danger';
    };

    const getVerificationClass = (status) => {
        const classes = {
            PENDING: 'badge-warning',
            APPROVED: 'badge-success',
            REJECTED: 'badge-danger',
        };
        return classes[status] || 'badge-secondary';
    };

    const getVehicleIcon = (type) => {
        const icons = { BIKE: '🏍️', SCOOTER: '🛵', CAR: '🚗', VAN: '🚐', TRUCK: '🚛' };
        return icons[type] || '🚗';
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
                    <h1 className="page-title">Delivery Partners</h1>
                    <p className="page-subtitle">Manage and verify delivery partners</p>
                </div>
                <button className="btn btn-primary" onClick={openAddModal}>
                    + Add Partner
                </button>
            </div>

            {/* Tabs */}
            <div className="tabs" style={{ marginBottom: '1.5rem', display: 'flex', gap: '0.5rem' }}>
                <button
                    className={`btn ${activeTab === 'all' ? 'btn-primary' : 'btn-secondary'}`}
                    onClick={() => setActiveTab('all')}
                >
                    All Partners ({partners.length})
                </button>
                <button
                    className={`btn ${activeTab === 'pending' ? 'btn-primary' : 'btn-secondary'}`}
                    onClick={() => setActiveTab('pending')}
                    style={pendingPartners.length > 0 ? { position: 'relative' } : {}}
                >
                    ⏳ Pending Verification ({pendingPartners.length})
                    {pendingPartners.length > 0 && (
                        <span style={{
                            position: 'absolute',
                            top: '-5px',
                            right: '-5px',
                            background: '#ef4444',
                            color: '#fff',
                            borderRadius: '50%',
                            width: '20px',
                            height: '20px',
                            fontSize: '0.7rem',
                            display: 'flex',
                            alignItems: 'center',
                            justifyContent: 'center',
                        }}>
                            {pendingPartners.length}
                        </span>
                    )}
                </button>
            </div>

            {/* Pending Verification Tab */}
            {activeTab === 'pending' && (
                <div className="table-container card">
                    <table className="table">
                        <thead>
                            <tr>
                                <th>Name</th>
                                <th>Phone</th>
                                <th>Email</th>
                                <th>Vehicle</th>
                                <th>Documents</th>
                                <th>Actions</th>
                            </tr>
                        </thead>
                        <tbody>
                            {pendingPartners.map((partner) => (
                                <tr key={partner.id}>
                                    <td><strong>{partner.name}</strong></td>
                                    <td>{partner.phone}</td>
                                    <td>{partner.email || '-'}</td>
                                    <td>
                                        <span className="vehicle-info">
                                            {getVehicleIcon(partner.vehicleType)} {partner.vehicleType}
                                            {partner.vehicleNumber && <span className="vehicle-number"> • {partner.vehicleNumber}</span>}
                                        </span>
                                    </td>
                                    <td>
                                        <div style={{ fontSize: '0.8rem' }}>
                                            {partner.identityDocType && <div>📄 {partner.identityDocType}: {partner.identityDocNumber}</div>}
                                            {partner.drivingLicenseNumber && <div>🪪 DL: {partner.drivingLicenseNumber}</div>}
                                            {partner.identityDocUrl && (
                                                <a href={partner.identityDocUrl} target="_blank" rel="noopener noreferrer" style={{ color: '#667eea' }}>
                                                    View ID Doc
                                                </a>
                                            )}
                                            {partner.drivingLicenseUrl && (
                                                <a href={partner.drivingLicenseUrl} target="_blank" rel="noopener noreferrer" style={{ color: '#667eea', marginLeft: '0.5rem' }}>
                                                    View DL
                                                </a>
                                            )}
                                        </div>
                                    </td>
                                    <td>
                                        <div className="action-buttons">
                                            <button
                                                className="btn btn-success btn-sm"
                                                onClick={() => handleApprove(partner.id)}
                                            >
                                                ✅ Approve
                                            </button>
                                            <button
                                                className="btn btn-danger btn-sm"
                                                onClick={() => handleRejectClick(partner)}
                                            >
                                                ❌ Reject
                                            </button>
                                        </div>
                                    </td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                    {pendingPartners.length === 0 && <p className="no-data">No pending verifications 🎉</p>}
                </div>
            )}

            {/* All Partners Tab */}
            {activeTab === 'all' && (
                <div className="table-container card">
                    <table className="table">
                        <thead>
                            <tr>
                                <th>Name</th>
                                <th>Phone</th>
                                <th>Email</th>
                                <th>Vehicle</th>
                                <th>Verification</th>
                                <th>Status</th>
                                <th>Deliveries</th>
                                <th>Actions</th>
                            </tr>
                        </thead>
                        <tbody>
                            {partners.map((partner) => (
                                <tr key={partner.id}>
                                    <td><strong>{partner.name}</strong></td>
                                    <td>{partner.phone}</td>
                                    <td>{partner.email || '-'}</td>
                                    <td>
                                        <span className="vehicle-info">
                                            {getVehicleIcon(partner.vehicleType)} {partner.vehicleType}
                                            {partner.vehicleNumber && <span className="vehicle-number"> • {partner.vehicleNumber}</span>}
                                        </span>
                                    </td>
                                    <td>
                                        <span className={`badge ${getVerificationClass(partner.verificationStatus)}`}>
                                            {partner.verificationStatus || 'PENDING'}
                                        </span>
                                    </td>
                                    <td>
                                        <span className={`badge ${getStatusClass(partner.status)}`}>
                                            {partner.status}
                                        </span>
                                    </td>
                                    <td>{partner.totalDeliveries || 0}</td>
                                    <td>
                                        <div className="action-buttons">
                                            <button
                                                className="btn btn-secondary btn-sm"
                                                onClick={() => handleEdit(partner)}
                                            >
                                                Edit
                                            </button>
                                            <button
                                                className="btn btn-danger btn-sm"
                                                onClick={() => handleDelete(partner.id)}
                                            >
                                                Delete
                                            </button>
                                        </div>
                                    </td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                    {partners.length === 0 && <p className="no-data">No delivery partners yet</p>}
                </div>
            )}

            {/* Add/Edit Modal */}
            {showModal && (
                <div className="modal-overlay" onClick={() => setShowModal(false)}>
                    <div className="modal" onClick={(e) => e.stopPropagation()}>
                        <div className="modal-header">
                            <h2 className="modal-title">{editingPartner ? '✏️ Edit Delivery Partner' : '🚚 Add Delivery Partner'}</h2>
                            <button className="modal-close" onClick={() => setShowModal(false)}>×</button>
                        </div>
                        <form onSubmit={handleSubmit} className="modal-body">
                            <div className="form-group">
                                <label className="form-label">Name *</label>
                                <input
                                    type="text"
                                    className="form-input"
                                    value={formData.name}
                                    onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                                    placeholder="Enter partner name"
                                    required
                                />
                            </div>
                            <div className="form-group">
                                <label className="form-label">Phone *</label>
                                <input
                                    type="tel"
                                    className="form-input"
                                    value={formData.phone}
                                    onChange={(e) => setFormData({ ...formData, phone: e.target.value })}
                                    placeholder="+91 9876543210"
                                    required
                                />
                            </div>
                            <div className="form-group">
                                <label className="form-label">Email</label>
                                <input
                                    type="email"
                                    className="form-input"
                                    value={formData.email}
                                    onChange={(e) => setFormData({ ...formData, email: e.target.value })}
                                    placeholder="partner@email.com"
                                />
                            </div>
                            <div className="form-row">
                                <div className="form-group">
                                    <label className="form-label">Vehicle Type</label>
                                    <select
                                        className="form-select"
                                        value={formData.vehicleType}
                                        onChange={(e) => setFormData({ ...formData, vehicleType: e.target.value })}
                                    >
                                        <option value="BIKE">🏍️ Bike</option>
                                        <option value="SCOOTER">🛵 Scooter</option>
                                        <option value="CAR">🚗 Car</option>
                                        <option value="VAN">🚐 Van</option>
                                        <option value="TRUCK">🚛 Truck</option>
                                    </select>
                                </div>
                                <div className="form-group">
                                    <label className="form-label">Vehicle Number</label>
                                    <input
                                        type="text"
                                        className="form-input"
                                        value={formData.vehicleNumber}
                                        onChange={(e) => setFormData({ ...formData, vehicleNumber: e.target.value })}
                                        placeholder="MH01AB1234"
                                    />
                                </div>
                            </div>
                            {editingPartner && (
                                <div className="form-group">
                                    <label className="form-label">Status</label>
                                    <select
                                        className="form-select"
                                        value={formData.status}
                                        onChange={(e) => setFormData({ ...formData, status: e.target.value })}
                                    >
                                        <option value="ACTIVE">✅ Active</option>
                                        <option value="INACTIVE">❌ Inactive</option>
                                    </select>
                                </div>
                            )}
                            <div className="modal-footer">
                                <button type="button" className="btn btn-secondary" onClick={() => setShowModal(false)}>
                                    Cancel
                                </button>
                                <button type="submit" className="btn btn-primary">
                                    {editingPartner ? '✓ Update Partner' : '+ Create Partner'}
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            )}

            {/* Rejection Modal */}
            {showRejectModal && (
                <div className="modal-overlay" onClick={() => setShowRejectModal(false)}>
                    <div className="modal" onClick={(e) => e.stopPropagation()} style={{ maxWidth: '400px' }}>
                        <div className="modal-header">
                            <h2 className="modal-title">❌ Reject Partner</h2>
                            <button className="modal-close" onClick={() => setShowRejectModal(false)}>×</button>
                        </div>
                        <div className="modal-body">
                            <p style={{ color: 'rgba(255,255,255,0.7)', marginBottom: '1rem' }}>
                                Rejecting: <strong>{rejectingPartner?.name}</strong>
                            </p>
                            <div className="form-group">
                                <label className="form-label">Rejection Reason *</label>
                                <textarea
                                    className="form-input"
                                    value={rejectionReason}
                                    onChange={(e) => setRejectionReason(e.target.value)}
                                    placeholder="Please provide a reason for rejection..."
                                    rows="3"
                                    required
                                    style={{ resize: 'vertical' }}
                                />
                            </div>
                            <div className="modal-footer">
                                <button className="btn btn-secondary" onClick={() => setShowRejectModal(false)}>
                                    Cancel
                                </button>
                                <button className="btn btn-danger" onClick={handleRejectSubmit}>
                                    Confirm Rejection
                                </button>
                            </div>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
};

export default DeliveryPartnersAdmin;

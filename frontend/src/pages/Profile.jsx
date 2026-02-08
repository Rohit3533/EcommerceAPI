import { useState, useEffect } from 'react';
import { profileAPI, addressesAPI } from '../api/api';
import { useAuth } from '../context/AuthContext';
import AddressForm from '../components/AddressForm';
import '../components/AddressForm.css';
import './Profile.css';

const Profile = () => {
    const { user, checkAuth } = useAuth();
    const [loading, setLoading] = useState(false);
    const [success, setSuccess] = useState('');
    const [error, setError] = useState('');
    const [formData, setFormData] = useState({
        name: user?.name || '',
        phone: user?.phone || '',
    });

    // Address state
    const [addresses, setAddresses] = useState([]);
    const [addressLoading, setAddressLoading] = useState(true);
    const [showAddressModal, setShowAddressModal] = useState(false);
    const [editingAddress, setEditingAddress] = useState(null);
    const [addressError, setAddressError] = useState('');

    // Fetch addresses
    useEffect(() => {
        const fetchAddresses = async () => {
            try {
                setAddressLoading(true);
                const data = await addressesAPI.getAll();
                setAddresses(data || []);
            } catch (err) {
                console.error('Failed to fetch addresses:', err);
            } finally {
                setAddressLoading(false);
            }
        };
        fetchAddresses();
    }, []);

    const handleSubmit = async (e) => {
        e.preventDefault();
        try {
            setLoading(true);
            setError('');
            setSuccess('');
            await profileAPI.update(formData);
            await checkAuth();
            setSuccess('Profile updated successfully!');
        } catch (err) {
            setError(err.message || 'Failed to update profile');
        } finally {
            setLoading(false);
        }
    };

    const handleAddAddress = async (addressData) => {
        try {
            setAddressLoading(true);
            setAddressError('');
            const newAddress = await addressesAPI.add(addressData);
            setAddresses([...addresses, newAddress]);
            setShowAddressModal(false);
            setSuccess('Address added successfully!');
        } catch (err) {
            setAddressError(err.message || 'Failed to add address');
        } finally {
            setAddressLoading(false);
        }
    };

    const handleEditAddress = async (addressData) => {
        try {
            setAddressLoading(true);
            setAddressError('');
            const updated = await addressesAPI.update(editingAddress.id, addressData);
            setAddresses(addresses.map(a => a.id === editingAddress.id ? updated : a));
            setEditingAddress(null);
            setShowAddressModal(false);
            setSuccess('Address updated successfully!');
        } catch (err) {
            setAddressError(err.message || 'Failed to update address');
        } finally {
            setAddressLoading(false);
        }
    };

    const handleDeleteAddress = async (id) => {
        if (!window.confirm('Are you sure you want to delete this address?')) return;
        try {
            setAddressLoading(true);
            await addressesAPI.delete(id);
            setAddresses(addresses.filter(a => a.id !== id));
            setSuccess('Address deleted successfully!');
        } catch (err) {
            setError(err.message || 'Failed to delete address');
        } finally {
            setAddressLoading(false);
        }
    };

    const handleSetDefault = async (id) => {
        try {
            setAddressLoading(true);
            await addressesAPI.setDefault(id);
            setAddresses(addresses.map(a => ({ ...a, isDefault: a.id === id })));
            setSuccess('Default address updated!');
        } catch (err) {
            setError(err.message || 'Failed to set default address');
        } finally {
            setAddressLoading(false);
        }
    };

    const openEditModal = (address) => {
        setEditingAddress(address);
        setShowAddressModal(true);
    };

    const closeModal = () => {
        setShowAddressModal(false);
        setEditingAddress(null);
        setAddressError('');
    };

    const formatDate = (date) => {
        return new Date(date).toLocaleDateString('en-US', {
            year: 'numeric',
            month: 'long',
            day: 'numeric',
        });
    };

    return (
        <div className="profile-page page">
            <div className="container">
                <h1 className="page-title">My Profile</h1>
                <p className="page-subtitle">Manage your account settings</p>

                <div className="profile-layout">
                    <div className="profile-card card">
                        <div className="profile-header">
                            <div className="profile-avatar">
                                {user?.name?.charAt(0).toUpperCase() || '?'}
                            </div>
                            <div className="profile-info">
                                <h2 className="profile-name">{user?.name}</h2>
                                <p className="profile-email">{user?.email}</p>
                                <span className={`badge ${user?.role === 'ADMIN' ? 'badge-info' : 'badge-success'}`}>
                                    {user?.role}
                                </span>
                                {user?.emailVerified && (
                                    <span className="badge badge-success" style={{ marginLeft: 8 }}>
                                        ✓ Verified
                                    </span>
                                )}
                            </div>
                        </div>

                        <div className="profile-meta">
                            <div className="meta-item">
                                <span className="meta-label">Member Since</span>
                                <span className="meta-value">{user?.createdAt ? formatDate(user.createdAt) : 'N/A'}</span>
                            </div>
                            <div className="meta-item">
                                <span className="meta-label">Account Status</span>
                                <span className="meta-value status-active">Active</span>
                            </div>
                        </div>
                    </div>

                    <div className="profile-form-card card">
                        <h3 className="form-title">Edit Profile</h3>

                        <form onSubmit={handleSubmit}>
                            <div className="form-group">
                                <label className="form-label">Email Address</label>
                                <input
                                    type="email"
                                    className="form-input"
                                    value={user?.email || ''}
                                    disabled
                                />
                                <p className="form-hint">Email cannot be changed</p>
                            </div>

                            <div className="form-group">
                                <label className="form-label">Full Name</label>
                                <input
                                    type="text"
                                    className="form-input"
                                    value={formData.name}
                                    onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                                    required
                                />
                            </div>

                            <div className="form-group">
                                <label className="form-label">Phone Number</label>
                                <input
                                    type="tel"
                                    className="form-input"
                                    value={formData.phone}
                                    onChange={(e) => setFormData({ ...formData, phone: e.target.value })}
                                    placeholder="Enter your phone number"
                                />
                            </div>

                            {error && <div className="error-message">{error}</div>}
                            {success && <div className="success-message">{success}</div>}

                            <button
                                type="submit"
                                className="btn btn-primary btn-lg save-btn"
                                disabled={loading}
                            >
                                {loading ? '⏳ Saving...' : '💾 Save Changes'}
                            </button>
                        </form>
                    </div>
                </div>

                {/* Addresses Section */}
                <div className="addresses-section">
                    <div className="addresses-header">
                        <h2 className="addresses-title">📍 My Addresses</h2>
                        <button
                            className="btn btn-primary"
                            onClick={() => setShowAddressModal(true)}
                        >
                            + Add Address
                        </button>
                    </div>

                    {addressLoading && addresses.length === 0 ? (
                        <div className="loading-spinner">Loading addresses...</div>
                    ) : addresses.length === 0 ? (
                        <div className="no-addresses card">
                            <div className="no-addresses-icon">📭</div>
                            <p>No addresses saved yet</p>
                            <button
                                className="btn btn-primary"
                                onClick={() => setShowAddressModal(true)}
                            >
                                Add Your First Address
                            </button>
                        </div>
                    ) : (
                        <div className="addresses-grid">
                            {addresses.map((address) => (
                                <div
                                    key={address.id}
                                    className={`address-card ${address.isDefault ? 'default' : ''}`}
                                >
                                    <div className="address-label">
                                        {address.label === 'Home' ? '🏠' : address.label === 'Office' ? '🏢' : '📍'}
                                        {address.label}
                                        {address.isDefault && <span className="default-badge">Default</span>}
                                    </div>
                                    <div className="address-name">{address.fullName}</div>
                                    <div className="address-phone">{address.phone}</div>
                                    <div className="address-text">
                                        {address.streetAddress}<br />
                                        {address.city}, {address.state} {address.postalCode}<br />
                                        {address.country}
                                    </div>
                                    <div className="address-actions">
                                        <button
                                            className="btn btn-secondary"
                                            onClick={() => openEditModal(address)}
                                        >
                                            ✏️ Edit
                                        </button>
                                        {!address.isDefault && (
                                            <button
                                                className="btn btn-secondary"
                                                onClick={() => handleSetDefault(address.id)}
                                            >
                                                ⭐ Set Default
                                            </button>
                                        )}
                                        <button
                                            className="btn btn-danger"
                                            onClick={() => handleDeleteAddress(address.id)}
                                        >
                                            🗑️
                                        </button>
                                    </div>
                                </div>
                            ))}
                        </div>
                    )}
                </div>

                {/* Address Modal */}
                {showAddressModal && (
                    <div className="modal-overlay" onClick={closeModal}>
                        <div className="modal-content" onClick={(e) => e.stopPropagation()}>
                            <div className="modal-header">
                                <h3 className="modal-title">
                                    {editingAddress ? 'Edit Address' : 'Add New Address'}
                                </h3>
                                <button className="modal-close" onClick={closeModal}>×</button>
                            </div>
                            {addressError && (
                                <div className="error-message" style={{ margin: '0 20px' }}>
                                    {addressError}
                                </div>
                            )}
                            <AddressForm
                                address={editingAddress}
                                onSubmit={editingAddress ? handleEditAddress : handleAddAddress}
                                onCancel={closeModal}
                                loading={addressLoading}
                            />
                        </div>
                    </div>
                )}
            </div>
        </div>
    );
};

export default Profile;

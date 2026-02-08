import { useState } from 'react';
import './AddressForm.css';

const AddressForm = ({ address, onSubmit, onCancel, loading }) => {
    const [formData, setFormData] = useState({
        label: address?.label || 'Home',
        fullName: address?.fullName || '',
        phone: address?.phone || '',
        streetAddress: address?.streetAddress || '',
        city: address?.city || '',
        state: address?.state || '',
        postalCode: address?.postalCode || '',
        country: address?.country || 'India',
        isDefault: address?.isDefault || false,
    });

    const handleSubmit = (e) => {
        e.preventDefault();
        onSubmit(formData);
    };

    return (
        <form onSubmit={handleSubmit} className="address-form">
            <div className="form-row">
                <div className="form-group">
                    <label className="form-label">Label</label>
                    <select
                        className="form-input"
                        value={formData.label}
                        onChange={(e) => setFormData({ ...formData, label: e.target.value })}
                    >
                        <option value="Home">🏠 Home</option>
                        <option value="Office">🏢 Office</option>
                        <option value="Other">📍 Other</option>
                    </select>
                </div>
                <div className="form-group">
                    <label className="form-label">Full Name</label>
                    <input
                        type="text"
                        className="form-input"
                        value={formData.fullName}
                        onChange={(e) => setFormData({ ...formData, fullName: e.target.value })}
                        placeholder="John Doe"
                        required
                    />
                </div>
            </div>

            <div className="form-group">
                <label className="form-label">Phone Number</label>
                <input
                    type="tel"
                    className="form-input"
                    value={formData.phone}
                    onChange={(e) => setFormData({ ...formData, phone: e.target.value })}
                    placeholder="+91 9999999999"
                    required
                />
            </div>

            <div className="form-group">
                <label className="form-label">Street Address</label>
                <textarea
                    className="form-input"
                    rows={2}
                    value={formData.streetAddress}
                    onChange={(e) => setFormData({ ...formData, streetAddress: e.target.value })}
                    placeholder="House/Flat No., Building, Street, Area"
                    required
                />
            </div>

            <div className="form-row">
                <div className="form-group">
                    <label className="form-label">City</label>
                    <input
                        type="text"
                        className="form-input"
                        value={formData.city}
                        onChange={(e) => setFormData({ ...formData, city: e.target.value })}
                        placeholder="Mumbai"
                        required
                    />
                </div>
                <div className="form-group">
                    <label className="form-label">State</label>
                    <input
                        type="text"
                        className="form-input"
                        value={formData.state}
                        onChange={(e) => setFormData({ ...formData, state: e.target.value })}
                        placeholder="Maharashtra"
                        required
                    />
                </div>
            </div>

            <div className="form-row">
                <div className="form-group">
                    <label className="form-label">Postal Code</label>
                    <input
                        type="text"
                        className="form-input"
                        value={formData.postalCode}
                        onChange={(e) => setFormData({ ...formData, postalCode: e.target.value })}
                        placeholder="400001"
                        required
                    />
                </div>
                <div className="form-group">
                    <label className="form-label">Country</label>
                    <input
                        type="text"
                        className="form-input"
                        value={formData.country}
                        onChange={(e) => setFormData({ ...formData, country: e.target.value })}
                        placeholder="India"
                        required
                    />
                </div>
            </div>

            <div className="form-group checkbox-group">
                <label className="checkbox-label">
                    <input
                        type="checkbox"
                        checked={formData.isDefault}
                        onChange={(e) => setFormData({ ...formData, isDefault: e.target.checked })}
                    />
                    <span>Set as default address</span>
                </label>
            </div>

            <div className="form-actions">
                <button type="button" className="btn btn-secondary" onClick={onCancel}>
                    Cancel
                </button>
                <button type="submit" className="btn btn-primary" disabled={loading}>
                    {loading ? '⏳ Saving...' : '💾 Save Address'}
                </button>
            </div>
        </form>
    );
};

export default AddressForm;

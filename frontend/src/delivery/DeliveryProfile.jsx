import { useState } from 'react';
import { useOutletContext } from 'react-router-dom';
import { deliveryPartnerAPI } from '../api/api';
import './DeliveryPages.css';

const DeliveryProfile = () => {
    const { profile, fetchProfile } = useOutletContext();
    const [loading, setLoading] = useState(false);
    const [docForm, setDocForm] = useState({
        identityDocType: 'AADHAAR',
        identityDocNumber: '',
        identityDocUrl: '',
        drivingLicenseNumber: '',
        drivingLicenseUrl: '',
    });

    const handleDocChange = (e) => {
        setDocForm({ ...docForm, [e.target.name]: e.target.value });
    };

    const handleDocSubmit = async (e) => {
        e.preventDefault();
        setLoading(true);
        try {
            await deliveryPartnerAPI.uploadDocuments(docForm);
            alert('Documents uploaded successfully! Awaiting admin verification.');
            fetchProfile();
        } catch (error) {
            alert(error.message || 'Failed to upload documents');
        } finally {
            setLoading(false);
        }
    };

    const getVerificationBadge = () => {
        const status = profile?.verificationStatus || 'PENDING';
        const badges = {
            PENDING: { class: 'pending', text: '⏳ Verification Pending' },
            APPROVED: { class: 'approved', text: '✅ Verified' },
            REJECTED: { class: 'rejected', text: '❌ Rejected' },
        };
        return badges[status] || badges.PENDING;
    };

    const badge = getVerificationBadge();

    return (
        <div className="delivery-dashboard">
            <div className="dashboard-header">
                <h1>👤 Profile & Documents</h1>
                <p>Manage your profile and verification documents</p>
            </div>

            {/* Verification Status */}
            <div className="profile-section">
                <h2>🔐 Verification Status</h2>
                <div className={`verification-status ${badge.class}`}>
                    {badge.text}
                </div>
                {profile?.verificationStatus === 'REJECTED' && profile?.rejectionReason && (
                    <div style={{
                        marginTop: '1rem',
                        color: '#ef4444',
                        padding: '0.8rem',
                        background: 'rgba(239, 68, 68, 0.1)',
                        borderRadius: '8px'
                    }}>
                        <strong>Reason:</strong> {profile.rejectionReason}
                    </div>
                )}
            </div>

            {/* Profile Info */}
            <div className="profile-section">
                <h2>📋 Profile Information</h2>
                <div style={{ display: 'grid', gap: '1rem', color: 'rgba(255,255,255,0.8)' }}>
                    <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                        <span>Name:</span>
                        <strong>{profile?.name}</strong>
                    </div>
                    <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                        <span>Email:</span>
                        <strong>{profile?.email}</strong>
                    </div>
                    <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                        <span>Phone:</span>
                        <strong>{profile?.phone}</strong>
                    </div>
                    <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                        <span>Vehicle:</span>
                        <strong>{profile?.vehicleType} - {profile?.vehicleNumber}</strong>
                    </div>
                </div>
            </div>

            {/* Document Upload */}
            <div className="profile-section">
                <h2>📄 Upload Documents</h2>
                <p style={{ color: 'rgba(255,255,255,0.6)', marginBottom: '1.5rem' }}>
                    Upload your identity proof and driving license for verification
                </p>

                <form onSubmit={handleDocSubmit} className="document-upload-form">
                    <div className="form-group">
                        <label>Identity Document Type</label>
                        <select
                            name="identityDocType"
                            value={docForm.identityDocType}
                            onChange={handleDocChange}
                            className="form-select"
                        >
                            <option value="AADHAAR">Aadhaar Card</option>
                            <option value="PAN">PAN Card</option>
                        </select>
                    </div>

                    <div className="form-group">
                        <label>{docForm.identityDocType === 'AADHAAR' ? 'Aadhaar' : 'PAN'} Number</label>
                        <input
                            type="text"
                            name="identityDocNumber"
                            value={docForm.identityDocNumber}
                            onChange={handleDocChange}
                            placeholder={docForm.identityDocType === 'AADHAAR' ? '1234 5678 9012' : 'ABCDE1234F'}
                            className="form-input"
                            required
                        />
                    </div>

                    <div className="form-group">
                        <label>{docForm.identityDocType === 'AADHAAR' ? 'Aadhaar' : 'PAN'} Document URL</label>
                        <input
                            type="url"
                            name="identityDocUrl"
                            value={docForm.identityDocUrl}
                            onChange={handleDocChange}
                            placeholder="https://drive.google.com/... or upload URL"
                            className="form-input"
                            required
                        />
                        <small style={{ color: 'rgba(255,255,255,0.5)', fontSize: '0.8rem' }}>
                            Upload to Google Drive/Dropbox and paste the shareable link
                        </small>
                    </div>

                    <hr style={{ border: 'none', borderTop: '1px solid rgba(255,255,255,0.1)', margin: '1.5rem 0' }} />

                    <div className="form-group">
                        <label>Driving License Number</label>
                        <input
                            type="text"
                            name="drivingLicenseNumber"
                            value={docForm.drivingLicenseNumber}
                            onChange={handleDocChange}
                            placeholder="e.g. KA0120210012345"
                            className="form-input"
                            required
                        />
                    </div>

                    <div className="form-group">
                        <label>Driving License Document URL</label>
                        <input
                            type="url"
                            name="drivingLicenseUrl"
                            value={docForm.drivingLicenseUrl}
                            onChange={handleDocChange}
                            placeholder="https://drive.google.com/... or upload URL"
                            className="form-input"
                            required
                        />
                    </div>

                    <button
                        type="submit"
                        className="btn btn-primary btn-block"
                        disabled={loading}
                        style={{ marginTop: '1rem' }}
                    >
                        {loading ? '⏳ Uploading...' : '📤 Submit Documents'}
                    </button>
                </form>

                {profile?.hasIdentityDoc && profile?.hasDrivingLicense && (
                    <div style={{
                        marginTop: '1.5rem',
                        padding: '1rem',
                        background: 'rgba(16, 185, 129, 0.1)',
                        borderRadius: '8px',
                        color: '#10b981'
                    }}>
                        ✅ Documents uploaded. {profile?.verificationStatus === 'PENDING' ? 'Awaiting admin review.' : ''}
                    </div>
                )}
            </div>
        </div>
    );
};

export default DeliveryProfile;

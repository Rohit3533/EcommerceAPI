import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { deliveryPartnerAPI } from '../api/api';
import './DeliveryPages.css';

const DeliveryAuth = () => {
    const [isLogin, setIsLogin] = useState(true);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');
    const [formData, setFormData] = useState({
        name: '',
        email: '',
        password: '',
        phone: '',
        vehicleType: 'BIKE',
        vehicleNumber: '',
    });
    const navigate = useNavigate();

    const handleChange = (e) => {
        setFormData({ ...formData, [e.target.name]: e.target.value });
        setError('');
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setLoading(true);
        setError('');

        try {
            if (isLogin) {
                const token = await deliveryPartnerAPI.login({
                    email: formData.email,
                    password: formData.password,
                });
                localStorage.setItem('token', token);
                localStorage.setItem('userRole', 'DELIVERY_PARTNER');
                navigate('/delivery/dashboard');
            } else {
                await deliveryPartnerAPI.register(formData);
                setIsLogin(true);
                setError('');
                alert('Registration successful! Please login and upload your documents for verification.');
            }
        } catch (err) {
            setError(err.message || 'Something went wrong');
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="delivery-auth-page">
            <div className="delivery-auth-container">
                <div className="delivery-auth-header">
                    <div className="delivery-logo">🚚</div>
                    <h1>Delivery Partner Portal</h1>
                    <p>{isLogin ? 'Login to manage your deliveries' : 'Join our delivery network'}</p>
                </div>

                <div className="auth-tabs">
                    <button
                        className={`auth-tab ${isLogin ? 'active' : ''}`}
                        onClick={() => setIsLogin(true)}
                    >
                        Login
                    </button>
                    <button
                        className={`auth-tab ${!isLogin ? 'active' : ''}`}
                        onClick={() => setIsLogin(false)}
                    >
                        Register
                    </button>
                </div>

                {error && <div className="error-message">⚠️ {error}</div>}

                <form onSubmit={handleSubmit} className="delivery-auth-form">
                    {!isLogin && (
                        <>
                            <div className="form-group">
                                <label>Full Name</label>
                                <input
                                    type="text"
                                    name="name"
                                    value={formData.name}
                                    onChange={handleChange}
                                    placeholder="Enter your full name"
                                    required
                                    className="form-input"
                                />
                            </div>
                            <div className="form-group">
                                <label>Phone Number</label>
                                <input
                                    type="tel"
                                    name="phone"
                                    value={formData.phone}
                                    onChange={handleChange}
                                    placeholder="Enter your phone number"
                                    required
                                    className="form-input"
                                />
                            </div>
                            <div className="form-row">
                                <div className="form-group">
                                    <label>Vehicle Type</label>
                                    <select
                                        name="vehicleType"
                                        value={formData.vehicleType}
                                        onChange={handleChange}
                                        className="form-select"
                                    >
                                        <option value="BIKE">🏍️ Bike</option>
                                        <option value="SCOOTER">🛵 Scooter</option>
                                        <option value="CAR">🚗 Car</option>
                                        <option value="VAN">🚐 Van</option>
                                    </select>
                                </div>
                                <div className="form-group">
                                    <label>Vehicle Number</label>
                                    <input
                                        type="text"
                                        name="vehicleNumber"
                                        value={formData.vehicleNumber}
                                        onChange={handleChange}
                                        placeholder="e.g. KA01N2243"
                                        required
                                        className="form-input"
                                    />
                                </div>
                            </div>
                        </>
                    )}

                    <div className="form-group">
                        <label>Email</label>
                        <input
                            type="email"
                            name="email"
                            value={formData.email}
                            onChange={handleChange}
                            placeholder="Enter your email"
                            required
                            className="form-input"
                        />
                    </div>

                    <div className="form-group">
                        <label>Password</label>
                        <input
                            type="password"
                            name="password"
                            value={formData.password}
                            onChange={handleChange}
                            placeholder="Enter your password"
                            required
                            className="form-input"
                        />
                    </div>

                    <button type="submit" className="btn btn-primary btn-block" disabled={loading}>
                        {loading ? '⏳ Please wait...' : (isLogin ? '🔐 Login' : '📝 Register')}
                    </button>
                </form>

                {!isLogin && (
                    <div className="registration-note">
                        <p>📋 <strong>Note:</strong> After registration, you'll need to upload your documents:</p>
                        <ul>
                            <li>✅ Aadhaar Card OR PAN Card</li>
                            <li>✅ Driving License (Required)</li>
                        </ul>
                    </div>
                )}

                <div className="auth-footer">
                    <a href="/">← Back to Store</a>
                </div>
            </div>
        </div>
    );
};

export default DeliveryAuth;

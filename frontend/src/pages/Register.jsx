import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { authAPI } from '../api/api';
import './Auth.css';

const Register = () => {
    const navigate = useNavigate();
    const [step, setStep] = useState(1); // 1: Form, 2: OTP Verification
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');
    const [success, setSuccess] = useState('');
    const [formData, setFormData] = useState({
        name: '',
        email: '',
        password: '',
        phone: '',
    });
    const [otp, setOtp] = useState(['', '', '', '', '', '']);

    const handleFormSubmit = async (e) => {
        e.preventDefault();
        try {
            setLoading(true);
            setError('');
            await authAPI.initiateRegistration(formData);
            setStep(2);
            setSuccess('Verification code sent to your email!');
        } catch (err) {
            setError(err.message || 'Failed to send verification code');
        } finally {
            setLoading(false);
        }
    };

    const handleOtpChange = (index, value) => {
        if (value.length > 1) return;
        const newOtp = [...otp];
        newOtp[index] = value;
        setOtp(newOtp);

        // Auto-focus next input
        if (value && index < 5) {
            const nextInput = document.getElementById(`otp-${index + 1}`);
            if (nextInput) nextInput.focus();
        }
    };

    const handleOtpKeyDown = (index, e) => {
        if (e.key === 'Backspace' && !otp[index] && index > 0) {
            const prevInput = document.getElementById(`otp-${index - 1}`);
            if (prevInput) prevInput.focus();
        }
    };

    const handleOtpSubmit = async (e) => {
        e.preventDefault();
        const otpCode = otp.join('');
        if (otpCode.length !== 6) {
            setError('Please enter the complete 6-digit code');
            return;
        }

        try {
            setLoading(true);
            setError('');
            await authAPI.verifyOtp({ email: formData.email, otp: otpCode });
            setSuccess('Email verified! Redirecting to login...');
            setTimeout(() => navigate('/login'), 2000);
        } catch (err) {
            setError(err.message || 'Invalid verification code');
        } finally {
            setLoading(false);
        }
    };

    const handleResendOtp = async () => {
        try {
            setLoading(true);
            setError('');
            await authAPI.resendOtp(formData.email);
            setSuccess('New verification code sent!');
        } catch (err) {
            setError(err.message || 'Failed to resend code');
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="auth-page page">
            <div className="auth-container">
                <div className="auth-card">
                    <div className="auth-header">
                        <span className="auth-icon">🛒</span>
                        <h1 className="auth-title">
                            {step === 1 ? 'Create Account' : 'Verify Email'}
                        </h1>
                        <p className="auth-subtitle">
                            {step === 1
                                ? 'Join us and start shopping'
                                : `Enter the code sent to ${formData.email}`
                            }
                        </p>
                    </div>

                    {step === 1 ? (
                        <form onSubmit={handleFormSubmit} className="auth-form">
                            <div className="form-group">
                                <label className="form-label">Full Name</label>
                                <input
                                    type="text"
                                    className="form-input"
                                    value={formData.name}
                                    onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                                    placeholder="John Doe"
                                    required
                                />
                            </div>

                            <div className="form-group">
                                <label className="form-label">Email Address</label>
                                <input
                                    type="email"
                                    className="form-input"
                                    value={formData.email}
                                    onChange={(e) => setFormData({ ...formData, email: e.target.value })}
                                    placeholder="you@example.com"
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
                                    placeholder="+1 (555) 123-4567"
                                    required
                                />
                            </div>

                            <div className="form-group">
                                <label className="form-label">Password</label>
                                <input
                                    type="password"
                                    className="form-input"
                                    value={formData.password}
                                    onChange={(e) => setFormData({ ...formData, password: e.target.value })}
                                    placeholder="••••••••"
                                    required
                                    minLength={6}
                                />
                            </div>

                            {error && <div className="error-message">{error}</div>}
                            {success && <div className="success-message">{success}</div>}

                            <button
                                type="submit"
                                className="btn btn-primary btn-lg auth-btn"
                                disabled={loading}
                            >
                                {loading ? '⏳ Sending code...' : 'Continue →'}
                            </button>
                        </form>
                    ) : (
                        <form onSubmit={handleOtpSubmit} className="auth-form">
                            <div className="otp-container">
                                <div className="otp-inputs">
                                    {otp.map((digit, index) => (
                                        <input
                                            key={index}
                                            id={`otp-${index}`}
                                            type="text"
                                            maxLength={1}
                                            className="otp-input"
                                            value={digit}
                                            onChange={(e) => handleOtpChange(index, e.target.value)}
                                            onKeyDown={(e) => handleOtpKeyDown(index, e)}
                                            autoFocus={index === 0}
                                        />
                                    ))}
                                </div>
                                <p className="otp-hint">
                                    Didn't receive the code?{' '}
                                    <button
                                        type="button"
                                        className="btn-link"
                                        onClick={handleResendOtp}
                                        disabled={loading}
                                    >
                                        Resend
                                    </button>
                                </p>
                            </div>

                            {error && <div className="error-message">{error}</div>}
                            {success && <div className="success-message">{success}</div>}

                            <button
                                type="submit"
                                className="btn btn-primary btn-lg auth-btn"
                                disabled={loading}
                            >
                                {loading ? '⏳ Verifying...' : 'Verify Email'}
                            </button>

                            <button
                                type="button"
                                className="btn btn-secondary btn-lg auth-btn"
                                onClick={() => { setStep(1); setOtp(['', '', '', '', '', '']); }}
                            >
                                ← Back
                            </button>
                        </form>
                    )}

                    <div className="auth-footer">
                        <p>
                            Already have an account?{' '}
                            <Link to="/login" className="auth-link">
                                Sign in →
                            </Link>
                        </p>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default Register;

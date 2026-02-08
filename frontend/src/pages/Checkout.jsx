import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { loadStripe } from '@stripe/stripe-js';
import { Elements } from '@stripe/react-stripe-js';
import { ordersAPI, paymentsAPI, addressesAPI } from '../api/api';
import { useCart } from '../context/CartContext';
import { useAuth } from '../context/AuthContext';
import StripePaymentForm from '../components/StripePaymentForm';
import AddressForm from '../components/AddressForm';
import '../components/AddressForm.css';
import './Checkout.css';

// Load Stripe with your publishable key
const stripePromise = loadStripe('pk_test_51SyU8PRozOglC3WSsXIHSEkO9KOlsfUTUUETcnzep9FtotLzp7slh0DtfTBu0suWvfUjh8xs1Wbgw9JjNSNxFi3r00KQ09VCL0');

const Checkout = () => {
    const navigate = useNavigate();
    const { user } = useAuth();
    const { cart, totalAmount, fetchCart } = useCart();
    const [loading, setLoading] = useState(false);
    const [step, setStep] = useState(1);
    const [error, setError] = useState('');
    const [order, setOrder] = useState(null);
    const [clientSecret, setClientSecret] = useState('');

    // Address state
    const [addresses, setAddresses] = useState([]);
    const [selectedAddressId, setSelectedAddressId] = useState(null);
    const [addressLoading, setAddressLoading] = useState(true);
    const [showAddressForm, setShowAddressForm] = useState(false);

    // Payment method state
    const [selectedPaymentMethod, setSelectedPaymentMethod] = useState(null);

    // Fetch addresses on mount
    useEffect(() => {
        const fetchAddresses = async () => {
            try {
                setAddressLoading(true);
                const data = await addressesAPI.getAll();
                setAddresses(data || []);
                // Auto-select default address
                const defaultAddr = data?.find(a => a.isDefault);
                if (defaultAddr) {
                    setSelectedAddressId(defaultAddr.id);
                } else if (data?.length > 0) {
                    setSelectedAddressId(data[0].id);
                }
            } catch (err) {
                console.error('Failed to fetch addresses:', err);
            } finally {
                setAddressLoading(false);
            }
        };
        fetchAddresses();
    }, []);

    // Reset state when user changes
    useEffect(() => {
        setStep(1);
        setOrder(null);
        setClientSecret('');
        setError('');
    }, [user?.id]);

    const formatPrice = (price) => {
        return new Intl.NumberFormat('en-US', {
            style: 'currency',
            currency: 'USD',
        }).format(price);
    };

    const getSelectedAddress = () => {
        return addresses.find(a => a.id === selectedAddressId);
    };

    // Compute available payment methods based on cart products
    const getAvailablePaymentMethods = () => {
        const items = cart?.items || [];
        if (items.length === 0) return ['CARD'];

        // Get intersection of all products' allowed payment methods
        let availableMethods = null;
        for (const item of items) {
            const productMethods = (item.allowedPaymentMethods || 'CARD').split(',');
            if (availableMethods === null) {
                availableMethods = new Set(productMethods);
            } else {
                availableMethods = new Set([...availableMethods].filter(m => productMethods.includes(m)));
            }
        }
        return availableMethods ? [...availableMethods] : ['CARD'];
    };

    const availablePaymentMethods = getAvailablePaymentMethods();

    const handleAddAddress = async (addressData) => {
        try {
            setAddressLoading(true);
            const newAddress = await addressesAPI.add(addressData);
            setAddresses([...addresses, newAddress]);
            setSelectedAddressId(newAddress.id);
            setShowAddressForm(false);
        } catch (err) {
            setError(err.message || 'Failed to add address');
        } finally {
            setAddressLoading(false);
        }
    };

    const handleContinueToPayment = async () => {
        if (!selectedAddressId) {
            setError('Please select a shipping address');
            return;
        }

        if (!selectedPaymentMethod) {
            setError('Please select a payment method');
            return;
        }

        try {
            setLoading(true);
            setError('');

            if (selectedPaymentMethod === 'COD') {
                // COD: Place order directly, no payment processing
                const placedOrder = await ordersAPI.placeWithPaymentMethod('COD', selectedAddressId);
                console.log('COD Order placed:', placedOrder);
                setOrder(placedOrder);
                await fetchCart();
                setStep(3); // Go directly to success
            } else {
                // CARD/UPI: Create order then go to payment
                const placedOrder = await ordersAPI.placeWithPaymentMethod(selectedPaymentMethod, selectedAddressId);
                console.log('Order placed:', placedOrder);
                setOrder(placedOrder);

                // Create Stripe payment intent
                const paymentIntent = await paymentsAPI.createIntent(placedOrder.orderId);
                console.log('Payment intent created:', paymentIntent);
                setClientSecret(paymentIntent.clientSecret);

                setStep(2);
            }
        } catch (err) {
            setError(err.message || 'Failed to create order');
        } finally {
            setLoading(false);
        }
    };

    const handlePaymentSuccess = async (paymentIntent) => {
        console.log('Payment successful:', paymentIntent);
        await fetchCart();
        setStep(3);
    };

    const handlePaymentError = (errorMessage) => {
        setError(errorMessage);
    };

    const items = cart?.items || [];

    if (items.length === 0 && step !== 3) {
        navigate('/cart');
        return null;
    }

    const selectedAddress = getSelectedAddress();

    const stripeOptions = {
        clientSecret,
        appearance: {
            theme: 'night',
            variables: {
                colorPrimary: '#8b5cf6',
                colorBackground: '#1a1a2e',
                colorText: '#ffffff',
                colorDanger: '#ef4444',
                borderRadius: '8px',
            },
        },
    };

    return (
        <div className="checkout-page page">
            <div className="container">
                <h1 className="page-title">Checkout</h1>

                {/* Progress Steps */}
                <div className="checkout-steps">
                    <div className={`step ${step >= 1 ? 'active' : ''} ${step > 1 ? 'completed' : ''}`}>
                        <span className="step-number">1</span>
                        <span className="step-label">Review</span>
                    </div>
                    <div className="step-line"></div>
                    <div className={`step ${step >= 2 ? 'active' : ''} ${step > 2 ? 'completed' : ''}`}>
                        <span className="step-number">2</span>
                        <span className="step-label">Payment</span>
                    </div>
                    <div className="step-line"></div>
                    <div className={`step ${step >= 3 ? 'active' : ''}`}>
                        <span className="step-number">3</span>
                        <span className="step-label">Complete</span>
                    </div>
                </div>

                {step === 1 && (
                    <div className="checkout-layout">
                        {/* Order Review */}
                        <div className="checkout-main">
                            <div className="checkout-section card">
                                <div className="section-header">
                                    <h2 className="section-title">📍 Shipping Address</h2>
                                    {!showAddressForm && addresses.length > 0 && (
                                        <button
                                            className="btn btn-secondary btn-sm"
                                            onClick={() => setShowAddressForm(true)}
                                        >
                                            + Add New
                                        </button>
                                    )}
                                </div>

                                {showAddressForm ? (
                                    <AddressForm
                                        onSubmit={handleAddAddress}
                                        onCancel={() => setShowAddressForm(false)}
                                        loading={addressLoading}
                                    />
                                ) : addressLoading ? (
                                    <div className="loading-text">Loading addresses...</div>
                                ) : addresses.length === 0 ? (
                                    <div className="no-address-box">
                                        <p>No shipping address saved</p>
                                        <button
                                            className="btn btn-primary"
                                            onClick={() => setShowAddressForm(true)}
                                        >
                                            + Add Address
                                        </button>
                                    </div>
                                ) : (
                                    <div className="address-selector">
                                        {addresses.map((address) => (
                                            <div
                                                key={address.id}
                                                className={`address-option ${selectedAddressId === address.id ? 'selected' : ''}`}
                                                onClick={() => setSelectedAddressId(address.id)}
                                            >
                                                <div className="address-radio">
                                                    <input
                                                        type="radio"
                                                        name="address"
                                                        checked={selectedAddressId === address.id}
                                                        onChange={() => setSelectedAddressId(address.id)}
                                                    />
                                                </div>
                                                <div className="address-content">
                                                    <div className="address-label">
                                                        {address.label === 'Home' ? '🏠' : address.label === 'Office' ? '🏢' : '📍'}
                                                        {address.label}
                                                        {address.isDefault && <span className="default-tag">Default</span>}
                                                    </div>
                                                    <div className="address-name">{address.fullName}</div>
                                                    <div className="address-phone">{address.phone}</div>
                                                    <div className="address-text">
                                                        {address.streetAddress}, {address.city}, {address.state} {address.postalCode}
                                                    </div>
                                                </div>
                                            </div>
                                        ))}
                                    </div>
                                )}
                            </div>

                            <div className="checkout-section card">
                                <h2 className="section-title">📦 Order Items</h2>
                                <div className="checkout-items">
                                    {items.map((item) => (
                                        <div key={item.productId} className="checkout-item">
                                            <img
                                                src={item.imageUrl || 'https://via.placeholder.com/60x60?text=No+Image'}
                                                alt={item.productName}
                                                className="checkout-item-image"
                                            />
                                            <div className="checkout-item-info">
                                                <p className="checkout-item-name">{item.productName}</p>
                                                <p className="checkout-item-qty">Qty: {item.quantity}</p>
                                            </div>
                                            <span className="checkout-item-price">
                                                {formatPrice(item.price * item.quantity)}
                                            </span>
                                        </div>
                                    ))}
                                </div>
                            </div>

                            {/* Payment Method Selection */}
                            <div className="checkout-section card">
                                <h2 className="section-title">💰 Payment Method</h2>
                                <div className="payment-methods">
                                    {availablePaymentMethods.map((method) => (
                                        <div
                                            key={method}
                                            className={`payment-method-option ${selectedPaymentMethod === method ? 'selected' : ''}`}
                                            onClick={() => setSelectedPaymentMethod(method)}
                                        >
                                            <div className="payment-method-radio">
                                                <input
                                                    type="radio"
                                                    name="paymentMethod"
                                                    checked={selectedPaymentMethod === method}
                                                    onChange={() => setSelectedPaymentMethod(method)}
                                                />
                                            </div>
                                            <div className="payment-method-info">
                                                <span className="payment-method-icon">
                                                    {method === 'COD' ? '💵' : method === 'UPI' ? '📱' : '💳'}
                                                </span>
                                                <div className="payment-method-details">
                                                    <span className="payment-method-name">
                                                        {method === 'COD' ? 'Cash on Delivery' : method === 'UPI' ? 'UPI Payment' : 'Credit/Debit Card'}
                                                    </span>
                                                    <span className="payment-method-desc">
                                                        {method === 'COD' ? 'Pay when you receive'
                                                            : method === 'UPI' ? 'Pay via UPI'
                                                                : 'Secure payment via Stripe'}
                                                    </span>
                                                </div>
                                            </div>
                                        </div>
                                    ))}
                                </div>
                                {availablePaymentMethods.length === 0 && (
                                    <p className="no-payment-methods">No common payment methods available for all items in cart.</p>
                                )}
                            </div>
                        </div>

                        {/* Summary */}
                        <div className="checkout-sidebar">
                            <div className="order-summary card">
                                <h2 className="summary-title">Order Summary</h2>

                                {selectedAddress && (
                                    <div className="selected-address-preview">
                                        <p className="preview-label">Delivering to:</p>
                                        <p className="preview-name">{selectedAddress.fullName}</p>
                                        <p className="preview-text">{selectedAddress.city}, {selectedAddress.state}</p>
                                    </div>
                                )}

                                <div className="summary-row">
                                    <span>Subtotal</span>
                                    <span>{formatPrice(totalAmount)}</span>
                                </div>
                                <div className="summary-row">
                                    <span>Shipping</span>
                                    <span className="free-shipping">FREE</span>
                                </div>
                                <div className="summary-divider"></div>
                                <div className="summary-row total-row">
                                    <span>Total</span>
                                    <span className="total-amount">{formatPrice(totalAmount)}</span>
                                </div>
                                {error && <div className="error-message">{error}</div>}
                                <button
                                    className="btn btn-primary btn-lg checkout-btn"
                                    onClick={handleContinueToPayment}
                                    disabled={loading || !selectedAddressId || !selectedPaymentMethod}
                                >
                                    {loading ? '⏳ Processing...' :
                                        selectedPaymentMethod === 'COD' ? 'Place Order (COD)' : 'Continue to Payment →'}
                                </button>
                            </div>
                        </div>
                    </div>
                )}

                {step === 2 && (
                    <div className="checkout-layout">
                        <div className="checkout-main">
                            <div className="checkout-section card">
                                <h2 className="section-title">💳 Payment</h2>
                                <div className="payment-info">
                                    <p className="payment-text">
                                        Enter your card details below. This is a <strong>test payment</strong> -
                                        use card: <code>4242 4242 4242 4242</code> with any future date and CVC.
                                    </p>
                                </div>

                                {clientSecret && (
                                    <Elements
                                        key={clientSecret}
                                        stripe={stripePromise}
                                        options={stripeOptions}
                                    >
                                        <StripePaymentForm
                                            orderId={order?.orderId}
                                            onSuccess={handlePaymentSuccess}
                                            onError={handlePaymentError}
                                        />
                                    </Elements>
                                )}

                                {error && <div className="error-message" style={{ marginTop: '20px' }}>{error}</div>}
                            </div>
                        </div>

                        <div className="checkout-sidebar">
                            <div className="order-summary card">
                                <h2 className="summary-title">Order #{order?.orderId}</h2>
                                <div className="summary-row">
                                    <span>Items ({items.length})</span>
                                    <span>{formatPrice(totalAmount)}</span>
                                </div>
                                <div className="summary-row">
                                    <span>Shipping</span>
                                    <span className="free-shipping">FREE</span>
                                </div>
                                <div className="summary-divider"></div>
                                <div className="summary-row total-row">
                                    <span>Total</span>
                                    <span className="total-amount">{formatPrice(totalAmount)}</span>
                                </div>
                            </div>
                        </div>
                    </div>
                )}

                {step === 3 && (
                    <div className="order-success">
                        <div className="success-icon">🎉</div>
                        <h2 className="success-title">Order Placed Successfully!</h2>
                        <p className="success-message">
                            Thank you for your purchase. Your order #{order?.orderId} has been confirmed.
                        </p>
                        <div className="success-actions">
                            <button
                                className="btn btn-primary btn-lg"
                                onClick={() => navigate('/orders')}
                            >
                                View My Orders
                            </button>
                            <button
                                className="btn btn-secondary btn-lg"
                                onClick={() => navigate('/products')}
                            >
                                Continue Shopping
                            </button>
                        </div>
                    </div>
                )}
            </div>
        </div>
    );
};

export default Checkout;

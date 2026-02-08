import { useState } from 'react';
import { useStripe, useElements, PaymentElement } from '@stripe/react-stripe-js';
import { paymentsAPI } from '../api/api';
import './StripePaymentForm.css';

const StripePaymentForm = ({ orderId, onSuccess, onError }) => {
    const stripe = useStripe();
    const elements = useElements();
    const [loading, setLoading] = useState(false);
    const [errorMessage, setErrorMessage] = useState('');

    const handleSubmit = async (e) => {
        e.preventDefault();

        if (!stripe || !elements) {
            return;
        }

        setLoading(true);
        setErrorMessage('');

        try {
            const { error, paymentIntent } = await stripe.confirmPayment({
                elements,
                redirect: 'if_required',
            });

            if (error) {
                setErrorMessage(error.message);
                onError && onError(error.message);
            } else if (paymentIntent && paymentIntent.status === 'succeeded') {
                // Confirm payment with backend
                await paymentsAPI.confirm(orderId, paymentIntent.id);
                onSuccess && onSuccess(paymentIntent);
            }
        } catch (err) {
            setErrorMessage(err.message || 'Payment failed');
            onError && onError(err.message);
        } finally {
            setLoading(false);
        }
    };

    return (
        <form onSubmit={handleSubmit} className="stripe-payment-form">
            <PaymentElement />

            {errorMessage && (
                <div className="payment-error">
                    {errorMessage}
                </div>
            )}

            <button
                type="submit"
                disabled={!stripe || loading}
                className="btn btn-primary btn-lg payment-submit-btn"
            >
                {loading ? '⏳ Processing...' : '💳 Pay Now'}
            </button>
        </form>
    );
};

export default StripePaymentForm;

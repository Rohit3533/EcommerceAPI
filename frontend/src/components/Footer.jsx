import './Footer.css';

const Footer = () => {
    return (
        <footer className="footer">
            <div className="container footer-container">
                <div className="footer-brand">
                    <span className="brand-icon">🛒</span>
                    <span className="brand-text">ShopHub</span>
                </div>
                <p className="footer-text">
                    © 2026 ShopHub. All rights reserved.
                </p>
                <div className="footer-links">
                    <a href="#" className="footer-link">Privacy</a>
                    <a href="#" className="footer-link">Terms</a>
                    <a href="#" className="footer-link">Contact</a>
                </div>
            </div>
        </footer>
    );
};

export default Footer;

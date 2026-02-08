-- Migration script to add delivery partner portal fields
-- Run this against your PostgreSQL database

ALTER TABLE delivery_partners ADD COLUMN IF NOT EXISTS verification_status VARCHAR(20) DEFAULT 'PENDING';
ALTER TABLE delivery_partners ADD COLUMN IF NOT EXISTS rejection_reason TEXT;
ALTER TABLE delivery_partners ADD COLUMN IF NOT EXISTS verified_at TIMESTAMP;
ALTER TABLE delivery_partners ADD COLUMN IF NOT EXISTS verified_by VARCHAR(255);
ALTER TABLE delivery_partners ADD COLUMN IF NOT EXISTS identity_doc_type VARCHAR(20);
ALTER TABLE delivery_partners ADD COLUMN IF NOT EXISTS identity_doc_number VARCHAR(50);
ALTER TABLE delivery_partners ADD COLUMN IF NOT EXISTS identity_doc_url VARCHAR(500);
ALTER TABLE delivery_partners ADD COLUMN IF NOT EXISTS driving_license_number VARCHAR(50);
ALTER TABLE delivery_partners ADD COLUMN IF NOT EXISTS driving_license_url VARCHAR(500);
ALTER TABLE delivery_partners ADD COLUMN IF NOT EXISTS current_latitude DOUBLE PRECISION;
ALTER TABLE delivery_partners ADD COLUMN IF NOT EXISTS current_longitude DOUBLE PRECISION;
ALTER TABLE delivery_partners ADD COLUMN IF NOT EXISTS last_location_update TIMESTAMP;
ALTER TABLE delivery_partners ADD COLUMN IF NOT EXISTS is_online BOOLEAN DEFAULT FALSE;
ALTER TABLE delivery_partners ADD COLUMN IF NOT EXISTS average_rating DOUBLE PRECISION DEFAULT 0.0;
ALTER TABLE delivery_partners ADD COLUMN IF NOT EXISTS user_id BIGINT;

-- Add foreign key for user_id if not exists
DO $$ 
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_delivery_partner_user') THEN
        ALTER TABLE delivery_partners ADD CONSTRAINT fk_delivery_partner_user 
        FOREIGN KEY (user_id) REFERENCES users(id);
    END IF;
END $$;

-- Add picked_up_at to orders table
ALTER TABLE orders ADD COLUMN IF NOT EXISTS picked_up_at TIMESTAMP;

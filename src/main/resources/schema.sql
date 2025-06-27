-- Create the database if it doesn't exist
CREATE DATABASE IF NOT EXISTS pdfmanagement;

-- Switch to the pdfmanagement database
\c pdfmanagement;


-- Create the documents table
CREATE TABLE IF NOT EXISTS documents (
    id SERIAL PRIMARY KEY,
    username VARCHAR(255) NOT NULL,          -- Store user as a string
    document_name VARCHAR(255) NOT NULL, -- Name of the document
    minio_path VARCHAR(255) NOT NULL,    -- Path to the document in MinIO
    file_size BIGINT NOT NULL,            -- Size of the file
    file_type VARCHAR(50) NOT NULL,       -- Type of the file (e.g., application/pdf)
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- Timestamp of creation
    tags VARCHAR(255)                     -- Store tags as a comma-separated string
);
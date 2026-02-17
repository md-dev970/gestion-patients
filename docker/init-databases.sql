-- ╔══════════════════════════════════════════════════════════════════════════════╗
-- ║                    DATABASE INITIALIZATION SCRIPT                            ║
-- ╠══════════════════════════════════════════════════════════════════════════════╣
-- ║  WHY THIS FILE EXISTS:                                                       ║
-- ║  Creates all databases needed by microservices in a single PostgreSQL        ║
-- ║  instance. Used for development environment.                                 ║
-- ║                                                                              ║
-- ║  This script runs automatically when the PostgreSQL container starts         ║
-- ║  for the first time (via docker-entrypoint-initdb.d).                        ║
-- ╚══════════════════════════════════════════════════════════════════════════════╝

-- Create databases for each microservice
CREATE DATABASE hospital_patients;
CREATE DATABASE hospital_staff;
CREATE DATABASE hospital_appointments;
CREATE DATABASE hospital_medical_records;
CREATE DATABASE hospital_auth;

-- Grant privileges (optional - postgres user already has access)
GRANT ALL PRIVILEGES ON DATABASE hospital_patients TO postgres;
GRANT ALL PRIVILEGES ON DATABASE hospital_staff TO postgres;
GRANT ALL PRIVILEGES ON DATABASE hospital_appointments TO postgres;
GRANT ALL PRIVILEGES ON DATABASE hospital_medical_records TO postgres;
GRANT ALL PRIVILEGES ON DATABASE hospital_auth TO postgres;


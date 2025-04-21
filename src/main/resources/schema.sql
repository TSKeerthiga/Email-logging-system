CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(100) NOT NULL,
    password VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL,
    phone_number VARCHAR(100) NOT NULL CHECK (phone_number ~ '^[+]?[0-9]{10,15}$'),
    is_active BOOLEAN NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE role (
    id BIGINT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);

CREATE TABLE email_logs (
    id SERIAL PRIMARY KEY,
    from_email VARCHAR(255),
    to_email VARCHAR(255),
    subject VARCHAR(255),
    mail_receive_date TIMESTAMP,
	body TEXT,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE role (
    id BIGINT PRIMARY KEY,
    attachmentUrl VARCHAR(100) NOT NULL,
    email_log_id FOREIGN KEY EMAIL_LOGS NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);

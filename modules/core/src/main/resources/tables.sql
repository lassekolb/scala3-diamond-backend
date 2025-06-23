CREATE TABLE users (
    id uuid PRIMARY KEY,
    name varchar UNIQUE NOT NULL,
    password VARCHAR NOT NULL
);

CREATE TABLE organizations (
    id uuid PRIMARY KEY,
    name varchar UNIQUE NOT NULL
);

CREATE TABLE memberships (
    user_id uuid NOT NULL,
    organization_id uuid NOT NULL,
    role VARCHAR NOT NULL CHECK (ROLE IN ('Admin', 'Member', 'Manager')),
    PRIMARY KEY (user_id, organization_id),
    CONSTRAINT fk_membership_user FOREIGN KEY (user_id) REFERENCES users (id) ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT fk_membership_org FOREIGN KEY (organization_id) REFERENCES organizations (id) ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE TABLE invoices (
    id uuid PRIMARY KEY,
    organization_id uuid NOT NULL,
    amount numeric NOT NULL,
    currency_code varchar NOT NULL,
    buyer varchar NOT NULL,
    seller varchar NOT NULL,
    date date NOT NULL,
    due_date date,
    CONSTRAINT fk_invoice_org FOREIGN KEY (organization_id) REFERENCES organizations (id) ON UPDATE CASCADE ON DELETE CASCADE
);


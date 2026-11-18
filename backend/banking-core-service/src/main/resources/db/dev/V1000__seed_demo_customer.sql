-- DEV PROFILE ONLY (spring.profiles.active=dev). Never loaded in other environments.
-- Owner id matches the fixed id of user "customer.demo" in docker/keycloak/banking-realm.json.
-- IBANs are the well-known documentation examples, not real accounts.

INSERT INTO accounts (id, owner_id, iban, display_name, account_type, currency, balance, status)
VALUES
    ('0b7a2f4e-8c1d-4e5a-9f3b-1a2b3c4d5e01', '6f1c2a9e-3b7d-4c55-9a1e-0b8f2d4c7e11',
     'FR7630006000011234567890189', 'Everyday current account', 'CURRENT', 'EUR', 4250.7500, 'ACTIVE'),
    ('0b7a2f4e-8c1d-4e5a-9f3b-1a2b3c4d5e02', '6f1c2a9e-3b7d-4c55-9a1e-0b8f2d4c7e11',
     'DE89370400440532013000', 'Savings account', 'SAVINGS', 'EUR', 18900.0000, 'ACTIVE');

INSERT INTO account_transactions (id, account_id, transaction_ref, direction, amount, currency, description,
                                  booking_date, value_date, source_system)
VALUES
    ('7d2e1c00-0000-4000-8000-000000000001', '0b7a2f4e-8c1d-4e5a-9f3b-1a2b3c4d5e01', 'CORE-SEED-0001',
     'CREDIT', 3200.0000, 'EUR', 'Salary November', CURRENT_DATE - 5, CURRENT_DATE - 5, 'SEED'),
    ('7d2e1c00-0000-4000-8000-000000000002', '0b7a2f4e-8c1d-4e5a-9f3b-1a2b3c4d5e01', 'CORE-SEED-0002',
     'DEBIT', 950.0000, 'EUR', 'Rent', CURRENT_DATE - 4, CURRENT_DATE - 4, 'SEED'),
    ('7d2e1c00-0000-4000-8000-000000000003', '0b7a2f4e-8c1d-4e5a-9f3b-1a2b3c4d5e01', 'CORE-SEED-0003',
     'DEBIT', 64.2500, 'EUR', 'Supermarket', CURRENT_DATE - 2, CURRENT_DATE - 2, 'SEED'),
    ('7d2e1c00-0000-4000-8000-000000000004', '0b7a2f4e-8c1d-4e5a-9f3b-1a2b3c4d5e01', 'CORE-SEED-0004',
     'DEBIT', 500.0000, 'EUR', 'Transfer to savings', CURRENT_DATE - 1, CURRENT_DATE - 1, 'SEED'),
    ('7d2e1c00-0000-4000-8000-000000000005', '0b7a2f4e-8c1d-4e5a-9f3b-1a2b3c4d5e02', 'CORE-SEED-0005',
     'CREDIT', 500.0000, 'EUR', 'Transfer from current account', CURRENT_DATE - 1, CURRENT_DATE - 1, 'SEED');

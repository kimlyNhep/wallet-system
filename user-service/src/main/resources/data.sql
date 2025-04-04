-- Insert Admin User
INSERT INTO WLT_USER(EMAIL, PASSWORD_HASH, ENABLED)
VALUES( 'admin@gmail.com', '$2a$10$/l2VAT.kFR5KD/IcjSBW/O4dEN65ZNcnYkRMDFjaSu3noFHhM53Ne', true)
    ON CONFLICT (EMAIL) DO NOTHING;

-- Insert Roles
INSERT INTO WLT_ROLE(NAME) VALUES ('ADMIN') ON CONFLICT (NAME) DO NOTHING;
INSERT INTO WLT_ROLE(NAME) VALUES ('USER') ON CONFLICT (NAME) DO NOTHING;
INSERT INTO WLT_ROLE(NAME) VALUES ('SUPER_ADMIN') ON CONFLICT (NAME) DO NOTHING;

-- Insert Permissions
INSERT INTO WLT_PERMISSION (NAME) VALUES('MAKE_FUND_TRANSFER') ON CONFLICT (NAME) DO NOTHING;
INSERT INTO WLT_PERMISSION (NAME) VALUES('GEN_GIFT_CODE') ON CONFLICT (NAME) DO NOTHING;
INSERT INTO WLT_PERMISSION (NAME) VALUES('GRANT_PERMISSION') ON CONFLICT (NAME) DO NOTHING;
INSERT INTO WLT_PERMISSION (NAME) VALUES('REDEEM_GIFT_CODE') ON CONFLICT (NAME) DO NOTHING;

-- Assign SUPER_ADMIN Role to Admin (Corrected)
INSERT INTO WLT_USER_ROLE(USER_ID, ROLE_ID)
SELECT
    (SELECT U.ID FROM WLT_USER U WHERE U.EMAIL = 'admin@gmail.com'),
    (SELECT R.ID FROM WLT_ROLE R WHERE R.NAME = 'SUPER_ADMIN')
    ON CONFLICT (USER_ID, ROLE_ID) DO NOTHING;

-- Role-Permission Mappings (All Corrected)
INSERT INTO WLT_ROLE_PERMISSION (ROLE_ID, PERMISSION_ID)
SELECT
    (SELECT R.ID FROM WLT_ROLE R WHERE R.NAME = 'USER'),
    (SELECT P.ID FROM WLT_PERMISSION P WHERE P.NAME = 'MAKE_FUND_TRANSFER')
    ON CONFLICT (ROLE_ID, PERMISSION_ID) DO NOTHING;

INSERT INTO WLT_ROLE_PERMISSION (ROLE_ID, PERMISSION_ID)
SELECT
    (SELECT R.ID FROM WLT_ROLE R WHERE R.NAME = 'USER'),
    (SELECT P.ID FROM WLT_PERMISSION P WHERE P.NAME = 'REDEEM_GIFT_CODE')
    ON CONFLICT (ROLE_ID, PERMISSION_ID) DO NOTHING;

INSERT INTO WLT_ROLE_PERMISSION (ROLE_ID, PERMISSION_ID)
SELECT
    (SELECT R.ID FROM WLT_ROLE R WHERE R.NAME = 'ADMIN'),
    (SELECT P.ID FROM WLT_PERMISSION P WHERE P.NAME = 'MAKE_FUND_TRANSFER')
    ON CONFLICT (ROLE_ID, PERMISSION_ID) DO NOTHING;

INSERT INTO WLT_ROLE_PERMISSION (ROLE_ID, PERMISSION_ID)
SELECT
    (SELECT R.ID FROM WLT_ROLE R WHERE R.NAME = 'ADMIN'),
    (SELECT P.ID FROM WLT_PERMISSION P WHERE P.NAME = 'GEN_GIFT_CODE')
    ON CONFLICT (ROLE_ID, PERMISSION_ID) DO NOTHING;

INSERT INTO WLT_ROLE_PERMISSION (ROLE_ID, PERMISSION_ID)
SELECT
    (SELECT R.ID FROM WLT_ROLE R WHERE R.NAME = 'ADMIN'),
    (SELECT P.ID FROM WLT_PERMISSION P WHERE P.NAME = 'REDEEM_GIFT_CODE')
    ON CONFLICT (ROLE_ID, PERMISSION_ID) DO NOTHING;

INSERT INTO WLT_ROLE_PERMISSION (ROLE_ID, PERMISSION_ID)
SELECT
    (SELECT R.ID FROM WLT_ROLE R WHERE R.NAME = 'SUPER_ADMIN'),
    (SELECT P.ID FROM WLT_PERMISSION P WHERE P.NAME = 'MAKE_FUND_TRANSFER')
    ON CONFLICT (ROLE_ID, PERMISSION_ID) DO NOTHING;

INSERT INTO WLT_ROLE_PERMISSION (ROLE_ID, PERMISSION_ID)
SELECT
    (SELECT R.ID FROM WLT_ROLE R WHERE R.NAME = 'SUPER_ADMIN'),
    (SELECT P.ID FROM WLT_PERMISSION P WHERE P.NAME = 'GEN_GIFT_CODE')
    ON CONFLICT (ROLE_ID, PERMISSION_ID) DO NOTHING;

INSERT INTO WLT_ROLE_PERMISSION (ROLE_ID, PERMISSION_ID)
SELECT
    (SELECT R.ID FROM WLT_ROLE R WHERE R.NAME = 'SUPER_ADMIN'),
    (SELECT P.ID FROM WLT_PERMISSION P WHERE P.NAME = 'REDEEM_GIFT_CODE')
    ON CONFLICT (ROLE_ID, PERMISSION_ID) DO NOTHING;

INSERT INTO WLT_ROLE_PERMISSION (ROLE_ID, PERMISSION_ID)
SELECT
    (SELECT R.ID FROM WLT_ROLE R WHERE R.NAME = 'SUPER_ADMIN'),
    (SELECT P.ID FROM WLT_PERMISSION P WHERE P.NAME = 'GRANT_PERMISSION')
    ON CONFLICT (ROLE_ID, PERMISSION_ID) DO NOTHING;
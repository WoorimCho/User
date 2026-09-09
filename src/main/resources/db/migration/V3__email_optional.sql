-- Email is now optional (nothing is ever sent to it). Drop the NOT NULL.
-- The uk_account_email unique index stays: MySQL treats each NULL as distinct,
-- so any number of accounts can have no email without colliding.
alter table account modify column email varchar(255) null;

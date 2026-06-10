CREATE USER auth_user WITH PASSWORD 'auth_password';
CREATE DATABASE auth_db OWNER auth_user;
\connect auth_db
GRANT ALL ON SCHEMA public TO auth_user;

CREATE USER account_user WITH PASSWORD 'account_password';
CREATE DATABASE account_db OWNER account_user;
\connect account_db
GRANT ALL ON SCHEMA public TO account_user;

CREATE USER analytics_user WITH PASSWORD 'analytics_password';
CREATE DATABASE analytics_db OWNER analytics_user;
\connect analytics_db
GRANT ALL ON SCHEMA public TO analytics_user;

CREATE USER transaction_user WITH PASSWORD 'transaction_password';
CREATE DATABASE transaction_db OWNER transaction_user;
\connect transaction_db
GRANT ALL ON SCHEMA public TO transaction_user;

CREATE USER budget_user WITH PASSWORD 'budget_password';
CREATE DATABASE budget_db OWNER budget_user;
\connect budget_db
GRANT ALL ON SCHEMA public TO budget_user;

CREATE USER notification_user WITH PASSWORD 'notification_password';
CREATE DATABASE notification_db OWNER notification_user;
\connect notification_db
GRANT ALL ON SCHEMA public TO notification_user;

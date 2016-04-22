Video Production Inventory Management
=====================================

Keeping track of who uses what equipment out in the field and ensuring that it
comes back in good shape is important. Accountability is key when it comes to
these things.

VIMS can track the current inventory of equipment, as well as who last checked
out/in equipment and what comments they left.

VIMS utilizes a DYMO Label printer to print both Inventory and User Identification
Labels on [Dymo LV-30332 Labels](http://amzn.com/B00004Z60O). A browser that
supports the Dymo Framework (So far only Safari has been tested, Chrome does
not support printing).

## Setup
VIMS is written primarily in Java and is run using the TomCat framework to run
the website. A MySQL DB is used to store user information, as well as store the
inventory and record check out/in transactions.

VIMS uses [Key Server](https://github.com/sdsu-its/key-server) to access
credentials for various tools and services (DataBase, APIs, Email, etc.)

Unit Testing is a feature in VIMS

### DB Config
To setup they MySQL database for VIMS, run the following commands in your
MySQL Database to setup the tables, you will need to first create the Database
and user to access the data, using your root credentials in the web-app is not
recommended.

You will also need to create the first user in the database, make sure that
supervisor is set to 1 for that user to allow them to access the admin panel.

#### Table Breakdown
- __Users__ stores the users for the system, including administrators.
- __Inventory__ stores all items that should be available for checkout, as well as
the last comment associated with the item, and its current status (in/out). Short
Name is used to print a short description at the top of the Inventory Sticker,
instead of the default text.
- __Transactions__ stores int inbound and outbound transactions as well as the
respective timestamps.
- __Quotes__ To add a bit of excitement and variety, a quote is displayed on the
start page, the front-end pulls the quote of the day daily, or when first loaded.
(A good source for quotes is [Brainy Quote](http://www.brainyquote.com/)).

#### Both
```
CREATE TABLE videoinv.users (
  `id`         INT PRIMARY KEY AUTO_INCREMENT NOT NULL,
  `pub_id`     INT(9),
  `first_name` TEXT,
  `last_name`  TEXT,
  `supervisor` TINYINT(1),
  `pin`        TEXT
);
CREATE TABLE videoinv.inventory (
  `id`          INT PRIMARY KEY AUTO_INCREMENT NOT NULL,
  `pub_id`      INT(8),
  `name`        TEXT,
  `short_name`  TEXT,
  `comments`    TEXT,
  `checked_out` TINYINT(1) DEFAULT 0
);
CREATE TABLE videoinv.transactions (
  `id`             INT PRIMARY KEY AUTO_INCREMENT          NOT NULL,
  `owner`          INT,
  `out_components` TEXT,
  `in_components`  TEXT,
  `out_time`       TIMESTAMP DEFAULT CURRENT_TIMESTAMP     NOT NULL,
  `in_time`        TIMESTAMP,
  FOREIGN KEY (`owner`) REFERENCES videoinv.users (`id`)
);
CREATE TABLE videoinv.quotes (
  `id`     INT PRIMARY KEY AUTO_INCREMENT NOT NULL,
  `text`   TEXT,
  `author` TEXT
);
```

#### Staging/Testing
You will also need to run the commands below to setup the testing environment
__in addition to__ the commands listed above. The use of a separate database is encouraged as the Tests will create transaction records which will show up in the Transaction History Report.
```
INSERT INTO users (pub_id, first_name, last_name, supervisor, pin) VALUES (123456, 'Test', 'User', 1, '');
INSERT INTO inventory (pub_id, name, short_name, comments, checked_out) VALUES (987654, 'Test Item', 'TI', '', 0);
```

### KeyServer Setup
You will need to create two applications in the key server. One for the production configuration, and an additional application for the staging configuration (_if they are different - they will likely be_).
You will need to set the `KSPATH`, `KSKEY`, and `VIMS_APP` environment variables to their corresponding values, so that the web-app can communicate with the Key Server
to retrieve the application configurations.
`VIMS_APP` should be set to the name of the application you created in the KeyServer admin area for the environment.

- `db-password` = Database Password
- `db-url` = jdbc:mysql://db_host:3306/db_name
 * *replace db_host, db_name and possibly the port with your MySQL server info*
-	`db-user` = Database Username

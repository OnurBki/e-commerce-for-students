CREATE TABLE IF NOT EXISTS "user" (
    user_id VARCHAR(255) PRIMARY KEY,
    user_name VARCHAR(100) UNIQUE NOT NULL,
    hashed_password VARCHAR NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL CHECK (email LIKE '%_@__%.__%'),
    phone_number VARCHAR(15) NOT NULL,
    student_id VARCHAR(20),
    reputation DECIMAL(2, 1) NOT NULL DEFAULT 0 CHECK (reputation BETWEEN 0 AND 5),
    balance DECIMAL(8, 2) NOT NULL DEFAULT 0 CHECK (balance >= 0),
    is_admin BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS "item" (
    item_id VARCHAR(255) PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    description VARCHAR(500) NOT NULL,
    category INT NOT NULL CHECK (category BETWEEN 0 AND 9),
    starting_price DECIMAL(6, 2) NOT NULL CHECK (starting_price > 0),
    current_price DECIMAL(6, 2) NOT NULL CHECK (current_price > 0),
    buyout_price DECIMAL(8, 2) NOT NULL CHECK (buyout_price > 0),
    condition INT NOT NULL CHECK (condition BETWEEN 0 AND 4), -- 0: New, 1: Like New, 2: Used, 3: Worn, 4: Damaged
    status INT NOT NULL CHECK (status BETWEEN 0 AND 4), -- 0: Active, 1: Sold, 2: Cancelled, 3: Expired 4: Passive
    auction_start_time TIMESTAMP WITH TIME ZONE NOT NULL,
    auction_end_time TIMESTAMP WITH TIME ZONE NOT NULL,
    user_id VARCHAR(255) NOT NULL,
    FOREIGN KEY (user_id) REFERENCES "user" (user_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS "bid" (
    bid_id VARCHAR(255) PRIMARY KEY,
    bid_amount DECIMAL(8, 2) NOT NULL CHECK (bid_amount > 0),
    user_id VARCHAR(255) NOT NULL,
    item_id VARCHAR(255) NOT NULL,
    FOREIGN KEY (user_id) REFERENCES "user"(user_id) ON DELETE CASCADE,
    FOREIGN KEY (item_id) REFERENCES "item"(item_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS "transaction" (
    transaction_id VARCHAR(255) PRIMARY KEY,
    amount DECIMAL(8, 2) NOT NULL CHECK (amount > 0),
    transaction_date TIMESTAMP WITH TIME ZONE NOT NULL,
    buyer_id VARCHAR(255),
    seller_id VARCHAR(255),
    item_id VARCHAR(255) NOT NULL,
    FOREIGN KEY (buyer_id) REFERENCES "user"(user_id) ON DELETE SET NULL,
    FOREIGN KEY (seller_id) REFERENCES "user"(user_id) ON DELETE SET NULL,
    FOREIGN KEY (item_id) REFERENCES "item"(item_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS "item_image" (
    image_url VARCHAR PRIMARY KEY,
    item_id VARCHAR(255) NOT NULL,
    FOREIGN KEY ("item_id") REFERENCES "item"("item_id") ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS "achievement" (
    achievement_id VARCHAR(255) PRIMARY KEY,
    title VARCHAR(100) NOT NULL UNIQUE CHECK (title != ''),
    description VARCHAR(500) NOT NULL,
    award_amount DECIMAL(8, 2) NOT NULL CHECK (award_amount > 0)
);

CREATE TABLE IF NOT EXISTS "user_achievement" (
    user_id VARCHAR(255) NOT NULL,
    achievement_id VARCHAR(255) NOT NULL,
    PRIMARY KEY (user_id, achievement_id),
    FOREIGN KEY (user_id) REFERENCES "user"(user_id) ON DELETE CASCADE,
    FOREIGN KEY (achievement_id) REFERENCES "achievement"(achievement_id) ON DELETE CASCADE
);

ALTER TABLE "bid" ADD COLUMN bid_time TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW();

CREATE INDEX IF NOT EXISTS idx_item_title ON public.item USING btree (title);

CREATE INDEX IF NOT EXISTS idx_item_status_active ON public.item USING btree (status) WHERE status = 0;

CREATE INDEX IF NOT EXISTS idx_bid_item_amount ON public.bid USING btree (item_id, bid_amount DESC);

CREATE INDEX IF NOT EXISTS idx_bid_item_time ON public.bid USING btree (item_id, bid_time DESC);

CREATE INDEX IF NOT EXISTS idx_transaction_transaction_date_buyer_seller ON public.transaction(buyer_id, seller_id, transaction_date DESC);

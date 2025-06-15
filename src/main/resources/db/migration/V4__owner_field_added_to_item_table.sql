ALTER TABLE item ADD COLUMN owner_id VARCHAR(255);

UPDATE item SET owner_id = user_id;

ALTER TABLE item
ADD CONSTRAINT fk_item_owner
FOREIGN KEY (owner_id)
REFERENCES "user" (user_id);

ALTER TABLE item ALTER COLUMN owner_id SET NOT NULL;
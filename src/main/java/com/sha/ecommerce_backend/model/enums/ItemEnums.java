package com.sha.ecommerce_backend.model.enums;

import lombok.Getter;

public class ItemEnums {
    @Getter
    public enum ItemCategory {
        ELECTRONICS(0),
        FURNITURE(1),
        CLOTHING(2),
        BOOKS(3),
        SPORTS(4),
        BEAUTY(5),
        KITCHEN(6),
        OTHER(7);

        private final int value;

        ItemCategory(int value) {
            this.value = value;
        }

        public static ItemCategory fromValue(int value) {
            for (ItemCategory category : ItemCategory.values()) {
                if (category.value == value) {
                    return category;
                }
            }
            throw new IllegalArgumentException("Unknown category value: " + value);
        }
    }

    @Getter
    public enum ItemCondition {
        NEW(0),
        USED(1),
        REFURBISHED(2),
        DAMAGED(3),
        OTHER(4);

        private final int value;

        ItemCondition(int value) {
            this.value = value;
        }

        public static ItemCondition fromValue(int value) {
            for (ItemCondition status : ItemCondition.values()) {
                if (status.value == value) {
                    return status;
                }
            }
            throw new IllegalArgumentException("Unknown status value: " + value);
        }
    }

    @Getter
    public enum ItemStatus {
        ACTIVE(0),
        INACTIVE(1),
        SOLD(2),
        EXPIRED(3),
        CANCELLED(4);

        private final int value;

        ItemStatus(int value) {
            this.value = value;
        }

        public static ItemStatus fromValue(int value) {
            for (ItemStatus status : ItemStatus.values()) {
                if (status.value == value) {
                    return status;
                }
            }
            throw new IllegalArgumentException("Unknown status value: " + value);
        }
    }
}

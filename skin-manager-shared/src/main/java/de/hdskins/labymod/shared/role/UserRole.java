package de.hdskins.labymod.shared.role;

import java.util.Optional;

public enum UserRole {

    ADMIN(true),
    STAFF(true),
    VIP(true),
    USER(false);

    private static final UserRole[] VALUES = values(); // prevent copy
    private final boolean ignoresRateLimits;

    UserRole(boolean ignoresRateLimits) {
        this.ignoresRateLimits = ignoresRateLimits;
    }

    public static Optional<UserRole> getByName(String name) {
        for (UserRole value : VALUES) {
            if (value.name().regionMatches(true, 0, name, 0, value.name().length())) {
                return Optional.of(value);
            }
        }

        return Optional.empty();
    }

    public boolean isHigherThan(UserRole other) {
        return super.ordinal() < other.ordinal();
    }

    public boolean isHigherOrEqualThan(UserRole other) {
        return super.ordinal() <= other.ordinal();
    }

    public boolean ignoresRateLimits() {
        return this.ignoresRateLimits;
    }
}

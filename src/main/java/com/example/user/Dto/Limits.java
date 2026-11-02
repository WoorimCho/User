package com.example.user.Dto;

/**
 * Upper bounds on what one account can hold or one request can carry (SECURITY.md
 * N4). Generous for a person, small for an attacker; the code length matches
 * {@code account_restriction.restriction_code varchar(100)}.
 */
public final class Limits {

    public static final int RESTRICTIONS = 100;
    public static final int RESTRICTION_CODE_LENGTH = 100;
    public static final int FAVORITE_ALTERNATIVES = 200;
    /** Per account: favourites are added one call at a time, so the cap is on the total, not the request. */
    public static final int FAVORITE_RECIPES = 1_000;

    private Limits() {
    }
}

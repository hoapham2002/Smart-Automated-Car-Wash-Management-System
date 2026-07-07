package com.autowash.backend.common.util;

/**
 * Normalizes a Vietnamese license plate for the `vehicles.plate_normalized`
 * column (uq_vehicle_plate constraint), so "59H1-12345", "59h1 12345" and
 * "59H112345" are all recognized as the same plate and de-duplicated
 * correctly, and so fuzzy plate search (A10 - walk-in booking) matches
 * regardless of how the staff types it in.
 */
public final class PlateNormalizer {

    private PlateNormalizer() {
    }

    public static String normalize(String rawPlate) {
        if (rawPlate == null) {
            return null;
        }
        return rawPlate
                .toUpperCase()
                .replaceAll("[\\s\\-.]", "");
    }
}

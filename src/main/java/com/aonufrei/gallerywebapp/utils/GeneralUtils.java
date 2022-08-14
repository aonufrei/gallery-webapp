package com.aonufrei.gallerywebapp.utils;

import java.util.function.Supplier;

public class GeneralUtils {

	public static void checkAndThrow(Supplier<Boolean> checkCondition, String message) {
		if (checkCondition.get()) {
			throw new RuntimeException(message);
		}
	}

	public static void checkAndThrow(Boolean checkCondition, String message) {
		if (checkCondition) {
			throw new RuntimeException(message);
		}
	}
}

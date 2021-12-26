package com.sunright.inventory.interceptor;

import com.sunright.inventory.dto.UserProfile;

public class UserProfileContext {
    private static final ThreadLocal<UserProfile> CONTEXT = new ThreadLocal<>();

    public static void setUserProfileContext(UserProfile userProfile) {
        CONTEXT.set(userProfile);
    }

    public static UserProfile getUserProfile() {
        return CONTEXT.get();
    }

    public static void clear() {
        CONTEXT.remove();
    }
}

package com.convivium.security.permission;

public enum Permission {

    // Platform-level
    MANAGE_PLATFORM,
    MANAGE_CONDOS,
    MANAGE_SUBSCRIPTIONS,

    // User management
    MANAGE_USERS,
    APPROVE_RESIDENTS,

    // Complaints
    MANAGE_COMPLAINTS,
    RESPOND_COMPLAINTS,
    VIEW_COMPLAINTS,
    CREATE_COMPLAINT,

    // Parcels
    MANAGE_PARCELS,
    VIEW_OWN_PARCELS,

    // Announcements
    MANAGE_ANNOUNCEMENTS,

    // Bookings
    MANAGE_BOOKINGS_ADMIN,
    CREATE_BOOKING,

    // Visitors
    MANAGE_VISITORS,
    PRE_REGISTER_VISITOR,

    // Maintenance
    MANAGE_MAINTENANCE,
    CREATE_MAINTENANCE_REQ,

    // Financial
    VIEW_FINANCIALS,

    // Documents
    MANAGE_DOCUMENTS,
    VIEW_DOCUMENTS,

    // Dashboard
    VIEW_DASHBOARD,

    // Schedules
    MANAGE_SCHEDULES
}

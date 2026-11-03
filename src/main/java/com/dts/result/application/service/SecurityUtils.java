package com.dts.result.application.service;

import com.dts.result.application.exception.AccessDeniedException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

import java.util.UUID;

/**
 * Utility hỗ trợ xác minh quyền sở hữu dữ liệu (Data Ownership) và quyền quản trị.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SecurityUtils {

    /**
     * Xác minh người gọi có quyền truy cập dữ liệu của targetUserId hay không.
     * Cho phép nếu:
     * 1. callerUserId trùng với targetUserId (chính chủ sở hữu dữ liệu).
     * 2. Hoặc callerRoles có chứa vai trò quản trị/giáo viên (ROLE_ADMIN, ROLE_TEACHER).
     *
     * @param callerUserId ID của người dùng đang gửi request (lấy từ Header X-User-Id)
     * @param callerRoles  Danh sách vai trò (lấy từ Header X-User-Roles, phân cách bằng dấu phẩy)
     * @param targetUserId ID của người dùng sở hữu dữ liệu cần truy cập
     * @throws AccessDeniedException nếu người gọi không có quyền truy cập
     */
    public static void validateOwnershipOrAdmin(UUID callerUserId, String callerRoles, UUID targetUserId) {
        if (targetUserId == null) {
            return;
        }

        if (callerUserId != null && callerUserId.equals(targetUserId)) {
            return;
        }

        if (isAdminOrTeacher(callerRoles)) {
            return;
        }

        throw new AccessDeniedException("You do not have permission to access another user's result.");
    }

    /**
     * Kiểm tra chuỗi role có chứa quyền Teacher hoặc Admin hay không.
     */
    public static boolean isAdminOrTeacher(String callerRoles) {
        if (!StringUtils.hasText(callerRoles)) {
            return false;
        }
        return callerRoles.contains("ROLE_ADMIN") || callerRoles.contains("ROLE_TEACHER");
    }
}


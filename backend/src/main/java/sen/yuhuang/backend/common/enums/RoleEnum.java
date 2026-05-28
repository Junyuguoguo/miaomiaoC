package sen.yuhuang.backend.common.enums;

/**
 * 角色枚举
 * 对应 role 表中的 role_code
 */
public enum RoleEnum {

    STUDENT("STUDENT", "学员"),
    VIP_STUDENT("VIP_STUDENT", "会员学员"),
    TEACHER("TEACHER", "教师"),
    ADMIN("ADMIN", "管理员");

    private final String code;
    private final String name;

    RoleEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    /**
     * 根据code获取枚举
     */
    public static RoleEnum fromCode(String code) {
        for (RoleEnum role : RoleEnum.values()) {
            if (role.getCode().equals(code)) {
                return role;
            }
        }
        return null;
    }

    /**
     * 判断是否为管理员
     */
    public static boolean isAdmin(String code) {
        return ADMIN.getCode().equals(code);
    }

    /**
     * 判断是否为教师
     */
    public static boolean isTeacher(String code) {
        return TEACHER.getCode().equals(code);
    }

    /**
     * 判断是否为学员（包括普通学员和会员学员）
     */
    public static boolean isStudent(String code) {
        return STUDENT.getCode().equals(code) || VIP_STUDENT.getCode().equals(code);
    }

    /**
     * 判断是否为会员学员
     */
    public static boolean isVipStudent(String code) {
        return VIP_STUDENT.getCode().equals(code);
    }
}

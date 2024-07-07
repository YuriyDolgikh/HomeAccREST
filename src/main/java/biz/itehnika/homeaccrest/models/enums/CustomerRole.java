package biz.itehnika.homeaccrest.models.enums;

public enum CustomerRole {
    ADMIN, USER;

    @Override
    public String toString() {
        return "ROLE_" + name();
    }
}

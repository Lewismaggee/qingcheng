package com.qingcheng.pojo.system;

import java.io.Serializable;
import java.util.List;

/**
 * 管理员角色权限实体类
 */
public class AdminRoleComonent implements Serializable {
    //管理员
    private Admin admin;
    //角色列表
    private List<Integer> roleList;

    private List<Role> roles;

    public List<Role> getRoles() {
        return roles;
    }

    public void setRoles(List<Role> roles) {
        this.roles = roles;
    }

    public Admin getAdmin() {
        return admin;
    }

    public void setAdmin(Admin admin) {
        this.admin = admin;
    }

    public List<Integer> getRoleList() {
        return roleList;
    }

    public void setRoleList(List<Integer> roleList) {
        this.roleList = roleList;
    }
}

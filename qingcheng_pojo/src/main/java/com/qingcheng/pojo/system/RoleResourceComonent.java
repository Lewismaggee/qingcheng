package com.qingcheng.pojo.system;

import java.io.Serializable;
import java.util.List;

/**
 * 角色权限实体类
 */
public class RoleResourceComonent implements Serializable {
    private Role role;

    private List<Integer> resourceIds;

    private List<Resource> resources;

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public List<Integer> getResourceIds() {
        return resourceIds;
    }

    public void setResourceIds(List<Integer> resourceIds) {
        this.resourceIds = resourceIds;
    }

    public List<Resource> getResources() {
        return resources;
    }

    public void setResources(List<Resource> resources) {
        this.resources = resources;
    }
}

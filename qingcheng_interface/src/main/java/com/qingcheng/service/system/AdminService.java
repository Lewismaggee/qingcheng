package com.qingcheng.service.system;
import com.qingcheng.entity.PageResult;
import com.qingcheng.pojo.system.Admin;
import com.qingcheng.pojo.system.AdminRoleComonent;

import java.util.*;

/**
 * admin业务逻辑层
 */
public interface AdminService {


    public List<Admin> findAll();


    public PageResult<Admin> findPage(int page, int size);


    public List<Admin> findList(Map<String,Object> searchMap);


    public PageResult<Admin> findPage(Map<String,Object> searchMap,int page, int size);


    public Admin findById(Integer id);

    public void add(Admin admin);


    public void update(Admin admin);


    public void delete(Integer id);

    public void updatePwd(Map<String,String> map);

    public void add(AdminRoleComonent adminRoleComonent);

    public AdminRoleComonent findByAdminId(Integer id);

    public void deleteAdminAndRoles(Integer id);

    public void update(AdminRoleComonent adminRoleComonent);

    public List<String> findResourceKeysByLoginName(String loginName);

}

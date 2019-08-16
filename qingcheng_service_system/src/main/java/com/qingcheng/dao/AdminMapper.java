package com.qingcheng.dao;

import com.qingcheng.pojo.system.Admin;
import com.qingcheng.pojo.system.AdminRoleComonent;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface AdminMapper extends Mapper<Admin> {
    @Select("SELECT res_key FROM tb_resource WHERE id IN( " +
            "SELECT resource_id FROM tb_role_resource WHERE role_id IN( " +
            "(SELECT role_id FROM tb_admin_role WHERE admin_id IN " +
            "(SELECT id FROM tb_admin WHERE login_name=#{name}))))")
    public List<String> findResKey(@Param("name") String loginName);
}

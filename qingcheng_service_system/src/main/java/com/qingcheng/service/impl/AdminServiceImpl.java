package com.qingcheng.service.impl;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.qingcheng.dao.AdminMapper;
import com.qingcheng.dao.AdminRoleMapper;
import com.qingcheng.dao.RoleMapper;
import com.qingcheng.entity.PageResult;
import com.qingcheng.pojo.system.Admin;
import com.qingcheng.pojo.system.AdminRole;
import com.qingcheng.pojo.system.AdminRoleComonent;
import com.qingcheng.pojo.system.Role;
import com.qingcheng.service.system.AdminService;
import com.qingcheng.util.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service(interfaceClass = AdminService.class)
public class AdminServiceImpl implements AdminService {

    @Autowired
    private AdminMapper adminMapper;

    @Autowired
    private AdminRoleMapper adminRoleMapper;

    @Autowired
    private RoleMapper roleMapper;

    /**
     * 返回全部记录
     * @return
     */
    public List<Admin> findAll() {
        return adminMapper.selectAll();
    }

    /**
     * 分页查询
     * @param page 页码
     * @param size 每页记录数
     * @return 分页结果
     */
    public PageResult<Admin> findPage(int page, int size) {
        PageHelper.startPage(page,size);
        Page<Admin> admins = (Page<Admin>) adminMapper.selectAll();
        return new PageResult<Admin>(admins.getTotal(),admins.getResult());
    }

    /**
     * 条件查询
     * @param searchMap 查询条件
     * @return
     */
    public List<Admin> findList(Map<String, Object> searchMap) {
        Example example = createExample(searchMap);
        return adminMapper.selectByExample(example);
    }

    /**
     * 分页+条件查询
     * @param searchMap
     * @param page
     * @param size
     * @return
     */
    public PageResult<Admin> findPage(Map<String, Object> searchMap, int page, int size) {
        PageHelper.startPage(page,size);
        Example example = createExample(searchMap);
        Page<Admin> admins = (Page<Admin>) adminMapper.selectByExample(example);
        return new PageResult<Admin>(admins.getTotal(),admins.getResult());
    }

    /**
     * 根据Id查询
     * @param id
     * @return
     */
    public Admin findById(Integer id) {
        return adminMapper.selectByPrimaryKey(id);
    }

    /**
     * 新增
     * @param admin
     */
    public void add(Admin admin) {
        adminMapper.insert(admin);
    }

    /**
     * 修改
     * @param admin
     */
    public void update(Admin admin) {
        adminMapper.updateByPrimaryKeySelective(admin);
    }

    /**
     *  删除
     * @param id
     */
    public void delete(Integer id) {
        adminMapper.deleteByPrimaryKey(id);
    }

    /**
     * 修改原密码
     * @param map
     */
    @Transactional
    public void updatePwd(Map<String, String> map) {
        Example example = new Example(Admin.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("loginName",map.get("loginName"));
        List<Admin> admins = adminMapper.selectByExample(example);
        if(admins == null || admins.size() == 0){
            throw new RuntimeException("该用户不存在");
        }
        //修改
        Admin admin = admins.get(0);


        //1.获取用户输入的新密码:
        String newPwd = map.get("newPwd");
        //2.加密
        String gensalt = BCrypt.gensalt();
        String hashpw = BCrypt.hashpw(newPwd, gensalt);

        //3.获取数据库中的原密码
        String oldHashPwd = admin.getPassword();
        //4.获取页面输入的原密码
        String oldPwd = map.get("oldPwd");
        //5.比较
        boolean checkpw = BCrypt.checkpw(oldPwd, oldHashPwd);
        if(!checkpw){
            throw new RuntimeException("原密码不正确,请重新输入");
        }

        admin.setPassword(hashpw);
        adminMapper.updateByPrimaryKeySelective(admin);
    }

    /**
     * 新增用户角色
     * @param adminRoleComonent
     */
    @Transactional
    public void add(AdminRoleComonent adminRoleComonent) {
        Admin admin = adminRoleComonent.getAdmin();
        //Crypt密码加密
        String password = admin.getPassword();
        String gensalt = BCrypt.gensalt();
        String hashpw = BCrypt.hashpw(password, gensalt);
        admin.setPassword(hashpw);
        //新增user账户表
        adminMapper.insert(admin);

        //再将adminId roleIds 新增admin_role表中
        List<Integer> roleList = adminRoleComonent.getRoleList();
        for(Integer role : roleList){
            AdminRole adminRole = new AdminRole();
            adminRole.setAdminId(admin.getId());
            adminRole.setRoleId(role);
            adminRoleMapper.insert(adminRole);
        }

    }

    @Transactional
    public AdminRoleComonent findByAdminId(Integer id) {
        AdminRoleComonent adminRoleComonent = new AdminRoleComonent();
        //1.根据id查询admin
        Admin admin = adminMapper.selectByPrimaryKey(id);
        //将密码设置为null
        admin.setPassword(null);
        //2.根据admin id 去admin_role表 查询role ids

        Example example = new Example(AdminRole.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("adminId", id);
        List<AdminRole> adminRoles = adminRoleMapper.selectByExample(example);
        //3.循环查询 roles
        //3.1 封装roleIds
        List<Integer> roleIds = new ArrayList<Integer>();
        for(AdminRole adminRole : adminRoles){
            Integer roleId = adminRole.getRoleId();
            roleIds.add(roleId);
        }
        //3.2 根据roleIds 查询Role
        Example example1 = new Example(Role.class);
        Example.Criteria criteria1 = example1.createCriteria();
        criteria1.andIn("id",roleIds);
        List<Role> roles = roleMapper.selectByExample(example1);

        //4.封装返回
        adminRoleComonent.setAdmin(admin);
        adminRoleComonent.setRoleList(roleIds);
        adminRoleComonent.setRoles(roles);
        return adminRoleComonent;
    }

    @Transactional
    public void deleteAdminAndRoles(Integer id) {
        //1.先删除admin_role表
        Example example1 = new Example(AdminRole.class);
        Example.Criteria criteria = example1.createCriteria();
        criteria.andEqualTo("adminId",id);
        adminRoleMapper.deleteByExample(example1);

        //2.删除 admin表
        adminMapper.deleteByPrimaryKey(id);

    }

    public void update(AdminRoleComonent adminRoleComonent) {
        //1.删除中间表admin_role数据
        Admin admin = adminRoleComonent.getAdmin();
        Example example = new Example(AdminRole.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("adminId",admin.getId());
        adminRoleMapper.deleteByExample(example);

        //2.循环插入中间表数据
        List<Integer> roleIds = adminRoleComonent.getRoleList();
        for(Integer roleId : roleIds){
            AdminRole adminRole = new AdminRole();
            adminRole.setRoleId(roleId);
            adminRole.setAdminId(admin.getId());
            adminRoleMapper.insert(adminRole);
        }

        //3.更新admin表数据
        //判断密码是否为Null,不为null: 加密后更新,为null: 不更新
        String newPwd = admin.getPassword();
        if(newPwd != null){
            //加密
            String gensalt = BCrypt.gensalt();
            String hashpw = BCrypt.hashpw(newPwd, gensalt);
            admin.setPassword(hashpw);
        }
        adminMapper.updateByPrimaryKeySelective(admin);
    }

    /**
     * 根据用户名查询 resource keys
     * @param loginName
     * @return
     */
    public List<String> findResourceKeysByLoginName(String loginName) {
        return adminMapper.findResKey(loginName);
    }

    /**
     * 构建查询条件
     * @param searchMap
     * @return
     */
    private Example createExample(Map<String, Object> searchMap){
        Example example=new Example(Admin.class);
        Example.Criteria criteria = example.createCriteria();
        if(searchMap!=null){
            // 用户名
            if(searchMap.get("loginName")!=null && !"".equals(searchMap.get("loginName"))){
                criteria.andEqualTo("loginName",searchMap.get("loginName"));
            }
            // 密码
            if(searchMap.get("password")!=null && !"".equals(searchMap.get("password"))){
                criteria.andLike("password","%"+searchMap.get("password")+"%");
            }
            // 状态
            if(searchMap.get("status")!=null && !"".equals(searchMap.get("status"))){
                criteria.andEqualTo("status",searchMap.get("status"));
            }

            // id
            if(searchMap.get("id")!=null ){
                criteria.andEqualTo("id",searchMap.get("id"));
            }

        }
        return example;
    }

}

package com.qingcheng.controller.system;

import com.alibaba.dubbo.config.annotation.Reference;
import com.qingcheng.entity.PageResult;
import com.qingcheng.entity.Result;
import com.qingcheng.pojo.system.Admin;
import com.qingcheng.pojo.system.AdminRoleComonent;
import com.qingcheng.service.system.AdminService;
import com.qingcheng.util.BCrypt;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/admin")
public class AdminController {

    @Reference
    private AdminService adminService;

    @GetMapping("/findAll")
    public List<Admin> findAll(){
        return adminService.findAll();
    }

    @GetMapping("/findPage")
    public PageResult<Admin> findPage(int page, int size){
        return adminService.findPage(page, size);
    }

    @PostMapping("/findList")
    public List<Admin> findList(@RequestBody Map<String,Object> searchMap){
        return adminService.findList(searchMap);
    }

    @PostMapping("/findPage")
    public PageResult<Admin> findPage(@RequestBody Map<String,Object> searchMap,int page, int size){
        return  adminService.findPage(searchMap,page,size);
    }

    @GetMapping("/findById")
    public Admin findById(Integer id){
        return adminService.findById(id);
    }

    /*
        根据id
     */
    @GetMapping("/findAdminRolesById")
    public AdminRoleComonent findAdminRolesById(Integer id){
        return adminService.findByAdminId(id);
    }


    @PostMapping("/add")
    public Result add(@RequestBody Admin admin){
        adminService.add(admin);
        return new Result();
    }

    @PostMapping("/update")
    public Result update(@RequestBody Admin admin){
        adminService.update(admin);
        return new Result();
    }

    @GetMapping("/delete")
    public Result delete(Integer id){
        adminService.delete(id);
        return new Result();
    }

    @GetMapping("/deleteById")
    public Result deleteAdminAndRoles(Integer id){
        adminService.deleteAdminAndRoles(id);
        return new Result();
    }

    @PostMapping("/updatePwd")
    public Result updatePwd(@RequestBody Map<String,String> searchMap){
        //获取当前登陆人的姓名
        String loginName = SecurityContextHolder.getContext().getAuthentication().getName();
        searchMap.put("loginName",loginName);
        adminService.updatePwd(searchMap);
        return new Result();
    }

    @PostMapping("/addAdminRoles")
    public Result add(@RequestBody AdminRoleComonent adminRoleComonent){
        adminService.add(adminRoleComonent);
        return new Result();
    }

    @PostMapping("/updateAdminAndRoles")
    public Result update(@RequestBody AdminRoleComonent adminRoleComonent){
        adminService.update(adminRoleComonent);
        return new Result();
    }

}

package com.example.goodlearnai.v1.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.goodlearnai.v1.common.Result;
import com.example.goodlearnai.v1.dto.UserLogin;
import com.example.goodlearnai.v1.dto.UserRegister;
import com.example.goodlearnai.v1.entity.Users;
import com.example.goodlearnai.v1.exception.CustomException;
import com.example.goodlearnai.v1.mapper.UsersMapper;
import com.example.goodlearnai.v1.service.IUsersService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.goodlearnai.v1.service.IVerificationCodesService;
import com.example.goodlearnai.v1.utils.AuthUtil;
import com.example.goodlearnai.v1.utils.JwtUtils;
import com.example.goodlearnai.v1.utils.MD5Util;
import com.example.goodlearnai.v1.vo.UserInfo;
import jakarta.annotation.Resource;
import jakarta.mail.MessagingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author author
 * @since 2025-02-27
 */
@Slf4j
@Service
public class UsersServiceImpl extends ServiceImpl<UsersMapper, Users> implements IUsersService {
    @Resource
    private IVerificationCodesService iverificationCodesService;
    
    @Resource
    private com.example.goodlearnai.v1.controller.CaptchaController captchaController;

    @Override
    /*
      用户注册接口
     */ public int register(UserRegister user) throws MessagingException {
        log.debug("注册用户对象{}", user);
        String code = user.getCode();
        if (!iverificationCodesService.checkVerificationCodes(user.getEmail(), code)) {
            log.warn("验证码问题");
            System.out.println(code);
            return -1;
        }
        LambdaQueryWrapper<Users> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Users::getEmail, user.getEmail());
        // 判断用户是否存在
        if (getOne(queryWrapper) != null) {
            log.warn("用户已经存在");
            return 0;
        }

        // md5加密用户的密码
        user.setPassword(MD5Util.encrypt(user.getPassword()));
        log.info("加密用户密码");
        try {
            Users users = new Users();
            BeanUtil.copyProperties(user, users);
            log.debug("注册用户{}", users);
            saveOrUpdate(users);
            log.info("注册成功");
        } catch (Exception e) {
            log.error("注册失败{}", e.getMessage());
            throw new CustomException("用户注册失败，未知异常");
        }
        return 1;
    }


    /**
     * 判断字符串是否为邮箱格式
     */
    private boolean isEmail(String str) {
        if (str == null || str.trim().isEmpty()) {
            return false;
        }
        // 简单的邮箱正则验证
        String emailPattern = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        return str.matches(emailPattern);
    }

    /**
     * 用户登陆
     */
    @Override
    public Result<UserInfo> login(UserLogin user) {
        // 获取账号（优先使用account字段，如果为空则使用email字段保持向后兼容）
        String account = user.getAccount();
        if (account == null || account.trim().isEmpty()) {
            account = user.getEmail();
        }
        
        if (account == null || account.trim().isEmpty()) {
            return Result.error("账号不能为空");
        }
        
        // 1. 验证图形验证码
        if (!captchaController.verifyCaptchaFromRedis(user.getCaptchaKey(), user.getCaptchaCode())) {
            log.warn("图形验证码验证失败: account={}, captchaKey={}", account, user.getCaptchaKey());
            return Result.error("图形验证码错误或已过期");
        }
        log.info("图形验证码验证成功: account={}", account);
        
        // 2. 判断账号是邮箱还是工号
        boolean isEmailLogin = isEmail(account);
        
        // 3. 根据账号类型构建查询条件
        LambdaQueryWrapper<Users> queryWrapper = new LambdaQueryWrapper<>();
        if (isEmailLogin) {
            // 邮箱登录
            queryWrapper.eq(Users::getEmail, account);
            log.info("使用邮箱登录: email={}", account);
        } else {
            // 工号登录
            try {
                Long schoolNumber = Long.parseLong(account);
                queryWrapper.eq(Users::getSchoolNumber, schoolNumber);
                log.info("使用工号登录: schoolNumber={}", schoolNumber);
            } catch (NumberFormatException e) {
                log.warn("账号格式错误，既不是有效邮箱也不是有效工号: account={}", account);
                return Result.error("账号格式错误");
            }
        }
        
        // 4. 查询用户并验证密码
        Users one = getOne(queryWrapper);
        if (one != null) {
            if (MD5Util.verify(user.getPassword(), one.getPassword())) {
                UserInfo userInfo = new UserInfo();
                // 拷贝用户信息
                BeanUtil.copyProperties(one, userInfo);
                // 添加token
                userInfo.setJwtToken(JwtUtils.generateToken(one.getUserId(), one.getRole()));
                log.info("用户登录成功: account={}, userId={}, loginType={}", 
                        account, one.getUserId(), isEmailLogin ? "邮箱" : "工号");
                return Result.success("用户登录成功", userInfo);
            }
            log.warn("密码验证错误: account={}", account);
        } else {
            log.warn("用户不存在: account={}, loginType={}", account, isEmailLogin ? "邮箱" : "工号");
        }
        return Result.error("用户名或密码错误");
    }

    @Override
    public Result<String> addTeacher(Users users) {
        String role = AuthUtil.getCurrentRole();
        if (!"admin".equals(role)&&!"root".equals(role)) {
            return Result.error("权限不足");
        }
        users.setPassword(MD5Util.encrypt(users.getPassword()));
        boolean flag = save(users);
        if (flag) {
            return Result.success("教师添加成功");
        }
        return Result.error("失败了");
    }

    @Override
    public Result<String> forgotPassword(String email, String code, String newPassword) throws MessagingException {
        // 验证邮箱验证码
        if (!iverificationCodesService.checkVerificationCodes(email, code)) {
            return Result.error("验证码错误或已过期");
        }

        // 查找用户
        LambdaQueryWrapper<Users> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Users::getEmail, email);
        Users user = getOne(queryWrapper);
        
        if (user == null) {
            return Result.error("用户不存在");
        }

        // 更新密码
        user.setPassword(MD5Util.encrypt(newPassword));
        boolean updated = updateById(user);
        
        if (updated) {
            return Result.success("密码重置成功");
        } else {
            return Result.error("密码重置失败");
        }
    }

    @Override
    public Result<String> changePassword(String oldPassword, String newPassword) {
        // 获取当前登录用户
        Long userId = AuthUtil.getCurrentUserId();
        Users user = getById(userId);
        
        if (user == null) {
            return Result.error("用户不存在");
        }

        // 验证原密码
        if (!MD5Util.verify(oldPassword, user.getPassword())) {
            return Result.error("原密码错误");
        }

        // 更新密码
        user.setPassword(MD5Util.encrypt(newPassword));
        boolean updated = updateById(user);
        
        if (updated) {
            return Result.success("密码修改成功");
        } else {
            return Result.error("密码修改失败");
        }
    }

    @Override
    public Result<UserInfo> getUserInfo() {
        Long userId = AuthUtil.getCurrentUserId();
        
        if (userId == null) {
            return Result.error("用户未登录");
        }
        
        Users user = getById(userId);
        
        if (user == null) {
            return Result.error("用户不存在");
        }
        
        UserInfo userInfo = new UserInfo();
        BeanUtil.copyProperties(user, userInfo);
        
        return Result.success("获取用户信息成功", userInfo);
    }
    
    @Override
    public Result<String> updateEmail(String newEmail, String code) {
        try {
            // 1. 检查用户是否为老师
            String role = AuthUtil.getCurrentRole();
            if (!"teacher".equals(role)) {
                return Result.error("只有老师可以修改邮箱");
            }
            
            // 2. 获取当前用户信息
            Long userId = AuthUtil.getCurrentUserId();
            Users currentUser = getById(userId);
            
            if (currentUser == null) {
                return Result.error("用户不存在");
            }
            
            // 3. 检查当前邮箱是否为有效邮箱格式
            String currentEmail = currentUser.getEmail();
            if (isValidEmail(currentEmail)) {
                return Result.error("当前邮箱格式有效，无需修改。如需更换邮箱请联系管理员");
            }
            
            // 4. 验证新邮箱格式
            if (!isValidEmail(newEmail)) {
                return Result.error("新邮箱格式不正确");
            }
            
            // 5. 检查新邮箱是否已被其他用户使用
            LambdaQueryWrapper<Users> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Users::getEmail, newEmail);
            Users existingUser = getOne(queryWrapper);
            
            if (existingUser != null && !existingUser.getUserId().equals(userId)) {
                return Result.error("该邮箱已被其他用户使用");
            }
            
            // 6. 验证新邮箱的验证码
            if (!iverificationCodesService.checkVerificationCodes(newEmail, code)) {
                return Result.error("验证码错误或已过期");
            }
            
            // 7. 更新邮箱
            currentUser.setEmail(newEmail);
            boolean updated = updateById(currentUser);
            
            if (updated) {
                log.info("老师邮箱修改成功: userId={}, oldEmail={}, newEmail={}", userId, currentEmail, newEmail);
                return Result.success("邮箱修改成功");
            } else {
                return Result.error("邮箱修改失败");
            }
            
        } catch (Exception e) {
            log.error("修改邮箱异常: {}", e.getMessage(), e);
            return Result.error("修改邮箱失败: " + e.getMessage());
        }
    }
    
    /**
     * 验证邮箱格式是否有效
     * @param email 邮箱地址
     * @return true-有效，false-无效
     */
    private boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        // 简单的邮箱格式验证正则表达式
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        return email.matches(emailRegex);
    }
    
    @Override
    public Result<Boolean> checkEmailValid() {
        // 检查用户是否为老师
        String role = AuthUtil.getCurrentRole();
        if (!"teacher".equals(role)) {
            return Result.error("只有老师可以检查邮箱状态");
        }
        
        // 获取当前用户信息
        Long userId = AuthUtil.getCurrentUserId();
        Users currentUser = getById(userId);
        
        if (currentUser == null) {
            return Result.error("用户不存在");
        }
        
        // 检查邮箱是否为有效格式
        boolean isValid = isValidEmail(currentUser.getEmail());
        
        log.info("检查老师邮箱有效性: userId={}, email={}, isValid={}", 
                userId, currentUser.getEmail(), isValid);
        
        return Result.success("查询成功", isValid);
    }

    @Override
    public Result<String> updateUsername(String username) {
        Long userId = AuthUtil.getCurrentUserId();
        Users user = getById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }
        user.setUsername(username);
        boolean updated = updateById(user);
        if (updated) {
            return Result.success("用户名修改成功");
        } else {
            return Result.error("用户名修改失败");
        }
    }
}

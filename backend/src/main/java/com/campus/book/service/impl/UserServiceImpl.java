package com.campus.book.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.book.common.constants.Constants;
import com.campus.book.dto.UserLoginDTO;
import com.campus.book.dto.UserRegisterDTO;
import com.campus.book.entity.User;
import com.campus.book.mapper.UserMapper;
import com.campus.book.security.JWT.JwtTokenUtil;
import com.campus.book.service.UserService;
import com.campus.book.util.LogUtils;
import com.campus.book.util.SecurityUtils;
import com.campus.book.util.UserPrincipal;
import com.campus.book.vo.UserVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
public class UserServiceImpl implements UserService, UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);
    private static final String EMAIL_PATTERN = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String mailFrom;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username));
        if (user == null) {
            throw new UsernameNotFoundException("用户不存在");
        }
        return new UserPrincipal(user);
    }

    @Override
    public User register(UserRegisterDTO dto) {
        logger.info("开始注册用户: username={}, phone={}", dto.getUsername(), dto.getPhone());
        
        if (dto.getPassword() == null || dto.getPassword().length() < 6) {
            logger.warn("注册失败: 密码长度不足 - username={}", dto.getUsername());
            throw new RuntimeException("密码至少6位");
        }
        if (!dto.getPassword().matches(".*\\d.*")) {
            logger.warn("注册失败: 密码不包含数字 - username={}", dto.getUsername());
            throw new RuntimeException("密码必须包含数字");
        }
        if (!dto.getPassword().matches(".*[a-zA-Z].*")) {
            logger.warn("注册失败: 密码不包含字母 - username={}", dto.getUsername());
            throw new RuntimeException("密码必须包含英文字母");
        }
        
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setPhone(dto.getPhone());
        user.setEmail(dto.getEmail());
        if (dto.getNickname() != null && !dto.getNickname().isEmpty()) {
            user.setNickname(dto.getNickname());
        } else {
            user.setNickname("用户" + (int) (Math.random() * 9000 + 1000));
        }
        user.setRole(Constants.ROLE_USER);
        user.setCreditScore(Constants.CREDIT_SCORE_MAX);
        user.setStatus("NORMAL");
        userMapper.insert(user);
        
        logger.info("用户注册成功: userId={}, username={}", user.getId(), user.getUsername());
        return user;
    }

    @Override
    public String login(UserLoginDTO dto) {
        logger.debug("用户登录验证: username={}", dto.getUsername());
        
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, dto.getUsername()));
        if (user == null) {
            logger.warn("登录失败: 用户不存在 - username={}", dto.getUsername());
            throw new RuntimeException("用户名或密码错误");
        }
        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            logger.warn("登录失败: 密码错误 - username={}, userId={}", dto.getUsername(), user.getId());
            throw new RuntimeException("用户名或密码错误");
        }
        if ("DISABLED".equals(user.getStatus())) {
            logger.warn("登录失败: 账号已被禁用 - username={}, userId={}", dto.getUsername(), user.getId());
            throw new RuntimeException("账号已被禁用");
        }

        UserPrincipal userPrincipal = new UserPrincipal(user);
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                userPrincipal, null, userPrincipal.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String token = jwtTokenUtil.generateToken(user.getUsername(), user.getRole());
        logger.info("用户登录成功: userId={}, username={}", user.getId(), user.getUsername());
        
        return token;
    }

    @Override
    public UserVO getUserInfo(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        UserVO vo = new UserVO();
        BeanUtils.copyProperties(user, vo);
        return vo;
    }

    @Override
    public UserVO getCurrentUserInfo() {
        return getUserInfo(SecurityUtils.getCurrentUserId());
    }

    @Override
    public void updateUserInfo(User user) {
        User currentUser = userMapper.selectById(SecurityUtils.getCurrentUserId());
        if (user.getNickname() != null) {
            currentUser.setNickname(user.getNickname());
        }
        if (user.getPhone() != null) {
            currentUser.setPhone(user.getPhone());
        }
        if (user.getEmail() != null) {
            currentUser.setEmail(user.getEmail());
        }
        if (user.getAvatar() != null) {
            currentUser.setAvatar(user.getAvatar());
        }
        userMapper.updateById(currentUser);
    }

    @Override
    public void updatePassword(String oldPassword, String newPassword) {
        Long userId = SecurityUtils.getCurrentUserId();
        logger.info("修改密码请求: userId={}", userId);
        
        User user = userMapper.selectById(userId);
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            logger.warn("修改密码失败: 原密码错误 - userId={}", userId);
            throw new RuntimeException("原密码错误");
        }
        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            logger.warn("修改密码失败: 新密码与原密码相同 - userId={}", userId);
            throw new RuntimeException("新密码不能与原密码相同");
        }
        if (newPassword == null || newPassword.length() < 6) {
            logger.warn("修改密码失败: 新密码长度不足 - userId={}", userId);
            throw new RuntimeException("密码至少6位");
        }
        if (!newPassword.matches(".*\\d.*")) {
            logger.warn("修改密码失败: 新密码不包含数字 - userId={}", userId);
            throw new RuntimeException("密码必须包含数字");
        }
        if (!newPassword.matches(".*[a-zA-Z].*")) {
            logger.warn("修改密码失败: 新密码不包含字母 - userId={}", userId);
            throw new RuntimeException("密码必须包含英文字母");
        }
        
        user.setPassword(passwordEncoder.encode(newPassword));
        userMapper.updateById(user);
        
        LogUtils.logBusiness(logger, userId, "修改", "密码", "用户修改密码成功");
        logger.info("密码修改成功: userId={}", userId);
    }

    @Override
    public void resetPassword(String phone, String verifyCode) {
        String cacheKey = "verify:code:" + phone;
        String cachedCode = stringRedisTemplate.opsForValue().get(cacheKey);

        if (cachedCode == null) {
            throw new RuntimeException("验证码已过期，请重新获取");
        }
        if (!verifyCode.equals(cachedCode)) {
            throw new RuntimeException("验证码错误");
        }

        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getPhone, phone));
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        user.setPassword(passwordEncoder.encode("123456abc"));
        userMapper.updateById(user);
        stringRedisTemplate.delete(cacheKey);
    }

    @Override
    public void sendVerifyCode(String phone) {
        logger.info("发送验证码: phone={}", phone);
        
        String cacheKey = "verify:code:" + phone;
        Random random = new Random();
        String code = String.valueOf(random.nextInt(9000) + 1000);

        try {
            stringRedisTemplate.opsForValue().set(cacheKey, code, 5, TimeUnit.MINUTES);
            // 使用logger替代System.out.println
            logger.info("========== 验证码 ==========");
            logger.info("手机号: {}", phone);
            logger.info("验证码: {}", code);
            logger.info("===========================");
        } catch (Exception e) {
            LogUtils.logError(logger, "发送验证码失败 - Redis错误", e);
            logger.error("Redis错误: {}", e.getMessage());
        }
    }

    @Override
    public void sendEmailVerifyCode(String email) {
        email = email == null ? null : email.trim();
        if (!StringUtils.hasText(email) || !email.matches(EMAIL_PATTERN)) {
            throw new RuntimeException("请输入正确的邮箱");
        }
        if (isEmailExists(email)) {
            throw new RuntimeException("邮箱已存在");
        }
        if (!StringUtils.hasText(mailFrom)) {
            throw new RuntimeException("邮箱服务未配置，请先填写邮件配置");
        }

        String cacheKey = buildEmailVerifyCodeKey(email);
        String code = String.valueOf(new Random().nextInt(900000) + 100000);
        stringRedisTemplate.opsForValue().set(cacheKey, code, 5, TimeUnit.MINUTES);

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(mailFrom);
            message.setTo(email);
            message.setSubject("校园二手书 - 邮箱验证码");
            message.setText("您的注册验证码是：" + code + "，5分钟内有效。如非本人操作请忽略。");
            mailSender.send(message);
            logger.info("邮箱验证码发送成功: email={}", email);
        } catch (MailAuthenticationException e) {
            stringRedisTemplate.delete(cacheKey);
            logger.error("邮箱验证码发送失败(认证失败): email={}, error={}", email, e.getMessage());
            throw new RuntimeException("邮件服务认证失败，请检查邮箱账号/SMTP授权码配置");
        } catch (MailSendException e) {
            stringRedisTemplate.delete(cacheKey);
            logger.error("邮箱验证码发送失败(发送失败): email={}, error={}", email, e.getMessage());
            throw new RuntimeException("邮件发送失败，请检查收件邮箱地址或SMTP服务配置");
        } catch (MailException e) {
            stringRedisTemplate.delete(cacheKey);
            logger.error("邮箱验证码发送失败(MailException): email={}, error={}", email, e.getMessage());
            throw new RuntimeException("邮件服务异常，请检查SMTP主机/端口/SSL配置");
        } catch (Exception e) {
            stringRedisTemplate.delete(cacheKey);
            logger.error("邮箱验证码发送失败: email={}, error={}", email, e.getMessage());
            throw new RuntimeException("验证码发送失败，请检查邮件配置后重试");
        }
    }

    @Override
    public void validateEmailVerifyCode(String email, String verifyCode) {
        if (!StringUtils.hasText(email)) {
            throw new RuntimeException("邮箱不能为空");
        }
        if (!StringUtils.hasText(verifyCode)) {
            throw new RuntimeException("邮箱验证码不能为空");
        }

        String cacheKey = buildEmailVerifyCodeKey(email);
        String cachedCode = stringRedisTemplate.opsForValue().get(cacheKey);
        if (!StringUtils.hasText(cachedCode)) {
            throw new RuntimeException("邮箱验证码已过期，请重新获取");
        }
        if (!verifyCode.equals(cachedCode)) {
            throw new RuntimeException("邮箱验证码错误");
        }
        stringRedisTemplate.delete(cacheKey);
    }

    private String buildEmailVerifyCodeKey(String email) {
        return "verify:email:register:" + email;
    }

    @Override
    public UserVO getUserById(Long userId) {
        return getUserInfo(userId);
    }

    @Override
    public boolean isUsernameExists(String username) {
        return userMapper.selectCount(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, username)) > 0;
    }

    @Override
    public boolean isPhoneExists(String phone) {
        if (!StringUtils.hasText(phone)) {
            return false;
        }
        return userMapper.selectCount(new LambdaQueryWrapper<User>()
                .eq(User::getPhone, phone)) > 0;
    }

    @Override
    public boolean isEmailExists(String email) {
        if (!StringUtils.hasText(email)) {
            return false;
        }
        return userMapper.selectCount(new LambdaQueryWrapper<User>()
                .eq(User::getEmail, email)) > 0;
    }

    @Override
    public void updatePhone(String phone, String verifyCode) {
        Long userId = SecurityUtils.getCurrentUserId();
        logger.info("更新手机号请求: userId={}, phone={}", userId, phone);
        
        if (phone == null || !phone.matches("^1[3-9]\\d{9}$")) {
            logger.warn("更新手机号失败: 手机号格式不正确 - userId={}, phone={}", userId, phone);
            throw new RuntimeException("请输入正确的手机号");
        }

        String cacheKey = "verify:code:" + phone;
        String cachedCode = stringRedisTemplate.opsForValue().get(cacheKey);

        logger.debug("updatePhone - phone={}, verifyCode={}, cachedCode={}", phone, verifyCode, cachedCode);
        if (!verifyCode.equals(cachedCode)) {
            logger.warn("更新手机号失败: 验证码错误 - userId={}, phone={}", userId, phone);
            throw new RuntimeException("验证码错误");
        }

        User user = userMapper.selectById(userId);
        user.setPhone(phone);
        userMapper.updateById(user);

        stringRedisTemplate.delete(cacheKey);
        
        LogUtils.logBusiness(logger, userId, "更新", "手机号", 
            String.format("旧手机号: %s, 新手机号: %s", user.getPhone(), phone));
        logger.info("手机号更新成功: userId={}, newPhone={}", userId, phone);
    }

}

package com.example.userservice.service;

import com.example.userservice.exception.CustomException;
import com.example.userservice.model.*;
import com.example.userservice.repository.DeletedUserRepository;
import com.example.userservice.repository.UserRepository;
import com.example.userservice.util.JwtUtility;
import com.example.userservice.util.UserUtils;
import io.jsonwebtoken.Claims;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.util.EnumUtils;

import java.security.Principal;
import java.util.*;
import java.util.function.Function;

import static com.example.userservice.constants.UserConstants.BEARER;

@Service
@EnableMethodSecurity
public class LoginRegisterService {

    private static final Logger logger = LoggerFactory.getLogger(LoginRegisterService.class);

    @Autowired
    UserRepository userRepository;

    @Autowired
    DeletedUserRepository deletedUserRepository;

    @Value("${spring.app.jwtExpirationMs}")
    private Integer expirationTime;

    @Autowired
    private UserDetailsServiceImpl userDetailsServiceImpl;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtility jwtUtility;

    public UserDto registerUser(RegisterRequest registerRequest, boolean admin) {
        logger.info("Registration request received for username: {}", registerRequest.getUsername());

        if (userRepository.findTopByUsername(registerRequest.getUsername()) != null) {
            logger.error("User {} already exists", registerRequest.getUsername());
            throw new CustomException("User already exists!", HttpStatus.NOT_ACCEPTABLE);
        }

        UserDto userDto = new UserDto();
        BeanUtils.copyProperties(registerRequest, userDto);


        validatePassword(registerRequest.getPassword());
        userDto.setPassword(passwordEncoder.encode(registerRequest.getPassword()));


//        checkUserRoles(registerRequest.getRole());
//        userDto.setRole(registerRequest.getRole());

        if (admin) userDto.setRole(List.of("Admin"));
        else userDto.setRole(List.of("Customer"));

        checkUserRoles(userDto.getRole());
        userDto.setLastUpdatedBy(registerRequest.getUsername());

        User user = new User();
        if (Boolean.TRUE.equals(validateUser(userDto)))
            try {
                user = userRepository.save(UserUtils.userDtoToDao(userDto));
                logger.info(user.toString());
            } catch (DataIntegrityViolationException dx) {
                logger.error(dx.getMessage());
                throw new CustomException("Data Integrity error", HttpStatus.INTERNAL_SERVER_ERROR);
            } catch (Exception e) {
                logger.error(e.getMessage());
                throw new CustomException("Query error. Check logs", HttpStatus.INTERNAL_SERVER_ERROR);
            }
//        else throw new CustomException("Invalid fields provided", HttpStatus.NOT_ACCEPTABLE);

        logger.info("User {} registered", user.getUsername());
        return UserUtils.userDaoToDto(user);
    }

    public LoginResponse login(LoginRequest loginRequest) {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                    loginRequest.getUsername(),
                    loginRequest.getPassword()
            ));
            logger.info("login request for {} authenticated", loginRequest.getUsername());
        } catch (AuthenticationException e) {
            logger.error("authentication exception with message {}", e.getMessage());
            throw new CustomException("Invalid credentials", HttpStatus.UNAUTHORIZED);
        } catch (Exception e) {
            logger.error("error while authentication");
            logger.error(Arrays.toString(e.getStackTrace()));
            throw new CustomException(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }

        checkDisabled(loginRequest.getUsername());

        final UserDetailsImpl userDetailsImpl = userDetailsServiceImpl.loadUserByUsername(loginRequest.getUsername());

        final String jwtToken = jwtUtility.generateToken(userDetailsImpl);
        logger.info("Token generated {} for user {}", jwtToken, loginRequest.getUsername());
        return new LoginResponse(jwtToken, expirationTime / 1000, BEARER);
    }

    @PreAuthorize("hasAuthority('ADMIN') or #username == authentication.name")
    public OkResponse logoutUser(String username, String accessToken) {
        jwtUtility.revokeToken(accessToken);
        return new OkResponse(String.format("User %s is logged out", username));
    }

    @PreAuthorize("hasAuthority('ADMIN') or #username == authentication.name")
    public UserDto update(String accessToken, String username, RegisterRequest updateRequest) {

        checkDisabled(username);

        UserDto oldUser = UserUtils.userDaoToDto(findExistingUser(username));
        UserDto newUser = new UserDto();

//        newUser.setUsername(Optional.ofNullable(updateRequest.getUsername()).orElse(oldUser.getUsername()));

        if (updateRequest.getUsername() == null) {
            newUser.setUsername(oldUser.getUsername());
        } else if (userRepository.existsByUsername(updateRequest.getUsername())) {
            throw new CustomException("Username already exists", HttpStatus.BAD_REQUEST);
        } else newUser.setUsername(updateRequest.getUsername());

//        newUser.setMobileNumber(Optional.ofNullable(updateRequest.getMobileNumber()).orElse(oldUser.getMobileNumber()));

        if (updateRequest.getMobileNumber() == null) {
            newUser.setMobileNumber(oldUser.getMobileNumber());
        } else if (userRepository.existsByMobileNumber(updateRequest.getMobileNumber())) {
            throw new CustomException("Mobile number already exists", HttpStatus.BAD_REQUEST);
        } else newUser.setMobileNumber(updateRequest.getMobileNumber());

//        newUser.setEmail(Optional.ofNullable(updateRequest.getEmail()).orElse(oldUser.getEmail()));
        if (updateRequest.getEmail() == null) {
            newUser.setEmail(oldUser.getEmail());
        } else if (userRepository.existsByEmail(updateRequest.getEmail())) {
            throw new CustomException("Email already exists", HttpStatus.BAD_REQUEST);
        } else newUser.setEmail(updateRequest.getEmail());

//        newUser.setAddress(Optional.ofNullable(updateRequest.getAddress()).orElse(oldUser.getAddress()));

        Address newAddress = new Address();
        if(updateRequest.getAddress()!=null) {
            newAddress.setAddressLine1(Optional.ofNullable(updateRequest.getAddress().getAddressLine1()).orElse(oldUser.getAddress().getAddressLine1()));
            newAddress.setAddressLine2(Optional.ofNullable(updateRequest.getAddress().getAddressLine2()).orElse(oldUser.getAddress().getAddressLine2()));
            newAddress.setPincode(Optional.ofNullable(updateRequest.getAddress().getPincode()).orElse(oldUser.getAddress().getPincode()));
            newAddress.setCity(Optional.ofNullable(updateRequest.getAddress().getCity()).orElse(oldUser.getAddress().getCity()));
            newAddress.setState(Optional.ofNullable(updateRequest.getAddress().getState()).orElse(oldUser.getAddress().getState()));
            newAddress.setCountry(Optional.ofNullable(updateRequest.getAddress().getCountry()).orElse(oldUser.getAddress().getCountry()));
            newAddress.setId(oldUser.getAddress().getId());
            newUser.setAddress(newAddress);
        }
        else newUser.setAddress(oldUser.getAddress());


//        if (updateRequest.getRole() != null) {
//            if (Boolean.TRUE.equals(verifyPrincipal())) {
//                newUser.setRole(updateRequest.getRole());
//            } else {
//                logger.error("Unable to set new role. Authority not given");
//                newUser.setRole(oldUser.getRole());
//            }
//        } else newUser.setRole(oldUser.getRole());

        if (updateRequest.getPassword() != null && username.equals(getUsernameFromToken(accessToken))) {
            newUser.setPassword(passwordEncoder.encode(updateRequest.getPassword()));
        } else {
            newUser.setPassword(oldUser.getPassword());
        }

        newUser.setRole(oldUser.getRole());
        newUser.setUserId(oldUser.getUserId());
        newUser.setCreatedAt(oldUser.getCreatedAt());
        newUser.setLastUpdatedBy(getUsernameFromToken(accessToken));

        try {
            newUser = UserUtils.userDaoToDto(userRepository.save(UserUtils.userDtoToDao(newUser)));
        } catch (Exception e) {
            logger.error(e.getLocalizedMessage());
            throw new CustomException("Internal error with database. Check logs.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
        logger.info("User {} updated", newUser.getUsername());
        return newUser;
    }
/*

    @PreAuthorize("hasAuthority('ADMIN') or #username == authentication.name")
    public UserDto update(String accessToken, String username, RegisterRequest updateRequest) {
        checkDisabled(username);
        UserDto oldUser = UserUtils.userDaoToDto(findExistingUser(username));
        UserDto newUser = updateUserFields(oldUser, updateRequest, accessToken, username);
        saveUpdatedUser(newUser);
        logger.info("User {} updated", newUser.getUsername());
        return newUser;
    }
*/

    public List<UserDto> getAllUsers(boolean disabled) {
        List<UserDto> userList;
        try {
            userList = userRepository.findAll().stream()
                    .map(UserUtils::userDaoToDto)
                    .toList();
        } catch (Exception e) {
            logger.error("Error while fetching users- {}", e.getMessage());
            throw new CustomException("Internal query error. Check logs", HttpStatus.INTERNAL_SERVER_ERROR);
        }
        if (userList.isEmpty()) {
            logger.info("User list empty");
            throw new CustomException("No record present", HttpStatus.NOT_FOUND);
        }

        if (!disabled) {
            userList = userList.stream()
                    .filter(UserDto::getIsEnabled)
                    .toList();
        }
        return userList;
    }

    @PreAuthorize("hasAuthority('ADMIN') or #principal.getName().equals(#username)")
    public UserDto viewUser(String username, Principal principal) {
        User user = findExistingUser(username);
        if (Boolean.FALSE.equals(user.getIsEnabled())) {
            logger.error("Tried performing actions on disabled account - {}", username);
            throw new CustomException(String.format("Account %s is disabled. Enable it to perform actions on it.", username), HttpStatus.BAD_REQUEST);
        }
        return UserUtils.userDaoToDto(user);
    }

    public OkResponse disableUser(String username) {
        User user = findExistingUser(username);
        if (Boolean.FALSE.equals(user.getIsEnabled()))
            return new OkResponse(String.format("User %s account already disabled", username));

        user.setIsEnabled(Boolean.FALSE);
        try {
            userRepository.save(user);
        } catch (Exception e) {
            logger.error(Arrays.toString(e.getStackTrace()));
            throw new CustomException(e.getLocalizedMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new OkResponse(String.format("User %s disabled", username));
    }

    public OkResponse enableUser(String username) {
        User user = findExistingUser(username);

        if (Boolean.TRUE.equals(user.getIsEnabled()))
            return new OkResponse(String.format("User %s account is already enabled", username));

        user.setIsEnabled(Boolean.TRUE);

        try {
            userRepository.save(user);
        } catch (Exception e) {
            logger.error(Arrays.toString(e.getStackTrace()));
            throw new CustomException(e.getLocalizedMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new OkResponse(String.format("User %s account is enabled", username));
    }

    public OkResponse deleteUser(String accessToken, String username) {
        User user = findExistingUser(username);
        DeletedUser deletedUser = new DeletedUser();

        BeanUtils.copyProperties(user, deletedUser);

        deletedUser.setAddress(user.getAddress().toString());
        deletedUser.setDeletedBy(getUsernameFromToken(accessToken));

        try {
            userRepository.delete(user);
            deletedUserRepository.save(deletedUser);
        } catch (DataAccessResourceFailureException dx) {
            logger.error(dx.getMessage());
            throw new CustomException("Query error. Check logs for details", HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception ex) {
            logger.error(ex.getMessage());
            throw new CustomException("Server error. Check logs", HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new OkResponse(String.format("User %s deleted successfully", username));
    }

    private void validatePassword(String password) {
        String passwordRegex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,15}$";
        if (!password.matches(passwordRegex))
            throw new CustomException("Weak password. " +
                    "Must have at least 8 characters and maximum 15;" +
                    " 1 small, 1 capital, 1 digit and a special character at least.",
                    HttpStatus.BAD_REQUEST);
    }

    /*used for finding users.*/
    private User findExistingUser(String username) {
        try {
            User user = userRepository.findTopByUsername(username);

            if (user == null) {
                logger.error("User - {} not found", username);
                throw new CustomException("Invalid credentials", HttpStatus.NOT_FOUND);
            }

            logger.info("Entry for user found: {}", String.valueOf(user));
            return user;

        } catch (CustomException cx) {
//            logger.error(Arrays.toString(cx.getStackTrace()));
            logger.error(cx.getMessage());
            throw new CustomException(cx.getMessage(), cx.getStatus());

        } catch (Exception e) {
            logger.error("Error while loading user- {}", e.getMessage());
            throw new CustomException("Error while loading user. Check logs for details.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private void checkDisabled(String username) {
        User user = findExistingUser(username);
        if (Boolean.FALSE.equals(user.getIsEnabled())) {
            logger.error("Tried performing actions on disabled account - {}", username);
            throw new CustomException(String.format("Account %s is disabled. Enable it to perform actions on it.", username), HttpStatus.BAD_REQUEST);
        }
    }

    private String getUsernameFromToken(String accessToken) {
        String token = accessToken.substring(7);
        return jwtUtility.getClaimFromToken(token, Claims::getSubject);
    }

    private void checkUserRoles(List<String> roleStrings) {
        roleStrings.forEach(role ->
        {
            try {
                EnumUtils.findEnumInsensitiveCase(UserRole.class, role);
            } catch (Exception e) {
                logger.error(e.getMessage());
                throw new CustomException("Provided role not found/other error", HttpStatus.INTERNAL_SERVER_ERROR);
            }
        });
    }

    private Boolean verifyPrincipal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ADMIN"));
    }

    private Boolean validateUser(UserDto userDto) {
        if (Boolean.TRUE.equals(userRepository.existsByEmail(userDto.getEmail())))
//            return Boolean.FALSE;
            throw new CustomException("Email already exists.", HttpStatus.BAD_REQUEST);
        if (Boolean.TRUE.equals(userRepository.existsByMobileNumber(userDto.getMobileNumber())))
//            return Boolean.FALSE;
            throw new CustomException("Mobile number already exists.", HttpStatus.BAD_REQUEST);

        if (Boolean.TRUE.equals(userRepository.existsByUsername(userDto.getUsername())))
//            return Boolean.FALSE;
            throw new CustomException("Username already exists.", HttpStatus.BAD_REQUEST);

        logger.info("user validated");
        return Boolean.TRUE;
    }

    private UserDto updateUserFields(UserDto oldUser, RegisterRequest updateRequest, String accessToken, String givenUsername) {
        UserDto newUser = new UserDto();
        newUser.setUsername(getUpdatedField(updateRequest.getUsername(), oldUser.getUsername(), userRepository::existsByUsername));
        newUser.setMobileNumber(getUpdatedField(updateRequest.getMobileNumber(), oldUser.getMobileNumber(), userRepository::existsByMobileNumber));
        newUser.setEmail(getUpdatedField(updateRequest.getEmail(), oldUser.getEmail(), userRepository::existsByEmail));
        newUser.setAddress(getUpdatedAddress(updateRequest.getAddress(), oldUser.getAddress()));
        newUser.setPassword(getUpdatedPassword(updateRequest.getPassword(), givenUsername, getUsernameFromToken(accessToken), oldUser.getPassword()));
        newUser.setRole(oldUser.getRole());
        newUser.setUserId(oldUser.getUserId());
        newUser.setCreatedAt(oldUser.getCreatedAt());
        newUser.setLastUpdatedBy(getUsernameFromToken(accessToken));
        return newUser;
    }

    private String getUpdatedField(String newField, String oldField, Function<String, Boolean> existsFunction) {
        if (newField == null) {
            return oldField;
        }
        if (!newField.equals(oldField) && existsFunction.apply(newField)) {
            throw new CustomException(newField + " already exists", HttpStatus.BAD_REQUEST);
        }
        return newField;
    }

    private Address getUpdatedAddress(Address updateAddress, Address oldAddress) {
        if (updateAddress == null) {
            return oldAddress;
        }
        Address newAddress = new Address();
        newAddress.setAddressLine1(updateAddress.getAddressLine1());
        newAddress.setAddressLine2(updateAddress.getAddressLine2());
        newAddress.setPincode(updateAddress.getPincode());
        newAddress.setCity(updateAddress.getCity());
        newAddress.setState(updateAddress.getState());
        newAddress.setCountry(updateAddress.getCountry());
        newAddress.setId(oldAddress.getId());
        return newAddress;
    }

    private String getUpdatedPassword(String newPassword, String givenUsername, String usernameFromToken, String oldPassword) {
        if (newPassword == null) {
            return oldPassword;
        }
        if (usernameFromToken.equals(givenUsername)) {
            return passwordEncoder.encode(newPassword);
        } else {
            logger.error("Unable to set new password. User not authorized");
            return oldPassword;
        }
    }

    private void saveUpdatedUser(UserDto newUser) {
        try {
            userRepository.save(UserUtils.userDtoToDao(newUser));
        } catch (Exception e) {
            logger.error(e.getLocalizedMessage());
            throw new CustomException("Internal error with database. Check logs.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}

# Authenticate using JWT
This tutorial will be divided in two parts:
1. Authenticating
2. Verifying authentication

In part 1 we will learn how a user can authenticate using his username and password to receive a token which will 
permit him to access the API resources

In part 2 we will learn how to intercept requests to verify if the user has a valid token and permissions.

# Authenticating
1. We install the dependencies we will be using, the lib used is [io.jsonwebtoken](https://github.com/jwtk/jjwt?tab=readme-ov-file#maven)
    ```xml
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-api</artifactId>
        <version>0.12.6</version>
    </dependency>
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-impl</artifactId>
        <version>0.12.6</version>
        <scope>runtime</scope>
    </dependency>
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-jackson</artifactId> <!-- or jjwt-gson if Gson is preferred -->
        <version>0.12.6</version>
        <scope>runtime</scope>
    </dependency>
    ```

2. Next we will create our token class to send the token as a response:
    ````java
    @Getter @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public class JwtToken {
    
        private String token;
    }
    ````
   
3. Now we will create a class that has operations to work with Jwt tokens, it has the following operations and utilities:
   1. Constants useful when working with jtw, like duration and secret key (This is not a secure way to use a secret key, it should be used an environment variable)
   2. validate a token
   3. get the subject/username of a token
   4. create a token
   5. The implementation can be [found here](https://github.com/kennedy-brito/demo-auth-jwt/blob/main/src/main/java/com/kennedy/demo_auth_jwt/jwt/JwtUtils.java)

4. Create a JwtUserDetails, this is a very important class. It extends from the ``User`` class from the spring security.
    - It is a important class because it is through it that spring registers our user in the context of Spring security.
   - It too has some methods useful to access user data 
   ```java
    import org.springframework.security.core.authority.AuthorityUtils;
    import org.springframework.security.core.userdetails.User;
    
    public class JwtUserDetails extends User {
    
        private com.kennedy.demo_auth_jwt.entities.User user;
    
        public JwtUserDetails(com.kennedy.demo_auth_jwt.entities.User user) {
            super(
                    user.getUsername(),
                    user.getPassword(),
                    AuthorityUtils.createAuthorityList(user.getRole().name())
            );
    
            this.user = user;
        }
    
        public Long getId(){
            return user.getId();
        }
    
        public String role(){
            return user.getRole().name();
        }
    }
    ```

5. Now we implement a service that get us a ``JwtUserDetails`` and create the user token
   - Note: im assuming you know how to implement the methods used in the ``UserService`` class 
   ````java
    @RequiredArgsConstructor
    @Service
    public class JwtUserDetailsService implements UserDetailsService {
    
        private final UserService userService;
    
        @Override
        public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
            User user = userService.findByUsername(username);
            return new JwtUserDetails(user);
        }
    
        public JwtToken getTokenAuthenticated(String username){
            User.Role role = userService.getRoleByUsername(username);
            return JwtUtils.createToken(username, role.name().substring("ROLE_".length()));
        }
    }
    ````
   
6. Now we should create an endpoint for the user authenticate
    ````java
    @RequiredArgsConstructor
    @RestController
    @RequestMapping("/auth")
    public class AuthResource {
        private static final Logger log = LoggerFactory.getLogger(AuthResource.class);
        private final JwtUserDetailsService userDetailsService;
    
        private final AuthenticationManager authenticationManager;

    }
    ````
   
7. Authentication is done in a post method
    - ``UsernamePasswordAuthenticationToken``: is a class that represents a user and hes credentials (in this case, his password)
    - ``AuthenticationManager``: it is a class that realizes the authentication
      - It delegates the logic of authentication to other classes, it works this way:
      - Uses an ``AuthenticationProvider`` to authenticate, this class has the logic
      - Search for an implementation of ``UserDetailsService`` to get user data, in our case we implemented this interface in the class ``JwtUserDetailsService``, so Spring uses this class for data access
      - Uses the ``PasswordEncoder`` that we configured to encrypt the received password and compare to the stored password
    ````java
    @PostMapping
        public ResponseEntity<?> authenticate(@RequestBody User user, HttpServletRequest request){
            try{
                UsernamePasswordAuthenticationToken authenticationToken
                        = new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword());
    
                authenticationManager.authenticate(authenticationToken);
    
                JwtToken token =userDetailsService.getTokenAuthenticated(user.getUsername());
                return ResponseEntity.ok(token);
            }catch (AuthenticationException e){
                log.error("Authentication error for user: {}", user.getUsername());
            }
    
            return ResponseEntity.badRequest().build();
        }
    ````
   
8. Now for the end of this part we should configure the access to this endpoint and the ``AuthenticationManager``. To do this we go back to the ``SprinSecuritygConfig``
   - We permit the access to the ``/auth`` endpoint in the ``filterChain`` method
   ````java
   @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
   ...
           .requestMatchers(
           ...
           antMatcher(HttpMethod.POST, "/auth")
           )
                   ...
   }
   ````
   - And configure the ``AuthenticationManager``
   ````java
   @Bean
   public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
   }
   ````
   
Test the endpoint and see that it returns a token!
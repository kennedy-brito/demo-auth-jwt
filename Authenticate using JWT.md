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

# Filtering request to see if the user is authenticated
Now that the user can authenticate and receive a valid toke, we should filter the request and verify if he is authenticated and if the token is valid.

1. First we should create a filter class, our filter will be used once per request, so we extend the ``OncePerRequest`` abstract class
   - We will need to use our ``JwtUserDetailsService`` here
   ````java
   public class JwtAuthorizationFilter extends OncePerRequestFilter {
      @Autowired
      private JwtUserDetailsService detailsService;
   }
   ````
2. This class has to Override the method ``doFilterInternal``
   - This filter does a lot of things
   - It gets the Authorization field in the request header
     - Because our token should be passed in this field in the format 'Bearer <token>'
   - Verifies with the token is null, in a correct format and if it is not valid
     - If this is true we continue our filter chain to verify if the url is restricted or not, to do that we call the ``doFilter`` in the filterChain object
   - If the token is valid we have to save our user in our security context
   - To do that we get the username from the payload and use the ``toAuthentication`` to save the user in the security context
   - finally we continue the filter of the request
   ````java
   @Override
       protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
           String token = request.getHeader(JwtUtils.JWT_AUTHORIZATION);
   
           if(token == null || !token.startsWith(JwtUtils.JWT_BEARER)){
               log.info("JWT Token is null, empty or don't start with 'Bearer '.");
               filterChain.doFilter(request, response);
               return;
           }
   
           if(!JwtUtils.isTokenValid(token)){
               log.warn("JWT Token is invalid or expired.");
               filterChain.doFilter(request, response);
               return;
           }
   
           String username = JwtUtils.getUsernameFromToken(token);
   
           toAuthentication(request, username);
   
           filterChain.doFilter(request, response);
       }
   ````
3. Now we should implement the ``toAuthentication`` method:
   - what this method do is:
     - find the user by the username using our detailsService
       - Remember that UserDetails returned from the method is the JwtUserDetails extension that we implemented
     - create an authenticated token with the userDetails object and it's authorities
     - set some details for the token, in this case, we're saving some data from the request
     - finally we save our authentication token in the security context, this will be useful for when we need to see permissions and user data
   ````java
    private void toAuthentication(HttpServletRequest request, String username) {
        UserDetails userDetails = detailsService.loadUserByUsername(username);

        UsernamePasswordAuthenticationToken authenticationToken
                = UsernamePasswordAuthenticationToken.authenticated(userDetails, null, userDetails.getAuthorities());

        authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
    }
   ````
   
4. Now we should configure the application to use this filter, so going back to the class ``SpringSecurityConfig`` we set it as a filter that is executed after `UsernamePasswordAuthenticationFilter` class
   ````java
   @Bean
   public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
       ...
       .sessionManagement(
               session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
      )
      .addFilterAfter(
              jwtAuthorizationFilter(), UsernamePasswordAuthenticationFilter.class
      )
      ...
   }
   ````

5. The final change we should the is remove the permission to access the url's ``/users/{id}`` and the get url `/users`
   - we do this deleting the lines from our ``filterChain`` method, now only authenticated users can access these url's
   ````java
   antMatcher(HttpMethod.POST, "/users"),
   antMatcher(HttpMethod.GET, "/users/{id}"),
   ````

Now we conclude this tutorial! The next step is control the methods access by user role!
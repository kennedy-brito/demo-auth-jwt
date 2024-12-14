# Encrypt user password

## Configure the application Security

First we should configure the security of the application, to do that we should:

1. import `spring security starter`
   ```xml
   <dependency>
       <groupId>org.springframework.boot</groupId>
       <artifactId>spring-boot-starter-security</artifactId>
   </dependency>
   ```
2. Create a configuration class:

   ```java
   @Configuration
   public class SpringSecurityConfig {}
   ```

   - The `@Configuration` annotation indicates to Spring that the class is a configuration class and permits the definition of java Beans

3. Define the URL's permissions in the method filterChain:
   ```java
   public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
       return http
                   .csrf( crfs -> crfs.disable())
                   .formLogin( form -> form.disable())
                   .httpBasic( basic -> basic.disable())
                   .authorizeHttpRequests(
                           auth -> auth
                                   .requestMatchers(
                                           antMatcher(HttpMethod.POST, "/users"),
                                           antMatcher(HttpMethod.GET, "/users/{id}"),
                                           antMatcher(HttpMethod.GET, "/users")
                                   ).permitAll()
                                   .anyRequest().authenticated()
                   ).sessionManagement(
                           session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                   ).build();
   }
   ```
   - `csrf( crfs -> crfs.disable())`: to disable Cross-Site Request Forgery, the reason this is done is because REST API'S are stateless, thus are not prone to crfs's attacks. See this [link](https://stackoverflow.com/questions/62696806/reason-to-disable-csrf-in-spring-boot);
   - `formLogin( form -> form.disable())`: disabling form login, our login will be based in requests with JSON body;
   - `.httpBasic( basic -> basic.disable())`: disabling HTTP basic login form, this auth method is insecure and we will be using JWT auth method.
   - `.authorizeHttpRequests(...)`: we are configuring the permissions of the URL's, for now we are liberating the use of all request for anyone, later we will use Role and auth based access to some request.
   - `.sessionManagement(...)`: we are configuring our session to be stateless.
   - `.build();`: finally we build the filter.
4. Configure the PasswordEncoder bean, this has other methods of encryption. See the [docs](https://docs.spring.io/spring-security/reference/features/authentication/password-storage.html#authentication-password-storage-bcrypt):
   ```java
   @Bean
   public PasswordEncoder passwordEncoder(){
       return new BCryptPasswordEncoder();
   }
   ```

## Encrypt the password

1. import the `PasswordEncoder class` and use the `encode method` to encode the password
   ```java
   // this is injected using Lombok
   private final PasswordEncoder passwordEncoder;

    // This is done inline in the save mehtod, this is only for clarity sake
   public String encode(String password){
        return passwordEncoder.encode(password);
   }
   ```
2. Save a user and confirm if the password was encrypted, this is the result of one request:
   ```json
   {
     "id": 3,
     "username": "snape",
     "password": "$2a$10$MJAdoO7e/eCH.ruPptQSS.V./F5ZO.HzJsWuEGI9nou1KIuDqIxUi",
     "role": "ROLE_ADMIN"
   }
   ```

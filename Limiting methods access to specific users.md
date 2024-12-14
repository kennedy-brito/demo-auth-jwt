# Limiting access of methods to users with permissions
In this tutorial we will be limiting the access of some methods to specific users and permissions

## Limiting access
1. First we should enable the security of a method. To do that we use the annotation ``EnableMethodSecurity`` in our config class
    ````java
    @EnableMethodSecurity
    @Configuration
    public class SpringSecurityConfig { }
    ````

2. Now we go to the method we will be limiting access, the first one will be the one who list all users. In the UserController we do:
   - we use the ``PreAuthorize`` annotation to verify if the user has the role `ADMIN` before the method execution
     - Note: Spring prefix automatically the ``ROLE_`` string to admin
   ````java
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<List<User>> listAll(){
        return ResponseEntity.ok(
                userService.findAll()
        );
    }
    ````

3. Now we restrict the url that retrieves the data of a single user to the admin or to the user that is authenticated
   - an admin can view the data from all users
   - an user can view only his own data
   - the part of the expression ``#id == authentication.principal.id)`` compares the id in the argument with the authenticated user id
    ````java
    @PreAuthorize("hasRole('ADMIN') OR (hasRole('CLIENT') AND #id == authentication.principal.id)")
    @GetMapping(value = "/{id}")
    public ResponseEntity<User> findById(@PathVariable Long id){
        return ResponseEntity.ok(
                userService.findById(id)
        );
    }
    ````
   
Now we conclude the tutorial of how to limit method access based in roles and user using Spring Security!
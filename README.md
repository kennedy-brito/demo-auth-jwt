# What is the purpose of this project?
This project is a way to practice using JWT auth with Spring security. It too should serves as a base tutorial to this process. 

The ``Tutorial.md`` has a tutorial of how each objective was achieved f.
# How it achieves that?
The branch ``initial_setup`` has the API without auth or security measures.
From this branch, the ``securing`` branch implements security measures.

### What are the measure?
The project should have the following security measures:

- [ ] Encrypt user password
- [ ] Authenticate using JWT
- [ ] The resource to create a user should be public
- [ ] The resource to show the data of a users should be accessible only to the user and the admin
  - [ ] The admin can access the data from all users
  - [ ] The user can access only his own data
- [ ] The resource to list all user should be accessible only to the admin
